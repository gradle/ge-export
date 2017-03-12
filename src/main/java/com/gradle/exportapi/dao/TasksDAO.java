package com.gradle.exportapi.dao;

import com.gradle.exportapi.Task;
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
        return Yank.insert(SQL, params);
    }
}
