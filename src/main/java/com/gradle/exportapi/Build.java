package com.gradle.exportapi;

public class Build {

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
