# Laboratorio de Java 21: JVM, Stack, Heap y paso por valor

Prácticas 6.1 a 6.6. Cada carpeta contiene programas pequeños y autónomos para observar cómo compila y ejecuta Java, cómo se organiza la memoria y por qué Java pasa **todos** sus argumentos por valor.

El escenario usa mascotas para mantener el vínculo con el caso de estudio de PetMatch. No depende de Spring, JPA ni de ninguna base de datos.

## Estado del laboratorio

| Práctica | Tema | Archivos | Estado |
|---|---|---|---|
| 6.1 | JDK, JRE y JVM | `VersionInfo.java` | Completa |
| 6.2 | Compilación, bytecode y ciclo de ejecución | `ExecutionFlow.java` y variantes con error | Completa |
| 6.3 | Stack: métodos, parámetros y variables locales | `MethodStack.java`, `MethodStackBug.java`, `SafeRecursion.java` | Completa |
| 6.4 | Heap: objetos, arreglos, identidad y referencias | `ObjectReferences.java`, `Identity.java`, `NullDemo.java` | Completa |
| 6.5 | Paso de parámetros y caso PetMatch | `PrimitiveParameters.java`, `ArrayReferences.java`, `MemoryDemo.java`, `MemoryDemoVariants.java` | Completa |
| 6.6 | Reto integrador y diagnóstico | `PetProfile.java`, `MemoryChallenge.java`, `MemoryChallengeTest.java` | Completa |

Todos los programas se compilaron y ejecutaron con OpenJDK 21 y producen las salidas que se muestran más abajo.

## Estructura del repositorio

```
.
├── README.md
├── practice-6-1/
│   └── VersionInfo.java
├── practice-6-2/
│   ├── ExecutionFlow.java
│   ├── error1-sintaxis/ExecutionFlow.java
│   └── error4-logica/ExecutionFlow.java
├── practice-6-3/
│   ├── MethodStack.java
│   ├── MethodStackBug.java
│   └── SafeRecursion.java
├── practice-6-4/
│   ├── ObjectReferences.java
│   ├── Identity.java
│   └── NullDemo.java
├── practice-6-5/
│   ├── PrimitiveParameters.java
│   ├── ArrayReferences.java
│   ├── MemoryDemo.java
│   └── MemoryDemoVariants.java
└── memory-challenge/
    ├── PetProfile.java
    ├── MemoryChallenge.java
    └── MemoryChallengeTest.java
```

## Requisitos

- **JDK 21** (no basta un runtime sin compilador).
- Una terminal. Opcionalmente VS Code con el Extension Pack for Java para usar el depurador.

Comprueba tu entorno:

```bash
java --version
javac --version
javap -version
```

Las tres herramientas deben provenir de la misma instalación y mostrar la versión 21.

## Cómo ejecutar los programas

Entra en la carpeta de la práctica, compila y ejecuta. El nombre de la clase se escribe **sin** la extensión `.class`.

```bash
cd practice-6-3
javac MethodStack.java
java MethodStack
```

### Práctica 6.1

```bash
cd practice-6-1
javac VersionInfo.java
java VersionInfo
```

Muestra versión, proveedor y si el entorno incluye compilador (JDK).

### Práctica 6.2

Esta práctica separa la compilación de la ejecución usando una carpeta de salida.

```bash
cd practice-6-2
javac -d out ExecutionFlow.java
java -cp out ExecutionFlow
javap -classpath out -c -p ExecutionFlow
```

Salida esperada: `Total: 12`.

Para comprobar que la JVM ejecuta el `.class` y no el `.java`, cambia `bonus` a 8, ejecuta sin recompilar (sigue dando 12), recompila y vuelve a ejecutar (ahora da 15).

Laboratorio de errores:

| Caso | Comando | Etapa en que falla |
|---|---|---|
| Sintaxis | `cd error1-sintaxis && javac -d out ExecutionFlow.java` | Compilación |
| Clase inexistente | `java -cp out MissingClass` | Inicio |
| Extensión en el nombre | `java -cp out ExecutionFlow.class` | Inicio |
| Lógica | `cd error4-logica && javac -d out ExecutionFlow.java && java -cp out ExecutionFlow` | Ejecución (imprime `Total: 2`) |

### Práctica 6.3

```bash
cd practice-6-3
javac MethodStack.java MethodStackBug.java SafeRecursion.java
java MethodStack       # Final result: 11
java MethodStackBug    # Final result: 7 (error a depurar)
java SafeRecursion
```

Para seguir las llamadas con el depurador, abre `MethodStack.java` en VS Code, coloca un punto de interrupción en `return number * 2;` y revisa el panel **Call Stack**.

### Práctica 6.4

```bash
cd practice-6-4
javac ObjectReferences.java Identity.java NullDemo.java
java ObjectReferences
java Identity
java NullDemo
```

`ObjectReferences` imprime `First and alias: true` y `First and independent: false`, y cierra con el ejercicio de reasignar un alias (`alias: Max`). `NullDemo` captura un `NullPointerException`; quita el `try/catch` para ver el stack trace completo.

### Práctica 6.5

```bash
cd practice-6-5
javac PrimitiveParameters.java ArrayReferences.java MemoryDemo.java MemoryDemoVariants.java
java PrimitiveParameters   # Inside: 15 / Outside: 5
java ArrayReferences       # Inside: 7 / Outside: 99, 2
java MemoryDemo            # Dentro: Otra / Fuera: Sol
java MemoryDemoVariants
```

### Práctica 6.6

Las dos clases dependen de `PetProfile`, así que se compilan juntas:

```bash
cd memory-challenge
javac *.java
java MemoryChallenge
java MemoryChallengeTest
```

Salida esperada de `MemoryChallenge`:

```
Inside: Nova (1)
Outside: Luna (1)
Same reference: true
Replacement: Max (0)
```

`MemoryChallengeTest` ejecuta verificaciones con `AssertionError` y termina con `Todas las verificaciones pasaron.`

## Idea central

Java pasa todos los argumentos **por valor**. Con primitivos se copia el valor. Con objetos se copia el valor de la referencia: el método puede modificar el objeto compartido, pero reasignar su parámetro local no cambia la variable del llamador.

## Solución de problemas

| Síntoma | Causa probable |
|---|---|
| `javac` no se reconoce como comando | Tienes un runtime sin JDK, o el `bin` del JDK no está en el `PATH`. |
| `Could not find or load main class` | Nombre de clase mal escrito, `.class` en el nombre, o falta `-cp`. |
| `cannot find symbol ... PetProfile` | En `memory-challenge` hay que compilar todos los archivos con `javac *.java`. |
| El resultado no cambia tras editar el código | Falta recompilar: la JVM ejecuta el `.class`, no el `.java`. |
