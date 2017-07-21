package com.gradle.exportapi.dao;

import com.gradle.exportapi.model.Task;
import static com.gradle.exportapi.dbutil.SqlHelper.*;
import org.knowm.yank.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TasksDAO {

    static final Logger LOGGER = LoggerFactory.getLogger(TasksDAO.class);

    public static long insertTask(Task task) {
        long duration = 0;
        try {
            duration = task.getTimer().durationInMillis();
        } catch (Exception e) {
            LOGGER.warn("Failed to get task duration.  It's possible the task executor crashed. Path:" + task.getPath() + " Build: " + task.getBuildId(), e);
        }

        Object[] params = new Object[] {
                task.getBuildId(),
                task.getTaskId(),
                task.getPath(),
                task.getType(),
                duration,
                task.getOutcome()};

        long newId = Yank.insertSQLKey("INSERT_TASK", params);
        LOGGER.debug("Inserted task {} for build {}", task.getPath(), task.getBuildId());
        return newId;
    }
}
