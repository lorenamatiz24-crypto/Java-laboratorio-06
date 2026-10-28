/** Verificaciones ejecutables de las reglas del paso por valor. */
public class MemoryChallengeTest {

    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError("FALLÓ: " + message);
        System.out.println("OK: " + message);
    }

    static void visitAndReplace(PetProfile current) {
        current.registerVisit();
        current = new PetProfile("Nova");
        current.registerVisit();
    }

    public static void main(String[] args) {
        PetProfile pet = new PetProfile("Luna");
        PetProfile alias = pet;

        visitAndReplace(pet);

        check(pet.getVisits() == 1, "modificar el objeto compartido es visible fuera");
        check(pet.getName().equals("Luna"), "reasignar el parámetro no cambia la variable del llamador");
        check(pet == alias, "pet y alias siguen refiriendo al mismo objeto");

        PetProfile other = new PetProfile("Max");
        check(other != pet, "new crea una instancia independiente");
        check(other.getVisits() == 0, "cada instancia tiene su propio contador");

        System.out.println("Todas las verificaciones pasaron.");
    }
}
