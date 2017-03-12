package com.gradle.exportapi.dbutil;


import java.util.Arrays;
import java.util.stream.Collectors;

public class SQLHelper {

    public static String insert(String body, Object[] params) {
        return "INSERT INTO " + body + valuesForInsert(params);
    }

    public static String valuesForInsert(Object[] params) {
        return " VALUES ("
                + Arrays.stream(params).map(o -> "?").collect(Collectors.joining(","))
                + ")";
    }
}
