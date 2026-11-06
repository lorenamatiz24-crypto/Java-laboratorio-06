# Diagnóstico final

## 1. Petición `/pets/2`
1. El navegador envía GET `/pets/2`; el `2` es el id de la mascota.
2. Lo atiende `PetController.detail` (`@RequestMapping("/pets")` + `@GetMapping("/{id}")`).
3. `@PathVariable("id")` recoge `2` y Spring lo convierte a `long`.
4. El controlador pide `petService.findById(2)`.
5. El servicio delega en el repositorio, que recorre la lista comparando `getId()`.
6. Si la encuentra: `Optional.of(pet)`; si no: `Optional.empty()` y `orElseThrow` lanza 404.
7. El controlador guarda la mascota en `Model` con la clave `pet`.
8. Thymeleaf procesa `pets/detail.html` y el navegador recibe HTML completo.

## 2. Versión A vs B
En A creas los objetos con `new`. En B el controlador solo declara qué necesita; Spring (IoC) crea repositorio y servicio y los entrega por el constructor (inyección de dependencias).

## 3. Errores de responsabilidad
Lista en `PetController` → al repositorio. HTML con `StringBuilder` → a la plantilla. Reglas del catálogo en HTML → al servicio.

## 4. Quinta mascota
Aparece sola al recargar `/pets`. No cambian controlador, servicio, plantillas, CSS ni fragments.

## 5. Términos
Paquete (`com.example.petmatch_lab.service`), clase (`Pet`), dependencia (starter de Thymeleaf), Maven (construye y descarga dependencias), starter (`spring-boot-starter-thymeleaf`), bean (`PetService`), inyección de dependencias (repositorio → constructor del servicio), controlador (`PetController`), ruta (`/pets/{id}`), modelo de dominio (`Pet`), `Model` (lleva `pets`/`pet` a la vista), vista/plantilla (`pets/list.html`), fragmento (`fragments/head.html`), recurso estático (`static/css/application.css`).
