package com.gradle.exportapi;

public class Build {

    private String buildId;
    private String timeZoneId;

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

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
