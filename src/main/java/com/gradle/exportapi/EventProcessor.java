package com.gradle.exportapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.gradle.exportapi.dao.TasksDAO;
import com.gradle.exportapi.model.Build;
import com.gradle.exportapi.model.Task;
import com.gradle.exportapi.model.Timer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.gradle.exportapi.dao.BuildDAO.*;


class EventProcessor {

    static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    private final Build currentBuild;

    // Maps to hold in-flight objects
    private final Map<String, Task> taskMap = new HashMap<>();

    public final static String EVENT_TYPES="BuildStarted,BuildAgent,ProjectStructure,Locality,BuildFinished,TaskStarted,TaskFinished";


    public EventProcessor(String buildId) {
        this.currentBuild = new Build( buildId );
        currentBuild.setId( insertBuild(currentBuild) );
        if(currentBuild.getId() == 0) {
            throw new RuntimeException("Unable to save build record for " + currentBuild.getBuildId());
        }
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
        int affectedRows = updateBuild(currentBuild);
        log.info("Updated " + affectedRows + " rows in builds table. Build: " + currentBuild.toString());
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

        assert taskMap.get(taskId) == null;

        Task task = new Task();
        task.setTaskId(taskId);
        task.setBuildId(this.currentBuild.getBuildId());
        task.setPath(data.get("path").asText());
        Timer timer = task.getTimer();
        timer.setStartTime( Instant.ofEpochMilli(json.get("timestamp").asLong()));

        taskMap.put(taskId, task);
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

        Task task = taskMap.get(taskId);
        if(task == null) {
            throw new RuntimeException("Could not find task with id: " + taskId + " in the task map");
        }

        JsonNode timestamp = json.get("timestamp");
        assert timestamp != null;
        Timer timer = task.getTimer();
        timer.setFinishTime( Instant.ofEpochMilli(timestamp.asLong()) );

        task.setOutcome( json.get("data").get("outcome").asText());

        // insert into DB
        long newId = TasksDAO.insertTask(task);
        taskMap.remove(taskId);
    }
}
