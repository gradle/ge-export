package com.gradle.exportapi.dao;

import com.gradle.exportapi.model.Task;
import static com.gradle.exportapi.dbutil.SQLHelper.*;
import org.knowm.yank.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TasksDAO {

    static final Logger log = LoggerFactory.getLogger(TasksDAO.class);

    public static long insertTask(Task task) {
        Object[] params = new Object[] {
                task.getTaskId(),
                task.getBuildId(),
                task.getPath(),
                task.getTimer().durationInMillis(),
                task.getOutcome()};

        String SQL = insert("tasks (task_id, build_id, path, duration_millis, outcome)", params);
        long newId = Yank.insert(SQL, params);
        log.debug("Created task id: " + newId + " task: " + task.getPath() + " for build: " + task.getBuildId());
        return newId;
    }
}
