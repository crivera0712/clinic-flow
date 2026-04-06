ALTER TABLE therapists
    ADD CONSTRAINT uq_therapists_clinic_name
        UNIQUE (clinic_id, therapist_name);