package com.gradle.apiexport;

class Task {
    private String taskId;
    private String buildId;
    private String outcome;

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

    private String path;

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

    private Timer timer = new Timer();

    long durationInMillis() {
        return timer.durationInMillis();
    }

    @Override
    public String toString() {
        return "Task {" +
                " taskId=" + taskId +
                ", buildId=" + buildId +
                ", path='" + path +
                ", durationInMillis=" + durationInMillis() +
                ", outcome=" + outcome +
                " }";
    }
}
