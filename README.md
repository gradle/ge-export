# Summary

The goal of this project is to create an simple ETL process for the Gradle Enterprise Export API.

# Motivation

Gradle Enterprise Export API (https://docs.gradle.com/enterprise/export-api) is a streaming API and can be imported into any appropriate datastore for ad-hoc analysis. This project will provide a simple ETL script to import data into a 'denormalized' RDBMS for easy querying via standard SQL.

Additionally RDBMS data can be easily imported into most analytics tools such as Tableau.

## Example Queries

select avg(build_duration) from builds;

select build_id, duration from tasks where path=':checkstyle' and duration > 6 and start_date > '10/01/2016';

# Running from Gradle

## Parameters:

-Dserver - Gradle Enterprise server name (assumes https on port 443)

-Dport - Gradle Enterprise server port. Defaults to 443

-Dhours - how many hours to go back from now. Default is 24hours. Use 'all' for all builds scans in the system (Warning: maybe be slow)

-DcreateDb - drops and creates all tables

## Setup

To run this sample:

1. Open a terminal window.
2. Run `./gradlew run -Dserver=your_server_name -Dhours=24` from the command line.