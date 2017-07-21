package com.gradle.exportapi.dao;


import com.gradle.exportapi.model.CustomValue;
import org.knowm.yank.Yank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomValueDAO {

    static final Logger LOGGER = LoggerFactory.getLogger(CustomValueDAO.class);

    public static long insertCustomValue(CustomValue cv) {
        Object[] params = new Object[]{
                cv.getBuildId(),
                cv.getKey(),
                cv.getValue(),
        };

        long newId = Yank.insertSQLKey("INSERT_CUSTOM_VALUE", params);
        LOGGER.debug("Inserted custom value with key {} for build {}", cv.getKey(), cv.getBuildId());
        return newId;
    }
}
