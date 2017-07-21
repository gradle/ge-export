package com.gradle.exportapi;

import com.gradle.exportapi.model.Build;
import com.gradle.exportapi.model.CustomValue;
import com.gradle.exportapi.model.Task;
import com.gradle.exportapi.model.Test;

import java.time.Instant;

public class DataHelper {

    public static Build createBuild() {
        return createBuild(Instant.now().toString());
    }

    public static Build createBuild(String buildId) {
        Build build = new Build(buildId);
        build.getTimer().setStartTime(Instant.now());
        build.getTimer().setFinishTime(Instant.now());
        build.getTimer().setTimeZoneId("Europe/Berlin");
        build.setUserName(DataHelper.class.getSimpleName());
        build.setRootProjectName("ge-export");
        return build;
    }

    public static CustomValue createCustomValue(String buildId) {
        CustomValue customValue = new CustomValue();
        customValue.setBuildId(buildId);
        customValue.setKey("SomeKey");
        customValue.setValue("SomeValue");
        return customValue;
    }

    public static Task createTask(String buildId) {
        Task task = new Task();
        task.setBuildId(buildId);
        task.setTaskId(System.currentTimeMillis());
        task.setPath(":some:task:path");
        task.setType("FakeTaskType");
        task.setOutcome("SUCCESS");
        task.getTimer().setStartTime(Instant.now());
        task.getTimer().setFinishTime(Instant.now());
        return task;
    }

    public static Test createTest(String buildId, long taskId) {
        Test test = new Test();
        test.setBuildId(buildId);
        test.setTaskId(taskId);
        test.setClassName("some.fake.test.MyFakeTest");
        test.setName("shouldBeAFakeTest");
        test.getTimer().setStartTime(Instant.now());
        test.getTimer().setFinishTime(Instant.now());
        test.resolveStatus();
        test.resolveStatus();
        test.setStatus("passed");
        test.setTestId(System.currentTimeMillis());
        return test;
    }
}
