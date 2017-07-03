# Summary

This project demonstrate the Extract-Transform-Load (ETL) of data obtained from the Gradle Enteprise Export API.

# Motivation

Gradle Enterprise Export API (https://docs.gradle.com/enterprise/export-api) is a streaming API that produces a stream of events that occured during the execution of the buid. Export API is implemented via Server-Sent Events which makes it convenient for real-time monitoring but not ncessarily for ah-hoc analysis. By loading the data into an RDBMS we gain the full power of SQL and the ability to easily load the data into specialized analysis and visualization tools.

## Example Queries

select b.build_id, b.start from builds b, tasks t where b.build_id = t.build_id and path = ':test' and duration_millis > 1000;

# Running from Gradle

## Parameters:

-Dserver - Gradle Enterprise server name. Start with http:// or https://

-Dport - Gradle Enterprise server port. Defaults to 80 for http and 443 for https.

-Dhours - how many hours to go back from now. Default is 24hours. Use 'all' for all builds scans in the system (Warning: maybe be slow)

-DcreateDb - drops and creates all tables

-Dbasic_auth - base64 encoded username:password

-Dnum_of_streams - number of build event streams to process in parallel (defaults to 5)

## Setup

To run this sample:

- Open a terminal window.

- Copy Copy POSTGRES.properties.template to POSTGRES.properties in same directory 
(i.e. src/main/resources/POSTGRES.properties) and fill in the information for the target PostgreSQL database that data will 
be exported to.

- Run `./gradlew run -Dserver=https://your_server_name -Dhours=24 -DcreateDb` from the command line.
