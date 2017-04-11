package com.gradle.exportapi.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test {

    public static final Logger log = LoggerFactory.getLogger(Test.class);

    private String testId;
    private String buildId;
    private String taskId;
    private String name;
    private String className;
    private String status; //success failure skipped
    private final Timer timer = new Timer();

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getBuildId() {
        return buildId;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long durationInMillis() {
        if(timer.getStartTime() == null) {
            throw new RuntimeException("Missing start time. We don't think this should ever happen. Build Id: " + buildId + " Task id: " + taskId + " Test id: " + testId);
        }
        if(timer.getFinishTime() == null) {
            log.warn("Finished time is missing for " + "Build Id: " + buildId + " Task id: " + taskId + " Test id: " + testId);
            // for now, if finish is missing set it to start time
            timer.setFinishTime( timer.getStartTime() );
            this.setStatus("interrupted");
        }
        return timer.durationInMillis();
    }

    public Timer getTimer() {
        return timer;
    }
}
