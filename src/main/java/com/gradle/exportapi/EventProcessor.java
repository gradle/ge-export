package com.gradle.exportapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.gradle.exportapi.dao.TasksDAO;
import com.gradle.exportapi.model.Build;
import com.gradle.exportapi.model.Task;
import com.gradle.exportapi.model.Test;
import com.gradle.exportapi.model.Timer;

import java.time.Instant;

import static com.gradle.exportapi.dao.BuildDAO.*;


class EventProcessor {

    static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    private final Build currentBuild;


    public final static String EVENT_TYPES="BuildStarted,BuildAgent,ProjectStructure,Locality,BuildFinished,TaskStarted,TaskFinished,TestStarted,TestFinished";


    public EventProcessor(String buildId) {
        this.currentBuild = new Build( buildId );

        log.debug("DB-generated id: " + currentBuild.getId());
    }

    public void process(JsonNode json) {
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
                testFinished(json);
                break;
        }
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
        String taskId = id.asText();

        assert currentBuild.taskMap.get(taskId) == null;

        Task task = new Task();
        task.setTaskId(taskId);
        task.setBuildId(this.currentBuild.getBuildId());
        task.setPath(data.get("path").asText());
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
        String taskId = id.asText();

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

    private void testStarted(JsonNode json) {
        JsonNode data = json.get("data");
        JsonNode id = data.get("id");
        assert id != null;
        String testId = id.asText();

        assert currentBuild.testMap.get(testId) == null;

        Test test = new Test();
        test.setTestId(data.get("id").asText());
    }

    private void testFinished(JsonNode json) {
        JsonNode data = json.get("data");
        JsonNode id = data.get("id");
        assert id != null;
        String testId = id.asText();

    }

    public static void persist(EventProcessor processor) {
        Build currentBuild = processor.currentBuild;
        currentBuild.setId( insertBuild(currentBuild) );

        currentBuild.taskMap.values().stream().forEach( TasksDAO::insertTask );

    }



    @Override
    public String toString() {
        return "EventProcessor{" +
                "currentBuild=" + currentBuild +
                '}';
    }
}
