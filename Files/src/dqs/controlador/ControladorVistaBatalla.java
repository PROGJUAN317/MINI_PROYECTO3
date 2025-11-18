package dqs.controlador;

import dqs.modelos.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Controlador que orquesta la lógica de una batalla para la vista.
 *
 * Implementación inicial (esqueleto): calcula el orden de ataque, inicia
 * turnos y ejecuta los turnos de enemigos con un breve delay. La idea es
 * que la vista delegue aquí la responsabilidad de control y el controlador
 * use la interfaz `VistaBatalla` para actualizar la UI.
 */
public class ControladorVistaBatalla implements BattleEventListener {
    private final Batalla batalla;
    private final VistaBatalla vista;
    private final List<Personaje> ordenAtaque = new ArrayList<>();
    private int indiceTurnoActual = 0;
    // Estado del turno
    private Heroe heroeActuando = null;
    private AccionSeleccionada accionPendiente = AccionSeleccionada.NONE;

    public ControladorVistaBatalla(Batalla batalla, VistaBatalla vista) {
        this.batalla = batalla;
        this.vista = vista;
    }
    /** Inicia la orquestación de la batalla desde el controlador */
    public void iniciar() {
        construirOrdenAtaque();
    // Registrar un listener de consola para enrutar mensajes del modelo a la salida estándar
    // (modo CLI: una sola línea cambia entre GUI y consola)
        boolean CLI_MODE = false; // <-- Cambia a true para ejecutar SOLO en consola
    BattleEventBus.setListener(this);
        vista.appendLog("Orden de ataque calculada por Controlador:");
        for (Personaje p : ordenAtaque) vista.appendLog("- " + p.getNombre() + " (" + p.getVelocidad() + ")");
        // refrescar UI (llamada en EDT desde la vista si es necesario)
        if (CLI_MODE) {
            // Modo consola: enrutar eventos a la consola e iniciar la simulación
            BattleEventBus.setListener(new ConsoleBattleEventListener());
            // Ejecutar la simulación usando BatallaManager (entrada estándar)
            new dqs.servicio.BatallaManager(batalla, new java.util.Scanner(System.in)).ejecutarSimulacion();
            return; // No continuar con inicialización GUI
        } else {
            // Modo GUI: enrutar eventos al controlador para que los muestre en la vista
            BattleEventBus.setListener(this);
        }
        SwingUtilities.invokeLater(() -> vista.refreshUI());
        iniciarTurno();
    }

    @Override
    public void onEvent(String message) {
        // Garantizar que la actualización de la vista ocurra en el EDT
        SwingUtilities.invokeLater(() -> vista.appendLog(message));
    }

    private void construirOrdenAtaque() {
        ordenAtaque.clear();
        Heroe[] heroes = batalla.getEquipoHeroes();
        Enemigo[] enemigos = batalla.getEquipoEnemigos();
        if (heroes != null) for (Heroe h : heroes) if (h != null && h.esta_vivo()) ordenAtaque.add(h);
        if (enemigos != null) for (Enemigo e : enemigos) if (e != null && e.esta_vivo()) ordenAtaque.add(e);
        ordenAtaque.sort((a, b) -> b.getVelocidad() - a.getVelocidad());
        indiceTurnoActual = 0;
    }

    private void iniciarTurno() {
        if (ordenAtaque.isEmpty()) return;
        if (indiceTurnoActual < 0 || indiceTurnoActual >= ordenAtaque.size()) indiceTurnoActual = 0;
        Personaje personaje = ordenAtaque.get(indiceTurnoActual);
        vista.appendLog("---------------------");
        vista.appendLog("Turno de " + personaje.getNombre());

        if (personaje instanceof Heroe heroe) {
            // Notificar a la vista que habilite acciones según tipo
            this.heroeActuando = heroe;
            this.accionPendiente = AccionSeleccionada.NONE;
            Set<AccionSeleccionada> acciones = accionesParaTipo(heroe.getTipo());
            vista.habilitarAcciones(acciones);
            // La vista recogerá la acción del usuario y llamará seleccionarAccion
        } else if (personaje instanceof Enemigo enemigo) {
            // No hay héroe actuando durante el turno enemigo
            this.heroeActuando = null;
            ejecutarTurnoEnemigo(enemigo);
        }
    }

    private Set<AccionSeleccionada> accionesParaTipo(Tipo_Heroe tipo) {
        Set<AccionSeleccionada> s = new HashSet<>();
        // Acciones comunes
        s.add(AccionSeleccionada.ATTACK);
        s.add(AccionSeleccionada.PASS);
        switch (tipo) {
            case GUERRERO -> { s.add(AccionSeleccionada.DEFEND); s.add(AccionSeleccionada.PROVOKE); s.add(AccionSeleccionada.INCREASE_DEF); }
            case PALADIN -> { s.add(AccionSeleccionada.DEFEND); s.add(AccionSeleccionada.PROVOKE); s.add(AccionSeleccionada.REVIVE); s.add(AccionSeleccionada.REMOVE_EFFECT); }
            case DRUIDA -> { s.add(AccionSeleccionada.HEAL); s.add(AccionSeleccionada.RESTORE_MP); s.add(AccionSeleccionada.SLEEP); }
            case MAGO -> { s.add(AccionSeleccionada.SLEEP); s.add(AccionSeleccionada.REINFORCE); s.add(AccionSeleccionada.PARALYZE); }
            default -> {}
        }
        return s;
    }

    private void ejecutarTurnoEnemigo(Enemigo enemigo) {
        if (enemigo == null || !enemigo.esta_vivo()) { terminarTurno(); return; }
        vista.appendLog("Turno enemigo: " + enemigo.getNombre());
        Timer t = new Timer(700, ev -> {
            ((Timer) ev.getSource()).stop();
            if (enemigo instanceof JefeEnemigo jefe) {
                jefe.actuar(batalla.getEquipoHeroes());
            } else {
                enemigo.atacarAleatorio(batalla.getEquipoHeroes());
            }
            SwingUtilities.invokeLater(() -> vista.refreshUI());
            if (!comprobarVictoria()) {
                terminarTurno();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private boolean comprobarVictoria() {
        boolean heroesVivos = false, enemigosVivos = false;
        for (Heroe h : batalla.getEquipoHeroes()) if (h != null && h.esta_vivo()) { heroesVivos = true; break; }
        for (Enemigo e : batalla.getEquipoEnemigos()) if (e != null && e.esta_vivo()) { enemigosVivos = true; break; }
        if (!heroesVivos) { vista.appendLog("¡Los Enemigos han ganado la batalla!"); return true; }
        if (!enemigosVivos) { vista.appendLog("¡Los Héroes han ganado la batalla!"); return true; }
        return false;
    }

    private void terminarTurno() {
        if (comprobarVictoria()) return;
        indiceTurnoActual++;
        if (indiceTurnoActual >= ordenAtaque.size()) {
            vista.appendLog("------ NUEVA RONDA ------- ");
            construirOrdenAtaque();
            indiceTurnoActual = 0;
        }
        while (indiceTurnoActual < ordenAtaque.size() && !ordenAtaque.get(indiceTurnoActual).esta_vivo()) indiceTurnoActual++;
        if (indiceTurnoActual < ordenAtaque.size()) iniciarTurno(); else vista.appendLog("Se ha acabado el combate");
    }

    // API pública (esqueleto) para que la vista notifique acciones/selecciones
    public void seleccionarAccion(AccionSeleccionada accion) {
        vista.appendLog("Acción seleccionada: " + accion.name());
        if (accion == AccionSeleccionada.PASS) {
            terminarTurno();
            return;
        }

        if (heroeActuando == null) {
            vista.appendLog("No hay un héroe en turno para ejecutar la acción.");
            return;
        }

        switch (accion) {
            case ATTACK -> {
                // Esperar selección de enemigo
                accionPendiente = AccionSeleccionada.ATTACK;
                vista.appendLog("Selecciona un enemigo para atacar.");
            }
            case INCREASE_DEF -> {
                heroeActuando.aumentarDefensa(10);
                vista.appendLog(heroeActuando.getNombre() + " aumenta su defensa en 10.");
                SwingUtilities.invokeLater(() -> vista.refreshUI());
                terminarTurno();
            }
            default -> {
                // Para otras acciones que requieren objetivo, dejamos la acción
                // pendiente y esperamos a que la vista seleccione un aliado/enemigo.
                accionPendiente = accion;
                vista.appendLog("Acción pendiente: " + accion.name() + ". Selecciona objetivo.");
            }
        }
    }

    public void seleccionarObjetivoEnemigo(int idx) {
        vista.appendLog("Objetivo enemigo seleccionado: " + idx);
        if (accionPendiente == AccionSeleccionada.NONE) {
            vista.appendLog("No hay acción pendiente para aplicar sobre el enemigo.");
            return;
        }

        Enemigo[] enemigos = batalla.getEquipoEnemigos();
        if (enemigos == null || idx < 0 || idx >= enemigos.length) {
            vista.appendLog("Índice de enemigo inválido.");
            return;
        }

        Enemigo objetivo = enemigos[idx];
        if (objetivo == null || !objetivo.esta_vivo()) {
            vista.appendLog("Objetivo inválido o ya está muerto.");
            return;
        }

            if (accionPendiente == AccionSeleccionada.ATTACK) {
            if (heroeActuando == null) {
                vista.appendLog("No hay héroe para ejecutar el ataque.");
                accionPendiente = AccionSeleccionada.NONE;
                return;
            }
            heroeActuando.atacar(objetivo);
            accionPendiente = AccionSeleccionada.NONE;
            SwingUtilities.invokeLater(() -> vista.refreshUI());
            terminarTurno();
            return;
        }

        // Otras acciones sobre enemigos
        switch (accionPendiente) {
            case PROVOKE -> {
                if (heroeActuando != null) heroeActuando.provocarEnemigo(objetivo);
            }
            case SLEEP -> {
                if (heroeActuando != null) heroeActuando.LanzaHechizoSueño(objetivo);
            }
            case PARALYZE -> {
                if (heroeActuando != null) heroeActuando.LanzaHechizoParalisis(objetivo);
            }
            default -> vista.appendLog("Acción sobre enemigo no implementada: " + accionPendiente);
        }
        accionPendiente = AccionSeleccionada.NONE;
        SwingUtilities.invokeLater(() -> vista.refreshUI());
        terminarTurno();
    }

    public void seleccionarObjetivoAliado(int idx) {
        vista.appendLog("Objetivo aliado seleccionado: " + idx);
        if (accionPendiente == AccionSeleccionada.NONE && heroeActuando == null) {
            vista.appendLog("No hay acción pendiente o héroe en turno para aplicar sobre el aliado.");
            return;
        }

        Heroe[] heroes = batalla.getEquipoHeroes();
        if (heroes == null || idx < 0 || idx >= heroes.length) {
            vista.appendLog("Índice de aliado inválido.");
            return;
        }

        Heroe aliado = heroes[idx];
        if (aliado == null) { vista.appendLog("Aliado inválido."); return; }

        switch (accionPendiente) {
            case DEFEND -> {
                if (heroeActuando != null) heroeActuando.defender(aliado);
            }
            case HEAL -> {
                if (heroeActuando != null) heroeActuando.curar(aliado);
            }
            case RESTORE_MP -> {
                if (heroeActuando != null) heroeActuando.restaurarMana(aliado);
            }
            case REMOVE_EFFECT -> {
                if (heroeActuando != null) heroeActuando.eliminarEfectoNegativo(aliado);
            }
            case REVIVE -> {
                if (heroeActuando != null) heroeActuando.revivir(aliado);
            }
            case REINFORCE -> {
                if (heroeActuando != null) heroeActuando.LanzaHechizoRefuerzo(aliado);
            }
            default -> vista.appendLog("Acción sobre aliado no implementada: " + accionPendiente);
        }
        accionPendiente = AccionSeleccionada.NONE;
        SwingUtilities.invokeLater(() -> vista.refreshUI());
        terminarTurno();
    }

    public void pasarTurno() {
        vista.appendLog("Jugador pasa turno (controlador)");
        terminarTurno();
    }
}
