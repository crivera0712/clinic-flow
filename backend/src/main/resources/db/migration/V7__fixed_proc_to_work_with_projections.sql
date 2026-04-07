DROP FUNCTION IF EXISTS findAllAppointmentsByDate(DATE);

CREATE OR REPLACE FUNCTION findAllAppointmentsByDate(apptDate DATE)
RETURNS TABLE (
    "scheduledAt"           TIMESTAMP,
    "aptId"                 BIGINT,
    "tId"                   BIGINT,
    "cId"                   BIGINT,
    "firstName"             VARCHAR,
    "lastName"              VARCHAR,
    "pId"                   BIGINT,
    "therapistId"           BIGINT,
    "therapistName"         VARCHAR,
    "therapistType"         VARCHAR,
    "bodyRegionDisplayName" VARCHAR
)
LANGUAGE SQL
AS $$
SELECT
    a.scheduled_at,
    a.apt_id,
    a.t_id,
    a.c_id,
    p.first_name,
    p.last_name,
    p.p_id,
    t.t_id,
    t.therapist_name,
    t.therapist_type,
    br.display_name
FROM appointments AS a
    JOIN therapists AS t ON t.t_id = a.t_id
    JOIN cases AS c ON c.c_id = a.c_id
    JOIN patients AS p ON p.p_id = c.p_id
    JOIN body_regions AS br ON br.br_id = c.br_id
WHERE a.scheduled_at >= apptDate
AND a.scheduled_at < apptDate + INTERVAL '1 day'
ORDER BY a.scheduled_at;
$$;
