# PetMatch Mini — laboratorios 8, 9 y 10

Proyecto `petmatch-lab` (Java 21, Spring Boot 4.1.2, Spring MVC, Thymeleaf, JPA/Hibernate, MySQL).

## Qué contiene
- `petmatch-lab/`: proyecto con CRUD de mascotas (lab 8), persistencia en MySQL (lab 9) y solicitudes de apoyo relacionadas con las mascotas (lab 10).
- `ejercicios/`: ejercicios, retos y preguntas resueltos (lab 9 y lab 10) y scripts SQL para MySQL Workbench.

## Cómo ejecutar
Define las variables de entorno antes de iniciar (no guardes la contraseña en archivos del repositorio):
```bash
export DB_URL='jdbc:mysql://127.0.0.1:3306/<tu_esquema>'
export DB_USERNAME='<tu_usuario>'
export DB_PASSWORD='<tu_contraseña>'
cd petmatch-lab
./mvnw spring-boot:run      # Windows PowerShell: .\mvnw.cmd spring-boot:run
./mvnw test                 # pruebas con H2 en memoria
```
Windows PowerShell: `$env:DB_URL = '...'`, `$env:DB_USERNAME = '...'`, `$env:DB_PASSWORD = '...'`.

Rutas del laboratorio 10: `/pets/{petId}/requests`, `/new`, `/{requestId}/complete`, `/{requestId}/cancel`, `/{requestId}/replace`.
