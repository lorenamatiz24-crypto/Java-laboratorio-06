## Laboratorio 7 — PetMatch Mini (Spring Boot + Thymeleaf)

**Estado:** completado (prácticas 7.1 a 7.6). Carpeta: [`laboratorio-07`](laboratorio-07/).

- Proyecto web `petmatch-lab` (Java 21, Maven, Spring Boot, Spring MVC, Thymeleaf) con arquitectura en capas: `controller`, `service`, `repository`, `model`.
- Rutas: `/`, `/about`, `/pets`, `/pets/{id}` (404 si no existe).
- Fragments reutilizables, CSS en `static/css`, reto integrador (5 mascotas, atributo `size`) y pruebas `PetServiceTests`.
- Ejercicios, depuración y diagnósticos en [`laboratorio-07/ejercicios`](laboratorio-07/ejercicios/).

**Ejecutar:**
```bash
cd laboratorio-07/petmatch-lab
./mvnw spring-boot:run     # Windows: .\mvnw.cmd spring-boot:run
```
Luego abrir `http://localhost:8080/`. Pruebas: `./mvnw test`.
