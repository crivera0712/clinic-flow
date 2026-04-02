ALTER TABLE appointments
    DROP CONSTRAINT appointments_case_c_id_fk,
    DROP CONSTRAINT appointments_therapists_t_id_fk;

ALTER TABLE cases
    DROP CONSTRAINT case_body_regions_br_id_fk,
    DROP CONSTRAINT case_patients_p_id_fk;

ALTER TABLE body_regions
    ALTER COLUMN br_id TYPE BIGINT;
ALTER SEQUENCE body_regions_br_id_seq AS BIGINT;

ALTER TABLE patients
    ALTER COLUMN p_id TYPE BIGINT;
ALTER SEQUENCE patients_p_id_seq AS BIGINT;

ALTER TABLE cases
    ALTER COLUMN c_id TYPE BIGINT;
ALTER SEQUENCE cases_c_id_seq AS BIGINT;

ALTER TABLE therapists
    ALTER COLUMN t_id TYPE BIGINT;
ALTER SEQUENCE therapists_t_id_seq AS BIGINT;

ALTER TABLE appointments
    ALTER COLUMN apt_id TYPE BIGINT;
ALTER SEQUENCE appointments_apt_id_seq AS BIGINT;

ALTER TABLE cases
    ALTER COLUMN p_id TYPE BIGINT,
    ALTER COLUMN br_id TYPE BIGINT;

ALTER TABLE appointments
    ALTER COLUMN t_id TYPE BIGINT,
    ALTER COLUMN c_id TYPE BIGINT;

ALTER TABLE cases
    ADD CONSTRAINT case_body_regions_br_id_fk
        FOREIGN KEY (br_id) REFERENCES body_regions (br_id),
    ADD CONSTRAINT case_patients_p_id_fk
        FOREIGN KEY (p_id) REFERENCES patients (p_id);

ALTER TABLE appointments
    ADD CONSTRAINT appointments_case_c_id_fk
        FOREIGN KEY (c_id) REFERENCES cases (c_id),
    ADD CONSTRAINT appointments_therapists_t_id_fk
        FOREIGN KEY (t_id) REFERENCES therapists (t_id);
