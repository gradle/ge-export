package com.gradle.exportapi.dao;

import static com.gradle.exportapi.dbutil.SQLHelper.*;

import com.gradle.exportapi.model.Build;
import org.knowm.yank.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public class BuildDAO {

    public static long insertBuild(Build build) {

        OffsetDateTime start = OffsetDateTime.ofInstant
                (build.getTimer().getStartTime(), ZoneId.of(build.getTimer().getTimeZoneId()));

        Object[] params = new Object[] {
                build.getBuildId(),
                start
                };

        String SQL = insert("builds (build_id, start)", params);
        return Yank.insert(SQL, params);
    }
}
