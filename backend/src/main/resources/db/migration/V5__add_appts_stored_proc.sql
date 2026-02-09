DELIMITER $$

CREATE PROCEDURE findAllAppointmentsByDate(
    apptDate DATE
)

BEGIN
    SELECT a.scheduled_at, a.apt_id, a.t_id, a.c_id, p.first_name, p.last_name, p.p_id, t.t_id, t.therapist_name, t.therapist_type, br.display_name
    FROM appointments AS a
        JOIN therapists AS t ON t.t_id = a.t_id
        JOIN cases AS c ON c.c_id = a.c_id
        JOIN patients AS p ON p.p_id = c.p_id
        JOIN body_regions AS br ON br.br_id = c.br_id
    WHERE a.scheduled_at >= apptDate
    AND a.scheduled_at < DATE_ADD(apptDate, INTERVAL 1 DAY)
    ORDER BY a.scheduled_at;
END $$

DELIMITER ;

