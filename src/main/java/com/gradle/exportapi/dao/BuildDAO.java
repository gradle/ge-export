package com.gradle.exportapi.dao;

import com.gradle.exportapi.dbutil.SqlHelper;
import com.gradle.exportapi.model.Build;
import org.knowm.yank.Yank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

public class BuildDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildDAO.class);

    static {
        SqlHelper.loadSqlQueries();
    }


    public static long insertBuild(Build build) {
        LOGGER.debug("Inserting build {} into the database.", build.getBuildId());

        Object start;
        Object finish;
        if (SqlHelper.isMySql()) {
            start = LocalDateTime.ofInstant(build.getTimer().getStartTime(), ZoneOffset.UTC);
            finish = LocalDateTime.ofInstant(build.getTimer().getFinishTime(), ZoneOffset.UTC);
        } else {
            start = OffsetDateTime.ofInstant(build.getTimer().getStartTime(), ZoneId.of(build.getTimer().getTimeZoneId()));
            finish = OffsetDateTime.ofInstant(build.getTimer().getFinishTime(), ZoneId.of(build.getTimer().getTimeZoneId()));
        }

        Object[] params = new Object[]{
                build.getBuildId(),
                build.getUserName(),
                build.getRootProjectName(),
                start,
                finish,
                build.getStatus(),
                build.getTagsAsSingleString()
        };

        Long generatedId = Yank.insertSQLKey("INSERT_BUILD", params);
        if (generatedId == 0) {
            throw new RuntimeException("Unable to save build record for " + build.getBuildId());
        }
        return generatedId;
    }

    /**
     * Get the `id` attribute of the build in the Build table, **NOT** the `build_id` attribute.
     *
     * @param build Build object
     * @return Optional of id
     */
    public static Optional<Long> getBuildTableId(Build build) {
        // If build id exists in the builds table, return the id directly
        Long id = Yank.queryScalarSQLKey("BUILD_TABLE_ID", Long.class, new Object[]{build.getBuildId()});
        return Optional.ofNullable(id);
    }

    public static String findLastBuildId() {
        Build build = Yank.queryBeanSQLKey("LAST_BUILD_ID", Build.class, null);
        return build != null ? build.getBuildId() : null;
    }
}
