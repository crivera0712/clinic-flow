CREATE OR REPLACE FUNCTION findAllAppointmentsByDate(apptDate DATE)
RETURNS TABLE (
    scheduled_at    TIMESTAMP,
    apt_id          BIGINT,
    t_id            BIGINT,
    c_id            BIGINT,
    first_name      VARCHAR,
    last_name       VARCHAR,
    p_id            BIGINT,
    therapist_id    BIGINT,
    therapist_name  VARCHAR,
    therapist_type  VARCHAR,
    display_name    VARCHAR
)
LANGUAGE SQL
AS $$
    SELECT a.scheduled_at, a.apt_id, a.t_id, a.c_id, p.first_name, p.last_name, p.p_id, t.t_id, t.name, t.therapist_type, br.display_name
    FROM appointments AS a
        JOIN therapists AS t ON t.t_id = a.t_id
        JOIN cases AS c ON c.c_id = a.c_id
        JOIN patients AS p ON p.p_id = c.p_id
        JOIN body_regions AS br ON br.br_id = c.br_id
    WHERE a.scheduled_at >= apptDate
    AND a.scheduled_at < apptDate + INTERVAL '1 day'
    ORDER BY a.scheduled_at;
$$;
