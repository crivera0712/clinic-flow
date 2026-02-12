DROP PROCEDURE IF EXISTS findAllAppointmentsByDate;

DELIMITER $$

CREATE PROCEDURE findAllAppointmentsByDate(
    apptDate DATE
)

BEGIN
SELECT
    a.scheduled_at AS scheduledAt,
    a.apt_id AS aptId,
    a.t_id AS tId,
    a.c_id AS cId,
    p.first_name AS firstName,
    p.last_name AS lastName,
    p.p_id AS pId,
    t.t_id AS therapistId,
    t.therapist_name AS therapistName,
    t.therapist_type AS therapistType,
    br.display_name AS bodyRegionDisplayName
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





