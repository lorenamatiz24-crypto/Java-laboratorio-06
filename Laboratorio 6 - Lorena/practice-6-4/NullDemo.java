public class NullDemo {

    static final class Pet {
        private final String name;
        Pet(String name) { this.name = name; }
        String getName() { return name; }
    }

    public static void main(String[] args) {
        Pet missing = null;

        // Parte 1: el error. Compila, pero falla al ejecutar.
        try {
            System.out.println(missing.getName());
        } catch (NullPointerException e) {
            System.out.println("NullPointerException capturada: " + e.getMessage());
        }
        // Para ver el stack trace real, quita el try/catch y ejecuta.

        // Parte 2: corrección
        if (missing != null) {
            System.out.println(missing.getName());
        } else {
            System.out.println("missing no refiere a ningún objeto");
        }

        missing = new Pet("Luna");
        System.out.println(missing.getName());
    }
}
