package com.gradle.exportapi.dao;


import com.gradle.exportapi.model.CustomValue;
import org.knowm.yank.Yank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gradle.exportapi.dbutil.SQLHelper.insert;

public class CustomValueDAO {

    static final Logger log = LoggerFactory.getLogger(CustomValueDAO.class);

    public static long insertCustomValue(CustomValue cv) {

        Object[] params = new Object[]{
                cv.getBuildId(),
                cv.getKey(),
                cv.getValue(),
        };

        String SQL = insert("custom_values (build_id, key, value)", params);

        long newId = Yank.insert(SQL, params);
        log.debug("Created custom value id: " + newId + " key: " + cv.getKey() + " for build: " + cv.getBuildId());
        return newId;
    }
}
