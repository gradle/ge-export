package com.gradle.exportapi;

import com.gradle.exportapi.dao.BuildDAO;
import com.gradle.exportapi.model.Build;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

public class BuildDaoIntegrationTest extends DatabaseBaseIntegrationTest {

    @Before
    public void createDb() {
        // Assuming the database has already been created
        // recreateDb();
    }

    @Test
    public void shouldInsertBuild() {
        // Given a Build
        String buildId = String.valueOf(Instant.now());
        Build build = DataHelper.createBuild(buildId);

        //When I insert that build
        long row = BuildDAO.insertBuild(build);

        // Then the build is inserted correctly, and can be retrieved
        Assert.assertEquals(String.valueOf(row), BuildDAO.getBuildTableId(build).get().toString());
        Assert.assertEquals(buildId, BuildDAO.findLastBuildId());
    }



}
