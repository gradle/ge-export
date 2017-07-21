package com.gradle.exportapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.gradle.exportapi.dao.BuildDAO;
import com.gradle.exportapi.dao.CustomValueDAO;
import com.gradle.exportapi.dao.TasksDAO;
import com.gradle.exportapi.dao.TestsDAO;
import com.gradle.exportapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;


class EventProcessor {

    static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    private final Build currentBuild;


    public final static String EVENT_TYPES="BuildStarted,BuildAgent,ProjectStructure,Locality,BuildFinished,TaskStarted,TaskFinished,TestStarted,TestFinished,UserTag,UserNamedValue";


    public EventProcessor(String buildId) {
        this.currentBuild = new Build( buildId );

        log.debug("DB-generated id: " + currentBuild.getId());
    }

    public EventProcessor process(JsonNode json) {
        String eventType = json.get("type").get("eventType").asText();

        switch (eventType) {
            case "BuildStarted":
                buildStarted(json);
                break;
            case "BuildAgent":
                buildAgent(json);
                break;
            case "ProjectStructure":
                projectStructure(json);
                break;
            case "Locality":
                locality(json);
                break;
            case "BuildFinished":
                buildFinished(json);
                break;
            case "TaskStarted":
                taskStarted(json);
                break;
            case "TaskFinished":
                taskFinished(json);
                break;
            case "TestStarted":
                testStarted(json);
                break;
            case "TestFinished":
                testFinished(json); //
                break;
            case "UserTag":
                tags(json);
                break;
            case "UserNamedValue":
                customValue(json);
                break;
        }
        return this;
    }



    private void buildAgent(JsonNode json) {
        currentBuild.setUserName(json.get("data").get("username").asText());
    }

    private void projectStructure(JsonNode json) {
        currentBuild.setRootProjectName(json.get("data").get("rootProjectName").asText());
    }

    private void locality(JsonNode json) {
        currentBuild.getTimer().setTimeZoneId( json.get("data").get("timeZoneId").asText() );

        assert currentBuild.getTimer().getTimeZoneId() != null;
    }

    private void buildStarted(JsonNode json) {
        currentBuild.getTimer().setStartTime( Instant.ofEpochMilli( json.get("timestamp").asLong()) );
    }

    private void buildFinished(JsonNode json) {
        currentBuild.getTimer().setFinishTime( Instant.ofEpochMilli( json.get("timestamp").asLong()) );


    }

    /*
    id: 35
event: BuildEvent
data: {"timestamp":1488495221555,"type":{"majorVersion":1,"minorVersion":2,"eventType":"TaskStarted"},"data":{"id":-2556824238716145285,"path":":compileJava","className":"org.gradle.api.tasks.compile.JavaCompile_Decorated","thread":0,"noActions":false}}
     */
    private void taskStarted(JsonNode json) {
        JsonNode data = json.get("data");
        JsonNode id = data.get("id");
        assert id != null;
        Long taskId = id.asLong();

        assert currentBuild.taskMap.get(taskId) == null;

        Task task = new Task();
        task.setTaskId(taskId);
        task.setBuildId(this.currentBuild.getBuildId());
        task.setPath(data.get("path").asText());
        task.setType(data.get("className").asText());
        Timer timer = task.getTimer();
        timer.setStartTime( Instant.ofEpochMilli(json.get("timestamp").asLong()));

        currentBuild.taskMap.put(taskId, task);
    }

    /*Example:
    id: 38
event: BuildEvent
data: {"timestamp":1488495221566,"type":{"majorVersion":1,"minorVersion":3,"eventType":"TaskFinished"},"data":{"id":-2556824238716145285,"path":":compileJava","outcome":"up_to_date","skipMessage":null,"cacheable":false,"cachingDisabledExplanation":null}}

id: 39
     */
    private void taskFinished(JsonNode json) {
        JsonNode id = json.get("data").get("id");
        assert id != null;
        Long taskId = id.asLong();

        Task task = currentBuild.taskMap.get(taskId);
        if(task == null) {
            throw new RuntimeException("Could not find task with id: " + taskId + " in the task map");
        }

        JsonNode timestamp = json.get("timestamp");
        assert timestamp != null;
        Timer timer = task.getTimer();
        timer.setFinishTime( Instant.ofEpochMilli(timestamp.asLong()) );

        task.setOutcome( json.get("data").get("outcome").asText());
    }

    /*
    'id: 7617
event: BuildEvent
data: {"timestamp":1491615403001,"type":{"majorVersion":1,"minorVersion":0,"eventType":"TestStarted"},
"data":{"task":4032790360760717493,"id":-1963500554409017618,"parent":3512790051736469310,"name":"verifyOk",
"className":"org.apereo.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandlerTests","suite":false}}
     */

    private void testStarted(JsonNode json) {
        JsonNode data = json.get("data");

        //for now, skip suites
        if(data.get("suite").asBoolean()) return;

        JsonNode id = data.get("id");
        assert id != null;
        String testId = id.asText();

        assert currentBuild.testMap.get(testId) == null;

        Test test = new Test();
        test.setBuildId(currentBuild.getBuildId());
        test.setTaskId(data.get("task").asLong());
        test.setTestId(data.get("id").asLong());
        test.setName(data.get("name").asText());
        test.setClassName(data.get("className").asText());

        Timer timer = test.getTimer();
        timer.setStartTime( Instant.ofEpochMilli(json.get("timestamp").asLong()));

        currentBuild.testMap.put(test.getTaskId()+":"+test.getTestId(), test);
    }

    /*
    id: 7618
event: BuildEvent
data: {"timestamp":1491615409161,"type":{"majorVersion":1,"minorVersion":0,"eventType":"TestFinished"},"data":{"task":4032790360760717493,"id":-1963500554409017618,
"failed":false,"skipped":false,"failure":null}}
     */
    private void testFinished(JsonNode json) {
        JsonNode data = json.get("data");
        String taskId = data.get("task").asText();
        String testId = data.get("id").asText();
        assert taskId != null;
        assert testId != null;
        Test test = currentBuild.testMap.get(taskId+":"+testId);

        if(test == null) return;

        boolean failed = data.get("failed").asBoolean();
        boolean skipped = data.get("skipped").asBoolean();
        test.setStatus( skipped ? "skipped" : failed ? "failed" : "success");
        Timer timer = test.getTimer();
        timer.setFinishTime( Instant.ofEpochMilli(json.get("timestamp").asLong()) );
    }

    private void tags(JsonNode json) {
        JsonNode data = json.get("data");
        currentBuild.tags.add(data.get("tag").asText());
    }

    private void customValue(JsonNode json) {
        JsonNode data = json.get("data");
        CustomValue cv = new CustomValue();
        cv.setBuildId(currentBuild.getBuildId());
        cv.setKey(data.get("key").asText());
        cv.setValue(data.get("value").asText());
        currentBuild.customValues.add(cv);
    }

    public static void persist(EventProcessor processor) {
        Build currentBuild = processor.currentBuild;
        currentBuild.resolveStatus();

        Optional<Long> buildTableId = BuildDAO.getBuildTableId(currentBuild);
        if (buildTableId.isPresent()) {
            log.warn("Skipped build {} that is already in db", currentBuild.getBuildId());
            return;
        }
        currentBuild.setId(BuildDAO.insertBuild(currentBuild));


        currentBuild.taskMap.values().stream().forEach(TasksDAO::insertTask);
        currentBuild.testMap.values().stream().forEach(TestsDAO::insertTest);
        currentBuild.customValues.stream().forEach(CustomValueDAO::insertCustomValue);
    }



    @Override
    public String toString() {
        return "EventProcessor{" +
                "currentBuild=" + currentBuild +
                '}';
    }
}
