# Schema Cleanup

# Introduction

This repository houses the service to be internally used from schema service and wherever required to cleanup necessary schemas from Cosmos database and Blob storage.


# Pre-requisites

You need

1. Maven 3.8.x
2. Java 17

# Service:
This is the initial draft that does the cleanup. The records are first fetched from Cosmos and then cleaned up from both Cosmos and Blob. 

# Unit testing: Yet to do

# Getting started guide

## Environment variables to be added in application.properties to fetch and delete the records.
| name                    | value                                                | description                                                              |
|-------------------------|------------------------------------------------------|--------------------------------------------------------------------------|
| `blob.account_key`      | `*****`                                              | storage blob account key                                                 |
| `blob.account_name`     | `*****`                                              | storage blob account name                                                |
| `cosmos.host`           | `*****`                                              | cosmos host                                                              |
| `cosmos.key`            | `*****`                                              | cosmos key                                                               |
| `LOG_PREFIX`            | `schema-cleanup`                                     | log prefix value                                                         |
| `schema_id_query_param` | ex: `opendes:SchemaSanityTest:testSource:testschema` | the id that will be used<br/> to create the contains query in cosmos db. |
