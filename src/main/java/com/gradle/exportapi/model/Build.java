package com.gradle.exportapi.model;

import org.knowm.yank.annotations.Column;

import java.util.*;
import java.util.stream.Collectors;

public class Build {

    private long id;

    @Column("build_id")
    private String buildId;

    private String userName;

    private String rootProjectName;

    private final Timer timer = new Timer();

    private String status;

    public final Map<Long, Task> taskMap = new HashMap<>();

    public final Map<String, Test> testMap = new HashMap<>();

    public final List<CustomValue> customValues = new ArrayList<>();

    public final List<String> tags = new ArrayList<>();

    public Build() {}

    public Build(String buildId) {

        this.buildId = buildId;
        this.status = "finished";
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

    public void setBuildId(String buildId) { this.buildId = buildId; }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRootProjectName() {
        return rootProjectName;
    }

    public void setRootProjectName(String rootProjectName) {
        this.rootProjectName = rootProjectName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timer getTimer() {
        return timer;
    }

    public void resolveStatus() {
        taskMap.values().forEach( task -> task.resolveStatus() );
        testMap.values().forEach( test -> test.resolveStatus() );
        if(testMap.values().stream().anyMatch( test -> test.isInterrupted()) ) setStatus("interrupted");
    }

    public String getTagsAsSingleString() {
        return tags.stream().collect(Collectors.joining(","));
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
