package com.gradle.exportapi;

import org.knowm.yank.PropertiesUtils;
import org.knowm.yank.Yank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

class CreateDB {

    static final Logger log = LoggerFactory.getLogger(CreateDB.class);

    public static void main(String[] args) {
        Properties dbProps = PropertiesUtils.getPropertiesFromClasspath("POSTGRES.properties");
        Yank.setupDefaultConnectionPool(dbProps);
        run();
        Yank.releaseDefaultConnectionPool();
    }

    public static void run() {
        log.info("Creating Database");

        Properties createTableProps = new Properties();
        createTableProps.put("DROP_BUILDS", "DROP TABLE IF EXISTS builds");
        createTableProps.put("DROP_TASKS", "DROP TABLE IF EXISTS tasks");
        createTableProps.put("DROP_TESTS", "DROP TABLE IF EXISTS tests");

        createTableProps.put("CREATE_BUILDS","CREATE TABLE builds(\n" +
                "   id                   bigserial PRIMARY KEY   NOT NULL,\n" +
                "   build_id             text      NOT NULL,\n" +
                "   user_name            text      NOT NULL,\n" +
                "   root_project_name    text      NOT NULL,\n" +
                "   start                timestamp with time zone NOT NULL,\n" +
                "   finish               timestamp with time zone NOT NULL,\n" +
                "   CONSTRAINT unique_build_id UNIQUE(build_id)\n" +
                ");");

        createTableProps.put("CREATE_TASKS","CREATE TABLE tasks(\n" +
                "   id                   bigserial PRIMARY KEY   NOT NULL,\n" +
                "   build_id             text      NOT NULL references builds(build_id) ON DELETE CASCADE,\n" +
                "   task_id              text      NOT NULL,\n" +
                "   path                 text      NOT NULL,\n" +
                "   type                 text      NOT NULL,\n" +
                "   duration_millis      int       NOT NULL,\n" +
                "   outcome              text      NOT NULL,\n" +
                "   CONSTRAINT unique_build_task UNIQUE(build_id,task_id)\n" +
                ");");

        createTableProps.put("CREATE_TESTS","CREATE TABLE tests(\n" +
                "   id                   bigserial PRIMARY KEY   NOT NULL,\n" +
                "   build_id             text      NOT NULL references builds(build_id) ON DELETE CASCADE,\n" +
                "   task_id              text      NOT NULL,\n" +
                "   test_id              text      NOT NULL,\n" +
                "   name                 text      NOT NULL,\n" +
                "   class_name           text      NOT NULL,\n" +
                "   status               text      NOT NULL,\n" +
                "   duration_millis      int       NOT NULL,\n" +
                "   CONSTRAINT unique_test UNIQUE(build_id,task_id,test_id)\n" +
                ");");

        Yank.addSQLStatements(createTableProps);

        Yank.executeSQLKey("DROP_TESTS", null);
        Yank.executeSQLKey("DROP_TASKS", null);
        Yank.executeSQLKey("DROP_BUILDS", null);

        Yank.executeSQLKey("CREATE_BUILDS", null);
        Yank.executeSQLKey("CREATE_TASKS", null);
        Yank.executeSQLKey("CREATE_TESTS", null);
    }
}
