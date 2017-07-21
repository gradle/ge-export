package com.gradle.exportapi;

import org.junit.After;
import org.junit.Before;
import org.knowm.yank.PropertiesUtils;
import org.knowm.yank.Yank;

import java.util.Properties;

public abstract class DatabaseBaseIntegrationTest {

    public static final String GE_EXPORT_TEST_DATABASE_PROPERTIES_KEY = "geexport.test.db.info";

    @Before
    public void setup() {
        String propertiesFile = System.getProperty(GE_EXPORT_TEST_DATABASE_PROPERTIES_KEY, "test-db-info.properties");
        Properties dbProps = PropertiesUtils.getPropertiesFromClasspath(propertiesFile);
        Yank.setThrowWrappedExceptions(true);
        Yank.setupDefaultConnectionPool(dbProps);
    }

    protected void recreateDb() {
        CreateDB.run();
    }

    @After
    public void tearDown() {
        Yank.releaseDefaultConnectionPool();
    }

}
