CREATE TABLE tasks (
   taskId          bigserial PRIMARY KEY   NOT NULL,
   build_id     bigint    NOT NULL,
   path         text     NOT NULL
);
