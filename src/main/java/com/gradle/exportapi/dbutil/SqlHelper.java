package com.gradle.exportapi.dbutil;


import org.knowm.yank.PropertiesUtils;
import org.knowm.yank.Yank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SqlHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlHelper.class);
    private static final AtomicBoolean sqlQueriesLoaded = new AtomicBoolean(false);
    private static final AtomicReference<String> databaseProductName = new AtomicReference<>(null);

    private static String getDatabaseProductName() {
        if (databaseProductName.get() == null) {
            synchronized (databaseProductName) {
                if (databaseProductName.get() == null) {
                    try (Connection connection = Yank.getDefaultConnectionPool().getConnection()) {
                        databaseProductName.set(connection.getMetaData().getDatabaseProductName());
                        LOGGER.info("Connected to a {} database.", databaseProductName.get());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return databaseProductName.get();
    }

    public static boolean isMySql() {
        return "MySQL".equals(getDatabaseProductName());
    }

    /**
     * Loads all of the SQL queries stored in a properties file specified by GE_EXPORT_SCHEMA_PROPERTY_KEY, or 'postgres-sql.properties'
     */
    public static void loadSqlQueries() {
        if (!sqlQueriesLoaded.get()) {
            synchronized (sqlQueriesLoaded) {
                if (!sqlQueriesLoaded.getAndSet(true)) {
                    String propertiesFile = isMySql() ? "mysql-sql.properties" : "postgres-sql.properties";
                    LOGGER.info("Loading SQL queries from {}", propertiesFile);
                    Properties createTableProps = PropertiesUtils.getPropertiesFromClasspath(propertiesFile);
                    Yank.addSQLStatements(createTableProps);
                }
            }
        }
    }
}
