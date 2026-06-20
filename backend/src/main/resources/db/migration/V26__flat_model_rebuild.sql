-- Flatten the data model: appointments point directly at patients; drop cases & body
-- regions; collapse appointment status to the 3-state check-in lifecycle; drop therapist
-- type and patient display_name. Existing appointments are backfilled before drops.

-- 1. Appointments: add patient FK and backfill from cases
ALTER TABLE appointments ADD COLUMN p_id BIGINT;
UPDATE appointments a SET p_id = c.p_id FROM cases c WHERE a.c_id = c.c_id;
ALTER TABLE appointments ALTER COLUMN p_id SET NOT NULL;
ALTER TABLE appointments
    ADD CONSTRAINT appointments_patients_p_id_fk FOREIGN KEY (p_id) REFERENCES patients (p_id);

-- 2. Drop the case linkage
ALTER TABLE appointments DROP CONSTRAINT appointments_case_c_id_fk;
ALTER TABLE appointments DROP COLUMN c_id;

-- 3. Collapse status to SCHEDULED / WAITING / DONE
UPDATE appointments SET status = 'WAITING' WHERE status = 'CHECKED_IN';
UPDATE appointments SET status = 'DONE'    WHERE status IN ('IN_SESSION', 'FINISHED');

-- 4. Drop cases & body regions (cases.br_id referenced body_regions, so cases drops first)
DROP TABLE cases;
DROP TABLE body_regions;

-- 5. Therapists: drop type
ALTER TABLE therapists DROP COLUMN therapist_type;
DROP TYPE IF EXISTS therapist_type_enum;

-- 6. Patients: drop display_name
ALTER TABLE patients DROP COLUMN display_name;

-- 7. Drop the legacy board stored proc (it joined cases/body_regions)
DROP FUNCTION IF EXISTS findAllAppointmentsByDate(DATE);
