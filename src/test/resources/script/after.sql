SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE users RESTART IDENTITY;
TRUNCATE TABLE task_statuses RESTART IDENTITY;
TRUNCATE TABLE labels RESTART IDENTITY;
TRUNCATE TABLE tasks RESTART IDENTITY;
TRUNCATE TABLE tasks_labels RESTART IDENTITY;
SET REFERENTIAL_INTEGRITY TRUE;