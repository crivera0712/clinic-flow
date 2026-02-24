ALTER TABLE appointments
    ADD CONSTRAINT fk_appointments_cases
        FOREIGN KEY (c_id) REFERENCES cases(c_id)
            ON DELETE CASCADE;