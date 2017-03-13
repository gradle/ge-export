package com.gradle.exportapi.model;

public class Build {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private String buildId;

    private Timer timer = new Timer();

    public Build(String buildId) {
        this.buildId = buildId;
    }

    public String getBuildId() {
        return buildId;
    }

    public Timer getTimer() {
        return timer;
    }


}
