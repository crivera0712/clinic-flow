ALTER TABLE users
    ADD CONSTRAINT uq_username_clinic_id
        UNIQUE (clinic_id, username);