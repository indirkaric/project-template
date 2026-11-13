--liquibase formatted sql

--changeset {{service-name}}:0.0.1-SNAPSHOT-create-schema

CREATE SCHEMA {{schema}};

CREATE EXTENSION IF NOT EXISTS citext;

--rollback DROP SCHEMA {{schema}};
