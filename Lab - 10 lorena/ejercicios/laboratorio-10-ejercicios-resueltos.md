# Laboratorio 10 — Relaciones JPA, reglas de negocio, transacciones y estados: ejercicios resueltos

> Proyecto: `petmatch-lab` (paquete `com.example.petmatch_lab`). Todo el código del laboratorio está implementado en el proyecto entregado.
> **Adaptación a tu entorno:** donde el texto dice MariaDB, tu proyecto usa **MySQL** consultado desde **MySQL Workbench**. Los scripts de verificación están en `ejercicios/sql/lab10-consultas.sql`. Los apartados **[completar]** requieren ejecutar la aplicación en tu equipo.

## Dónde está cada cosa en el proyecto
| Pieza del laboratorio | Archivo |
|---|---|
| Enums | `model/SupportType.java`, `model/RequestStatus.java` |
| Entidad y transiciones | `model/SupportRequest.java` |
| Excepciones | `exception/BusinessRuleException.java`, `exception/ResourceNotFoundException.java` |
| Repositorio | `repository/SupportRequestRepository.java` |
| Formulario | `dto/SupportRequestForm.java` |
| Servicio | `service/SupportRequestService.java`; `PetService.delete(...)` protegido |
| Controladores | `controller/SupportRequestController.java`, `controller/PageErrorHandler.java` |
| Vistas | `templates/requests/list.html`, `form.html`, `templates/errors/status.html`; enlace en `pets/detail.html` |
| Estilos | final de `static/css/application.css` |
| Pruebas | `model/SupportRequestTests`, `repository/SupportRequestRepositoryTests`, `service/SupportRequestServiceTests`, `service/SupportRequestTransactionIntegrationTests`, `controller/SupportRequestControllerIntegrationTests`, y `PetServiceTests` actualizada |

Dependencias: ya existían en tu `pom.xml` (JPA, validación, `mysql-connector-j`, pruebas JPA y H2). No hizo falta añadir ninguna.

---

## Diagnóstico inicial
1. **¿Por qué el id es distinto del nombre?** El nombre puede repetirse y cambiar; el id identifica una sola fila y se conserva en ediciones normales.
2. **¿Cómo evitar una solicitud para una mascota inexistente?** Con la clave foránea `pet_id` (la base rechaza referencias inexistentes) y, antes, con el servicio, que recupera la `Pet` con `PetRepository.findById` y lanza `ResourceNotFoundException` (404) si no existe.
3. **¿Si cambio el nombre de la mascota se pierde la relación?** No: la relación es por `pet_id`, no por el nombre.
4. **Datos válidos vs operación permitida.** Un formulario puede estar bien formado (`@NotBlank`, `@Size`) y aun así la operación puede estar prohibida por el estado o por una regla (por ejemplo, una segunda solicitud abierta).
5. **Si falla la segunda modificación:** esperaría que la primera también se deshaga (rollback). Es la atomicidad de una transacción.
6. **Eliminar una mascota referenciada:** la clave foránea impedirá el borrado (o una cascada destruiría el historial). Hace falta una política explícita.

---

## Práctica 10.1 — Modelar relaciones y reglas antes de programar

### Ejercicio — Diseña antes de implementar
**pets**
| id | name | species |
|---|---|---|
| 1 | Luna | Dog |
| 2 | Milo | Cat |
| 3 | Kira | Dog |

**support_requests**
| id | pet_id | type | status |
|---|---|---|---|
| 10 | 1 | WALK | COMPLETED |
| 11 | 1 | FEEDING | OPEN |
| 12 | 2 | COMPANY | CANCELLED |
| 13 | 2 | WALK | OPEN |
| 14 | 3 | COMPANY | COMPLETED |

(Las solicitudes 10 y 11 pertenecen a la misma mascota. Cada mascota tiene como máximo una solicitud `OPEN`.)

1. **Claves primarias:** `pets.id` y `support_requests.id`.
2. **Clave foránea:** `support_requests.pet_id` (apunta a `pets.id`).
3. **¿Por qué una solicitud no puede tener dos `pet_id`?** Porque es N:1: cada solicitud pertenece a una sola mascota; la columna guarda un único valor.
4. **¿`OPEN` puede pasar a `COMPLETED` con el formulario de edición de mascota?** No. Ese formulario edita datos de `Pet`; el estado solo cambia con `complete()`/`cancel()` en `SupportRequest`.
5. **Eliminar una mascota con solicitudes históricas:** se rechaza con 409 y el mensaje «No se puede eliminar una mascota que tiene solicitudes.» para conservar el historial.

### Depuración — error conceptual
1. **Nombre como clave foránea:** dos mascotas con el mismo nombre serían indistinguibles y no habría restricción real de integridad.
2. **Duplicar los datos de `Pet` en cada solicitud:** nombre, especie, edad… quedarían desactualizados si cambia la mascota.
3. **Borrar solicitudes al borrar la mascota:** no respeta la regla de conservar el historial.

### Comprobación
`id` identifica la fila propia; `pet_id` es la referencia a otra fila. El lado **1** es `Pet`; el lado **N** es `SupportRequest`. Integridad: «toda solicitud tiene una mascota existente» (FK). Negocio: «una sola abierta», «los estados finales no cambian».

---

## Práctica 10.2 — Implementar la relación con entidades JPA

### Ejercicio — Sigue una asociación (solicitud 15, `pet_id` = 3)
1. `support_requests.pet_id` vale **3**.
2. `request.getPet()` devuelve un objeto **`Pet`**.
3. Eliminar la mascota 3 sin resolver la relación: MySQL rechaza el borrado por la **violación de la clave foránea** (o, sin FK, quedaría una solicitud huérfana). Por eso `PetService.delete` lo bloquea antes.
4. Cambiar el nombre de la mascota 3 **no afecta** la relación.
5. `mappedBy = "pet"` (en un `@OneToMany`) indica que el lado propietario es el campo `pet` de `SupportRequest`; **no** crea una segunda clave foránea.

### Depuración — errores de mapeo
- **Not a managed type:** `@Entity` en `SupportRequest`, paquete bajo el raíz, imports de `jakarta.persistence`.
- **Unknown column 'pet_id':** comparar el esquema real (`DESCRIBE support_requests;`) con `@JoinColumn(name = "pet_id")`.
- **Error de clave foránea:** la `Pet` debe existir y el tipo del id debe coincidir (`Long`).
- **Error de enum:** los textos guardados deben coincidir con los nombres de las constantes (por eso `EnumType.STRING`).

### Verificación del esquema en Workbench
```sql
DESCRIBE support_requests;
SHOW CREATE TABLE support_requests;
```
Debes ver: `id` autoincremental, `pet_id` no nulo, `type`, `description`, `status`, y una restricción `FOREIGN KEY` hacia `pets(id)`. Resultado de tu base: **[completar]**.
> En MySQL, Hibernate puede representar los enums `STRING` como columnas `ENUM(...)`; es normal.

### Comprobación
La FK se ve en SQL (`pet_id` + `FOREIGN KEY`) y en Java (`@ManyToOne` + `@JoinColumn`). `LAZY` significa que `Pet` no se carga hasta que se necesita (con `open-in-view: false` no se debe acceder a ella fuera de una transacción). Los valores del enum (`OPEN`) son estables; la etiqueta («Abierta») es solo para la interfaz.

---

## Práctica 10.3 — Consultar relaciones mediante repositorios

### Ejercicio — Escoge el método correcto (mascota 7)
1. Todas sus solicitudes → `findByPet_IdOrderByIdDesc(7L)`.
2. ¿Tiene alguna solicitud histórica? → `existsByPet_Id(7L)`.
3. Solicitud 32 solo si es de la mascota 7 → `findByIdAndPet_Id(32L, 7L)`.
4. ¿Ya tiene una `OPEN`? → `existsByPet_IdAndStatus(7L, RequestStatus.OPEN)`.

**¿Por qué el controlador no decide si se puede eliminar una mascota?** Porque eso es una regla de negocio; vive en `PetService.delete`, que consulta el repositorio y decide. El controlador solo traduce HTTP.

### Depuración
`findByAnimalId(...)` fallaría al arrancar porque `SupportRequest` no tiene propiedad `animal`; el campo es `pet`, y para llegar al id se escribe `Pet_Id` (el guion bajo delimita el recorrido `pet.id`).

### Comprobación — los métodos en español
- `findByPet_IdOrderByIdDesc`: solicitudes cuya mascota tiene ese id, de la más reciente a la más antigua.
- `findByIdAndPet_Id`: la solicitud con ese id **y** perteneciente a esa mascota.
- `existsByPet_Id`: ¿existe alguna solicitud de esa mascota?
- `existsByPet_IdAndStatus`: ¿existe alguna con ese estado?
- (Ampliación) `findByPet_IdAndStatusOrderByIdDesc`: solicitudes de la mascota en un estado, más recientes primero.

---

## Práctica 10.4 — Servicios, formularios y reglas de negocio

### Ejercicio — Regla frente a validación
| Situación | Quién la detecta | Respuesta |
|---|---|---|
| `description` solo espacios | `@NotBlank` en `SupportRequestForm` (y `validateForm` en el servicio) | Vuelve al formulario con error (200) |
| Mascota de la URL inexistente | `SupportRequestService.findPet` → `ResourceNotFoundException` | 404 |
| Completar una solicitud cancelada | `SupportRequest.complete()` (`ensureOpen`) → `BusinessRuleException` | 409 |
| Segunda solicitud abierta | Servicio (`existsByPet_IdAndStatus`) → `BusinessRuleException`; el controlador la convierte en error del formulario | Formulario con mensaje global (200) |
| Consultar la solicitud de otra mascota cambiando el id | `findByIdAndPet_Id` vacío → `ResourceNotFoundException` | 404 |

### Depuración — el formulario funciona pero no registra
- Error de conversión en `type`: comparar los `value` de las `<option>` con las constantes del enum.
- Formulario sin opciones: falta `supportTypes` (lo añade `prepareForm`).
- Lista siempre vacía: revisar el `petId` enviado y las filas con `SELECT ... WHERE pet_id = ...`.
- Los botones desaparecen: revisar `isOpen()` y el estado guardado.
- 500 ante una regla del dominio: falta la traducción de excepciones (`PageErrorHandler`).

### Recorrido de la creación
`POST /pets/{petId}/requests` → `SupportRequestController.create` (`@Valid` sobre el DTO) → `SupportRequestService.create`: valida, busca la `Pet`, comprueba que no haya otra `OPEN`, construye `new SupportRequest(...)` (estado `OPEN` lo fija el constructor) y `save`. La fila queda con `pet_id` correcto.

### Prueba desde el navegador (registra el resultado)
| Paso | Esperado | Resultado |
|---|---|---|
| Listado vacío | «Sin solicitudes» | **[completar]** |
| Enviar vacío | Errores de validación | **[completar]** |
| Registrar válida | Aparece «Abierta» | **[completar]** |
| Segunda abierta | Error de regla sin insertar fila | **[completar]** |
| Completar/cancelar | Cambia el estado y desaparecen los botones | **[completar]** |
| Nueva tras cerrar la anterior | Permitida | **[completar]** |

### Comprobación
Recorrido: formulario → DTO validado → servicio → entidad → repositorio → fila SQL. `OPEN` se asigna en el **constructor de `SupportRequest`**. `@Valid` no sustituye las reglas de negocio porque solo ve el formulario, no la base ni el estado de otras filas.

---

## Práctica 10.5 — Transacciones

### Ejercicio — Dos cambios, una operación
| # | Caso | Estado final de la original | ¿Solicitud nueva? |
|---|---|---|---|
| 1 | Reemplazar una `OPEN` con datos válidos | `CANCELLED` | **Sí**, `OPEN` |
| 2 | Reemplazar una `COMPLETED` | Sigue `COMPLETED` (`cancel()` lanza `BusinessRuleException`, 409) | No |
| 3 | Texto nuevo vacío | Sigue `OPEN` (se rechaza antes de tocar nada) | No |
| 4 | La solicitud es de otra mascota | Sin cambios (`ResourceNotFoundException`, 404) | No |
| 5 | Fallo de persistencia tras cancelar | Vuelve a `OPEN` (rollback) | No |

### Depuración
- Dos `OPEN`: revisar qué comprobaciones existen, si corrieron en transacción y si hubo concurrencia.
- `cancel()` no cambia la base: la entidad debe obtenerse dentro de un método `@Transactional`.
- Cambios parciales tras una excepción: límite `@Transactional` real y que la excepción no esté capturada y silenciada.
- Reemplazo que crea en vez de reemplazar: comparar `formAction` con `@PostMapping("/{requestId}/replace")`.

### Conceptos
- **¿Por qué `replace` usa `@Transactional`?** Porque son dos cambios (`UPDATE` de la original e `INSERT` de la nueva) que deben confirmarse o deshacerse juntos.
- **¿Por qué no hay `save(previous)`?** La entidad está administrada dentro de la transacción; Hibernate detecta el cambio (*dirty checking*) y lo sincroniza al confirmar. La nueva sí necesita `save(...)`.
- **flush vs commit:** `flush` envía el SQL pendiente a la base; `commit` confirma definitivamente la transacción.
- **Rollback:** por defecto Spring revierte ante `RuntimeException`/`Error`; `BusinessRuleException` es `RuntimeException`.
- **Límites:** una transacción no garantiza por sí sola unicidad ante concurrencia (dos peticiones simultáneas podrían ver «sin abierta» y crear dos). Para eso harían falta restricciones y bloqueos adicionales.

### Verificación SQL
```sql
SELECT id, pet_id, type, description, status
FROM support_requests
WHERE pet_id = <id_de_la_mascota>
ORDER BY id DESC;
```
Esperado tras reemplazar: al menos dos filas, la original `CANCELLED` y la nueva `OPEN`. Resultado: **[completar]**.

### Comprobación
`replace` es un caso de uso transaccional (cancelar + insertar). El servicio concentra el cambio de estado y la inserción para que ninguna otra capa los ejecute a medias.

---

## Práctica 10.6 — Excepciones, pruebas y reto integrador

### Casos de depuración A a F
- **A (excepción de clave foránea):** el id llegó del navegador sin recuperarse la `Pet`; el servicio debe obtenerla con `PetRepository` antes de crear la solicitud.
- **B (solicitud equivocada):** se buscó solo por id; debe usarse `findByIdAndPet_Id`.
- **C (`request.pet.name` falla):** la asociación es `LAZY` y `open-in-view` está en `false`; la plantilla usa el `pet` cargado por el controlador, no `request.pet`.
- **D (500 al completar una cancelada):** comprobar que sea `BusinessRuleException`, que exista `PageErrorHandler` y que no haya un `catch (Exception)` general.
- **E (borra una mascota con historial):** `PetService` debe recibir `SupportRequestRepository` y consultar `existsByPet_Id`; no añadir cascadas.
- **F (test de servicio no compila):** el constructor de `PetService` ahora tiene dos dependencias; la prueba añade `@Mock SupportRequestRepository` y lo pasa en `setUp()`.

### Códigos HTTP del laboratorio
| Código | Cuándo |
|---|---|
| 200 | Página o formulario con errores de validación |
| 302 | Redirección tras operación correcta |
| 404 | No existe la mascota o la solicitud dentro de esa mascota |
| 409 | Existe, pero una regla de negocio prohíbe la operación |
| 500 | Fallo inesperado que no se debe ocultar |

### Reto integrador — registro de evidencia
| # | Paso | Esperado | Resultado |
|---|---|---|---|
| 1 | Registrar solicitud de paseo | Fila con `pet_id` correcto | **[completar]** |
| 2 | Verificar `pet_id` en Workbench | Coincide con el id de la mascota | **[completar]** |
| 3 | Intentar una segunda abierta | Error visible, sin nueva fila | **[completar]** |
| 4 | Reemplazar por otro tipo | Original `CANCELLED`, nueva `OPEN` | **[completar]** |
| 5 | Verificar en SQL | Dos filas con los estados anteriores | **[completar]** |
| 6 | Completar la nueva y volver a completarla | Segunda vez: 409 | **[completar]** |
| 7 | Eliminar la mascota | Bloqueado (409), mascota conservada | **[completar]** |
| 8 | Reiniciar la aplicación | El historial sigue ahí | **[completar]** |
| 9 | `./mvnw test` | Todas las pruebas pasan | **[completar]** |

**Qué comprueba cada grupo de pruebas:**
- `SupportRequestTests`: transiciones de estado de la entidad, sin Spring ni base.
- `SupportRequestServiceTests` y `PetServiceTests`: decisiones del servicio con repositorios simulados (Mockito).
- `SupportRequestRepositoryTests`: asociación, consultas derivadas y `existsBy...` con H2.
- `SupportRequestTransactionIntegrationTests`: reemplazo consistente y rollback real.
- `SupportRequestControllerIntegrationTests`: 404, 409, errores de formulario y redirecciones con MockMvc.

### Ampliación opcional (implementada)
`SupportRequestRepository.findByPet_IdAndStatusOrderByIdDesc` y `SupportRequestService.findAllForPetByStatus(petId, status)` (valida que exista la mascota y que el estado no sea nulo). Probadas en `SupportRequestRepositoryTests` y `SupportRequestServiceTests`. Una consulta por estado pertenece al repositorio; decidir si una transición es válida sigue siendo del dominio y del servicio.

### Comprobación final
| Comprobación | Resultado esperado | Cubierto por |
|---|---|---|
| Crear para mascota existente | Fila con `pet_id` y `OPEN` | `shouldPersistRequestWithExistingPet`, `shouldCreateOpenRequestForExistingPet` |
| Crear sin descripción | Formulario con errores, sin guardar | `shouldRejectInvalidRequestForm` |
| Crear para mascota inexistente | 404 | `shouldReturnNotFoundForUnknownPet`, `shouldRejectMissingPetBeforeSaving` |
| Segunda abierta | Error, sin nueva fila | `shouldRejectSecondOpenRequest`, `shouldShowFormErrorForSecondOpenRequest` |
| Completar / cancelar abierta | `COMPLETED` / `CANCELLED` | `SupportRequestTests`, `shouldCancelOpenRequest` |
| Completar una cancelada | 409, estado sin cambios | `shouldReturnConflictForInvalidStateTransition` |
| Solicitud de otra mascota | 404 | `shouldReturnNotFoundForRequestOfAnotherPet`, `shouldNotFindRequestUnderAnotherPet` |
| Reemplazar abierta | Original `CANCELLED`, nueva `OPEN` | `shouldReplaceRequestWithTwoConsistentChanges`, `shouldReplaceOpenRequestCancellingTheOriginal` |
| Fallo antes de confirmar | Cambios revertidos | `shouldRollbackWhenFailureOccursInsideTransaction` |
| Eliminar mascota sin solicitudes | Se elimina | `shouldDeleteExistingPet` |
| Eliminar mascota con solicitudes | 409 y mascota conservada | `shouldRejectDeletingPetWithRequests`, `shouldRejectPetDeletionWhenRequestsExist` |
| Reiniciar | Filas siguen en MySQL | Manual en Workbench |

---

## Diagnóstico final (15 preguntas)
1. **¿Por qué `pet_id` vive en `support_requests`?** Porque es el lado N de la relación: cada solicitud apunta a una mascota. Poner una lista de solicitudes en `pets` no cabe en una columna.
2. **`@ManyToOne` y `@JoinColumn`:** `@ManyToOne` declara que muchas solicitudes pertenecen a una mascota; `@JoinColumn(name = "pet_id")` indica la columna FK en la tabla.
3. **`@OneToMany(mappedBy = "pet")`:** permite recorrer las solicitudes desde `Pet`. No fue imprescindible porque el repositorio consulta por `pet.id`; evita colecciones perezosas en plantillas.
4. **¿Por qué el formulario no tiene `status`?** Para que el navegador no pueda fijar el estado; lo controla el dominio (`OPEN` al crear, transiciones por métodos).
5. **`@NotBlank` vs «solo una abierta»:** la primera valida la forma del dato; la segunda es una regla que requiere consultar otras filas.
6. **Ventaja de `findByIdAndPet_Id`:** impide actuar sobre una solicitud que pertenece a otra mascota.
7. **¿Por qué no se elimina una mascota con solicitudes?** Para conservar el historial y respetar la integridad referencial.
8. **Llamar dos veces a `complete()`:** la segunda lanza `BusinessRuleException` (409) y el estado no cambia.
9. **¿Por qué `replace` es `@Transactional`?** Son dos cambios que deben ser atómicos.
10. **Si la segunda parte falla:** rollback; la primera modificación se deshace.
11. **`readOnly = true` no es un sistema de permisos:** solo indica la intención de lectura (optimizaciones); no impide por sí solo escrituras ni sustituye permisos en la base.
12. **404 vs 409:** 404 = el recurso no existe en ese contexto; 409 = existe pero la regla prohíbe la operación.
13. **Unitaria vs repositorio:** la unitaria demuestra reglas con colaboradores simulados, pero no el mapeo ni el SQL; la de repositorio demuestra mapeo y consultas con base real de prueba, pero no la lógica del servicio.
14. **¿Por qué H2 no basta?** No es MySQL: tipos, claves foráneas, enums y transacciones pueden comportarse distinto; hay que verificar también en MySQL.
15. **Riesgo de concurrencia:** dos peticiones simultáneas pueden ver «sin solicitud abierta» y crear dos. La comprobación en el servicio no es una garantía global; haría falta una restricción en base o un bloqueo.

## Errores frecuentes
- *Not a managed type*: falta `@Entity` o paquete fuera del escaneo.
- *No identifier specified for entity*: falta `@Id`.
- *Table doesn't exist*: esquema equivocado o `ddl-auto` mal configurado; no borres la base para «arreglarlo».
- *LazyInitializationException*: se accedió a `request.pet` fuera de una transacción.
- *Pruebas con H2 pasan pero MySQL falla*: revisar usuario, permisos, puerto y esquema reales.
