package dqs.vista;
import dqs.modelos.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Vista única que inicia una batalla con héroes por defecto y enemigos aleatorios.
 * El jugador controla las acciones de los héroes desde la interfaz. Después de
 * que todos los héroes actúen, los enemigos realizan su turno automáticamente.
 */
public class VistaIniciarBatallaNueva extends JFrame {
    private Batalla batalla;
    private JPanel panelEstado;
    private JTextArea areaLog;
    private JButton btnVolverMenu;
    private final JPanel panelAcciones;

    // Visuales por personaje
    private final JPanel[] panelHeroesUI;
    private final JLabel[] lblHeroeNombre;
    private final JProgressBar[] barraHpHeroes;

    private final JPanel[] panelEnemigosUI;
    private final JLabel[] lblEnemigoNombre;
    private final JProgressBar[] barraHpEnemigos;

    // Turnos
    private int indiceHeroeActual = 0; // índice del héroe que debe actuar

    // Modo de selección de objetivo
    private enum TargetMode { NONE, SELECT_ENEMY, SELECT_ALLY, SELECT_REVIVE }
    private TargetMode targetMode = TargetMode.NONE;
    private Heroe heroeActuando = null;
    private ActionSeleccionada accionSeleccionada = ActionSeleccionada.NONE;

    private enum ActionSeleccionada {
        NONE, ATTACK, DEFEND, PROVOKE, INCREASE_DEF, HEAL, RESTORE_MP, REMOVE_EFFECT, REVIVE, SLEEP, REINFORCE, PARALYZE, PASS
    }

    /**
     * Carga un recurso de imagen desde el classpath y lo escala al tamaño
     * solicitado. Busca en el paquete `dqs.vista.utilidades` que contiene
     * las imágenes del proyecto.
     *
     * @param recurso ruta dentro del classpath (por ejemplo "/dqs/vista/utilidades/dragon.png")
     * @param w ancho en píxeles
     * @param h alto en píxeles
     * @return ImageIcon escalado, o null si no se encuentra el recurso.
     */
    private ImageIcon cargarIcono(String recurso, int w, int h) {
        try {
            // Intento por classpath primero
            java.net.URL url = getClass().getResource(recurso);
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }

            // Fallback forzado: buscar en la ruta de desarrollo exacta Files/src/dqs/vista/utilidades/
            String nombreArchivo = recurso;
            if (nombreArchivo.contains("/")) {
                nombreArchivo = nombreArchivo.substring(nombreArchivo.lastIndexOf('/') + 1);
            }
            String proyectoRoot = System.getProperty("user.dir");
            java.io.File f = new java.io.File(proyectoRoot + java.io.File.separator + "Files" + java.io.File.separator + "src" + java.io.File.separator + "dqs" + java.io.File.separator + "vista" + java.io.File.separator + "utilidades" + java.io.File.separator + nombreArchivo);
            if (f.exists()) {
                Image img = new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }

            appendLog("Recurso no encontrado en classpath ni en Files/src/...: " + recurso + " (buscado en " + f.getAbsolutePath() + ")");
            return null;
        } catch (Exception ex) {
            appendLog("No se pudo cargar imagen: " + recurso + " -> " + ex.getMessage());
            return null;
        }
    }

    public VistaIniciarBatallaNueva() {
        setTitle("Dragon Quest VIII - Batalla");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 700);

                // Inicializar componentes visuales
                panelEstado = new JPanel(new GridLayout(2, 4, 10, 10));
                panelEstado.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                areaLog = new JTextArea();
                areaLog.setEditable(false);
                areaLog.setFont(new Font("Consolas", Font.PLAIN, 12));
                areaLog.setBackground(new Color(12, 12, 18));
                areaLog.setForeground(Color.WHITE);

                JScrollPane scroll = new JScrollPane(areaLog);
                scroll.setPreferredSize(new Dimension(400, 200));

                btnVolverMenu = new JButton("Salir");
                btnVolverMenu.addActionListener(e -> dispose());

                JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                bottom.add(btnVolverMenu);

                add(panelEstado, BorderLayout.CENTER);
                add(scroll, BorderLayout.EAST);
                add(bottom, BorderLayout.SOUTH);

                // Crear batalla y equipos
                this.batalla = new Batalla();
                try {
                    this.batalla.crearEquipoHeroesPorDefecto();
                } catch (RuntimeException ex) {
                    // Si la creación por defecto falla (por validaciones en modelo),
                    // registrar el error en el log y continuar con equipos vacíos para
                    // permitir que la UI se inicie sin bloquear la aplicación.
                    appendLog("Advertencia: no se pudieron crear héroes por defecto: " + ex.getMessage());
                }

                try {
                    this.batalla.crearEquipoEnemigos();
                } catch (RuntimeException ex) {
                    appendLog("Advertencia: no se pudieron crear enemigos por defecto: " + ex.getMessage());
                }

                // Inicializar arrays UI acorde a tamaños del modelo
                Heroe[] heroes = batalla.getEquipoHeroes();
                Enemigo[] enemigos = batalla.getEquipoEnemigos();

                panelHeroesUI = new JPanel[heroes.length];
                lblHeroeNombre = new JLabel[heroes.length];
                barraHpHeroes = new JProgressBar[heroes.length];

                panelEnemigosUI = new JPanel[enemigos.length];
                lblEnemigoNombre = new JLabel[enemigos.length];
                barraHpEnemigos = new JProgressBar[enemigos.length];

                // Construir UI de héroes
                for (int i = 0; i < heroes.length; i++) {
                    JPanel p = crearPanelPersonaje(true, i);
                    panelHeroesUI[i] = p;
                    panelEstado.add(p);
                }

                // Construir UI de enemigos
                for (int i = 0; i < enemigos.length; i++) {
                    JPanel p = crearPanelPersonaje(false, i);
                    panelEnemigosUI[i] = p;
                    panelEstado.add(p);
                }

                // Panel derecho inferior de acciones
                this.panelAcciones = crearPanelAcciones();
                add(this.panelAcciones, BorderLayout.NORTH);

                refreshUI();

                // Empezar turno en el primer héroe vivo
                indiceHeroeActual = buscarSiguienteHeroeVivo(0);
                if (indiceHeroeActual >= 0) {
                    iniciarTurnoHeroe(indiceHeroeActual);
                }

                setVisible(true);
            }

            /**
             * Crea y devuelve un JPanel que representa a un personaje (héroe o enemigo).
             *
             * Comportamiento:
             * - Construye un panel con un icono centrado, una etiqueta de nombre arriba y
             *   una barra de vida en la parte inferior.
             * - Registra un MouseListener que actúa como selector de objetivo según el
             *   modo de selección actual: seleccionar aliado, seleccionar enemigo o
             *   revivir. Cuando se detecta un clic y el modo coincide, se delega en los
             *   métodos aplicarAccionSobreAliado/aplicarAccionSobreEnemigo.
             *
             * Efectos secundarios:
             * - Modifica los arreglos `lblHeroeNombre`/`lblEnemigoNombre` y
             *   `barraHpHeroes`/`barraHpEnemigos` para mantener referencias a los
             *   componentes creados.
             *
             * Parámetros:
             * - esHeroe: true si el panel corresponde a un héroe, false para enemigo.
             * - idx: índice dentro del arreglo correspondiente (se usa para localizar
             *   el objeto modelo en `batalla`).
             *
             * Retorna:
             * - JPanel configurado listo para añadirse a la interfaz.
             */
            private JPanel crearPanelPersonaje(boolean esHeroe, int idx) {
                JPanel panel = new JPanel(new BorderLayout());
                panel.setPreferredSize(new Dimension(200, 120));
                panel.setBackground(new Color(28, 28, 36));
                panel.setBorder(new LineBorder(esHeroe ? Color.CYAN : Color.RED, 2));

                JLabel lblNombre = new JLabel("", SwingConstants.CENTER);
                lblNombre.setForeground(Color.WHITE);
                lblNombre.setFont(new Font("Arial", Font.BOLD, 12));

                JProgressBar barra = new JProgressBar();
                barra.setStringPainted(true);

                JLabel lblIcon = new JLabel();
                lblIcon.setHorizontalAlignment(SwingConstants.CENTER);

                // Intentar cargar un icono representativo desde resources
                // Para jefes se usa "dragon.png" (siempre que exista en utilidades)
                if (esHeroe) {
                    Heroe h = batalla.getEquipoHeroes()[idx];
                    if (h != null) {
                        // Mapear tipos de héroe a imágenes (nombres aproximados en la carpeta utilidades)
                        switch (h.getTipo()) {
                            case MAGO -> lblIcon.setIcon(cargarIcono("/dqs/vista/utilidades/Un mago de videojueg.png", 80, 80));
                            case GUERRERO -> lblIcon.setIcon(cargarIcono("/dqs/vista/utilidades/Un guerrero de video.png", 80, 80));
                            case PALADIN -> lblIcon.setIcon(cargarIcono("/dqs/vista/utilidades/Un paladín de videoj.png", 80, 80));
                            case DRUIDA -> lblIcon.setIcon(cargarIcono("/dqs/vista/utilidades/Un druida teriantrop.png", 80, 80));
                            default -> lblIcon.setIcon(null);
                        }
                    }
                } else {
                    Enemigo e = batalla.getEquipoEnemigos()[idx];
                    if (e != null) {
                        // Si es jefe, preferimos la imagen del dragón
                        if (e instanceof JefeEnemigo) {
                            lblIcon.setIcon(cargarIcono("/dqs/vista/utilidades/dragon.png", 100, 100));
                        } else {
                            // Mapear algunos tipos de enemigo comunes a imágenes disponibles
                            String nombreTipo = e.getTipo().name().toLowerCase();
                            if (nombreTipo.contains("orco")) {
                                lblIcon.setIcon(cargarIcono("/dqs/vista/utilidades/dibuja un orco.png", 100, 100));
                            } else if (nombreTipo.contains("troll")) {
                                lblIcon.setIcon(cargarIcono("/dqs/vista/utilidades/dibuja un troll.png", 100, 100));
                            } else if (nombreTipo.contains("golem") || nombreTipo.contains("golem")) {
                                lblIcon.setIcon(cargarIcono("/dqs/vista/utilidades/golem de piedra para.png", 100, 100));
                            } else if (nombreTipo.contains("dragon") || nombreTipo.contains("rey_dragon") ) {
                                lblIcon.setIcon(cargarIcono("/dqs/vista/utilidades/dragon.png", 100, 100));
                            } else {
                                // recurso genérico si no se encuentra mapping
                                lblIcon.setIcon(cargarIcono("/dqs/vista/utilidades/dragon.png", 100, 100));
                            }
                        }
                    }
                }

                panel.add(lblIcon, BorderLayout.CENTER);
                panel.add(lblNombre, BorderLayout.NORTH);
                panel.add(barra, BorderLayout.SOUTH);

                if (esHeroe) {
                    lblHeroeNombre[idx] = lblNombre;
                    barraHpHeroes[idx] = barra;
                    panel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            // Si estamos en modo elegir aliado (curar/defensa/revive)
                            if (targetMode == TargetMode.SELECT_ALLY && heroeActuando != null) {
                                Heroe aliado = batalla.getEquipoHeroes()[idx];
                                if (aliado != null) aplicarAccionSobreAliado(aliado);
                            } else if (targetMode == TargetMode.SELECT_REVIVE && heroeActuando != null) {
                                Heroe candidato = batalla.getEquipoHeroes()[idx];
                                if (candidato != null && !candidato.esta_vivo()) aplicarAccionSobreAliado(candidato);
                            }
                        }
                    });
                } else {
                    lblEnemigoNombre[idx] = lblNombre;
                    barraHpEnemigos[idx] = barra;
                    panel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (targetMode == TargetMode.SELECT_ENEMY && heroeActuando != null) {
                                Enemigo objetivo = batalla.getEquipoEnemigos()[idx];
                                if (objetivo != null && objetivo.esta_vivo()) aplicarAccionSobreEnemigo(objetivo);
                            }
                        }
                    });
                }

                return panel;
            }

            /**
             * Construye el panel de acciones con todos los botones disponibles para
             * los héroes. Los botones activan modos de selección o ejecutan acciones
             * inmediatas y se almacenan como propiedades del panel para poder
             * habilitarlos/deshabilitarlos dinámicamente.
             *
             * Retorna:
             * - JPanel con la botonera totalmente configurada.
             */
            private JPanel crearPanelAcciones() {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                panel.setBackground(new Color(20, 20, 30));

                JButton btnAtacar = new JButton("Atacar");
                JButton btnDefender = new JButton("Defender");
                JButton btnProvocar = new JButton("Provocar");
                JButton btnAumDef = new JButton("Aumentar Def");
                JButton btnCurar = new JButton("Curar");
                JButton btnRestaurar = new JButton("Restaurar MP");
                JButton btnEliminarEfecto = new JButton("Eliminar Efecto");
                JButton btnRevivir = new JButton("Revivir");
                JButton btnSueno = new JButton("Hechizo Sueño");
                JButton btnRefuerzo = new JButton("Refuerzo");
                JButton btnParalisis = new JButton("Parálisis");
                JButton btnPasar = new JButton("Pasar Turno");

                panel.add(btnAtacar);
                panel.add(btnDefender);
                panel.add(btnProvocar);
                panel.add(btnAumDef);
                panel.add(btnCurar);
                panel.add(btnRestaurar);
                panel.add(btnEliminarEfecto);
                panel.add(btnRevivir);
                panel.add(btnSueno);
                panel.add(btnRefuerzo);
                panel.add(btnParalisis);
                panel.add(btnPasar);

                btnAtacar.addActionListener(e -> pedirSeleccionEnemigo(ActionSeleccionada.ATTACK));
                btnDefender.addActionListener(e -> pedirSeleccionAliado(ActionSeleccionada.DEFEND));
                btnProvocar.addActionListener(e -> pedirSeleccionEnemigo(ActionSeleccionada.PROVOKE));
                btnAumDef.addActionListener(e -> {
                    if (heroeActuando != null) {
                        heroeActuando.aumentarDefensa(10);
                        appendLog(heroeActuando.getNombre() + " aumenta su defensa en 10.");
                        terminarTurnoHeroe();
                    }
                });
                btnCurar.addActionListener(e -> pedirSeleccionAliado(ActionSeleccionada.HEAL));
                btnRestaurar.addActionListener(e -> pedirSeleccionAliado(ActionSeleccionada.RESTORE_MP));
                btnEliminarEfecto.addActionListener(e -> pedirSeleccionAliado(ActionSeleccionada.REMOVE_EFFECT));
                btnRevivir.addActionListener(e -> pedirSeleccionAliado(ActionSeleccionada.REVIVE));
                btnSueno.addActionListener(e -> pedirSeleccionEnemigo(ActionSeleccionada.SLEEP));
                btnRefuerzo.addActionListener(e -> pedirSeleccionAliado(ActionSeleccionada.REINFORCE));
                btnParalisis.addActionListener(e -> pedirSeleccionEnemigo(ActionSeleccionada.PARALYZE));
                btnPasar.addActionListener(e -> {
                    if (heroeActuando != null) appendLog(heroeActuando.getNombre() + " pasa el turno.");
                    terminarTurnoHeroe();
                });

                // Guardar botones para habilitarlos según tipo
                panel.putClientProperty("btnAtacar", btnAtacar);
                panel.putClientProperty("btnDefender", btnDefender);
                panel.putClientProperty("btnProvocar", btnProvocar);
                panel.putClientProperty("btnAumDef", btnAumDef);
                panel.putClientProperty("btnCurar", btnCurar);
                panel.putClientProperty("btnRestaurar", btnRestaurar);
                panel.putClientProperty("btnEliminarEfecto", btnEliminarEfecto);
                panel.putClientProperty("btnRevivir", btnRevivir);
                panel.putClientProperty("btnSueno", btnSueno);
                panel.putClientProperty("btnRefuerzo", btnRefuerzo);
                panel.putClientProperty("btnParalisis", btnParalisis);
                panel.putClientProperty("btnPasar", btnPasar);

                return panel;
            }

            /**
             * Prepara la interfaz para seleccionar un enemigo como objetivo de la
             * acción indicada.
             *
             * Parámetros:
             * - accion: enumeración que indica qué acción se realizará sobre el
             *   enemigo seleccionado (ATTACK, PROVOKE, SLEEP, PARALYZE, ...).
             *
             * Efectos secundarios:
             * - Cambia `accionSeleccionada` y `targetMode` a SELECT_ENEMY.
             * - Añade una línea al log informando al jugador que debe seleccionar
             *   un enemigo.
             */
            private void pedirSeleccionEnemigo(ActionSeleccionada accion) {
                if (heroeActuando == null) return;
                this.accionSeleccionada = accion;
                this.targetMode = TargetMode.SELECT_ENEMY;
                appendLog("Selecciona un enemigo para: " + accion.name());
            }

            /**
             * Prepara la interfaz para seleccionar un aliado como objetivo de la
             * acción indicada (curar, restaurar MP, revivir, etc.).
             *
             * Parámetros:
             * - accion: enumeración con la acción a realizar sobre el aliado.
             *
             * Efectos secundarios:
             * - Cambia `accionSeleccionada` y `targetMode` a SELECT_ALLY o
             *   SELECT_REVIVE (si la acción es REVIVE).
             * - Añade una línea al log instructiva.
             */
            private void pedirSeleccionAliado(ActionSeleccionada accion) {
                if (heroeActuando == null) return;
                this.accionSeleccionada = accion;
                if (accion == ActionSeleccionada.REVIVE) {
                    this.targetMode = TargetMode.SELECT_REVIVE;
                    appendLog("Selecciona un aliado caído para revivir.");
                } else {
                    this.targetMode = TargetMode.SELECT_ALLY;
                    appendLog("Selecciona un aliado para: " + accion.name());
                }
            }

            /**
             * Ejecuta la acción actualmente seleccionada por el héroe en el
             * objetivo enemigo provisto.
             *
             * Reglas y comportamiento:
             * - Comprueba que exista `heroeActuando` y que el `objetivo` no sea
             *   nulo. Si no se cumple, no hace nada.
             * - Según `accionSeleccionada` llama a los métodos del modelo
             *   (`atacar`, `provocarEnemigo`, `LanzaHechizoSueño`, ...).
             * - Registra textos descriptivos en el log.
             * - Resetea `accionSeleccionada` y `targetMode` al finalizar.
             * - Actualiza la UI con `refreshUI()` y termina el turno del héroe
             *   llamando a `terminarTurnoHeroe()`.
             *
             * Parámetros:
             * - objetivo: instancia de `Enemigo` que recibirá la acción.
             */
            private void aplicarAccionSobreEnemigo(Enemigo objetivo) {
                if (heroeActuando == null || objetivo == null) return;

                switch (accionSeleccionada) {
                    case ATTACK -> {
                        heroeActuando.atacar(objetivo);
                        appendLog(heroeActuando.getNombre() + " ataca a " + objetivo.getNombre());
                    }
                    case PROVOKE -> {
                        heroeActuando.provocarEnemigo(objetivo);
                        appendLog(heroeActuando.getNombre() + " provoca a " + objetivo.getNombre());
                    }
                    case SLEEP -> {
                        heroeActuando.LanzaHechizoSueño(objetivo);
                        appendLog(heroeActuando.getNombre() + " lanza sueño sobre " + objetivo.getNombre());
                    }
                    case PARALYZE -> {
                        heroeActuando.LanzaHechizoParalisis(objetivo);
                        appendLog(heroeActuando.getNombre() + " lanza parálisis sobre " + objetivo.getNombre());
                    }
                    default -> appendLog("Acción no soportada sobre enemigo: " + accionSeleccionada);
                }

                accionSeleccionada = ActionSeleccionada.NONE;
                targetMode = TargetMode.NONE;

                refreshUI();
                terminarTurnoHeroe();
            }

            /**
             * Ejecuta la acción seleccionada por el héroe en un aliado del equipo.
             *
             * Comportamiento:
             * - Valida `heroeActuando` y `aliado`.
             * - Según `accionSeleccionada` delega en los métodos del modelo
             *   (defender, curar, restaurarMana, revivir, etc.).
             * - Es idempotente respecto a la selección: si la acción no aplica al
             *   aliado o al héroe se registra un mensaje.
             * - Al final resetea `accionSeleccionada` y `targetMode`, refresca la
             *   UI y termina el turno del héroe.
             *
             * Parámetros:
             * - aliado: instancia de `Heroe` que será afectada por la acción.
             */
            private void aplicarAccionSobreAliado(Heroe aliado) {
                if (heroeActuando == null || aliado == null) return;

                switch (accionSeleccionada) {
                    case DEFEND -> {
                        heroeActuando.defender(aliado);
                        appendLog(heroeActuando.getNombre() + " defiende a " + aliado.getNombre());
                    }
                    case HEAL -> {
                        heroeActuando.curar(aliado);
                        appendLog(heroeActuando.getNombre() + " cura a " + aliado.getNombre());
                    }
                    case RESTORE_MP -> {
                        heroeActuando.restaurarMana(aliado);
                        appendLog(heroeActuando.getNombre() + " restaura MP a " + aliado.getNombre());
                    }
                    case REMOVE_EFFECT -> {
                        heroeActuando.eliminarEfectoNegativo(aliado);
                        appendLog(heroeActuando.getNombre() + " elimina efectos negativos de " + aliado.getNombre());
                    }
                    case REVIVE -> {
                        heroeActuando.revivir(aliado);
                        appendLog(heroeActuando.getNombre() + " revive a " + aliado.getNombre());
                    }
                    case REINFORCE -> {
                        heroeActuando.LanzaHechizoRefuerzo(aliado);
                        appendLog(heroeActuando.getNombre() + " lanza refuerzo sobre " + aliado.getNombre());
                    }
                    default -> appendLog("Acción no soportada sobre aliado: " + accionSeleccionada);
                }

                accionSeleccionada = ActionSeleccionada.NONE;
                targetMode = TargetMode.NONE;

                refreshUI();
                terminarTurnoHeroe();
            }

            /**
             * Inicia el turno del héroe en la posición `indice` del equipo.
             *
             * Lógica:
             * - Si el índice está fuera de rango o no hay héroe/vivo en esa
             *   posición, busca el siguiente héroe vivo.
             * - Asigna `heroeActuando` y escribe en el log qué héroe está en turno.
             * - Habilita las acciones disponibles según el tipo del héroe llamando
             *   a `habilitarAccionesSegunTipo()`.
             *
             * Parámetros:
             * - indice: posición en el array de héroes que debe actuar.
             */
            private void iniciarTurnoHeroe(int indice) {
                Heroe[] heroes = batalla.getEquipoHeroes();
                if (indice < 0 || indice >= heroes.length) return;
                heroeActuando = heroes[indice];
                if (heroeActuando == null || !heroeActuando.esta_vivo()) {
                    indiceHeroeActual = buscarSiguienteHeroeVivo(indice + 1);
                    if (indiceHeroeActual >= 0) iniciarTurnoHeroe(indiceHeroeActual);
                    return;
                }

                appendLog("Turno del héroe: " + heroeActuando.getNombre() + " [" + heroeActuando.getTipo().name() + "]");
                habilitarAccionesSegunTipo(heroeActuando.getTipo());
            }

            /**
             * Habilita o deshabilita los botones de la botonera (`panelAcciones`)
             * según el tipo de héroe que está en turno.
             *
             * Reglas:
             * - Guerrero/Paladín: pueden defender/provocar/aumentar defensa.
             * - Druida/Paladín: pueden curar y eliminar efectos.
             * - Mago/Druida: pueden lanzar hechizos (sueño/refuerzo/parálisis según caso).
             *
             * Parámetros:
             * - tipo: valor de `Tipo_Heroe` que condiciona qué botones estarán
             *   disponibles.
             */
            private void habilitarAccionesSegunTipo(Tipo_Heroe tipo) {
                JPanel panel = this.panelAcciones;
                if (panel == null) return;

                ((JButton) panel.getClientProperty("btnAtacar")).setEnabled(true);
                ((JButton) panel.getClientProperty("btnDefender")).setEnabled(tipo == Tipo_Heroe.GUERRERO || tipo == Tipo_Heroe.PALADIN);
                ((JButton) panel.getClientProperty("btnProvocar")).setEnabled(tipo == Tipo_Heroe.GUERRERO || tipo == Tipo_Heroe.PALADIN);
                ((JButton) panel.getClientProperty("btnAumDef")).setEnabled(tipo == Tipo_Heroe.GUERRERO || tipo == Tipo_Heroe.PALADIN);
                ((JButton) panel.getClientProperty("btnCurar")).setEnabled(tipo == Tipo_Heroe.DRUIDA || tipo == Tipo_Heroe.PALADIN);
                ((JButton) panel.getClientProperty("btnRestaurar")).setEnabled(tipo == Tipo_Heroe.DRUIDA);
                ((JButton) panel.getClientProperty("btnEliminarEfecto")).setEnabled(tipo == Tipo_Heroe.DRUIDA || tipo == Tipo_Heroe.PALADIN);
                ((JButton) panel.getClientProperty("btnRevivir")).setEnabled(tipo == Tipo_Heroe.PALADIN);
                ((JButton) panel.getClientProperty("btnSueno")).setEnabled(tipo == Tipo_Heroe.DRUIDA || tipo == Tipo_Heroe.MAGO);
                ((JButton) panel.getClientProperty("btnRefuerzo")).setEnabled(tipo == Tipo_Heroe.MAGO);
                ((JButton) panel.getClientProperty("btnParalisis")).setEnabled(tipo == Tipo_Heroe.MAGO);
                ((JButton) panel.getClientProperty("btnPasar")).setEnabled(true);
            }

            /**
             * Finaliza el turno del héroe actual y determina el siguiente paso:
             * - Si existe otro héroe vivo, inicia su turno.
             * - Si todos los héroes han actuado, lanza `ejecutarTurnoEnemigos()`
             *   para procesar las acciones enemigas automáticamente.
             */
            private void terminarTurnoHeroe() {
                indiceHeroeActual = buscarSiguienteHeroeVivo(indiceHeroeActual + 1);
                heroeActuando = null;
                if (indiceHeroeActual >= 0) {
                    iniciarTurnoHeroe(indiceHeroeActual);
                } else {
                    ejecutarTurnoEnemigos();
                }
            }

            /**
             * Busca y retorna el índice del siguiente héroe vivo comenzando desde
             * la posición `desde`.
             *
             * Retorna:
             * - Índice del héroe vivo encontrado, o -1 si no hay ninguno.
             */
            private int buscarSiguienteHeroeVivo(int desde) {
                Heroe[] heroes = batalla.getEquipoHeroes();
                for (int i = desde; i < heroes.length; i++) {
                    if (heroes[i] != null && heroes[i].esta_vivo()) return i;
                }
                return -1;
            }

            /**
             * Ejecuta el turno de los enemigos en un hilo background (SwingWorker)
             * para no bloquear la EDT.
             *
             * Flujo:
             * - Itera los enemigos vivos y llama a su método de acción
             *   (`actuar` en jefes o `atacarAleatorio` en normales).
             * - Tras cada acción duerme brevemente (Thread.sleep) y solicita
             *   refrescar la UI en la EDT.
             * - Si detecta victoria durante el proceso, aborta el resto de
             *   acciones.
             * - Al terminar, reinicia el flujo de turnos pasando de nuevo a los
             *   héroes (buscando el primer héroe vivo).
             *
             * Nota: maneja excepciones y registra errores en el log.
             */
            private void ejecutarTurnoEnemigos() {
                appendLog("--- Turno de los enemigos ---");

                // Usar un Timer para espaciar las acciones de los enemigos en lugar de
                // bloquear el hilo con Thread.sleep dentro de un bucle.
                Enemigo[] enemigos = batalla.getEquipoEnemigos();
                final int[] indice = {0};

                javax.swing.Timer timer = new javax.swing.Timer(600, null);
                timer.addActionListener(ev -> {
                    // Avanzar hasta el siguiente enemigo vivo
                    while (indice[0] < enemigos.length && (enemigos[indice[0]] == null || !enemigos[indice[0]].esta_vivo())) {
                        indice[0]++;
                    }

                    if (indice[0] >= enemigos.length) {
                        // Terminar el turno de enemigos y pasar el turno a los héroes
                        timer.stop();
                        indiceHeroeActual = buscarSiguienteHeroeVivo(0);
                        if (indiceHeroeActual >= 0) iniciarTurnoHeroe(indiceHeroeActual);
                        return;
                    }

                    Enemigo e = enemigos[indice[0]];
                    appendLog("El enemigo " + e.getNombre() + " está actuando...");
                    if (e instanceof JefeEnemigo jefeEnemigo) {
                        jefeEnemigo.actuar(batalla.getEquipoHeroes());
                    } else {
                        e.atacarAleatorio(batalla.getEquipoHeroes());
                    }

                    // Refrescar la UI y comprobar victoria después de la acción
                    refreshUI();
                    if (comprobarVictoria()) {
                        timer.stop();
                        return;
                    }

                    // Prepararse para el siguiente enemigo en el siguiente tick
                    indice[0]++;
                });

                timer.setInitialDelay(0);
                timer.start();
            }

            /**
             * Comprueba si alguno de los bandos (héroes o enemigos) ha quedado sin
             * miembros vivos, y registra el resultado en el log.
             *
             * Retorna:
             * - true si hay un ganador (batalla finalizada), false en caso contrario.
             */
            private boolean comprobarVictoria() {
                boolean heroesVivos = false, enemigosVivos = false;
                for (Heroe h : batalla.getEquipoHeroes()) if (h != null && h.esta_vivo()) { heroesVivos = true; break; }
                for (Enemigo e : batalla.getEquipoEnemigos()) if (e != null && e.esta_vivo()) { enemigosVivos = true; break; }

                if (!heroesVivos) {
                    appendLog("¡Los Enemigos han ganado la batalla!");
                    return true;
                } else if (!enemigosVivos) {
                    appendLog("¡Los Héroes han ganado la batalla!");
                    return true;
                }
                return false;
            }

            /**
             * Actualiza las vistas de estado (nombres y barras de vida) para todos
             * los héroes y enemigos en pantalla.
             *
             * Implementación actual:
             * - Usa `getHp()` como máximo cuando no existe `getMaxHp()` en los
             *   modelos (se recomienda añadir getters de máximos para mostrar
             *   porcentajes correctos).
             * - Actualiza el texto de la barra con el número de HP actual.
             *
             * Efectos secundarios: modifica los componentes UI existentes en
             * `lblHeroeNombre`, `barraHpHeroes`, `lblEnemigoNombre`, `barraHpEnemigos`.
             */
            private void refreshUI() {
                Heroe[] heroes = batalla.getEquipoHeroes();
                for (int i = 0; i < heroes.length; i++) {
                    Heroe h = heroes[i];
                    if (h != null) {
                        lblHeroeNombre[i].setText(h.getNombre() + " (" + h.getTipo().name() + ")");
                        int max = Math.max(1, h.getHp());
                        barraHpHeroes[i].setMaximum(max);
                        barraHpHeroes[i].setValue(Math.max(0, h.getHp()));
                        barraHpHeroes[i].setString(h.getHp() + " HP");
                    } else {
                        lblHeroeNombre[i].setText("Vacío");
                        barraHpHeroes[i].setValue(0);
                    }
                }

                Enemigo[] enemigos = batalla.getEquipoEnemigos();
                for (int i = 0; i < enemigos.length; i++) {
                    Enemigo e = enemigos[i];
                    if (e != null) {
                        lblEnemigoNombre[i].setText(e.getNombre() + " (" + e.getTipo().name() + ")");
                        int max = Math.max(1, e.getHp());
                        barraHpEnemigos[i].setMaximum(max);
                        barraHpEnemigos[i].setValue(Math.max(0, e.getHp()));
                        barraHpEnemigos[i].setString(e.getHp() + " HP");
                    } else {
                        lblEnemigoNombre[i].setText("Vacío");
                        barraHpEnemigos[i].setValue(0);
                    }
                }
            }

            /**
             * Añade una línea al registro de la batalla (`areaLog`) en la EDT.
             *
             * Acepta una cadena `linea` y la concatena con un salto de línea.
             * Garantiza que la modificación del componente Swing se haga en el
             * hilo de despacho de eventos usando `SwingUtilities.invokeLater`.
             *
             * Parámetros:
             * - linea: texto a añadir al log.
             */
            private void appendLog(String linea) {
                SwingUtilities.invokeLater(() -> {
                    areaLog.append(linea + "\n");
                    areaLog.setCaretPosition(areaLog.getDocument().getLength());
                });
            }
        }