package com.gradle.exportapi.dao;

import static com.gradle.exportapi.dbutil.SQLHelper.*;

import com.gradle.exportapi.model.Build;
import org.knowm.yank.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public class BuildDAO {

    static final Logger log = LoggerFactory.getLogger(BuildDAO.class);

    public static long insertBuild(Build build) {

        Object[] params = new Object[]{
                build.getBuildId()
        };

        String SQL = insert("builds (build_id)", params);
        return Yank.insert(SQL, params);
    }

    public static int updateBuild(Build build) {
        log.info("Updating build:" + build.getBuildId());
        String sql = "UPDATE builds SET user_name = ?, root_project_name = ?, start =  ?, finish = ? WHERE build_id = '" + build.getBuildId() + "'";

        OffsetDateTime start = OffsetDateTime.ofInstant
                (build.getTimer().getStartTime(), ZoneId.of(build.getTimer().getTimeZoneId()));

        OffsetDateTime finish = OffsetDateTime.ofInstant
                (build.getTimer().getStartTime(), ZoneId.of(build.getTimer().getTimeZoneId()));

        Object[] params = new Object[] { build.getUserName(), build.getRootProjectName(), start, finish };
        return Yank.execute(sql, params);
    }

    public static String findLastBuildId() {
        String sql = "select build_id from builds where id in (select max(id) from builds);";
        Build build = Yank.queryBean(sql, Build.class, new Object[0] );
        return build !=null ? build.getBuildId() : null;
    }
}
