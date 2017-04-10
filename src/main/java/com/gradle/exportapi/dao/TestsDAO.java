package com.gradle.exportapi.dao;

import com.gradle.exportapi.model.Test;
import org.knowm.yank.Yank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gradle.exportapi.dbutil.SQLHelper.insert;

public class TestsDAO {

    static final Logger log = LoggerFactory.getLogger(TestsDAO.class);

    public static long insertTest(Test test) {

        // If the task executor crashed, checking its duration may result in an NPE
        long duration = 0;
        try {
            duration = test.getTimer().durationInMillis();
        } catch (Exception e) {
            log.warn("Failed to get duration of test " + test.getName() + " in build " + test.getBuildId() +
                     ", setting its duration to " + duration);
            log.debug("test " + test.getName() + " in build " + test.getBuildId(), e);
        }

        // If the task executor crashed, its status may be null
        if(test.getStatus() == null) {
            log.warn("Test " + test.getName() + " for build " + test.getBuildId() + " has no value for status, " +
                     "recording its status as 'error'");
            test.setStatus("error");
        }

        Object[] params = new Object[] {
                test.getBuildId(),
                test.getTaskId(),
                test.getTestId(),
                test.getName(),
                test.getClassName(),
                test.getStatus(),
                duration,
        };

        String SQL = insert("tests (build_id, task_id, test_id, name, class_name, status, duration_millis)", params);
        long newId = Yank.insert(SQL, params);
        log.debug("Created test id: " + newId + " test: " + test.getName() + " for build: " + test.getBuildId());
        return newId;
    }
}
