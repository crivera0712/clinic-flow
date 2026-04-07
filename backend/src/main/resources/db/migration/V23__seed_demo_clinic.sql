INSERT INTO clinics (name, slug, is_demo, created_at)
SELECT 'Clinic Flow Demo', 'demo', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM clinics
    WHERE slug = 'demo'
);

INSERT INTO users (username, password_hash, enabled, created_at, role, clinic_id)
SELECT 'demo_admin', '$2a$10$qB3Ki0SZtwytpKHBVwU9DO/BvxlTjlLDckxPjxa8NffOOmNB9rK.6', TRUE, CURRENT_TIMESTAMP, 'ADMIN', c.clinic_id
FROM clinics c
WHERE c.slug = 'demo'
  AND NOT EXISTS (
    SELECT 1
    FROM users u
    WHERE u.username = 'demo_admin'
      AND u.clinic_id = c.clinic_id
);

INSERT INTO body_regions (code, display_name, is_active)
SELECT 'SHOULDER', 'Shoulder', TRUE
WHERE NOT EXISTS (SELECT 1 FROM body_regions WHERE code = 'SHOULDER');

INSERT INTO body_regions (code, display_name, is_active)
SELECT 'KNEE', 'Knee', TRUE
WHERE NOT EXISTS (SELECT 1 FROM body_regions WHERE code = 'KNEE');

INSERT INTO patients (first_name, last_name, display_name, clinic_id)
SELECT 'Jordan', 'Reed', 'Reed, J', c.clinic_id
FROM clinics c
WHERE c.slug = 'demo'
  AND NOT EXISTS (
    SELECT 1
    FROM patients p
    WHERE p.clinic_id = c.clinic_id
      AND p.display_name = 'Reed, J'
);

INSERT INTO patients (first_name, last_name, display_name, clinic_id)
SELECT 'Avery', 'Cole', 'Cole, A', c.clinic_id
FROM clinics c
WHERE c.slug = 'demo'
  AND NOT EXISTS (
    SELECT 1
    FROM patients p
    WHERE p.clinic_id = c.clinic_id
      AND p.display_name = 'Cole, A'
);

INSERT INTO therapists (therapist_name, therapist_type, clinic_id)
SELECT 'Taylor Brooks', 'PHYSICAL_THERAPIST', c.clinic_id
FROM clinics c
WHERE c.slug = 'demo'
  AND NOT EXISTS (
    SELECT 1
    FROM therapists t
    WHERE t.clinic_id = c.clinic_id
      AND t.therapist_name = 'Taylor Brooks'
);

INSERT INTO therapists (therapist_name, therapist_type, clinic_id)
SELECT 'Morgan Shaw', 'OCCUPATIONAL_THERAPIST', c.clinic_id
FROM clinics c
WHERE c.slug = 'demo'
  AND NOT EXISTS (
    SELECT 1
    FROM therapists t
    WHERE t.clinic_id = c.clinic_id
      AND t.therapist_name = 'Morgan Shaw'
);

INSERT INTO cases (p_id, br_id, clinic_id, created_at)
SELECT p.p_id, br.br_id, c.clinic_id, CURRENT_TIMESTAMP - INTERVAL '10 days'
FROM clinics c
JOIN patients p ON p.clinic_id = c.clinic_id AND p.display_name = 'Reed, J'
JOIN body_regions br ON br.code = 'SHOULDER'
WHERE c.slug = 'demo'
  AND NOT EXISTS (
    SELECT 1
    FROM cases existing
    WHERE existing.clinic_id = c.clinic_id
      AND existing.p_id = p.p_id
      AND existing.br_id = br.br_id
);

INSERT INTO cases (p_id, br_id, clinic_id, created_at)
SELECT p.p_id, br.br_id, c.clinic_id, CURRENT_TIMESTAMP - INTERVAL '7 days'
FROM clinics c
JOIN patients p ON p.clinic_id = c.clinic_id AND p.display_name = 'Cole, A'
JOIN body_regions br ON br.code = 'KNEE'
WHERE c.slug = 'demo'
  AND NOT EXISTS (
    SELECT 1
    FROM cases existing
    WHERE existing.clinic_id = c.clinic_id
      AND existing.p_id = p.p_id
      AND existing.br_id = br.br_id
);

INSERT INTO appointments (scheduled_at, status, type, created_at, c_id, t_id, clinic_id)
SELECT CURRENT_DATE + TIME '09:00', 'SCHEDULED', 'EVALUATION', CURRENT_TIMESTAMP,
       cs.c_id, t.t_id, c.clinic_id
FROM clinics c
JOIN patients p ON p.clinic_id = c.clinic_id AND p.display_name = 'Reed, J'
JOIN body_regions br ON br.code = 'SHOULDER'
JOIN cases cs ON cs.clinic_id = c.clinic_id AND cs.p_id = p.p_id AND cs.br_id = br.br_id
JOIN therapists t ON t.clinic_id = c.clinic_id AND t.therapist_name = 'Taylor Brooks'
WHERE c.slug = 'demo'
  AND NOT EXISTS (
    SELECT 1
    FROM appointments a
    WHERE a.clinic_id = c.clinic_id
      AND a.c_id = cs.c_id
      AND a.t_id = t.t_id
      AND a.scheduled_at = CURRENT_DATE + TIME '09:00'
);

INSERT INTO appointments (scheduled_at, status, type, created_at, c_id, t_id, clinic_id)
SELECT CURRENT_DATE + TIME '11:00', 'CHECKED_IN', 'FOLLOW_UP', CURRENT_TIMESTAMP,
       cs.c_id, t.t_id, c.clinic_id
FROM clinics c
JOIN patients p ON p.clinic_id = c.clinic_id AND p.display_name = 'Cole, A'
JOIN body_regions br ON br.code = 'KNEE'
JOIN cases cs ON cs.clinic_id = c.clinic_id AND cs.p_id = p.p_id AND cs.br_id = br.br_id
JOIN therapists t ON t.clinic_id = c.clinic_id AND t.therapist_name = 'Morgan Shaw'
WHERE c.slug = 'demo'
  AND NOT EXISTS (
    SELECT 1
    FROM appointments a
    WHERE a.clinic_id = c.clinic_id
      AND a.c_id = cs.c_id
      AND a.t_id = t.t_id
      AND a.scheduled_at = CURRENT_DATE + TIME '11:00'
);
