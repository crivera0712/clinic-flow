
ALTER TABLE appointments
DROP FOREIGN KEY appointments_case_c_id_fk,
    DROP FOREIGN KEY appointments_therapists_t_id_fk;

ALTER TABLE cases
DROP FOREIGN KEY case_body_regions_br_id_fk,
    DROP FOREIGN KEY case_patients_p_id_fk;

ALTER TABLE body_regions
    MODIFY br_id BIGINT AUTO_INCREMENT;

ALTER TABLE patients
    MODIFY p_id BIGINT AUTO_INCREMENT;

ALTER TABLE cases
    MODIFY c_id BIGINT AUTO_INCREMENT;

ALTER TABLE therapists
    MODIFY t_id BIGINT AUTO_INCREMENT;

ALTER TABLE appointments
    MODIFY apt_id BIGINT AUTO_INCREMENT;



ALTER TABLE cases
    MODIFY p_id BIGINT NOT NULL,
    MODIFY br_id BIGINT NOT NULL;

ALTER TABLE appointments
    MODIFY t_id BIGINT NOT NULL,
    MODIFY c_id BIGINT NOT NULL;


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
