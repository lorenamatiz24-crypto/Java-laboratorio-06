public class SafeRecursion {

    static void countDown(int value) {
        System.out.println("Entering: " + value);

        if (value == 0) {
            System.out.println("Base case");
            return;
        }

        countDown(value - 1);
        System.out.println("Returning: " + value);
    }

    public static void main(String[] args) {
        System.out.println("--- countDown(3) ---");
        countDown(3);

        System.out.println("--- Ejercicio: countDown(2) ---");
        countDown(2);
    }
}
