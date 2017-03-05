CREATE TABLE TASKS(
   ID           bigserial PRIMARY KEY   NOT NULL,
   BUILD_ID     bigint    NOT NULL,
   PATH         text     NOT NULL
);