package com.gradle.exportapi;

import com.gradle.exportapi.dao.BuildDAO;
import org.junit.Assert;
import org.junit.Test;

public class SchemaIntegrationTest extends DatabaseBaseIntegrationTest {

    @Test
    public void shouldDropAndCreateSchema() {
        recreateDb();

        // Assert the DB exists and is empty
        // This isn't a very good assert, but right now we'll assert that we can look for a build and not find one
        Assert.assertNull(BuildDAO.findLastBuildId());
    }
}
