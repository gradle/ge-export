package com.gradle.exportapi.model;

public class Build {

    private long id;
    private String buildId;
    private Timer timer = new Timer();

    public Build(String buildId) {
        this.buildId = buildId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBuildId() {
        return buildId;
    }

    public Timer getTimer() {
        return timer;
    }

    @Override
    public String toString() {
        return "Build{" +
                "id=" + id +
                ", buildId='" + buildId + '\'' +
                ", timer=" + timer +
                '}';
    }


}
