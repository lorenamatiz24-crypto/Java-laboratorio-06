public class ObjectReferences {

    static final class Pet {
        private String name;

        Pet(String name) { this.name = name; }
        String getName() { return name; }
        void rename(String newName) { name = newName; }
    }

    public static void main(String[] args) {
        Pet first = new Pet("Luna");
        Pet alias = first;
        Pet independent = new Pet("Luna");

        System.out.println("First and alias: " + (first == alias));
        System.out.println("First and independent: " + (first == independent));

        alias.rename("Sol");

        System.out.println("First name: " + first.getName());
        System.out.println("Independent name: " + independent.getName());

        // ----- Ejercicio: reasignar un alias -----
        System.out.println("--- Ejercicio: reasignar un alias ---");
        Pet third = first;
        System.out.println("third: " + third.getName());      // Sol

        alias = new Pet("Nova");   // alias ahora apunta a un objeto nuevo (C)
        alias.rename("Max");       // renombra C, no A

        System.out.println("first: " + first.getName());             // Sol
        System.out.println("third: " + third.getName());             // Sol
        System.out.println("independent: " + independent.getName()); // Luna
        System.out.println("alias: " + alias.getName());             // Max
    }
}
