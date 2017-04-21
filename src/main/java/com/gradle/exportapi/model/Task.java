package com.gradle.exportapi.model;

public class Task {
    private Long taskId;
    private String buildId;
    private String path;
    private String type;
    private String outcome;
    private final Timer timer = new Timer();

    public String getOutcome() {
        return outcome;
    }
    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public Long getTaskId() {
        return taskId;
    }
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getBuildId() { return buildId;}
    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public Timer getTimer() {
        return timer;
    }

    public void resolveStatus() { /*NO-OP*/ }

    @Override
    public String toString() {
        return "Task {" +
                " taskId=" + taskId +
                ", buildId=" + buildId +
                ", path='" + path +
                ", type='" + type +
                ", durationInMillis=" + getTimer().durationInMillis() +
                ", outcome=" + outcome +
                " }";
    }


}
