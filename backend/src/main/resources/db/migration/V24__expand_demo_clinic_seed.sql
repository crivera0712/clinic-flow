INSERT INTO users (username, password_hash, enabled, created_at, role, clinic_id)
SELECT 'demo_display', '$2a$10$qB3Ki0SZtwytpKHBVwU9DO/BvxlTjlLDckxPjxa8NffOOmNB9rK.6', TRUE, CURRENT_TIMESTAMP, 'DISPLAY', c.clinic_id
FROM clinics c
WHERE c.slug = 'demo'
  AND NOT EXISTS (
    SELECT 1
    FROM users u
    WHERE u.username = 'demo_display'
      AND u.clinic_id = c.clinic_id
);

WITH seed_regions (code, display_name) AS (
    VALUES
        ('SHOULDER', 'Shoulder'),
        ('KNEE', 'Knee'),
        ('HIP', 'Hip'),
        ('ANKLE', 'Ankle'),
        ('ELBOW', 'Elbow'),
        ('WRIST', 'Wrist'),
        ('NECK', 'Neck'),
        ('LOW_BACK', 'Low Back'),
        ('HAND', 'Hand'),
        ('FOOT', 'Foot')
)
INSERT INTO body_regions (code, display_name, is_active)
SELECT sr.code, sr.display_name, TRUE
FROM seed_regions sr
WHERE NOT EXISTS (
    SELECT 1
    FROM body_regions br
    WHERE br.code = sr.code
);

WITH demo_clinic AS (
    SELECT clinic_id
    FROM clinics
    WHERE slug = 'demo'
),
seed_therapists (therapist_name, therapist_type) AS (
    VALUES
        ('Jill Valentine', 'PHYSICAL_THERAPIST'),
        ('Claire Redfield', 'OCCUPATIONAL_THERAPIST'),
        ('Rebecca Chambers', 'PHYSICAL_THERAPY_ASSISTANT'),
        ('Carlos Oliveira', 'PHYSICAL_THERAPIST'),
        ('Sheva Alomar', 'OCCUPATIONAL_THERAPIST'),
        ('Helena Harper', 'PHYSICAL_THERAPY_ASSISTANT')
)
INSERT INTO therapists (therapist_name, therapist_type, clinic_id)
SELECT st.therapist_name, st.therapist_type, dc.clinic_id
FROM demo_clinic dc
CROSS JOIN seed_therapists st
WHERE NOT EXISTS (
    SELECT 1
    FROM therapists t
    WHERE t.clinic_id = dc.clinic_id
      AND t.therapist_name = st.therapist_name
);

WITH demo_clinic AS (
    SELECT clinic_id
    FROM clinics
    WHERE slug = 'demo'
),
seed_patients (first_name, last_name, display_name) AS (
    VALUES
        ('Leon', 'Kennedy', 'Kennedy, L'),
        ('Chris', 'Redfield', 'Redfield, C'),
        ('Ada', 'Wong', 'Wong, A'),
        ('Barry', 'Burton', 'Burton, B'),
        ('Sherry', 'Birkin', 'Birkin, S'),
        ('Ethan', 'Winters', 'Winters, E'),
        ('Mia', 'Winters', 'Winters, M'),
        ('Rose', 'Winters', 'Winters, R'),
        ('Moira', 'Burton', 'Burton, M'),
        ('Ashley', 'Graham', 'Graham, A'),
        ('Ingrid', 'Hunnigan', 'Hunnigan, I'),
        ('Parker', 'Luciani', 'Luciani, P')
)
INSERT INTO patients (first_name, last_name, display_name, clinic_id)
SELECT sp.first_name, sp.last_name, sp.display_name, dc.clinic_id
FROM demo_clinic dc
CROSS JOIN seed_patients sp
WHERE NOT EXISTS (
    SELECT 1
    FROM patients p
    WHERE p.clinic_id = dc.clinic_id
      AND p.display_name = sp.display_name
);

WITH demo_clinic AS (
    SELECT clinic_id
    FROM clinics
    WHERE slug = 'demo'
),
case_seed (display_name, body_region_code, days_ago) AS (
    VALUES
        ('Kennedy, L', 'SHOULDER', 45),
        ('Redfield, C', 'KNEE', 42),
        ('Wong, A', 'HIP', 39),
        ('Burton, B', 'ANKLE', 36),
        ('Birkin, S', 'ELBOW', 33),
        ('Winters, E', 'WRIST', 30),
        ('Winters, M', 'NECK', 27),
        ('Winters, R', 'LOW_BACK', 24),
        ('Burton, M', 'HAND', 21),
        ('Graham, A', 'FOOT', 18),
        ('Hunnigan, I', 'SHOULDER', 15),
        ('Luciani, P', 'KNEE', 12)
)
INSERT INTO cases (p_id, br_id, clinic_id, created_at)
SELECT p.p_id, br.br_id, dc.clinic_id, CURRENT_TIMESTAMP - make_interval(days => cs.days_ago)
FROM demo_clinic dc
JOIN case_seed cs ON TRUE
JOIN patients p ON p.clinic_id = dc.clinic_id AND p.display_name = cs.display_name
JOIN body_regions br ON br.code = cs.body_region_code
WHERE NOT EXISTS (
    SELECT 1
    FROM cases existing
    WHERE existing.clinic_id = dc.clinic_id
      AND existing.p_id = p.p_id
      AND existing.br_id = br.br_id
);

WITH demo_clinic AS (
    SELECT clinic_id
    FROM clinics
    WHERE slug = 'demo'
),
appointment_seed (
    display_name,
    therapist_name,
    days_offset,
    scheduled_time,
    status,
    type
) AS (
    VALUES
        ('Kennedy, L', 'Jill Valentine', 0, TIME '08:00', 'CHECKED_IN', 'EVALUATION'),
        ('Redfield, C', 'Carlos Oliveira', 0, TIME '08:30', 'CHECKED_IN', 'FOLLOW_UP'),
        ('Wong, A', 'Claire Redfield', 0, TIME '09:00', 'SCHEDULED', 'EVALUATION'),
        ('Burton, B', 'Sheva Alomar', 0, TIME '09:30', 'SCHEDULED', 'FOLLOW_UP'),
        ('Birkin, S', 'Rebecca Chambers', 0, TIME '10:00', 'SCHEDULED', 'REASSESSMENT'),
        ('Winters, E', 'Helena Harper', 0, TIME '10:30', 'SCHEDULED', 'EVALUATION'),
        ('Winters, M', 'Jill Valentine', 0, TIME '11:00', 'IN_SESSION', 'FOLLOW_UP'),
        ('Winters, R', 'Claire Redfield', 0, TIME '11:30', 'FINISHED', 'REASSESSMENT'),
        ('Burton, M', 'Carlos Oliveira', -14, TIME '09:00', 'FINISHED', 'EVALUATION'),
        ('Graham, A', 'Sheva Alomar', -14, TIME '10:00', 'FINISHED', 'FOLLOW_UP'),
        ('Hunnigan, I', 'Rebecca Chambers', -10, TIME '13:00', 'FINISHED', 'EVALUATION'),
        ('Luciani, P', 'Helena Harper', -10, TIME '14:00', 'FINISHED', 'FOLLOW_UP'),
        ('Kennedy, L', 'Jill Valentine', 1, TIME '09:00', 'SCHEDULED', 'FOLLOW_UP'),
        ('Redfield, C', 'Carlos Oliveira', 1, TIME '10:00', 'SCHEDULED', 'REASSESSMENT'),
        ('Wong, A', 'Claire Redfield', 2, TIME '11:00', 'SCHEDULED', 'FOLLOW_UP'),
        ('Burton, B', 'Sheva Alomar', 2, TIME '13:30', 'SCHEDULED', 'FOLLOW_UP'),
        ('Birkin, S', 'Rebecca Chambers', 5, TIME '09:30', 'SCHEDULED', 'FOLLOW_UP'),
        ('Winters, E', 'Helena Harper', 5, TIME '10:30', 'SCHEDULED', 'FOLLOW_UP'),
        ('Winters, M', 'Jill Valentine', -7, TIME '15:00', 'FINISHED', 'EVALUATION'),
        ('Winters, R', 'Claire Redfield', -7, TIME '16:00', 'FINISHED', 'FOLLOW_UP'),
        ('Burton, M', 'Carlos Oliveira', 9, TIME '08:30', 'SCHEDULED', 'REASSESSMENT'),
        ('Graham, A', 'Sheva Alomar', 9, TIME '09:30', 'SCHEDULED', 'FOLLOW_UP'),
        ('Hunnigan, I', 'Rebecca Chambers', -3, TIME '12:00', 'FINISHED', 'REASSESSMENT'),
        ('Luciani, P', 'Helena Harper', -3, TIME '13:00', 'FINISHED', 'FOLLOW_UP')
),
resolved_appointments AS (
    SELECT
        dc.clinic_id,
        cs.c_id,
        t.t_id,
        ((CURRENT_DATE + aps.days_offset) + aps.scheduled_time) AS scheduled_at,
        aps.status,
        aps.type,
        GREATEST(1, ABS(aps.days_offset)) AS created_days_ago
    FROM demo_clinic dc
    JOIN appointment_seed aps ON TRUE
    JOIN patients p ON p.clinic_id = dc.clinic_id AND p.display_name = aps.display_name
    JOIN cases cs ON cs.clinic_id = dc.clinic_id AND cs.p_id = p.p_id
    JOIN therapists t ON t.clinic_id = dc.clinic_id AND t.therapist_name = aps.therapist_name
)
INSERT INTO appointments (scheduled_at, status, type, created_at, c_id, t_id, clinic_id)
SELECT
    ra.scheduled_at,
    ra.status,
    ra.type,
    CURRENT_TIMESTAMP - make_interval(days => ra.created_days_ago),
    ra.c_id,
    ra.t_id,
    ra.clinic_id
FROM resolved_appointments ra
WHERE NOT EXISTS (
    SELECT 1
    FROM appointments a
    WHERE a.clinic_id = ra.clinic_id
      AND a.c_id = ra.c_id
      AND a.t_id = ra.t_id
      AND a.scheduled_at = ra.scheduled_at
);
