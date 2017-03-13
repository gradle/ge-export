package com.gradle.exportapi.model;

public class Task {
    private String taskId;
    private String buildId;
    private String path;
    private String outcome;
    private Timer timer = new Timer();

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }



    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public String getBuildId() {

        return buildId;
    }

    public String getPath() {
        return path;
    }

    public Timer getTimer() {
        return timer;
    }


    @Override
    public String toString() {
        return "Task {" +
                " taskId=" + taskId +
                ", buildId=" + buildId +
                ", path='" + path +
                ", durationInMillis=" + getTimer().durationInMillis() +
                ", outcome=" + outcome +
                " }";
    }
}
