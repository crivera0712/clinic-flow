ALTER TABLE users DROP CONSTRAINT uq_username_clinic_id;
ALTER TABLE users ADD CONSTRAINT uq_username UNIQUE (username);
