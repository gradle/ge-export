package com.gradle.exportapi;

import com.gradle.exportapi.dao.BuildDAO;
import com.gradle.exportapi.dao.CustomValueDAO;
import com.gradle.exportapi.dao.TasksDAO;
import com.gradle.exportapi.model.Build;
import com.gradle.exportapi.model.CustomValue;
import com.gradle.exportapi.model.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TaskDaoIntegrationTest extends DatabaseBaseIntegrationTest {

    @Before
    public void createDb() {
        // Assuming the database has already been created
        // recreateDb();
    }

    @Test
    public void shouldInsertTask() {
        // Given a custom value associated to a build
        Build build = DataHelper.createBuild();
        BuildDAO.insertBuild(build);
        Task task = DataHelper.createTask(build.getBuildId());

        //When I insert that custom value
        long row = TasksDAO.insertTask(task);

        // Then the custom value is inserted correctly
        Assert.assertTrue(row > 0);
    }
}
