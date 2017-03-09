package com.gradle.apiexport;

import java.util.Properties;
import com.xeiam.yank.*;

public class DatabaseOps {
    public static void main(String[] args) {

        Properties dbProps = PropertiesUtils.getPropertiesFromClasspath("POSTGRES.properties");
        dbProps.setProperty("jdbcUrl", "jdbc:postgresql://localhost:5432/postgres");
        dbProps.setProperty("username", "dvydra");
        dbProps.setProperty("password", "");
        dbProps.setProperty("maximumPoolSize", "5");

        // SQL Statements in Properties file
       // Properties sqlProps = PropertiesUtils.getPropertiesFromClasspath("MYSQL_SQL.properties");
        Properties createTableProps = new Properties();
        createTableProps.put("CREATE_TASKS","CREATE TABLE TASKS(\n" +
                "   id          bigserial PRIMARY KEY   NOT NULL,\n" +
                "   build_id     bigint    NOT NULL,\n" +
                "   path         text     NOT NULL\n" +
                ");");

        Yank.setupDataSource(dbProps);
        Yank.addSQLStatements(createTableProps);

        // create table
        Yank.executeSQLKey("CREATE_TASKS", null);

        Yank.releaseDataSource();

    }
}
