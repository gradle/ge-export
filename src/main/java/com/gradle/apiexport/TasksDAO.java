package com.gradle.apiexport;

import org.knowm.yank.*;

public class TasksDAO {

    public static long insertTask(Task task) {

        Object[] params = new Object[] { task.getBuildId(), task.getPath()};
        String SQL = "INSERT INTO tasks (build_id, path) VALUES (?, ?)";
        return Yank.insert(SQL, params);
    }
}
