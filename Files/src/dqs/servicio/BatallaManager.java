package dqs.servicio;

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

    public BatallaManager(Batalla batalla) {
        this.batalla = batalla;
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
            dqs.events.BattleEventBus.log("\n=== TURNO " + turno + " ===");
            mostrarEstadoActual();

            // Turno de los héroes (MANUAL)
            dqs.events.BattleEventBus.log("\n--- Turno de los Héroes ---");
            turnoHeroesManual();

            if (verificarVictoria()) break;

            // Turno de los enemigos (automático pero con pausa)
            dqs.events.BattleEventBus.log("\n--- Turno de los Enemigos ---");
            dqs.events.BattleEventBus.log("Presione Enter para continuar con el turno de los enemigos...");
            // Nota: la lectura de entrada la gestiona la vista/controlador en la nueva arquitectura MVC.

            // Ejecutar acciones de enemigos con un scheduler para evitar bloquear el hilo principal
            Enemigo[] enemigos = batalla.getEquipoEnemigos();
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            long delayMs = 0L;
            final long stepMs = 1000L; // pausa entre acciones

            for (Enemigo enemigo : enemigos) {
                if (enemigo != null && enemigo.esta_vivo()) {
                    Enemigo eFinal = enemigo; // para usar dentro de la lambda
                    scheduler.schedule(() -> {
                        dqs.events.BattleEventBus.log("\n" + eFinal.getNombre() + " está actuando...");

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
                                dqs.events.BattleEventBus.log("Error al ejecutar acción del enemigo: " + ex.getMessage());
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
                dqs.events.BattleEventBus.log(" ¡La batalla ha durado demasiado! Es un empate.");
                break;
            }

            dqs.events.BattleEventBus.log("\nPresione Enter para continuar al siguiente turno...");
            // Nota: la lectura de entrada la gestiona la vista/controlador en la nueva arquitectura MVC.
        }
    }

    // Helpers (trasladados desde App para mantener el comportamiento)

    /**
     * Imprime por consola el estado actual de héroes y enemigos vivos.
     * Muestra nombre, tipo, HP y MP para facilitar la toma de decisiones
     * por parte del usuario en los turnos manuales.
     */
    private void mostrarEstadoActual() {
    dqs.events.BattleEventBus.log("\n ESTADO ACTUAL DE LA BATALLA:");

    dqs.events.BattleEventBus.log("\n HÉROES VIVOS:");
        for (int i = 0; i < batalla.getEquipoHeroes().length; i++) {
            Heroe heroe = batalla.getEquipoHeroes()[i];
            if (heroe != null && heroe.esta_vivo()) {
                dqs.events.BattleEventBus.log((i + 1) + ". " + heroe.getNombre() + " [" + heroe.getTipo().name() + "] " +
                                 "HP: " + heroe.getHp() + " | MP: " + heroe.getMp());
            }
        }

    dqs.events.BattleEventBus.log("\n ENEMIGOS VIVOS:");
        for (int i = 0; i < batalla.getEquipoEnemigos().length; i++) {
            Enemigo enemigo = batalla.getEquipoEnemigos()[i];
            if (enemigo != null && enemigo.esta_vivo()) {
                dqs.events.BattleEventBus.log((i + 1) + ". " + enemigo.getNombre() + " [" + enemigo.getTipo().name() + "] " +
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
                dqs.events.BattleEventBus.log("\n Es el turno de: " + heroe.getNombre() + " [" + heroe.getTipo().name() + "]");
                dqs.events.BattleEventBus.log("HP: " + heroe.getHp() + " | MP: " + heroe.getMp());

                // En MVC el controlador solicita a la vista la acción del jugador
                // y luego invoca BatallaManager.ejecutarAccionHeroe(...).
                dqs.events.BattleEventBus.log("[turnoHeroesManual] Esperando acción del controlador para " + heroe.getNombre());

                if (verificarVictoria()) break;
            }
        }
    }
    /**
     * Devuelve las opciones de acción disponibles para el héroe.
     * La vista/controlador debe mostrar estas opciones y pedir al
     * usuario que seleccione una, luego invocar
     * BatallaManager.ejecutarAccionHeroe(...).
     */
    public String[] obtenerOpcionesAccion(Heroe heroe) {
        java.util.List<String> ops = new java.util.ArrayList<>();
        ops.add("Atacar Enemigo");

        if (heroe.getTipo() == Tipo_Heroe.GUERRERO || heroe.getTipo() == Tipo_Heroe.PALADIN) {
            ops.add("Defender Aliado");
            ops.add("Provocar Enemigo");
            ops.add("Aumentar Defensa");
        }

        if (heroe.getTipo() == Tipo_Heroe.DRUIDA || heroe.getTipo() == Tipo_Heroe.PALADIN) {
            ops.add("Curar Aliado");
            ops.add("Restaurar Mana");
            ops.add("Eliminar Efecto Negativo");
        }

        if (heroe.getTipo() == Tipo_Heroe.PALADIN) {
            ops.add("Revivir Aliado");
        }

        if (heroe.getTipo() == Tipo_Heroe.MAGO || heroe.getTipo() == Tipo_Heroe.DRUIDA) {
            ops.add("Lanzar Hechizo de sueño");
        }

        if (heroe.getTipo() == Tipo_Heroe.MAGO) {
            ops.add("Lanzar Hechizo de refuerzo");
            ops.add("Lanzar Hechizo de parálisis");
        }

        ops.add("Pasar Turno");
        return ops.toArray(new String[0]);
    }

    /**
     * Lista los nombres de enemigos vivos (para la UI)
     */
    public String[] listarEnemigosVivos() {
        java.util.List<String> lista = new java.util.ArrayList<>();
        for (Enemigo enemigo : batalla.getEquipoEnemigos()) {
            if (enemigo != null && enemigo.esta_vivo()) {
                lista.add(enemigo.getNombre());
            }
        }
        if (lista.isEmpty()) lista.add("[No hay enemigos vivos]");
        return lista.toArray(new String[0]);
    }

    /**
     * Lista los nombres de héroes vivos (para la UI)
     */
    public String[] listarHeroesVivos() {
        java.util.List<String> lista = new java.util.ArrayList<>();
        for (Heroe h : batalla.getEquipoHeroes()) {
            if (h != null && h.esta_vivo()) lista.add(h.getNombre());
        }
        if (lista.isEmpty()) lista.add("[No hay héroes vivos]");
        return lista.toArray(new String[0]);
    }

    /**
     * Lista los nombres de héroes caídos (para revivir)
     */
    public String[] listarHeroesCaidos() {
        java.util.List<String> lista = new java.util.ArrayList<>();
        for (Heroe h : batalla.getEquipoHeroes()) {
            if (h != null && !h.esta_vivo()) lista.add(h.getNombre());
        }
        if (lista.isEmpty()) lista.add("[No hay héroes caídos]");
        return lista.toArray(new String[0]);
    }

    /**
     * Ejecuta la acción elegida para un héroe. El controlador obtiene
     * la opción (índice dentro de obtenerOpcionesAccion) y si hace
     * falta pasa un objetivo (índice basado en arrays retornados por
     * listarEnemigosVivos/listarHeroesVivos/listarHeroesCaidos).
     *
     * @param objetivoIndex índice del objetivo en la lista correspondiente o -1 si no aplica
     */
    public boolean ejecutarAccionHeroe(Heroe heroe, int opcion, int objetivoIndex) {
        // Mapear opción (basada en el array devuelto por obtenerOpcionesAccion)
        String[] ops = obtenerOpcionesAccion(heroe);
        if (opcion < 0 || opcion >= ops.length) {
            dqs.events.BattleEventBus.log(" Opción inválida o fuera de rango.");
            return false;
        }

        String seleccion = ops[opcion];
        switch (seleccion) {
            case "Atacar Enemigo" -> {
                return atacarConHeroe(heroe, objetivoIndex);
            }
            case "Defender Aliado" -> {
                return defenderConHeroe(heroe, objetivoIndex);
            }
            case "Provocar Enemigo" -> {
                return provocarConHeroe(heroe, objetivoIndex);
            }
            case "Aumentar Defensa" -> {
                heroe.aumentarDefensa(10);
                return true;
            }
            case "Curar Aliado" -> {
                return curarConHeroe(heroe, objetivoIndex);
            }
            case "Restaurar Mana" -> {
                return restaurarManaConHeroe(heroe, objetivoIndex);
            }
            case "Eliminar Efecto Negativo" -> {
                return eliminarEfectoConHeroe(heroe, objetivoIndex);
            }
            case "Revivir Aliado" -> {
                return revivirConHeroe(heroe, objetivoIndex);
            }
            case "Lanzar Hechizo de sueño" -> {
                return LanzaHechizoDeSueño(heroe, objetivoIndex);
            }
            case "Lanzar Hechizo de refuerzo" -> {
                return LanzaHechizoRefuerzo(heroe, objetivoIndex);
            }
            case "Lanzar Hechizo de parálisis" -> {
                return LanzaHechizoParalisis(heroe, objetivoIndex);
            }
            case "Pasar Turno" -> {
                dqs.events.BattleEventBus.log("\nHas decidido pasar el turno.");
                return true;
            }
            default -> {
                dqs.events.BattleEventBus.log(" Opción no implementada: " + seleccion);
                return false;
            }
        }
    }











    // --- Nuevas versiones que aceptan índices (no interactivo) ---
    private Enemigo getEnemigoVivoPorIndice(int idx) {
        if (idx < 0) return null;
        int contador = 0;
        for (Enemigo enemigo : batalla.getEquipoEnemigos()) {
            if (enemigo != null && enemigo.esta_vivo()) {
                if (contador == idx) return enemigo;
                contador++;
            }
        }
        return null;
    }

    private Heroe getHeroeVivoPorIndice(int idx) {
        if (idx < 0) return null;
        int contador = 0;
        for (Heroe h : batalla.getEquipoHeroes()) {
            if (h != null && h.esta_vivo()) {
                if (contador == idx) return h;
                contador++;
            }
        }
        return null;
    }

    private Heroe getHeroeMuertoPorIndice(int idx) {
        if (idx < 0) return null;
        int contador = 0;
        for (Heroe h : batalla.getEquipoHeroes()) {
            if (h != null && !h.esta_vivo()) {
                if (contador == idx) return h;
                contador++;
            }
        }
        return null;
    }

    private boolean atacarConHeroe(Heroe heroe, int enemigoIdx) {
        Enemigo objetivo = getEnemigoVivoPorIndice(enemigoIdx);
        if (objetivo != null) {
            heroe.atacar(objetivo);
            return true;
        }
        dqs.events.BattleEventBus.log(" Objetivo inválido para atacar.");
        return false;
    }

    private boolean defenderConHeroe(Heroe heroe, int aliadoIdx) {
        Heroe aliado = getHeroeVivoPorIndice(aliadoIdx);
        if (aliado != null && aliado != heroe) {
            heroe.defender(aliado);
            return true;
        } else if (aliado == heroe) {
            dqs.events.BattleEventBus.log(" No puedes defenderte a ti mismo.");
        }
        return false;
    }

    private boolean provocarConHeroe(Heroe heroe, int enemigoIdx) {
        Enemigo enemigo = getEnemigoVivoPorIndice(enemigoIdx);
        if (enemigo != null) {
            heroe.provocarEnemigo(enemigo);
            return true;
        }
        return false;
    }

    private boolean curarConHeroe(Heroe heroe, int aliadoIdx) {
        Heroe aliado = getHeroeVivoPorIndice(aliadoIdx);
        if (aliado != null) {
            heroe.curar(aliado);
            return true;
        }
        return false;
    }

    private boolean restaurarManaConHeroe(Heroe heroe, int aliadoIdx) {
        Heroe aliado = getHeroeVivoPorIndice(aliadoIdx);
        if (aliado != null) {
            heroe.restaurarMana(aliado);
            return true;
        }
        return false;
    }

    private boolean eliminarEfectoConHeroe(Heroe heroe, int aliadoIdx) {
        Heroe aliado = getHeroeVivoPorIndice(aliadoIdx);
        if (aliado != null) {
            heroe.eliminarEfectoNegativo(aliado);
            return true;
        }
        return false;
    }

    private boolean revivirConHeroe(Heroe heroe, int muertoIdx) {
        Heroe aliado = getHeroeMuertoPorIndice(muertoIdx);
        if (aliado != null) {
            heroe.revivir(aliado);
            return true;
        }
        return false;
    }

    private boolean LanzaHechizoRefuerzo(Heroe heroe, int aliadoIdx){
        Heroe aliado = getHeroeVivoPorIndice(aliadoIdx);
        if (aliado != null){
            heroe.LanzaHechizoRefuerzo(aliado);
            return true;
        }
        return false;
    }

    private boolean LanzaHechizoDeSueño(Heroe heroe, int enemigoIdx){
        Enemigo objetivo = getEnemigoVivoPorIndice(enemigoIdx);
        if (objetivo != null){
            heroe.LanzaHechizoSueño(objetivo);
            return true;
        }
        return false;
    }

    private boolean LanzaHechizoParalisis(Heroe heroe, int enemigoIdx){
        Enemigo objetivo = getEnemigoVivoPorIndice(enemigoIdx);
        if (objetivo != null){
            heroe.LanzaHechizoParalisis(objetivo);
            return true;
        }
        return false;
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
            dqs.events.BattleEventBus.log("\n ¡Los Enemigos han ganado la batalla!");
            batalla.setBatallaTerminada(true);
            return true;
        } else if (!enemigosVivos) {
            dqs.events.BattleEventBus.log("\n ¡Los Héroes han ganado la batalla!");
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
