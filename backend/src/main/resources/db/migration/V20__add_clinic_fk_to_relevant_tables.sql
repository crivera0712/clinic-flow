ALTER TABLE users ADD COLUMN clinic_id BIGINT;
ALTER TABLE patients ADD COLUMN clinic_id BIGINT;
ALTER TABLE therapists ADD COLUMN clinic_id BIGINT;
ALTER TABLE cases ADD COLUMN clinic_id BIGINT;
ALTER TABLE appointments ADD COLUMN clinic_id BIGINT;
ALTER TABLE auth_sessions ADD COLUMN clinic_id BIGINT;

UPDATE users SET clinic_id = 1;
UPDATE patients SET clinic_id = 1;
UPDATE therapists SET clinic_id = 1;
UPDATE cases SET clinic_id = 1;
UPDATE appointments SET clinic_id = 1;
UPDATE auth_sessions SET clinic_id = 1;

ALTER TABLE users ALTER COLUMN clinic_id SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT fk_users_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(clinic_id);

ALTER TABLE patients ALTER COLUMN clinic_id SET NOT NULL;
ALTER TABLE patients ADD CONSTRAINT fk_patients_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(clinic_id);

ALTER TABLE therapists ALTER COLUMN clinic_id SET NOT NULL;
ALTER TABLE therapists ADD CONSTRAINT fk_therapists_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(clinic_id);

ALTER TABLE cases ALTER COLUMN clinic_id SET NOT NULL;
ALTER TABLE cases ADD CONSTRAINT fk_cases_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(clinic_id);

ALTER TABLE appointments ALTER COLUMN clinic_id SET NOT NULL;
ALTER TABLE appointments ADD CONSTRAINT fk_appointments_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(clinic_id);

ALTER TABLE auth_sessions ALTER COLUMN clinic_id SET NOT NULL;
ALTER TABLE auth_sessions ADD CONSTRAINT fk_auth_sessions_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(clinic_id);