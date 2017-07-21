package com.gradle.exportapi;

import com.gradle.exportapi.dao.BuildDAO;
import com.gradle.exportapi.dao.CustomValueDAO;
import com.gradle.exportapi.model.Build;
import com.gradle.exportapi.model.CustomValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.crypto.Data;
import java.time.Instant;

public class CustomValueDaoIntegrationTest extends DatabaseBaseIntegrationTest {

    @Before
    public void createDb() {
        // Assuming the database has already been created
        // recreateDb();
    }

    @Test
    public void shouldInsertCustomValue() {
        // Given a custom value associated to a build
        Build build = DataHelper.createBuild();
        BuildDAO.insertBuild(build);
        CustomValue value = DataHelper.createCustomValue(build.getBuildId());

        //When I insert that custom value
        long row = CustomValueDAO.insertCustomValue(value);

        // Then the custom value is inserted correctly
        Assert.assertTrue(row > 0);
    }
}
