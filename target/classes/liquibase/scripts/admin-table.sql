--liquibase formatted sql

--changeset schikin: 1
CREATE TABLE notification (
    id BIGINT PRIMARY KEY ,
    chat_id BIGINT ,
    "task" VARCHAR(70) ,
    "date" TIMESTAMP
)