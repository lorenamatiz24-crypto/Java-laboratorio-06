// Depuración: 'doubled' se calcula pero no se usa. Esperado 11, imprime 7.
public class MethodStackBug {

    public static void main(String[] args) {
        int initialValue = 4;
        int result = calculate(initialValue);
        System.out.println("Final result: " + result);
    }

    static int calculate(int value) {
        int doubled = doubleValue(value); // doubled = 8 (inspecciónalo en Variables)
        return value + 3;                 // BUG: debería ser doubled + 3
    }

    static int doubleValue(int number) {
        return number * 2;
    }
}
