package com.gradle.exportapi.dao;

import com.gradle.exportapi.dbutil.SQLHelper;
import com.gradle.exportapi.model.Build;
import org.knowm.yank.Yank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

public class BuildDAO {

    private static final Logger log = LoggerFactory.getLogger(BuildDAO.class);

    public static long insertBuild(Build build) {

        OffsetDateTime start = OffsetDateTime.ofInstant
                (build.getTimer().getStartTime(), ZoneId.of(build.getTimer().getTimeZoneId()));

        OffsetDateTime finish = OffsetDateTime.ofInstant
                (build.getTimer().getFinishTime(), ZoneId.of(build.getTimer().getTimeZoneId()));

        Object[] params = new Object[]{
                build.getBuildId(),
                build.getUserName(),
                build.getRootProjectName(),
                start,
                finish,
                build.getStatus(),
                build.getTagsAsSingleString()
        };

        String SQL = SQLHelper.insert("builds (build_id, user_name, root_project_name, start, finish, status, tags)", params);
        Long generatedId = Yank.insert(SQL, params);
        if (generatedId == 0) {
            throw new RuntimeException("Unable to save build record for " + build.getBuildId());
        }
        return generatedId;
    }

    /**
     * Get the `id` attribute of the build in the Build table, **NOT** the `build_id` attribute.
     * @param build Build object
     * @return Optional of id
     */
    public static Optional<Long> getBuildTableId(Build build) {
        // If build id exists in the builds table, return the id directly
        Long id = Yank.queryScalar("SELECT id FROM builds WHERE build_id = ?", Long.class, new Object[]{build.getBuildId()});
        return Optional.ofNullable(id);
    }

    public static String findLastBuildId() {
        String sql = "select build_id from builds where id in (select max(id) from builds);";
        Build build = Yank.queryBean(sql, Build.class, new Object[0]);
        return build != null ? build.getBuildId() : null;
    }
}
