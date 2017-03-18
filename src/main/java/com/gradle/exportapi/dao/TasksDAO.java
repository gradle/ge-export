package com.gradle.exportapi.dao;

import com.gradle.exportapi.model.Task;
import static com.gradle.exportapi.dbutil.SQLHelper.*;
import org.knowm.yank.*;

public class TasksDAO {

    public static long insertTask(Task task) {
        Object[] params = new Object[] {
                task.getTaskId(),
                task.getBuildId(),
                task.getPath(),
                task.getTimer().durationInMillis(),
                task.getOutcome()};

        String SQL = insert("tasks (task_id, build_id, path, duration_millis, outcome)", params);
        long newId = Yank.insert(SQL, params);
        System.out.println("Created task id: " + newId + " task: " + task.getPath() + " for build: " + task.getBuildId());
        return newId;
    }
}
