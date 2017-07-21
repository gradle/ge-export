package com.gradle.exportapi.dao;

import com.gradle.exportapi.model.Test;
import org.knowm.yank.Yank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestsDAO {

    static final Logger LOGGER = LoggerFactory.getLogger(TestsDAO.class);

    public static long insertTest(Test test) {
        if (test.getStatus() == null) {
            LOGGER.warn("Test {} for build {} has no status.  Setting the status to error.", test.getName(), test.getBuildId());
            test.setStatus("error");
        }

        Object[] params = new Object[]{
                test.getBuildId(),
                test.getTaskId(),
                test.getTestId(),
                test.getName(),
                test.getClassName(),
                test.getStatus(),
                test.durationInMillis()
        };

        long newId = Yank.insertSQLKey("INSERT_TEST", params);
        LOGGER.debug("Inserted test {} for build {}", test.getName(), test.getBuildId());
        return newId;
    }
}
