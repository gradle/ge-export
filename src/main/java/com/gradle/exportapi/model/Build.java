package com.gradle.exportapi.model;

import org.knowm.yank.annotations.Column;

public class Build {

    private long id;

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    @Column("BUILD_ID")
    private String buildId;

    private String userName;

    private final Timer timer = new Timer();

    public Build() {}

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
