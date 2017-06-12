package com.gradle.exportapi.dbutil;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SQLHelper {

    /**
     * Produces an INSERT statement that adds values to the table
     *
     * Default conflict resolution is to skip the insert
     *
     * @param table
     * @param values
     * @return
     */
    public static String insert(String table, Object[] values) {
        return insert(table, values, true);
    }

    /**
     * Produces an INSERT statement that adds values to the table
     *
     * @param table name of table to insert values into
     * @param values values to insert
     * @param skipOnConflict if false adds 'ON CONFLICT DO NOTHING' to query
     * @return
     */
    public static String insert(String table, Object[] values, boolean skipOnConflict) {
        String insertStatement = "INSERT INTO " + table + valuesForInsert(values);
        if(skipOnConflict) {
            // https://wiki.postgresql.org/wiki/UPSERT
            insertStatement += " ON CONFLICT DO NOTHING";
        }
        return insertStatement;
    }

    private static String valuesForInsert(Object[] values) {
        return " VALUES ("
                + Arrays.stream(values).map(o -> "?").collect(Collectors.joining(","))
                + ")";
    }
}
