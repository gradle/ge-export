package com.gradle.exportapi;

import java.time.Duration;
import java.time.Instant;


public class Timer {

    private Instant startTime;
    private Instant finishTime;
    private String timeZoneId;

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

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }


    public double durationInSec() {
        return durationInMillis() / 1000;
    }

    public long durationInMillis() { return Duration.between(startTime, finishTime).toMillis(); }

    @Override
    public String toString() {
        return "Timer{" +
                "startTime=" + startTime +
                ", finishTime=" + finishTime +
                '}';
    }
}
