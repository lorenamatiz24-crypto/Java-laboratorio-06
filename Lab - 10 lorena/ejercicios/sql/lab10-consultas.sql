-- Laboratorio 10 - consultas para MySQL Workbench
-- Cambia <tu_esquema> por el esquema que usa DB_URL.
USE <tu_esquema>;

SHOW TABLES;
DESCRIBE support_requests;
SHOW CREATE TABLE support_requests;

-- Solicitudes con el nombre de su mascota (JOIN)
SELECT r.id, r.pet_id, p.name AS pet_name, r.type, r.status
FROM support_requests AS r
INNER JOIN pets AS p ON p.id = r.pet_id
ORDER BY r.pet_id, r.id DESC;

-- Solicitudes de una mascota (sustituye 1 por el id real)
SELECT id, pet_id, type, description, status
FROM support_requests
WHERE pet_id = 1
ORDER BY id DESC;

-- Debe haber como máximo una OPEN por mascota en el flujo normal
SELECT pet_id, COUNT(*) AS abiertas
FROM support_requests
WHERE status = 'OPEN'
GROUP BY pet_id
HAVING COUNT(*) > 1;

-- Mascotas con historial (no deberían poder eliminarse)
SELECT p.id, p.name, COUNT(r.id) AS solicitudes
FROM pets AS p
INNER JOIN support_requests AS r ON r.pet_id = p.id
GROUP BY p.id, p.name;
