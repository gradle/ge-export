package com.gradle.exportapi.dbutil;

import org.knowm.yank.PropertiesUtils;
import org.knowm.yank.Yank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SqlHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlHelper.class);

    public static final String GE_EXPORT_SCHEMA_PROPERTY_KEY = "geexport.sql.properties";
    private static final AtomicBoolean sqlQueriesLoaded = new AtomicBoolean(false);

    /**
     * Loads all of the SQL queries stored in a properties file specified by GE_EXPORT_SCHEMA_PROPERTY_KEY, or 'postgres-sql.properties'
     */
    public static void loadSqlQueries() {
        if (!sqlQueriesLoaded.get()) {
            synchronized (sqlQueriesLoaded) {
                if (!sqlQueriesLoaded.get()) {
                    String propertiesFile = System.getProperty(GE_EXPORT_SCHEMA_PROPERTY_KEY, "postgres-sql.properties");
                    LOGGER.info("Loading SQL queries from {}", propertiesFile);
                    Properties createTableProps = PropertiesUtils.getPropertiesFromClasspath(propertiesFile);
                    Yank.addSQLStatements(createTableProps);
                }
            }
        }
    }
}
