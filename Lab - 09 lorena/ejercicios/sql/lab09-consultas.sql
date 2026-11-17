-- Laboratorio 09 - consultas para MySQL Workbench
-- 1) Cambia <tu_esquema> por el esquema que usa DB_URL.
USE <tu_esquema>;

SELECT DATABASE();
SHOW TABLES;
DESCRIBE pets;
SHOW CREATE TABLE pets;

-- Contenido y conteo
SELECT id, name, species, age, description FROM pets ORDER BY id;
SELECT COUNT(*) AS total_pets FROM pets;

-- Especies, edades y búsquedas
SELECT DISTINCT species FROM pets ORDER BY species;
SELECT id, name, age FROM pets WHERE age >= 3 ORDER BY age DESC, name ASC;
SELECT id, name, species FROM pets WHERE name LIKE '%a%' ORDER BY name;

-- Verificar una mascota concreta (sustituye 7 por el id real)
SELECT id, name, species, age FROM pets WHERE id = 7;

-- NO ejecutes UPDATE/DELETE sin WHERE. Ejemplos solo de referencia:
-- UPDATE pets SET age = 5 WHERE id = 7;
-- DELETE FROM pets WHERE id = 7;
