package com.gradle.apiexport;

import java.util.Properties;
import org.knowm.yank.*;

public class CreateDB {
    public static void main(String[] args) {

        Properties dbProps = PropertiesUtils.getPropertiesFromClasspath("POSTGRES.properties");


        // SQL Statements in Properties file
       // Properties sqlProps = PropertiesUtils.getPropertiesFromClasspath("MYSQL_SQL.properties");
        Properties createTableProps = new Properties();
        createTableProps.put("DROP_TASKS", "DROP TABLE tasks");
        createTableProps.put("CREATE_TASKS","CREATE TABLE tasks(\n" +
                "   id          bigserial PRIMARY KEY   NOT NULL,\n" +
                "   build_id     text    NOT NULL,\n" +
                "   path         text     NOT NULL\n" +
                ");");

        Yank.setupDefaultConnectionPool(dbProps);
        Yank.addSQLStatements(createTableProps);

        // create table
        Yank.executeSQLKey("DROP_TASKS", null);
        Yank.executeSQLKey("CREATE_TASKS", null);


        Yank.releaseDefaultConnectionPool();

    }
}

/*
pg_ctl -D /usr/local/var/postgres/data -l /usr/local/var/postgres/data/server.log start

 */
