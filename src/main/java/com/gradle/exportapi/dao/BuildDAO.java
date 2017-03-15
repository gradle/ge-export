package com.gradle.exportapi.dao;

import static com.gradle.exportapi.dbutil.SQLHelper.*;

import com.gradle.exportapi.model.Build;
import org.knowm.yank.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public class BuildDAO {

    public static long insertBuild(Build build) {



        Object[] params = new Object[] {
                build.getBuildId()
                };

        String SQL = insert("builds (build_id)", params);
        return Yank.insert(SQL, params);
    }

    public static int updateBuild(Build build) {
        String sql = "UPDATE builds SET start =  ?, finish = ? WHERE build_id = '" + build.getBuildId() + "'";

        OffsetDateTime start = OffsetDateTime.ofInstant
                (build.getTimer().getStartTime(), ZoneId.of(build.getTimer().getTimeZoneId()));

        OffsetDateTime finish = OffsetDateTime.ofInstant
                (build.getTimer().getStartTime(), ZoneId.of(build.getTimer().getTimeZoneId()));

        Object[] params = new Object[] {
                start, finish
        };
        return Yank.execute(sql, params);

    }

}
