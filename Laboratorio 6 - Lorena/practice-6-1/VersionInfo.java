public class VersionInfo {
    public static void main(String[] args) {
        System.out.println("java.version : " + System.getProperty("java.version"));
        System.out.println("java.vendor  : " + System.getProperty("java.vendor"));
        System.out.println("java.home    : " + System.getProperty("java.home"));
        System.out.println("vm.name      : " + System.getProperty("java.vm.name"));
        System.out.println("Procesadores : " + Runtime.getRuntime().availableProcessors());

        // ¿Este entorno incluye el compilador? (solo un JDK lo trae)
        boolean hayCompilador = javax.tools.ToolProvider.getSystemJavaCompiler() != null;
        System.out.println("Compilador disponible (JDK): " + hayCompilador);
    }
}
