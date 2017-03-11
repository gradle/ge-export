package com.gradle.apiexport;

import org.knowm.yank.*;

import java.util.*;
import java.util.stream.Collectors;

public class TasksDAO {

    public static String valuesForInsert(Object[] params) {
        return " VALUES ("
                + Arrays.stream(params).map( o -> "?").collect(Collectors.joining(","))
                + ")";
    }

    public static long insertTask(Task task) {

        Object[] params = new Object[] { task.getTaskId(), task.getBuildId(), task.getPath(), task.durationInMillis()};
        String SQL = "INSERT INTO tasks (task_id, build_id, path, duration_millis) " + valuesForInsert(params);
        return Yank.insert(SQL, params);
    }
}
