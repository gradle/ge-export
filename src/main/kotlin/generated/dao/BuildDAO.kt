package generated.dao

import com.gradle.exportapi.dbutil.SQLHelper.*

import com.gradle.exportapi.model.Build
import org.knowm.yank.*

import java.time.OffsetDateTime
import java.time.ZoneId

object BuildDAO {

    fun insertBuild(build: Build): Long {


        val params = arrayOf<Any>(build.buildId)

        val SQL = insert("builds (build_id)", params)
        return Yank.insert(SQL, params)!!
    }

    fun updateBuild(build: Build): Int {
        println("Updating build:" + build.buildId)
        val sql = "UPDATE builds SET start =  ?, finish = ? WHERE build_id = '$build.buildId' "

        val start = OffsetDateTime.ofInstant(build.timer.startTime, ZoneId.of(build.timer.timeZoneId))

        val finish = OffsetDateTime.ofInstant(build.timer.startTime, ZoneId.of(build.timer.timeZoneId))

        val params = arrayOf<Any>(start, finish)
        return Yank.execute(sql, params)

    }

}
