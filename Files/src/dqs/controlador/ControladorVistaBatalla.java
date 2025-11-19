package dqs.controlador;

import dqs.modelos.*;
import dqs.events.BattleEventBus;
import dqs.events.BattleEventListener;
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
    private final dqs.servicio.BatallaManager manager;
    private final VistaBatalla vista;
    private final List<Personaje> ordenAtaque = new ArrayList<>();
    private int indiceTurnoActual = 0;
    private boolean cliMode = false;
    // Opciones actuales disponibles para el héroe en turno (texto tal como provisto por BatallaManager)
    private String[] opcionesActuales = null;
    // Estado del turno
    private Heroe heroeActuando = null;
    private AccionSeleccionada accionPendiente = AccionSeleccionada.NONE;

    public ControladorVistaBatalla(Batalla batalla, VistaBatalla vista) {
        this.batalla = batalla;
        this.manager = new dqs.servicio.BatallaManager(batalla);
        this.vista = vista;
    }

    /** Inicia la orquestación de la batalla desde el controlador */
    public void iniciar() {
        construirOrdenAtaque();
        // Detectar automáticamente si la vista es la implementación CLI
        this.cliMode = (vista instanceof dqs.vista.VistaBatallaCLI) || this.cliMode;
        vista.appendLog("Orden de ataque calculada por Controlador:");
        for (Personaje p : ordenAtaque) vista.appendLog("- " + p.getNombre() + " (" + p.getVelocidad() + ")");
        // Registrar listener en el bus: en CLI la vista misma escucha; en GUI el controlador escucha
        if (this.cliMode && vista instanceof dqs.events.BattleEventListener) {
            dqs.events.BattleEventBus.setListener((dqs.events.BattleEventListener) vista);
        } else {
            dqs.events.BattleEventBus.setListener(this);
        }
        SwingUtilities.invokeLater(() -> vista.refreshUI());
        iniciarTurno();
    }

    @Override
    public void onEvent(String message) {
        // Si estamos en modo CLI no necesitamos usar el EDT
        if (cliMode) {
            vista.appendLog(message);
        } else {
            // Garantizar que la actualización de la vista ocurra en el EDT
            SwingUtilities.invokeLater(() -> vista.appendLog(message));
        }
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
            // Obtener opciones desde BatallaManager y habilitar acciones según el enum local
            this.opcionesActuales = manager.obtenerOpcionesAccion(heroe);
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
        if (cliMode) {
            // Ejecutar sin timers en modo consola (síncrono)
            if (enemigo instanceof JefeEnemigo jefe) {
                jefe.actuar(batalla.getEquipoHeroes());
            } else {
                enemigo.atacarAleatorio(batalla.getEquipoHeroes());
            }
            vista.refreshUI();
            if (!comprobarVictoria()) {
                terminarTurno();
            }
            return;
        }

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
            // Delegar en BatallaManager
            int idx = opcionIndexParaAccion(accion);
            if (idx >= 0 && heroeActuando != null) {
                manager.ejecutarAccionHeroe(heroeActuando, idx, -1);
            }
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
                // Delegar en BatallaManager
                int idx = opcionIndexParaAccion(accion);
                if (idx >= 0 && heroeActuando != null) {
                    manager.ejecutarAccionHeroe(heroeActuando, idx, -1);
                    vista.appendLog(heroeActuando.getNombre() + " aumenta su defensa en 10.");
                    SwingUtilities.invokeLater(() -> vista.refreshUI());
                    terminarTurno();
                } else {
                    vista.appendLog("Acción no disponible: " + accion.name());
                }
            }
            default -> {
                // Para otras acciones que requieren objetivo, dejamos la acción
                // pendiente y esperamos a que la vista seleccione un aliado/enemigo.
                accionPendiente = accion;
                vista.appendLog("Acción pendiente: " + accion.name() + ". Selecciona objetivo.");
            }
        }
        // En modo CLI, pedir objetivo inmediatamente usando la vista (lectura bloqueante aceptable en CLI)
        if (cliMode && accionPendiente != AccionSeleccionada.NONE) {
            switch (accionPendiente) {
                case ATTACK, PROVOKE, SLEEP, PARALYZE -> {
                    String[] enemigos = manager.listarEnemigosVivos();
                    int sel = vista.solicitarSeleccion("Seleccione el enemigo:", enemigos);
                    if (sel >= 0) seleccionarObjetivoEnemigo(sel); else { accionPendiente = AccionSeleccionada.NONE; vista.appendLog("Selección cancelada."); }
                }
                case DEFEND, HEAL, RESTORE_MP, REMOVE_EFFECT, REINFORCE -> {
                    String[] aliados = manager.listarHeroesVivos();
                    int sel = vista.solicitarSeleccion("Seleccione el aliado:", aliados);
                    if (sel >= 0) seleccionarObjetivoAliado(sel); else { accionPendiente = AccionSeleccionada.NONE; vista.appendLog("Selección cancelada."); }
                }
                case REVIVE -> {
                    String[] muertos = manager.listarHeroesCaidos();
                    int sel = vista.solicitarSeleccion("Seleccione el aliado a revivir:", muertos);
                    if (sel >= 0) seleccionarObjetivoAliado(sel); else { accionPendiente = AccionSeleccionada.NONE; vista.appendLog("Selección cancelada."); }
                }
                default -> {}
            }
        }
    }

    /**
     * Devuelve las opciones textuales disponibles para el héroe que está en turno.
     * La vista CLI la usa para mostrar un menú más descriptivo.
     */
    public String[] getOpcionesParaHeroeActual() {
        if (heroeActuando == null) return new String[0];
        opcionesActuales = manager.obtenerOpcionesAccion(heroeActuando);
        return opcionesActuales == null ? new String[0] : opcionesActuales;
    }

    /**
     * Método auxiliar para que la vista seleccione una opción por índice
     * (basado en el array devuelto por getOpcionesParaHeroeActual()).
     */
    public void seleccionarAccionPorIndice(int opcionIdx) {
        String[] ops = getOpcionesParaHeroeActual();
        if (ops == null || opcionIdx < 0 || opcionIdx >= ops.length) {
            vista.appendLog("Opción inválida (índice fuera de rango).");
            return;
        }
        String opcion = ops[opcionIdx];
        // Mapear la cadena a AccionSeleccionada si es posible
        AccionSeleccionada accion = switch (opcion) {
            case "Atacar Enemigo" -> AccionSeleccionada.ATTACK;
            case "Defender Aliado" -> AccionSeleccionada.DEFEND;
            case "Provocar Enemigo" -> AccionSeleccionada.PROVOKE;
            case "Aumentar Defensa" -> AccionSeleccionada.INCREASE_DEF;
            case "Curar Aliado" -> AccionSeleccionada.HEAL;
            case "Restaurar Mana" -> AccionSeleccionada.RESTORE_MP;
            case "Eliminar Efecto Negativo" -> AccionSeleccionada.REMOVE_EFFECT;
            case "Revivir Aliado" -> AccionSeleccionada.REVIVE;
            case "Lanzar Hechizo de sueño" -> AccionSeleccionada.SLEEP;
            case "Lanzar Hechizo de refuerzo" -> AccionSeleccionada.REINFORCE;
            case "Lanzar Hechizo de parálisis" -> AccionSeleccionada.PARALYZE;
            case "Pasar Turno" -> AccionSeleccionada.PASS;
            default -> null;
        };

        if (accion != null) seleccionarAccion(accion);
        else vista.appendLog("Acción no reconocida: " + opcion);
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

            // Delegar ejecución a BatallaManager usando índices
            if (heroeActuando == null) {
                vista.appendLog("No hay héroe para ejecutar la acción.");
                accionPendiente = AccionSeleccionada.NONE;
                return;
            }
            int opcionIdx = opcionIndexParaAccion(accionPendiente);
            boolean ok = false;
            if (opcionIdx >= 0) {
                ok = manager.ejecutarAccionHeroe(heroeActuando, opcionIdx, idx);
            } else {
                vista.appendLog("Opción no disponible para este héroe: " + accionPendiente);
            }
            accionPendiente = AccionSeleccionada.NONE;
            SwingUtilities.invokeLater(() -> vista.refreshUI());
            if (ok) terminarTurno();
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

        // Delegar ejecución a BatallaManager usando índices
        if (heroeActuando == null) {
            vista.appendLog("No hay héroe para ejecutar la acción.");
            accionPendiente = AccionSeleccionada.NONE;
            return;
        }
        int opcionIdx = opcionIndexParaAccion(accionPendiente);
        boolean ok = false;
        if (opcionIdx >= 0) {
            ok = manager.ejecutarAccionHeroe(heroeActuando, opcionIdx, idx);
        } else {
            vista.appendLog("Opción no disponible para este héroe: " + accionPendiente);
        }
        accionPendiente = AccionSeleccionada.NONE;
        SwingUtilities.invokeLater(() -> vista.refreshUI());
        if (ok) terminarTurno();
    }

    /**
     * Busca en las `opcionesActuales` la opción correspondiente al enum
     * `AccionSeleccionada` y devuelve su índice; -1 si no existe.
     */
    private int opcionIndexParaAccion(AccionSeleccionada accion) {
        if (opcionesActuales == null && heroeActuando != null) {
            opcionesActuales = manager.obtenerOpcionesAccion(heroeActuando);
        }
        if (opcionesActuales == null) return -1;
        String buscada = switch (accion) {
            case ATTACK -> "Atacar Enemigo";
            case DEFEND -> "Defender Aliado";
            case PROVOKE -> "Provocar Enemigo";
            case INCREASE_DEF -> "Aumentar Defensa";
            case HEAL -> "Curar Aliado";
            case RESTORE_MP -> "Restaurar Mana";
            case REMOVE_EFFECT -> "Eliminar Efecto Negativo";
            case REVIVE -> "Revivir Aliado";
            case SLEEP -> "Lanzar Hechizo de sueño";
            case REINFORCE -> "Lanzar Hechizo de refuerzo";
            case PARALYZE -> "Lanzar Hechizo de parálisis";
            case PASS -> "Pasar Turno";
            default -> null;
        };
        if (buscada == null) return -1;
        for (int i = 0; i < opcionesActuales.length; i++) if (buscada.equals(opcionesActuales[i])) return i;
        return -1;
    }

    public void pasarTurno() {
        vista.appendLog("Jugador pasa turno (controlador)");
        terminarTurno();
    }
}
