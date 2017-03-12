package com.gradle.exportapi.dao;

import static com.gradle.exportapi.dbutil.SQLHelper.*;

import com.gradle.exportapi.Build;
import org.knowm.yank.*;

public class BuildDAO {

    public static long insertBuild(Build build) {

        Object[] params = new Object[] {
                build.getBuildId(),
                };

        String SQL = insert("builds (build_id)", params);
        return Yank.insert(SQL, params);
    }
}
