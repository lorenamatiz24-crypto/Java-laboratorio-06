# Laboratorio 7 — De Java a la web: PetMatch Mini

Catálogo web de mascotas con **Java 21, Spring Boot, Spring MVC, Thymeleaf y Maven**.

## Estado
| Práctica | Tema | Estado |
|---|---|---|
| 7.1 | Paquetes, responsabilidades, MVC y capas | Completada ([ejercicios](ejercicios/7.1-paquetes-y-capas.md)) |
| 7.2 | Spring Boot, Maven, `pom.xml`, Wrapper | Completada ([ejercicios](ejercicios/7.2-spring-boot-y-maven.md)) |
| 7.3 | Spring MVC: rutas, controladores, vistas | Completada ([ejercicios](ejercicios/7.3-spring-mvc.md)) |
| 7.4 | Servicios, repositorio, inyección de dependencias | Completada ([ejercicios](ejercicios/7.4-componentes-e-inyeccion.md)) |
| 7.5 | Thymeleaf y rutas con parámetros | Completada ([ejercicios](ejercicios/7.5-thymeleaf.md)) |
| 7.6 | Fragments, CSS, reto integrador y pruebas | Completada ([ejercicios](ejercicios/7.6-fragments-estilos-y-reto.md)) |

Extras: [creación y verificación del proyecto](ejercicios/7.2-creacion-y-verificacion-del-proyecto.md), [diagnóstico final](ejercicios/diagnostico-final.md).

## Estructura
```
laboratorio-07/
├── README.md
├── ejercicios/
└── petmatch-lab/
    ├── pom.xml, mvnw, mvnw.cmd
    └── src/
        ├── main/java/com/example/petmatch_lab/
        │   ├── PetmatchLabApplication.java
        │   ├── controller/  (HomeController, PetController)
        │   ├── model/       (Pet)
        │   ├── repository/  (InMemoryPetRepository)
        │   └── service/     (PetService)
        ├── main/resources/
        │   ├── application.yaml
        │   ├── static/css/application.css
        │   └── templates/   (home, about, fragments/, pets/)
        └── test/java/com/example/petmatch_lab/ (PetmatchLabApplicationTests, service/PetServiceTests)
```

## Cómo ejecutar
Requisitos: JDK 21 (`java --version`). Desde `laboratorio-07/petmatch-lab`:
```bash
./mvnw test                 # Windows: .\mvnw.cmd test
./mvnw spring-boot:run      # Windows: .\mvnw.cmd spring-boot:run
```
Abrir `http://localhost:8080/`.

Rutas: `/` · `/about` · `/pets` · `/pets/{id}` (404 si no existe) · `/css/application.css`.
