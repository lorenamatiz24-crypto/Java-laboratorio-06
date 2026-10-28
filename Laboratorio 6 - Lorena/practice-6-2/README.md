# Práctica 6.2 (desde esta carpeta)
    javac -d out ExecutionFlow.java
    java -cp out ExecutionFlow            -> Total: 12
    javap -classpath out -c -p ExecutionFlow

Ejercicio "¿por qué no cambió?":
  1. bonus = 8, guarda, y ejecuta: java -cp out ExecutionFlow   (sigue 12)
  2. javac -d out ExecutionFlow.java
  3. java -cp out ExecutionFlow                                  (ahora 15)

Laboratorio de errores:
  Error 1: cd error1-sintaxis && javac -d out ExecutionFlow.java   (falla al COMPILAR)
  Error 2: java -cp out MissingClass                               (falla al INICIAR)
  Error 3: java -cp out ExecutionFlow.class                        (falla al INICIAR)
  Error 4: cd error4-logica && javac -d out ExecutionFlow.java && java -cp out ExecutionFlow
           (compila y corre, pero imprime Total: 2 -> error de LÓGICA)
