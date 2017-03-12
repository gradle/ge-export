package com.gradle.exportapi;

import java.time.Instant;

public class Build {

    private String buildId;
    private Instant startTime;
    private Instant finishTime;

    public Build(String buildId) {
        this.buildId = buildId;
    }

    public String getBuildId() {
        return buildId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Instant finishTime) {
        this.finishTime = finishTime;
    }
}
