package com.gradle.exportapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.gradle.exportapi.dao.BuildDAO;
import com.gradle.exportapi.dao.TasksDAO;
import com.gradle.exportapi.model.Build;
import com.gradle.exportapi.model.Task;
import com.gradle.exportapi.model.Timer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


class EventProcessor {

    Build currentBuild;

    // Maps to hold in-flight objects
    Map<String, Task> taskMap = new HashMap<>();


    public EventProcessor(String buildId) {
        this.currentBuild = new Build( buildId );
    }

    public void process(JsonNode json) {
        String eventType = json.get("type").get("eventType").asText();

        switch (eventType) {
            case "BuildStarted":
                buildStarted(json);
                break;
            case "Locality":
                locality(json);
                break;
            case "GradleLoaded":
                gradleLoaded(json);
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

    private void gradleLoaded(JsonNode json) {
        // insert into DB

        currentBuild.setId( BuildDAO.insertBuild(currentBuild) );
        System.out.println("DB-generated id: " + currentBuild.getId());
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
    void taskStarted(JsonNode json) {
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
    void taskFinished(JsonNode json) {
        JsonNode id = json.get("data").get("id");
        assert id != null;
        String taskId = id.asText();

        Task task = taskMap.get(taskId);
        if(task == null) {
            throw new RuntimeException("Could not find task with id: " + taskId + " in the task map");
        };

        JsonNode timestamp = json.get("timestamp");
        assert timestamp != null;
        Timer timer = task.getTimer();
        timer.setFinishTime( Instant.ofEpochMilli(timestamp.asLong()) );
        System.out.println("Task: " + task);

        task.setOutcome( json.get("data").get("outcome").asText());

        // insert into DB
        long newId = TasksDAO.insertTask(task);
        System.out.println("Created rec id: " + newId);
        taskMap.remove(taskId);
    }
}
