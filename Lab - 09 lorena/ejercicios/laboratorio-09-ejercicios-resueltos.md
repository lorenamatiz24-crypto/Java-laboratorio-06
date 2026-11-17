# Laboratorio 09 — Persistencia con JPA, Hibernate y Spring Data JPA: ejercicios resueltos

> Proyecto: `petmatch-lab` (paquete `com.example.petmatch_lab`, Spring Boot 4.1.2, Java 21).
> **Adaptación a tu entorno:** el texto del laboratorio habla de MariaDB; tu proyecto usa **MySQL** (`mysql-connector-j`) y consultas desde **MySQL Workbench**. Los conceptos y el SQL de este laboratorio son los mismos. Solo cambian: el driver (`com.mysql:mysql-connector-j`), la URL (`jdbc:mysql://127.0.0.1:3306/<tu_esquema>`), el cliente (Workbench en lugar del cliente `mariadb`) y el modo de H2 en pruebas (`MODE=MySQL`).
> Los apartados marcados **[completar]** son resultados que solo puedes obtener ejecutando el proyecto en tu equipo; las respuestas conceptuales están resueltas.

---

## Diagnóstico inicial

1. **¿Dónde guardaba los objetos `InMemoryPetRepository`?** En un `Map<Long, Pet>` (`LinkedHashMap`) dentro de la memoria del proceso Java.
2. **¿Qué pasa con esos objetos cuando el proceso termina?** Desaparecen: el `Map` solo existe mientras el proceso está vivo.
3. **¿Por qué un formulario HTML no debería conocer la contraseña de la base de datos?** Todo lo que llega al navegador (HTML, JavaScript) lo puede leer el usuario. Las credenciales solo las necesita el servidor: viven en variables de entorno (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) y solo las lee Spring Boot.
4. **¿Debería importar si los datos están en un `Map` o en la base de datos?** Para las reglas del servicio, no: depende del contrato del repositorio (`findAll`, `findById`, `save`, `delete`). Matiz: `PetService` ahora depende de `PetRepository`, que extiende `JpaRepository`, así que está acoplado a Spring Data pero no al motor (MySQL/H2/MariaDB).
5. **¿Objeto Java vs fila?** El objeto tiene clase, referencias y métodos; la fila son valores con tipo SQL en columnas, con restricciones (`NOT NULL`, clave primaria) que administra el motor. JPA convierte de uno a otro.
6. **¿Cómo demostrar que un dato sobrevive a un reinicio?** Crearlo desde la web, detener Spring Boot (con MySQL encendido), consultarlo en Workbench (`SELECT`), reiniciar y verlo de nuevo con el mismo `id`.

---

## Práctica 9.1 — De la memoria a una base de datos relacional

**Predice (apagar la aplicación y seguir viendo las mascotas en SQL):** demuestra que los datos viven en el servidor de base de datos, no en el proceso Java.

### Ejercicio — Traduce una consulta
- `SELECT name, age FROM pets WHERE age >= 3 ORDER BY age DESC;` → «muestra el nombre y la edad de las mascotas de 3 años o más, de la mayor a la menor».
- `SELECT COUNT(*) FROM pets;` → «cuántas filas tiene la tabla `pets`».

1. ¿La primera consulta modifica una fila? **No**, `SELECT` solo lee.
2. ¿Qué indica `COUNT(*)`? El **número de filas** de la tabla (o del resultado filtrado).
3. ¿Qué columna para buscar por identificador? **`id`** (clave primaria).
4. ¿Dos filas con el mismo `name` son un error? **No necesariamente**: el nombre puede repetirse; lo que distingue las filas es el `id`.

### Depuración — tipo de problema
| Síntoma | Primera comprobación |
|---|---|
| La tabla no existe | Que estés en el esquema correcto (`SELECT DATABASE();`) y que Hibernate la haya creado |
| Aparecen filas de otra aplicación | Qué esquema está seleccionado en Workbench (esquema por defecto) |
| Cero filas | Si la tabla está vacía o el `WHERE` excluye todo (`SELECT COUNT(*) FROM pets;`) |
| Un id parece duplicarse | Que `id` sea realmente clave primaria (`SHOW CREATE TABLE pets;`) |

### Comprobación
Memoria = vive en el proceso; persistencia = sobrevive a él. Una fila es un registro con columnas tipadas; la clave primaria (`id`) la identifica. CRUD ↔ `INSERT`, `SELECT`, `UPDATE`, `DELETE`.

---

## Práctica 9.2 — Preparar MySQL y conectar Spring Boot

### Ejercicio — Diagnóstico antes de iniciar
| # | Problema | Categoría |
|---|---|---|
| 1 | `mysql-connector-j` no puede descargarse | **Maven** (resolución de dependencias / red) |
| 2 | `DB_PASSWORD` no definida | **Configuración** (Spring no resuelve el marcador `${DB_PASSWORD}`; falla antes de conectar) |
| 3 | El servidor escucha en otro puerto | **Conectividad** (conexión rechazada) |
| 4 | Usuario sin permisos sobre la base | **Autenticación/permisos** (`Access denied`) |
| 5 | Error tipográfico en el nombre de la base de la URL | **Configuración** (MySQL responde `Unknown database`) |
| 6 | `username` mal indentado en YAML | **Configuración** (la propiedad no queda bajo `spring.datasource`) |

### Antes de seguir (comprobación en Workbench)
Conéctate con el mismo usuario que usa la aplicación y ejecuta `SELECT DATABASE();`. Resultado: **[completar]**.

### Comprobación
- **Servidor** = MySQL; **driver** = `mysql-connector-j` (implementa JDBC); **URL** = dónde y qué base; **cuenta** = usuario/permisos.
- La contraseña no está en `pom.xml` ni en `application.yaml`: ahí solo aparece `${DB_PASSWORD}`.
- `ddl-auto: update` es una comodidad local; no es una migración versionada. No se debe cambiar a `create`/`create-drop` sobre datos que quieras conservar.

> **Observación sobre tu proyecto:** `application.properties` (con `logging.level.org.springframework.web=DEBUG` y `server.error.include-message=always`) convive con `application.yaml`. No chocan, pero son ajustes de depuración: no los dejes así en un entorno real.

---

## Práctica 9.3 — Convertir `Pet` en una entidad JPA

### Ejercicio — Reconoce la responsabilidad
1. Nombre de la tabla → `@Table(name = "pets")`.
2. Propiedad que identifica una fila → el campo `id` con `@Id`.
3. Estrategia de generación → `@GeneratedValue(strategy = GenerationType.IDENTITY)` (`AUTO_INCREMENT` en MySQL).
4. `description` puede no tener valor → `@Column(length = 1000)` **sin** `nullable = false`.
5. Longitud del nombre → `@Column(nullable = false, length = 100)` en `name`.
6. Constructor al registrar → `public Pet(String name, String species, int age, String description)`.
7. Modificar datos editables → `updateDetails(...)`.

**¿Qué pasa con `${pet.name}` y `${pet.id}` si se eliminan los getters?** Thymeleaf resuelve `pet.name` mediante `getName()`. Sin el getter la expresión no encuentra la propiedad y la página falla al procesarse (error 500).

### Laboratorio de errores (efectos esperados)
- **Quitar `@Entity`:** al arrancar, JPA/Spring Data indica que `Pet` no es un tipo administrado (*Not a managed type*), porque `PetRepository` apunta a una clase que JPA no conoce.
- **`Long id` → `String id` con `IDENTITY`:** `IDENTITY` necesita un identificador numérico generado por la columna autoincremental; con `String` no hay correspondencia clara y Hibernate lo rechaza.
- **Quitar el constructor sin argumentos:** JPA necesita crear instancias al leer filas; sin constructor `public` o `protected` sin argumentos no puede instanciar la entidad (error al recuperar datos).

Resultados observados: **[completar]** (restaura `Pet.java` después de cada prueba).

### Comprobación
`Pet` es entidad (`@Entity`) mapeada a `pets`; su `id` comienza en `null` porque lo asigna la base al insertar; el constructor vacío `protected` existe para JPA; una columna SQL tiene tipo y restricciones propias, no es "solo un atributo".

---

## Práctica 9.4 — Spring Data JPA

### Ejercicio — Identifica el origen
1. Mostrar `pets/form` si hay errores de validación → **`PetController`**.
2. Ofrecer `findById(...)` → **`PetRepository`** (heredado de `JpaRepository`).
3. Generar el valor de `AUTO_INCREMENT` → **MySQL**.
4. Quitar espacios al inicio y final del nombre → **`PetService`**.
5. Convertir la ausencia de registro en HTTP 404 → **`PetController`**.
6. Ejecutar el almacenamiento solicitado por Hibernate → **MySQL**.

### Depuración
Si cambiaste a `PetRepository` pero el servicio sigue importando `InMemoryPetRepository`, el error es de **compilación** (referencia a una clase que ya no existe), no de base de datos. En tu proyecto no queda ninguna referencia a `InMemoryPetRepository`.

### Comprobación
Spring Data JPA genera la implementación de `PetRepository` en tiempo de ejecución; `findById`, `save` y `delete` se heredan de `JpaRepository<Pet, Long>`; controlador y plantillas no conocen el motor de almacenamiento.

---

## Práctica 9.5 — Persistir el CRUD y comprobar en MySQL

### Pruebas con SQL en Workbench
Los scripts están en `ejercicios/sql/lab09-consultas.sql`. Registra los resultados:

| Operación | Qué hacer | Comprobación SQL | Resultado |
|---|---|---|---|
| Crear | `/pets/new` → Kira, Dog, 4 | `SELECT * FROM pets ORDER BY id;` | **[completar]** |
| Editar | Cambiar edad a 5 | `SELECT id, name, age FROM pets WHERE id = <id>;` | **[completar]** |
| Eliminar | Crear y borrar una segunda | `SELECT * FROM pets;` y `/pets/<id>` → 404 | **[completar]** |
| Reiniciar | Ctrl+C y volver a iniciar | La mascota conservada sigue con el mismo id | **[completar]** |

### Ejercicio — El identificador no es un contador
1. ¿Se conservan las mascotas no eliminadas? **Sí.**
2. ¿El nuevo id es el siguiente sin saltos? **No necesariamente**: un id eliminado no se reutiliza y los intentos fallidos también pueden consumir valores.
3. ¿Por qué no usar el id más alto como total? Porque hay saltos; el total real es `COUNT(*)`.
4. Consulta correcta: `SELECT COUNT(*) AS total_pets FROM pets;`
5. ¿Dónde se decide qué mascota eliminar? En el **servicio y el repositorio** (a partir del id recibido), no en la vista.

### Reto — Consulta derivada por especie (implementado)
En tu proyecto ya existe `PetRepository.findBySpeciesIgnoreCaseOrderByNameAsc(String)` y `PetService.findBySpecies(String)` (rechaza especie vacía con `IllegalArgumentException`). Probado por `PetRepositoryTests.shouldFindPetsBySpeciesIgnoringCaseOrderedByName` y `PetServiceTests` (valor recortado y especie en blanco). La consulta se lee: *buscar por* `species` *ignorando mayúsculas, ordenar por* `name` *ascendente*.

### Depuración
| Síntoma | Revisar primero |
|---|---|
| No compila, falta `InMemoryPetRepository` | Imports y pruebas antiguas |
| No inicia por excepción JDBC | Servidor, URL, puerto, cuenta, contraseña, permisos |
| No existe `pets` | `@Entity`, paquete escaneado, `ddl-auto` |
| Errores de edad en el formulario | Validación (`@Min`) / conversión, no la base |
| Catálogo vacío al inicio | La tabla nueva no tiene filas |
| Datos desaparecen al reiniciar | Que se use el repositorio JPA y la misma base |
| Hay datos en SQL pero no en HTML | `findAll` → `Model` → Thymeleaf |
| Una edición no se guarda | Que se recupere la entidad y se llame a `save(...)` |
| Datos inesperados | `SELECT DATABASE();` y la URL del `DataSource` |

---

## Práctica 9.6 — Pruebas, depuración y reto integrador

### Qué prueba cada archivo (según tu proyecto)
| Archivo | Tipo | Qué demuestra |
|---|---|---|
| `PetServiceTests` | Unitaria con Mockito | Reglas del servicio con repositorio simulado |
| `PetRepositoryTests` | `@DataJpaTest` (H2) | Mapeo y operaciones JPA |
| `PetControllerIntegrationTests` | `@SpringBootTest` + MockMvc | Rutas, validación y 404 |
| Reinicio con MySQL | Manual | Persistencia real entre ejecuciones |

H2 no es MySQL: `MODE=MySQL` aproxima el dialecto, pero no replica todo. Por eso se mantienen las comprobaciones manuales con MySQL.

### Laboratorio de errores — A a G
- **A (variable ausente):** `Could not resolve placeholder 'DB_PASSWORD'`; falla antes de conectar. Contraseña incorrecta ≠ ausente: la incorrecta produce `Access denied` ya durante la conexión.
- **B (puerto equivocado):** error de conexión rechazada del driver JDBC; no tiene relación con Thymeleaf (la vista ni se ejecuta).
- **C (sin `@Id`):** falla el arranque (*No identifier specified for entity*): JPA necesita la clave primaria para distinguir entidades.
- **D (import viejo):** error de **compilación** de pruebas; ocurre aunque MySQL funcione.
- **E (dato inválido):** el formulario vuelve con errores y el servicio no llama al repositorio; en SQL `SELECT COUNT(*) FROM pets;` da el mismo valor antes y después.
- **F (`/pets/99999999`):** 404, porque `findById` devuelve `Optional.empty()`; inventar una entidad vacía sería fabricar datos.
- **G (base distinta):** dos esquemas con nombres parecidos funcionan los dos; si el cliente y `DB_URL` apuntan a esquemas distintos verás datos diferentes sin ningún error.

Resultados observados: **[completar]**.

### Reto integrador (11 pasos) — registro de evidencia
| # | Paso | Evidencia | Hecho |
|---|---|---|---|
| 1 | Registrar dos mascotas | ids reales: **[completar]** | |
| 2 | Anotar identificadores | | |
| 3 | Consultarlas por su detalle | | |
| 4 | Editar la edad de una | | |
| 5 | Confirmar con SQL que cambió solo esa fila | | |
| 6 | Eliminar la segunda | | |
| 7 | Comprobar 404 del id eliminado | | |
| 8 | Detener la aplicación (no MySQL) | | |
| 9 | Consultar la tabla en Workbench | | |
| 10 | Reiniciar y verificar datos e id | | |
| 11 | `./mvnw test`: distinguir H2 y mocks | salida: **[completar]** | |

### Ampliación — buscar por especie
Hecha (ver 9.5): `PetRepositoryTests` guarda `Dog`/`dog`/`Cat`, consulta `"DOG"`, comprueba que solo vuelven los perros y en orden por nombre.

### Diagnóstico final
**A — Registrar.** El navegador envía `POST /pets` con `application/x-www-form-urlencoded`. Spring MVC construye un `PetForm`. `@Valid` valida la entrada (`@NotBlank`, `@Size`, `@Min`, `@NotNull`). `PetService.create` vuelve a validar, aplica `strip()` y normaliza la descripción; ahí se construye `new Pet(...)`. El servicio llama a `petRepository.save(pet)`; Hibernate hace el `INSERT` y MySQL genera el `id`. El controlador redirige con `redirect:/pets/{id}` usando el id de la entidad devuelta, sin escribir SQL.

**B — Consultar `/pets/7`.** Lo recibe `PetController.detail`; `@PathVariable("id")` toma el 7. `Optional<Pet>` representa «hay o no hay fila». Los atributos que ve Thymeleaf vienen de la entidad cargada desde la fila. Si no existe, se lanza `ResponseStatusException` 404.

**C — Editar.** No se crea un objeto nuevo con un id inventado: se carga la entidad existente y se llama a `updateDetails(...)`, que no toca el id. El servicio llama a `save(...)` de forma explícita (no hay transacción que abarque lectura y modificación). Se verifica con `SELECT ... WHERE id = ...` en Workbench.

**D — Reiniciar.** Se pierden los objetos Java (el proceso termina). Permanece la información de la base. `findAll()` reconstruye la lista con `SELECT` ordenado por id. Los ids no empiezan en 1 porque el contador `AUTO_INCREMENT` lo mantiene MySQL.

### Comprobación — glosario con el ejemplo del proyecto
- **Persistencia:** conservar datos más allá del proceso (filas de `pets`).
- **Tabla / fila / columna:** `pets` / una mascota / `species`.
- **Clave primaria:** `pets.id`.
- **SQL:** lenguaje de consultas (`SELECT`, `INSERT`...).
- **JDBC / driver:** API estándar / `mysql-connector-j`.
- **DataSource:** fábrica de conexiones configurada con `spring.datasource.*`.
- **ORM:** correspondencia clase↔tabla.
- **JPA / Hibernate:** especificación (`@Entity`) / implementación que ejecuta el SQL.
- **Entidad:** `Pet`. **Repositorio:** `PetRepository`. **`JpaRepository`:** interfaz con CRUD heredado.
- **Identificador generado:** `id` con `IDENTITY`.
- **Contexto de persistencia:** el «seguimiento» de entidades de una unidad de trabajo; por eso el servicio llama a `save(...)` explícitamente.
