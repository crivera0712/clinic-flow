ALTER TABLE therapists
    ALTER COLUMN therapist_type TYPE VARCHAR(50) USING therapist_type::VARCHAR;
DROP TYPE therapist_type_enum;
