ALTER TABLE appointments
    RENAME COLUMN start_time TO scheduled_at;

ALTER TABLE appointments
    ADD COLUMN created_at TIMESTAMP NOT NULL;

ALTER TABLE appointments
    ADD COLUMN modified_at TIMESTAMP NULL;
