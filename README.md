# Summary

This project demonstrates the Extract-Transform-Load (ETL) of data obtained from the Gradle Enteprise Export API.

# Motivation

The Gradle Enterprise Export API (https://docs.gradle.com/enterprise/export-api) is a streaming API that produces a stream of events that occurred during the execution of the build. Export API is implemented via Server-Sent Events which makes it convenient for real-time monitoring but not necessarily for ah-hoc analysis. By loading the data into an RDBMS we gain the full power of SQL and the ability to easily load the data into specialized analysis and visualization tools.

## Example Queries

```SQL
SELECT b.build_id, 
       b.start 
FROM   builds b, 
       tasks t 
WHERE  b.build_id = t.build_id 
       AND path = ':test' 
       AND duration_millis > 1000; 
```

# Running from Gradle

To run ge-export:

* Open a terminal window.
* Copy `./src/main/resources/db-info.properties.template` to `./src/main/resources/db-info.properties`
    * Open the new `db-info.properties` file and fill in the target database information
* Copy `./sample.gradle.properties` to `./gradle.properties`
    * Open `gradle.properties` file and fill out the configuration
* Run `./gradlew run`
