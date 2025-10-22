package dqs.main;

import java.util.Scanner;


import dqs.modelo.*;

/**
 * Punto de entrada y capa de interacción (CLI) del juego.
 *
 * Esta clase orquesta la creación de equipos, la ejecución de batallas y
 * contiene menús y pruebas rápidas. La lógica de batalla usa la clase
 * `Batalla` (modelo) y las entidades `Heroe` y `Enemigo` del paquete
 * `dqs.modelo`.
 */
public class App {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Batalla batalla = new Batalla();

    /**
     * Método principal: punto de entrada de la aplicación.
     * Imprime un mensaje de bienvenida y arranca el menú principal.
     *
     * Efectos secundarios: usa el `Scanner` estático para leer entrada
     * del usuario y delega en `mostrarMenuPrincipal()`.
     */
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
     */
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
     */
    private static void iniciarBatalla() {
        // Verificar que ambos equipos tengan al menos un miembro
        boolean hayHeroes = false, hayEnemigos = false;

        for (Heroe heroe : batalla.getEquipoHeroes()) {
            if (heroe != null) { hayHeroes = true; break; }
        }

        for (Enemigo enemigo : batalla.getEquipoEnemigos()) {
            if (enemigo != null) { hayEnemigos = true; break; }
        }

        if (!hayHeroes || !hayEnemigos) {
            System.out.println(" Ambos equipos deben tener al menos un miembro para iniciar la batalla.");
            return;
        }

        System.out.println("\n ¡LA BATALLA COMIENZA! ");
        batalla.mostrarEquipos();

        // Delegar la ejecución del bucle de batalla a BatallaManager (capa de servicio)
        new dqs.servicio.BatallaManager(batalla, scanner).ejecutarSimulacion();
    }
    
    /**
     * Ejecuta la acción seleccionada por el jugador para el héroe.
     *
     * Retorna `true` cuando la acción se ha ejecutado y el turno del héroe
     * debe terminar; retorna `false` para indicar opción inválida y volver
     * a solicitar entrada.
     */
    // Las lógicas de ejecución de acciones fueron trasladadas a BatallaManager.
    
    /**
     * Comprueba el estado de victoria/derrota: si todos los héroes están
     * muertos => derrota; si todos los enemigos están muertos => victoria.
     *
     * Si la batalla ha terminado, marca `batalla.setBatallaTerminada(true)`
     * y retorna `true`.
     */
    // La comprobación de victoria fue trasladada a BatallaManager.
    
    /**
     * Menú para crear equipos: opción simple para crear todos los héroes
     * o enemigos, o crear individualmente de forma interactiva.
     */
    private static void menuCrearEquipos() {
        System.out.println("\n=== CREAR EQUIPOS ===");
        System.out.println("1. Crear equipo de héroes (completo)");
        System.out.println("2. Crear equipo de enemigos (completo)");
        System.out.println("3. Crear héroe en una posición (interactivo)");
        System.out.println("4. Crear enemigo en una posición (interactivo)");
        System.out.println("5. Volver");
        System.out.print("Seleccione: ");

        int op = leerEntero();
        switch (op) {
            case 1 -> batalla.crearEquipoHeroes();
            case 2 -> batalla.crearEquipoEnemigos();
            case 3 -> batalla.crearHeroeInteractivo(scanner);
            case 4 -> batalla.crearEnemigoInteractivo(scanner);
            default -> System.out.println("Volviendo al menú principal.");
        }
    }

    /**
     * Menú para ejecutar pruebas rápidas de mecánicas concretas: defensa
     * de tanque, provocación y curación. Útil para depuración y ver
     * el comportamiento de los métodos en `Heroe`/`Enemigo`.
     */
    private static void menuPruebaMecanicas() {
        System.out.println("\n=== PRUEBA DE MECÁNICAS ===");
        System.out.println("1. Prueba de Defensa del Tanque");
        System.out.println("2. Prueba de Provocación");
        System.out.println("3. Prueba de Curación");
        System.out.println("4. Volver al Menú Principal");
        System.out.print("Seleccione una opción: ");
        
        int opcion = leerEntero();
        
        switch (opcion) {
            case 1 -> pruebaDefensaTanque();
            case 2 -> pruebaProvocacion();
            case 3 -> pruebaCuracion();
            default -> System.out.println(" Opción inválida.");
        }
    }
    
    /**
     * Prueba de la mecánica de defensa por tanque:
     *  - Crea un tanque (Heroe), un mago (Heroe) y un enemigo.
     *  - El tanque defiende al mago y se muestra la reducción de daño.
     */
    private static void pruebaDefensaTanque() {
        System.out.println("\n PRUEBA DE DEFENSA DEL TANQUE ");
        
    // Crear personajes de prueba
        Heroe tanque = new Heroe("Tanque", Tipo_Heroe.GUERRERO, 200, 50, 40, 30, 15);
        Heroe mago = new Heroe("Mago", Tipo_Heroe.MAGO, 80, 150, 35, 15, 20);
        Enemigo enemigo = Enemigo.crearEnemigo(Tipo_Enemigo.ORCO, "Orco Feroz");
        
        System.out.println("Antes de la defensa:");
        mago.mostrarEstado();
        
        // El tanque defiende al mago
        tanque.defender(mago);
        
        System.out.println("\nEl enemigo ataca al mago defendido:");
        enemigo.atacar(mago);
        
        System.out.println("\nDespués del ataque:");
        mago.mostrarEstado();
    }
    
    /**
     * Prueba de la mecánica de provocación:
     *  - Crea un tanque y provoca a un enemigo.
     *  - Verifica que `atacarConProvocacion` haga que el enemigo ataque
     *    al provocador cuando corresponda.
     */
    private static void pruebaProvocacion() {
        System.out.println("\n PRUEBA DE PROVOCACIÓN ");
        
        // Crear personajes de prueba
        Heroe tanque = new Heroe("Tanque", Tipo_Heroe.PALADIN, 180, 80, 35, 35, 18);
        Heroe mago = new Heroe("Mago", Tipo_Heroe.MAGO, 80, 150, 50, 15, 20);
        Enemigo enemigo = Enemigo.crearEnemigo(Tipo_Enemigo.TROLL, "Troll Gigante");
        
        Personaje[] heroes = {tanque, mago};
        
        System.out.println("Sin provocación - el enemigo puede atacar a cualquiera:");
        enemigo.seleccionarObjetivo(heroes);
        
        // El tanque provoca al enemigo
        tanque.provocarEnemigo(enemigo);
        
        System.out.println("\nCon provocación - el enemigo DEBE atacar al tanque:");
        enemigo.atacarConProvocacion(heroes);
    }
    
    /**
     * Prueba de la mecánica de curación:
     *  - Crea un sanador y un héroe con HP bajo y muestra el efecto de
     *    `curar(herido)`.
     */
    private static void pruebaCuracion() {
        System.out.println("\n PRUEBA DE CURACIÓN ");
        
        // Crear personajes de prueba
        Heroe sanador = new Heroe("Druida", Tipo_Heroe.DRUIDA, 120, 200, 30, 25, 18);
        Heroe herido = new Heroe("Guerrero", Tipo_Heroe.GUERRERO, 50, 30, 45, 30, 15); // HP bajo
        
        System.out.println("Antes de la curación:");
        herido.mostrarEstado();
        
        sanador.curar(herido);
        
        System.out.println("\nDespués de la curación:");
        herido.mostrarEstado();
    }
    
    // Nota: helpers relacionados con la simulación (mostrar estado, turnos,
    // conversión de arrays) fueron trasladados a `dqs.servicio.BatallaManager`.
    
    // Métodos para acciones específicas de héroes
    /**
     * Helper: muestra la lista de enemigos disponibles y ejecuta el ataque
     * seleccionado por el héroe.
     *
     * Retorna true si el héroe atacó correctamente (consumió su acción).
     */
    // Helpers moved to BatallaManager; kept App minimal.
    
    /**
     * Lee un entero de la entrada estándar de forma robusta. Si llega EOF
     * termina la aplicación de forma limpia.
     *
     * Reintenta en caso de formato inválido.
     */
    private static int leerEntero() {
        while (true) {
            try {
                // Si no hay más líneas (entrada cerrada), salir de forma limpia
                if (!scanner.hasNextLine()) {
                    System.out.println("\nNo hay entrada disponible. Terminando la aplicación.");
                    System.exit(0);
                }
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print(" Ingrese un número válido: ");
            }
        }
    }
}
