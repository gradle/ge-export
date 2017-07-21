package com.gradle.exportapi;

import com.gradle.exportapi.dbutil.SqlHelper;
import org.knowm.yank.Yank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CreateDB {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDB.class);

    public static void run() {
        LOGGER.info("Creating Database");
        SqlHelper.loadSqlQueries();

        Yank.executeSQLKey("DROP_CUSTOM_VALUES", null);
        Yank.executeSQLKey("DROP_TESTS", null);
        Yank.executeSQLKey("DROP_TASKS", null);
        Yank.executeSQLKey("DROP_BUILDS", null);

        Yank.executeSQLKey("CREATE_BUILDS", null);
        Yank.executeSQLKey("CREATE_TASKS", null);
        Yank.executeSQLKey("CREATE_TESTS", null);
        Yank.executeSQLKey("CREATE_CUSTOM_VALUES", null);
    }
}
