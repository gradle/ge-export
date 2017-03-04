package com.gradle.apiexport;

class Task {
    String buildId;
    String path;
    Timer timer = new Timer();

    double durationInSec() {
        return timer.durationInSec();
    }

    @Override
    public String toString() {
        return "Task{" +
                "buildId=" + buildId +
                ", path='" + path +
                ", durationInSec=" + durationInSec() +
                '}';
    }
}
