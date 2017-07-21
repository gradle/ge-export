package com.gradle.exportapi;

import com.gradle.exportapi.dao.BuildDAO;
import com.gradle.exportapi.dao.TasksDAO;
import com.gradle.exportapi.dao.TestsDAO;
import com.gradle.exportapi.model.Build;
import com.gradle.exportapi.model.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestDaoIntegrationTest extends DatabaseBaseIntegrationTest {

    @Before
    public void createDb() {
        // Assuming the database has already been created
        // recreateDb();
    }

    @Test
    public void shouldInsertTest() {
        // Given a Test associated with a Task
        Build build = DataHelper.createBuild();
        BuildDAO.insertBuild(build);
        Task task = DataHelper.createTask(build.getBuildId());
        TasksDAO.insertTask(task);
        com.gradle.exportapi.model.Test test = DataHelper.createTest(build.getBuildId(), task.getTaskId());

        //When I insert that custom value
        long row = TestsDAO.insertTest(test);

        // Then the custom value is inserted correctly
        Assert.assertTrue(row > 0);
    }
}
