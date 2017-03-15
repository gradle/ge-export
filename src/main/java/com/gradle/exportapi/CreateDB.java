package com.gradle.exportapi;

import org.knowm.yank.PropertiesUtils;
import org.knowm.yank.Yank;

import java.util.Properties;

class CreateDB {

    public static void main(String[] args) {
        Properties dbProps = PropertiesUtils.getPropertiesFromClasspath("POSTGRES.properties");
        Yank.setupDefaultConnectionPool(dbProps);
        run();
        Yank.releaseDefaultConnectionPool();
    }

    public static void run() {
        System.out.println("Creating Database");

        Properties createTableProps = new Properties();
        createTableProps.put("DROP_BUILDS", "DROP TABLE builds");
        createTableProps.put("DROP_TASKS", "DROP TABLE tasks");

        createTableProps.put("CREATE_BUILDS","CREATE TABLE builds(\n" +
                "   id                   bigserial PRIMARY KEY   NOT NULL,\n" +
                "   build_id             text      NOT NULL,\n" +
                "   start                timestamp with time zone,\n" +
                "   finish               timestamp with time zone,\n" +
                "   CONSTRAINT unigue_build_id UNIQUE(build_id)\n" +
                ");");

        createTableProps.put("CREATE_TASKS","CREATE TABLE tasks(\n" +
                "   id                   bigserial PRIMARY KEY   NOT NULL,\n" +
                "   task_id              text      NOT NULL,\n" +
                "   build_id             text      NOT NULL references builds(build_id),\n" +
                "   path                 text      NOT NULL,\n" +
                "   duration_millis      int       NOT NULL,\n" +
                "   outcome              text      NOT NULL\n" +
                ");");


        Yank.addSQLStatements(createTableProps);

        // create tables

        Yank.executeSQLKey("DROP_TASKS", null);
        Yank.executeSQLKey("DROP_BUILDS", null);

        Yank.executeSQLKey("CREATE_BUILDS", null);
        Yank.executeSQLKey("CREATE_TASKS", null);


    }
}

/*
pg_ctl -D /usr/local/var/postgres/data -l /usr/local/var/postgres/data/server.log start

 */
