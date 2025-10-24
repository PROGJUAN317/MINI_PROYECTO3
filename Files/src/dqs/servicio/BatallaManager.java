package dqs.servicio;

import dqs.modelo.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    /** Ejecuta la simulación de la batalla (bucle principal). */
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
                        eFinal.atacarConProvocacion(convertirHeroesAPersonajes(batalla.getEquipoHeroes()));
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

            System.out.println("9. Pasar Turno");
            System.out.print("Seleccione una opción: ");

            int opcion = leerEntero();

            if (ejecutarAccionHeroe(heroe, opcion)) {
                break; // Salir del bucle cuando se ejecute una acción válida
            }
        }
    }

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
                System.out.println(heroe.getNombre() + " pasa su turno.");
                return true;
            }
        }

        System.out.println(" Opción inválida o no disponible para este tipo de héroe.");
        return false;
    }

    private boolean atacarConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el enemigo a atacar:");
        Enemigo objetivo = seleccionarEnemigo();
        if (objetivo != null) {
            heroe.atacar(objetivo);
            return true;
        }
        return false;
    }

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

    private boolean provocarConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el enemigo a provocar:");
        Enemigo enemigo = seleccionarEnemigo();
        if (enemigo != null) {
            heroe.provocarEnemigo(enemigo);
            return true;
        }
        return false;
    }

    private boolean curarConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el aliado a curar:");
        Heroe aliado = seleccionarHeroe();
        if (aliado != null) {
            heroe.curar(aliado);
            return true;
        }
        return false;
    }

    private boolean restaurarManaConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el aliado para restaurar mana:");
        Heroe aliado = seleccionarHeroe();
        if (aliado != null) {
            heroe.restaurarMana(aliado);
            return true;
        }
        return false;
    }

    private boolean eliminarEfectoConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el aliado para eliminar efectos negativos:");
        Heroe aliado = seleccionarHeroe();
        if (aliado != null) {
            heroe.eliminarEfectoNegativo(aliado);
            return true;
        }
        return false;
    }

    private boolean revivirConHeroe(Heroe heroe) {
        System.out.println("\n Seleccione el aliado a revivir:");
        Heroe aliado = seleccionarHeroeMuerto();
        if (aliado != null) {
            heroe.revivir(aliado);
            return true;
        }
        return false;
    }

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

    private Personaje[] convertirHeroesAPersonajes(Heroe[] heroes) {
        Personaje[] personajes = new Personaje[heroes.length];
        System.arraycopy(heroes, 0, personajes, 0, heroes.length);
        return personajes;
    }

    /*
     * Ejecuta un encuentro automático entre héroes y enemigos.
     * Los héroes atacan automáticamente al primer enemigo vivo.
     * Los enemigos actúan automáticamente (si son jefes pueden usar
     * su habilidad especial). Devuelve true si los héroes ganan.
     */
    public boolean ejecutarEncuentroAuto(Heroe[] heroes, Enemigo[] enemigos) {
        int turno = 1;
        while (true) {
            // Turno héroes (ataque automático)
            for (Heroe h : heroes) {
                if (h != null && h.esta_vivo()) {
                    h.atacarEnemigo(enemigos);
                }
            }

            // Verificar si héroes ganaron
            boolean anyEnemigoVivo = false;
            for (Enemigo e : enemigos) {
                if (e != null && e.esta_vivo()) { anyEnemigoVivo = true; break; }
            }
            if (!anyEnemigoVivo) {
                System.out.println("Encuentro ganado por los héroes.");
                return true;
            }

            // Turno enemigos
            for (Enemigo e : enemigos) {
                if (e != null && e.esta_vivo()) {
                    // Usamos pattern matching para instanceof (Java 16+)
                    if (e instanceof JefeEnemigo jefe) {
                        // 30% de probabilidad de usar habilidad especial
                        double r = Math.random();
                        Heroe objetivo = e.buscarHeroeVivo(heroes);
                        if (r < 0.3 && objetivo != null) {
                            jefe.usarHabilidadEspecial(objetivo);
                        } else {
                            e.atacarConProvocacion(convertirHeroesAPersonajes(heroes));
                        }
                    } else {
                        e.atacarConProvocacion(convertirHeroesAPersonajes(heroes));
                    }
                }
            }

            // Verificar si enemigos ganaron
            boolean anyHeroeVivo = false;
            for (Heroe h : heroes) {
                if (h != null && h.esta_vivo()) { anyHeroeVivo = true; break; }
            }
            if (!anyHeroeVivo) {
                System.out.println("Encuentro perdido. Todos los héroes han caído.");
                return false;
            }

            turno++;
            if (turno > 500) {
                System.out.println("El encuentro se ha extendido demasiado. Se considera derrota técnica.");
                return false;
            }
        }
    }

    /**
     * Inicia una batalla por fases (oleadas + jefe por oleada).
     * Para prototipo usa encuentros automáticos (sin menús interactivos).
     */
    public void iniciarBatallaPorFases(Heroe[] heroes, List<Enemigo[]> oleadas, List<Tipo_JefeEnemigo> tiposJefes, boolean curarEntreFases) {
        if (heroes == null || oleadas == null || oleadas.isEmpty()) {
            System.out.println("Entradas inválidas para iniciar la batalla por fases.");
            return;
        }

        for (int i = 0; i < oleadas.size(); i++) {
            Enemigo[] oleada = oleadas.get(i);
            System.out.println("\n--- Inicia la oleada " + (i + 1) + " ---");

            boolean ganado = ejecutarEncuentroAuto(heroes, oleada);
            if (!ganado) {
                System.out.println("Derrota en la oleada " + (i + 1));
                batalla.setBatallaTerminada(true);
                return;
            }

            System.out.println("Oleada " + (i + 1) + " completada. Preparando jefe...");

            // Elegir tipo de jefe (si se proporcionó la lista) o usar aleatorio
            Tipo_JefeEnemigo tipoJefe;
            if (tiposJefes != null && !tiposJefes.isEmpty()) {
                tipoJefe = tiposJefes.get(i % tiposJefes.size());
            } else {
                Tipo_JefeEnemigo[] valores = Tipo_JefeEnemigo.values();
                tipoJefe = valores[(int) (Math.random() * valores.length)];
            }

            JefeEnemigo jefe = JefeFactory.crearJefeAleatorio(tipoJefe);
            System.out.println("¡Ha aparecido el jefe: " + jefe.getNombre() + "! Tipo: " + tipoJefe.name());

            boolean vencido = ejecutarEncuentroAuto(heroes, new Enemigo[] { jefe });
            if (!vencido) {
                System.out.println("Derrota ante el jefe " + tipoJefe.name());
                batalla.setBatallaTerminada(true);
                return;
            }

            if (curarEntreFases) {
                System.out.println("Aplicando curación parcial a los héroes entre fases...");
                for (Heroe h : heroes) {
                    if (h != null && h.esta_vivo()) {
                        int recuperarHp = (int) (h.getMaxHp() * 0.2);
                        int recuperarMp = (int) (h.getMaxMp() * 0.2);
                        h.setHp(Math.min(h.getHp() + recuperarHp, h.getMaxHp()));
                        h.setMp(Math.min(h.getMp() + recuperarMp, h.getMaxMp()));
                    }
                }
            }
        }

        System.out.println("\n¡Victoria! Todas las oleadas y jefes derrotados.");
        batalla.setBatallaTerminada(true);
    }
}
