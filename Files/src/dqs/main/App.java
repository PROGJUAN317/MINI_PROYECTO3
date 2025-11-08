package dqs.main;

im<<<<<<< HEAD
import java.util.Scanner;


import dqs.modelos.*;
=======
import dqs.modelo.*;
import java.util.Scanner;
>>>>>>> c574db6d2e5751d6a10a3701ab6c9250e59c472f

/**
 * Punto de entrada y capa de interacción (CLI) del juego.
 *
 * Esta clase orquesta la creación de equipos, la ejecución de batallas y
 * contiene menús y pruebas rápidas. La lógica de batalla usa la clase
 * `Batalla` (modelo) y las entidades `Heroe` y `Enemigo` del paquete
 * `dqs.modelo`.
 
public class App {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Batalla batalla = new Batalla();

    /**
     * Método principal: punto de entrada de la aplicación.
     * Imprime un mensaje de bienvenida y arranca el menú principal.
     *
     * Efectos secundarios: usa el `Scanner` estático para leer entrada
     * del usuario y delega en `mostrarMenuPrincipal()`.
     
    public static void main(String[] args) {
        System.out.println("  ¡Bienvenido al Sistema de Batallas RPG!");
        System.out.println("==========================================");

        mostrarMenuPrincipal();
    }
    
    /**
     * Muestra el menú principal en bucle y atiende la entrada del usuario.
     *
     * Opciones principales:
     * - Crear equipos
     * - Mostrar equipos (delegado a `Batalla`)
     * - Iniciar batalla (invoca `iniciarBatalla`)
     * - Pruebas rápidas de mecánicas
     * - Salir
     *
     * Lee la opción con `leerEntero()` y delega en los métodos correspondientes.
     
    private static void mostrarMenuPrincipal() {
        while (true) {
            System.out.println("\n=== MENÚ PRINCIPAL ===");
            System.out.println("1. Crear Equipos");
            System.out.println("2. Mostrar Equipos");
            System.out.println("3. Iniciar Batalla");
            System.out.println("4. Prueba de Mecánicas");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opción: ");

            int opcion = leerEntero();

            switch (opcion) {
                case 1 -> menuCrearEquipos();
                case 2 -> batalla.mostrarEquipos();
                case 3 -> iniciarBatalla();
                case 4 -> menuPruebaMecanicas();
                case 5 -> {
                    System.out.println("¡Gracias por jugar! ");
                    System.exit(0);
                }
                default -> System.out.println(" Opción inválida. Intente de nuevo.");
            }
        }
    }

    /**
     * Inicia la simulación de batalla.
     *
     * Verifica precondiciones (al menos un héroe y un enemigo). Si se cumple,
     * muestra los equipos y delega la ejecución en `BatallaManager`.
     
    private static void iniciarBatalla() {
        package dqs.main;
        import dqs.vista.*;
        import javax.swing.SwingUtilities;

        /**
         * Punto de entrada minimal que lanza la vista gráfica principal.
         *
         * Esta clase delega la responsabilidad de construir y mostrar la ventana
         * en {@link dqs.vista.VistaIniciarBatallaNueva}. Se asegura de que la
         * creación de la UI ocurra en el Event Dispatch Thread usando
         * {@link SwingUtilities#invokeLater(Runnable)}.
         */
        public class App {

            public static void main(String[] args) {
                System.out.println("  ¡Bienvenido al Sistema de Batallas RPG!");
                System.out.println("==========================================");

                mostrarVistaIniciarBatallaNueva();
            }

            /**
             * Crea y muestra la vista principal en el EDT. Cualquier excepción se
             * imprime en la salida de error para facilitar depuración.
             */
            @SuppressWarnings("CallToPrintStackTrace")
            private static void mostrarVistaIniciarBatallaNueva() {
                try {
                    SwingUtilities.invokeLater(() -> new VistaIniciarBatallaNueva());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Error al iniciar la vista: " + e.getMessage());
                }
            }
        }
     *
