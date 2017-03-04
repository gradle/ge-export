package com.gradle.apiexport;

import java.time.Duration;
import java.time.Instant;


class Timer {
    Instant startTime;
    Instant finishTime;

    double durationInSec() {
        return Duration.between(startTime, finishTime).toMillis() / 1000;
    }

    @Override
    public String toString() {
        return "Timer{" +
                "startTime=" + startTime +
                ", finishTime=" + finishTime +
                '}';
    }
}
