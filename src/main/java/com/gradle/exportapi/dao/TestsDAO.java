package com.gradle.exportapi.dao;

import com.gradle.exportapi.model.Test;
import org.knowm.yank.Yank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gradle.exportapi.dbutil.SQLHelper.insert;

public class TestsDAO {

    static final Logger log = LoggerFactory.getLogger(TestsDAO.class);

    public static long insertTest(Test test) {
        Object[] params = new Object[] {
                test.getBuildId(),
                test.getTaskId(),
                test.getTestId(),
                test.getName(),
                test.getClassName(),
                test.getStatus(),
                test.getTimer().durationInMillis(),
        };

        String SQL = insert("tests (build_id, task_id, test_id, name, class_name, status, duration_millis)", params);
        long newId = Yank.insert(SQL, params);
        log.debug("Created test id: " + newId + " test: " + test.getName() + " for build: " + test.getBuildId());
        return newId;
    }
}
