/** Variantes A-D de MemoryDemo. Cada una trabaja con un objeto nuevo. */
public class MemoryDemoVariants {
    static final class Pet {
        String name;
        Pet(String name) { this.name = name; }
    }

    // Variante A: reasignar ANTES de modificar
    static void changeA(Pet local) {
        local = new Pet("Otra");
        local.name = "Sol";               // modifica el objeto nuevo (B)
        System.out.println("A Dentro: " + local.name);
    }

    // Variante B: sin reasignación
    static void changeB(Pet local) {
        local.name = "Sol";
        System.out.println("B Dentro: " + local.name);
    }

    // Variante C: retornar un objeto nuevo
    static Pet replace() {
        return new Pet("Otra");
    }

    // Ejercicio: reasignar parámetro a null
    static void clearLocalReference(Object value) {
        value = null;
    }

    public static void main(String[] args) {
        Pet a = new Pet("Luna");
        changeA(a);
        System.out.println("A Fuera: " + a.name + "  (AssertionError si exigieras 'Sol')");

        Pet b = new Pet("Luna");
        changeB(b);
        System.out.println("B Fuera: " + b.name);

        Pet c = new Pet("Luna");
        c = replace();                    // el llamador decide asignar
        System.out.println("C: " + c.name);

        Pet d = new Pet("Luna");
        Pet alias = d;                    // NO crea otro objeto
        changeB(d);
        System.out.println("D alias.name: " + alias.name + " | mismo objeto: " + (alias == d));

        Object obj = new Object();
        clearLocalReference(obj);
        System.out.println("¿obj es null tras el método? " + (obj == null)); // false
    }
}
