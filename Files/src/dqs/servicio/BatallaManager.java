package dqs.servicio;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import dqs.modelos.*;

/**
 * Clase encargada de orquestar la simulación de una batalla.
 * Mantiene la responsabilidad fuera de la UI (`App`) y del modelo (`Batalla`).
 */
public class BatallaManager {
    private final Batalla batalla;
    private final Scanner scanner;

    public BatallaManager(Batalla batalla, Scanner scanner) {
        this.batalla = batalla;
        this.scanner = scanner;
    }
    /**
     * Crea un manager para orquestar la simulación de una batalla.
     *
     * @param batalla instancia del modelo que contiene equipos y estado
     * @param scanner  scanner compartido por la UI para leer entradas del usuario
     */
    
    /**
     * Ejecuta la simulación de la batalla (bucle principal).
     *
     * El método alterna entre turnos de héroes (interactivo/manual) y
     * turnos de enemigos (automatizados). Para los enemigos se utiliza
     * un scheduler que introduce pequeñas pausas entre acciones para
     * mejorar la legibilidad en consola.
     *
     * El bucle termina cuando `batalla.isBatallaTerminada()` es true o
     * se alcanza un límite de turnos para evitar bucles infinitos.
     */
    public void ejecutarSimulacion() {
        int turno = 1;

        while (!batalla.isBatallaTerminada()) {
            System.out.println("\n=== TURNO " + turno + " ===");
            mostrarEstadoActual();

            // Turno de los héroes (MANUAL)
            System.out.println("\n--- Turno de los Héroes ---");
            turnoHeroesManual();

            if (verificarVictoria()) break;

            // Turno de los enemigos (automático pero con pausa)
            System.out.println("\n--- Turno de los Enemigos ---");
            System.out.println("Presione Enter para continuar con el turno de los enemigos...");
            scanner.nextLine();

            // Ejecutar acciones de enemigos con un scheduler para evitar bloquear el hilo principal
            Enemigo[] enemigos = batalla.getEquipoEnemigos();
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            long delayMs = 0L;
            final long stepMs = 1000L; // pausa entre acciones

            for (Enemigo enemigo : enemigos) {
                if (enemigo != null && enemigo.esta_vivo()) {
                    Enemigo eFinal = enemigo; // para usar dentro de la lambda
                    scheduler.schedule(() -> {
                        System.out.println("\n" + eFinal.getNombre() + " está actuando...");

                        // Si el enemigo es un jefe con comportamiento especial, delegar en su actuar
                        try {
                            if (eFinal instanceof dqs.modelos.JefeEnemigo) {
                                // JefeEnemigo.actuar maneja cooldowns y usa la habilidad especial cada N turnos
                                ((dqs.modelos.JefeEnemigo)eFinal).actuar(batalla.getEquipoHeroes());
                            } else {
                                // Enemigos normales atacan a un héroe vivo aleatorio
                                eFinal.atacarAleatorio(batalla.getEquipoHeroes());
                            }
                        } catch (Exception ex) {
                            System.out.println("Error al ejecutar acción del enemigo: " + ex.getMessage());
                        }
                    }, delayMs, TimeUnit.MILLISECONDS);
                    delayMs += stepMs;
                }
            }

            // esperar a que terminen todas las tareas programadas
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(delayMs + 500, TimeUnit.MILLISECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                scheduler.shutdownNow();
            }

            if (verificarVictoria()) break;

            turno++;

            if (turno > 50) { // Límite de seguridad
                System.out.println(" ¡La batalla ha durado demasiado! Es un empate.");
                break;
            }

            System.out.println("\nPresione Enter para continuar al siguiente turno...");
            scanner.nextLine();
        }
    }

    // Helpers (trasladados desde App para mantener el comportamiento)

    /**
     * Imprime por consola el estado actual de héroes y enemigos vivos.
     * Muestra nombre, tipo, HP y MP para facilitar la toma de decisiones
     * por parte del usuario en los turnos manuales.
     */
    private void mostrarEstadoActual() {
        System.out.println("\n ESTADO ACTUAL DE LA BATALLA:");

        System.out.println("\n HÉROES VIVOS:");
        for (int i = 0; i < batalla.getEquipoHeroes().length; i++) {
            Heroe heroe = batalla.getEquipoHeroes()[i];
            if (heroe != null && heroe.esta_vivo()) {
                System.out.println((i + 1) + ". " + heroe.getNombre() + " [" + heroe.getTipo().name() + "] " +
                                 "HP: " + heroe.getHp() + " | MP: " + heroe.getMp());
            }
        }

        System.out.println("\n ENEMIGOS VIVOS:");
        for (int i = 0; i < batalla.getEquipoEnemigos().length; i++) {
            Enemigo enemigo = batalla.getEquipoEnemigos()[i];
            if (enemigo != null && enemigo.esta_vivo()) {
                System.out.println((i + 1) + ". " + enemigo.getNombre() + " [" + enemigo.getTipo().name() + "] " +
                                 "HP: " + enemigo.getHp() + " | MP: " + enemigo.getMp());
            }
        }
    }

    /**
     * Itera por cada héroe vivo y solicita la acción a realizar (menú
     * interactivo). Si la comprobación de victoria se cumple durante
     * el proceso, el bucle se interrumpe.
     */
    private void turnoHeroesManual() {
        for (Heroe heroe : batalla.getEquipoHeroes()) {
            if (heroe != null && heroe.esta_vivo()) {
                System.out.println("\n Es el turno de: " + heroe.getNombre() + " [" + heroe.getTipo().name() + "]");
                System.out.println("HP: " + heroe.getHp() + " | MP: " + heroe.getMp());

                mostrarMenuAccionHeroe(heroe);

                if (verificarVictoria()) break;
            }
        }
    }

    /**
     * Muestra el menú de acciones disponible para un héroe concreto,
     * adaptando las opciones según el tipo del héroe (Guerrero,
     * Paladín, Druida, etc.). Repite hasta que se ejecuta una acción
     * válida que consume el turno.
     *
     * @param heroe el héroe que va a actuar
     */
    private void mostrarMenuAccionHeroe(Heroe heroe) {
        while (true) {
            System.out.println("\n¿Qué acción desea realizar?");
            System.out.println("1. Atacar Enemigo");

            if (heroe.getTipo() == Tipo_Heroe.GUERRERO || heroe.getTipo() == Tipo_Heroe.PALADIN) {
                System.out.println("2. Defender Aliado");
                System.out.println("3. Provocar Enemigo");
                System.out.println("4. Aumentar Defensa");
            }

            if (heroe.getTipo() == Tipo_Heroe.DRUIDA || heroe.getTipo() == Tipo_Heroe.PALADIN) {
                System.out.println("5. Curar Aliado");
                System.out.println("6. Restaurar Mana");
                System.out.println("7. Eliminar Efecto Negativo");
            }

            if (heroe.getTipo() == Tipo_Heroe.PALADIN) {
                System.out.println("8. Revivir Aliado");
            }

            if (heroe.getTipo() == Tipo_Heroe.MAGO || heroe.getTipo() == Tipo_Heroe.DRUIDA) {
                System.out.println("9. Lanzar Hechizo de sueño");
            }

            if (heroe.getTipo() == Tipo_Heroe.MAGO){
                System.out.println("10. Lanzar Hechizo de refuerzo");
                System.out.println("11. Lanzar Hechizo de parálisis");
            }

            System.out.println("12. Pasar Turno");
            System.out.print("Seleccione una opción: ");

            int opcion = leerEntero();

            if (ejecutarAccionHeroe(heroe, opcion)) {
                break; // Salir del bucle cuando se ejecute una acción válida
            }
        }
    }

    /**
     * Ejecuta la acción elegida por el usuario para el héroe.
     *
     * @return true si la acción fue válida y el héroe consumió su turno;
     *         false si la opción fue inválida para que el menú la repita.
     */
private boolean ejecutarAccionHeroe(Heroe heroe, int opcion) {
    switch (opcion) {
        case 1 -> {
            return atacarConHeroe(heroe);
        }
        case 2 -> {
            if (heroe.getTipo() == Tipo_Heroe.GUERRERO || heroe.getTipo() == Tipo_Heroe.PALADIN) {
                return defenderConHeroe(heroe);
            }
        }
        case 3 -> {
            if (heroe.getTipo() == Tipo_Heroe.GUERRERO || heroe.getTipo() == Tipo_Heroe.PALADIN) {
                return provocarConHeroe(heroe);
            }
        }
        case 4 -> {
            if (heroe.getTipo() == Tipo_Heroe.GUERRERO || heroe.getTipo() == Tipo_Heroe.PALADIN) {
                heroe.aumentarDefensa(10);
                return true;
            }
        }
        case 5 -> {
            if (heroe.getTipo() == Tipo_Heroe.DRUIDA || heroe.getTipo() == Tipo_Heroe.PALADIN) {
                return curarConHeroe(heroe);
            }
        }
        case 6 -> {
            if (heroe.getTipo() == Tipo_Heroe.DRUIDA) {
                return restaurarManaConHeroe(heroe);
            }
        }
        case 7 -> {
            if (heroe.getTipo() == Tipo_Heroe.DRUIDA || heroe.getTipo() == Tipo_Heroe.PALADIN) {
                return eliminarEfectoConHeroe(heroe);
            }
        }
        case 8 -> {
            if (heroe.getTipo() == Tipo_Heroe.PALADIN) {
                return revivirConHeroe(heroe);
            }
        }
        case 9 -> {
            // Para Druida: hechizo de sueño; para Mago: refuerzo (según menú)
            if (heroe.getTipo() == Tipo_Heroe.DRUIDA || heroe.getTipo() == Tipo_Heroe.MAGO) {
                return LanzaHechizoDeSueño(heroe);
            }
        }
        case 10 -> {
            if (heroe.getTipo() == Tipo_Heroe.MAGO) {
                return LanzaHechizoRefuerzo(heroe);
            }
        }
        case 11 -> {
            if (heroe.getTipo() == Tipo_Heroe.MAGO) {
                return LanzaHechizoParalisis(heroe);
            }
        }
        case 12 -> {
            System.out.println("\nHas decidido pasar el turno.");
            return true;
            }
        default -> {
            // caído por defecto si la opción no coincide
        }
    }

    System.out.println(" Opción inválida o no disponible para este tipo de héroe.");
    return false;
}

    /**
     * Solicita seleccionar un enemigo y ejecuta el ataque del héroe sobre
     * ese objetivo.
     */
    private boolean atacarConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el enemigo a atacar:");
        Enemigo objetivo = seleccionarEnemigo();
        if (objetivo != null) {
            heroe.atacar(objetivo);
            return true;
        }
        return false;
    }

    /**
     * Permite que el héroe defienda a un aliado: delega en la mecánica
     * implementada en la clase `Heroe`.
     */
    private boolean defenderConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el aliado a defender:");
        Heroe aliado = seleccionarHeroe();
        if (aliado != null && aliado != heroe) {
            heroe.defender(aliado);
            return true;
        } else if (aliado == heroe) {
            System.out.println(" No puedes defenderte a ti mismo.");
        }
        return false;
    }

    /**
     * Aplica la acción de provocar sobre un enemigo seleccionado.
     */
    private boolean provocarConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el enemigo a provocar:");
        Enemigo enemigo = seleccionarEnemigo();
        if (enemigo != null) {
            heroe.provocarEnemigo(enemigo);
            return true;
        }
        return false;
    }

    /**
     * Solicita seleccionar un aliado para curar y ejecuta la habilidad
     * de curación del héroe (si procede según su clase).
     */
    private boolean curarConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el aliado a curar:");
        Heroe aliado = seleccionarHeroe();
        if (aliado != null) {
            heroe.curar(aliado);
            return true;
        }
        return false;
    }

    /**
     * Restaura mana a un aliado según la habilidad del héroe.
     */
    private boolean restaurarManaConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el aliado para restaurar mana:");
        Heroe aliado = seleccionarHeroe();
        if (aliado != null) {
            heroe.restaurarMana(aliado);
            return true;
        }
        return false;
    }

    /**
     * Elimina efectos negativos de un aliado seleccionado, delegando a
     * la implementación del héroe.
     */
    private boolean eliminarEfectoConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el aliado para eliminar efectos negativos:");
        Heroe aliado = seleccionarHeroe();
        if (aliado != null) {
            heroe.eliminarEfectoNegativo(aliado);
            return true;
        }
        return false;
    }

    /**
     * Revive a un aliado caído (si la clase del héroe lo permite).
     */
    private boolean revivirConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el aliado a revivir:");
        Heroe aliado = seleccionarHeroeMuerto();
        if (aliado != null) {
            heroe.revivir(aliado);
            return true;
        }
        return false;
    }

    //Aumenta el ataque de un aliado
    private boolean LanzaHechizoRefuerzo(Heroe heroe){
        System.out.println("\n Seleccione el aliado a reforzar:");
        Heroe aliado = seleccionarHeroe();
        if (aliado != null){
            heroe.LanzaHechizoRefuerzo(aliado);
            return true;
        }
        return false;
    }

    /* 
     * deja lanzar hechizo de sueño
     */
    private boolean LanzaHechizoDeSueño(Heroe heroe){
        System.out.println("\n Seleccionar el enemigo a dormir:");
        Enemigo objetivo = seleccionarEnemigo();
        if (objetivo != null){
            heroe.LanzaHechizoSueño(objetivo);
            return true;
        }
        return false;
    }

    // Método para lanzar parálisis desde el manager (para magos)
    private boolean LanzaHechizoParalisis(Heroe heroe){
        System.out.println("\n Seleccionar el enemigo a paralizar:");
        Enemigo objetivo = seleccionarEnemigo();
        if (objetivo != null){
            heroe.LanzaHechizoParalisis(objetivo);
            return true;
        }
        return false;
    }



    /**
     * Muestra la lista de enemigos vivos y devuelve la selección del
     * usuario. Si el usuario elige cancelar, retorna null.
     */
    private Enemigo seleccionarEnemigo() {
        System.out.println("Enemigos disponibles:");
        int contador = 1;
        for (Enemigo enemigo : batalla.getEquipoEnemigos()) {
            if (enemigo != null && enemigo.esta_vivo()) {
                System.out.println(contador + ". " + enemigo.getNombre() + " [" + enemigo.getTipo().name() + "] " +
                        "HP: " + enemigo.getHp());
                contador++;
            }
        }
        System.out.println(contador + ". Cancelar");
        System.out.print("Seleccione: ");

        int opcion = leerEntero();
        if (opcion == contador) return null; // Cancelar

        // Buscar el enemigo seleccionado
        contador = 1;
        for (Enemigo enemigo : batalla.getEquipoEnemigos()) {
            if (enemigo != null && enemigo.esta_vivo()) {
                if (contador == opcion) {
                    return enemigo;
                }
                contador++;
            }
        }

        System.out.println(" Selección inválida.");
        return null;
    }

    /**
     * Muestra la lista de héroes vivos y devuelve el seleccionado; si
     * el usuario cancela retorna null.
     */
    private Heroe seleccionarHeroe() {
        System.out.println("Héroes disponibles:");
        int contador = 1;
        for (Heroe heroe : batalla.getEquipoHeroes()) {
            if (heroe != null && heroe.esta_vivo()) {
                System.out.println(contador + ". " + heroe.getNombre() + " [" + heroe.getTipo().name() + "] " +
                        "HP: " + heroe.getHp() + " | MP: " + heroe.getMp());
                contador++;
            }
        }
        System.out.println(contador + ". Cancelar");
        System.out.print("Seleccione: ");

        int opcion = leerEntero();
        if (opcion == contador) return null; // Cancelar

        // Buscar el héroe seleccionado
        contador = 1;
        for (Heroe heroe : batalla.getEquipoHeroes()) {
            if (heroe != null && heroe.esta_vivo()) {
                if (contador == opcion) {
                    return heroe;
                }
                contador++;
            }
        }

        System.out.println(" Selección inválida.");
        return null;
    }

    /**
     * Muestra la lista de héroes caídos (no vivos) para operaciones como
     * revivir. Retorna null si no hay candidatos o si el usuario
     * cancela.
     */
    private Heroe seleccionarHeroeMuerto() {
        System.out.println("Héroes caídos:");
        int contador = 1;
        for (Heroe heroe : batalla.getEquipoHeroes()) {
            if (heroe != null && !heroe.esta_vivo()) {
                System.out.println(contador + ". " + heroe.getNombre() + " [" + heroe.getTipo().name() + "] " +
                        "HP: " + heroe.getHp());
                contador++;
            }
        }

        if (contador == 1) {
            System.out.println("No hay héroes caídos para revivir.");
            return null;
        }

        System.out.println(contador + ". Cancelar");
        System.out.print("Seleccione: ");

        int opcion = leerEntero();
        if (opcion == contador) return null; // Cancelar

        // Buscar el héroe muerto seleccionado
        contador = 1;
        for (Heroe heroe : batalla.getEquipoHeroes()) {
            if (heroe != null && !heroe.esta_vivo()) {
                if (contador == opcion) {
                    return heroe;
                }
                contador++;
            }
        }

        System.out.println(" Selección inválida.");
        return null;
    }

    /**
     * Lee un entero de la entrada usando el `Scanner` compartido.
     * Si la entrada se cierra devuelve -1 para que el caller lo
     * maneje (en lugar de cerrar la JVM directamente).
     */
    private int leerEntero() {
        while (true) {
            try {
                if (!scanner.hasNextLine()) {
                    System.out.println("\nNo hay entrada disponible. Terminando la simulación.");
                    // En lugar de System.exit, devolvemos un valor inválido para que el caller pueda manejar.
                    return -1;
                }
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print(" Ingrese un número válido: ");
            }
        }
    }

    /**
     * Comprueba si uno de los bandos ha perdido a todos sus miembros
     * vivos. En caso de victoria marca la batalla como terminada y
     * devuelve true.
     */
    private boolean verificarVictoria() {
        boolean heroesVivos = false, enemigosVivos = false;

        for (Heroe heroe : batalla.getEquipoHeroes()) {
            if (heroe != null && heroe.esta_vivo()) {
                heroesVivos = true;
                break;
            }
        }

        for (Enemigo enemigo : batalla.getEquipoEnemigos()) {
            if (enemigo != null && enemigo.esta_vivo()) {
                enemigosVivos = true;
                break;
            }
        }

        if (!heroesVivos) {
            System.out.println("\n ¡Los Enemigos han ganado la batalla!");
            batalla.setBatallaTerminada(true);
            return true;
        } else if (!enemigosVivos) {
            System.out.println("\n ¡Los Héroes han ganado la batalla!");
            batalla.setBatallaTerminada(true);
            return true;
        }

        return false;
    }

    /**
     * Convierte un arreglo de `Heroe` a `Personaje[]` para reutilizar
     * APIs que trabajen con el tipo base `Personaje`.
     */
    @SuppressWarnings("unused")
    private Personaje[] convertirHeroesAPersonajes(Heroe[] heroes) {
        Personaje[] personajes = new Personaje[heroes.length];
        System.arraycopy(heroes, 0, personajes, 0, heroes.length);
        return personajes;
    }
}
