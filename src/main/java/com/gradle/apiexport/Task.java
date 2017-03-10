package com.gradle.apiexport;

class Task {
    private String id;
    private String buildId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String path;

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
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

    double durationInSec() {
        return timer.durationInSec();
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                "buildId=" + buildId +
                ", path='" + path +
                ", durationInSec=" + durationInSec() +
                '}';
    }
}
