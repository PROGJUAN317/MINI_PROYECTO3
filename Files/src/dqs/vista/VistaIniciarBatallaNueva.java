package dqs.vista;
import dqs.modelos.*;
import dqs.controlador.AccionSeleccionada;
import dqs.controlador.ControladorVistaBatalla;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Vista única que inicia una batalla con héroes por defecto y enemigos aleatorios.
 * El jugador controla las acciones de los héroes desde la interfaz. Después de
 * que todos los héroes actúen, los enemigos realizan su turno automáticamente.
 */
public class VistaIniciarBatallaNueva extends JFrame implements dqs.controlador.VistaBatalla {


    // Nota: la orquestación de turnos y el orden de ataque se han movido
    // al controlador (ControladorVistaBatalla). Estos campos eran parte de
    // la implementación anterior de la vista y se han dejado fuera.


    private Batalla batalla;
    private ControladorVistaBatalla controlador;
    private JPanel panelEstado;
    private JTextArea areaLog;
    private JButton btnVolverMenu;
    private final JPanel panelAcciones;

    // Visuales por personaje
    private JPanel[] panelHeroesUI;
    private JLabel[] lblHeroeNombre;
    private JProgressBar[] barraHpHeroes;
    private JProgressBar[] barraMpHeroes;

    private JPanel[] panelEnemigosUI;
    private JLabel[] lblEnemigoNombre;
    private JProgressBar[] barraHpEnemigos;
    private JProgressBar[] barraMpEnemigos;


    // El controlador orquesta la selección de objetivos y acciones.
    // La vista ya no mantiene estado de acción/objetivo; se limita a renderizar
    // y notificar la selección al controlador.


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
                barraMpHeroes = new JProgressBar[heroes.length];

                panelEnemigosUI = new JPanel[enemigos.length];
                lblEnemigoNombre = new JLabel[enemigos.length];
                barraHpEnemigos = new JProgressBar[enemigos.length];
                barraMpEnemigos = new JProgressBar[enemigos.length];


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
                // Delegar la orquestación de turnos al controlador (MVC)
                this.controlador = new ControladorVistaBatalla(this.batalla, this);
                this.controlador.iniciar();

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

                JPanel panelBarras = new JPanel();
                panelBarras.setLayout(new BoxLayout(panelBarras, BoxLayout.Y_AXIS));
                panelBarras.setOpaque(false);

                JProgressBar barra = new JProgressBar(0,100);
                //barra.setValue(80);
                barra.setStringPainted(true);
                barra.setForeground(Color.BLUE);
                barra.setBackground(Color.DARK_GRAY);
                panelBarras.add(barra);

                

                JProgressBar barra2 = new JProgressBar(0,300);
                //barra2.setValue(60);
                barra2.setStringPainted(true);
                barra2.setForeground(Color.RED);
                barra2.setBackground(Color.DARK_GRAY);
                panelBarras.add(barra2);

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
                panel.add(panelBarras, BorderLayout.SOUTH);

                if (esHeroe) {
                    lblHeroeNombre[idx] = lblNombre;
                    barraHpHeroes[idx] = barra;
                    barraMpHeroes[idx] = barra2;
                    panel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            // Delegar la selección de aliado al controlador. El controlador
                            // decidirá si esa selección aplica según el flujo actual.
                            if (controlador != null) controlador.seleccionarObjetivoAliado(idx);
                        }
                    });
                } else {
                    lblEnemigoNombre[idx] = lblNombre;
                    barraHpEnemigos[idx] = barra;
                    barraMpEnemigos[idx] = barra2;
                    panel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            // Delegar la selección de enemigo al controlador.
                            if (controlador != null) controlador.seleccionarObjetivoEnemigo(idx);
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

                btnAtacar.addActionListener(e -> {
                    if (controlador != null) controlador.seleccionarAccion(AccionSeleccionada.ATTACK);
                });
                btnDefender.addActionListener(e -> {
                    if (controlador != null) controlador.seleccionarAccion(AccionSeleccionada.DEFEND);
                });
                btnProvocar.addActionListener(e -> {
                    if (controlador != null) controlador.seleccionarAccion(AccionSeleccionada.PROVOKE);
                });
                btnAumDef.addActionListener(e -> {
                    if (controlador != null) controlador.seleccionarAccion(AccionSeleccionada.INCREASE_DEF);
                });
                btnCurar.addActionListener(e -> {
                    if (controlador != null) controlador.seleccionarAccion(AccionSeleccionada.HEAL);
                });
                btnRestaurar.addActionListener(e -> {
                    if (controlador != null) controlador.seleccionarAccion(AccionSeleccionada.RESTORE_MP);
                });
                btnEliminarEfecto.addActionListener(e -> {
                    if (controlador != null) controlador.seleccionarAccion(AccionSeleccionada.REMOVE_EFFECT);
                });
                btnRevivir.addActionListener(e -> {
                    if (controlador != null) controlador.seleccionarAccion(AccionSeleccionada.REVIVE);
                });
                btnSueno.addActionListener(e -> {
                    if (controlador != null) controlador.seleccionarAccion(AccionSeleccionada.SLEEP);
                });
                btnRefuerzo.addActionListener(e -> {
                    if (controlador != null) controlador.seleccionarAccion(AccionSeleccionada.REINFORCE);
                });
                btnParalisis.addActionListener(e -> {
                    if (controlador != null) controlador.seleccionarAccion(AccionSeleccionada.PARALYZE);
                });
                btnPasar.addActionListener(e -> {
                    if (controlador != null) controlador.pasarTurno();
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
            // Selección de objetivos y ejecución de acciones ahora la maneja el controlador.

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
            // Selección de objetivos y ejecución de acciones ahora la maneja el controlador.

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
            // La ejecución de acciones sobre enemigos la realiza el controlador.

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
            // La ejecución de acciones sobre aliados la realiza el controlador.

            /*Funcion que mostrara el orden de ataque de los personajes. Este no siempre sera el mismo ya quw escoge 
             * X cantidad de enemigos de un enum con el tipo de enemigo los cuales varian sus estadisticas incluso la de velocidad
             * por lo que los turnos cambian mayormente por parte de los enemigos 
             */

            // Orden de ataque: ahora es responsabilidad del controlador.


            /*funcion para iniciar los turnos en base a la velocidad de los perosnajes */
            // El ciclo de inicio de turnos ahora está gestionado por el
            // `ControladorVistaBatalla`. La vista sólo renderiza y envía eventos
            // al controlador.


            // La vista ahora recibe desde el controlador un Set<AccionSeleccionada>
            // indicando qué botones habilitar; el método específico por tipo se
            // ha eliminado en favor del contrato con el controlador.

            /**
             * Implementación del método de la interfaz `VistaBatalla` que permite
             * al controlador indicar explícitamente qué acciones deben estar
             * habilitadas en la vista. Recibe un Set de `AccionSeleccionada` y
             * habilita/deshabilita los JButtons correspondientes.
             */
            @Override
            public void habilitarAcciones(Set<AccionSeleccionada> acciones) {
                if (panelAcciones == null) return;
                // Primero deshabilitar todo
                ((JButton) panelAcciones.getClientProperty("btnAtacar")).setEnabled(false);
                ((JButton) panelAcciones.getClientProperty("btnDefender")).setEnabled(false);
                ((JButton) panelAcciones.getClientProperty("btnProvocar")).setEnabled(false);
                ((JButton) panelAcciones.getClientProperty("btnAumDef")).setEnabled(false);
                ((JButton) panelAcciones.getClientProperty("btnCurar")).setEnabled(false);
                ((JButton) panelAcciones.getClientProperty("btnRestaurar")).setEnabled(false);
                ((JButton) panelAcciones.getClientProperty("btnEliminarEfecto")).setEnabled(false);
                ((JButton) panelAcciones.getClientProperty("btnRevivir")).setEnabled(false);
                ((JButton) panelAcciones.getClientProperty("btnSueno")).setEnabled(false);
                ((JButton) panelAcciones.getClientProperty("btnRefuerzo")).setEnabled(false);
                ((JButton) panelAcciones.getClientProperty("btnParalisis")).setEnabled(false);
                ((JButton) panelAcciones.getClientProperty("btnPasar")).setEnabled(false);

                if (acciones == null) return;
                for (AccionSeleccionada a : acciones) {
                    switch (a) {
                        case ATTACK -> ((JButton) panelAcciones.getClientProperty("btnAtacar")).setEnabled(true);
                        case DEFEND -> ((JButton) panelAcciones.getClientProperty("btnDefender")).setEnabled(true);
                        case PROVOKE -> ((JButton) panelAcciones.getClientProperty("btnProvocar")).setEnabled(true);
                        case INCREASE_DEF -> ((JButton) panelAcciones.getClientProperty("btnAumDef")).setEnabled(true);
                        case HEAL -> ((JButton) panelAcciones.getClientProperty("btnCurar")).setEnabled(true);
                        case RESTORE_MP -> ((JButton) panelAcciones.getClientProperty("btnRestaurar")).setEnabled(true);
                        case REMOVE_EFFECT -> ((JButton) panelAcciones.getClientProperty("btnEliminarEfecto")).setEnabled(true);
                        case REVIVE -> ((JButton) panelAcciones.getClientProperty("btnRevivir")).setEnabled(true);
                        case SLEEP -> ((JButton) panelAcciones.getClientProperty("btnSueno")).setEnabled(true);
                        case REINFORCE -> ((JButton) panelAcciones.getClientProperty("btnRefuerzo")).setEnabled(true);
                        case PARALYZE -> ((JButton) panelAcciones.getClientProperty("btnParalisis")).setEnabled(true);
                        case PASS -> ((JButton) panelAcciones.getClientProperty("btnPasar")).setEnabled(true);
                        default -> { /* NONE o acciones no mapeadas */ }
                    }
                }
            }

            // La gestión de turnos (terminarTurno, ejecutarTurnoHeroes, ejecutarTurnoEnemigos)
            // ya está implementada en `ControladorVistaBatalla`. La vista se limita a
            // renderizar y enviar eventos (selección de acción/objetivo) al
            // controlador.


            // Comprobación de victoria se delega al controlador; la vista no
            // necesita este método cuando sigue el patrón MVC.

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
            @Override
            public void refreshUI() {
                Heroe[] heroes = batalla.getEquipoHeroes();
                for (int i = 0; i < heroes.length; i++) {
                    Heroe h = heroes[i];
                    if (h != null) {
                        lblHeroeNombre[i].setText(h.getNombre() + " (" + h.getTipo().name() + ")");
                        int max = Math.max(1, h.getHp());
                        int max2 = Math.max(1,h.getMp());
                        barraHpHeroes[i].setMaximum(max);
                        barraHpHeroes[i].setValue(Math.max(0, h.getHp()));
                        barraHpHeroes[i].setString(h.getHp() + " HP");
                        barraMpHeroes[i].setMaximum(max2);
                        barraMpHeroes[i].setValue(Math.max(0, h.getMp()));
                        barraMpHeroes[i].setString(h.getMp() + "MP");
                    } else {
                        lblHeroeNombre[i].setText("Vacío");
                        barraHpHeroes[i].setValue(0);
                        barraMpHeroes[i].setValue(0);
                    }
                }

                Enemigo[] enemigos = batalla.getEquipoEnemigos();
                for (int i = 0; i < enemigos.length; i++) {
                    Enemigo e = enemigos[i];
                    if (e != null) {
                        lblEnemigoNombre[i].setText(e.getNombre() + " (" + e.getTipo().name() + ")");
                        int max = Math.max(1, e.getHp());
                        int max2 =  Math.max(1, e.getMp());
                        barraHpEnemigos[i].setMaximum(max);
                        barraHpEnemigos[i].setValue(Math.max(0, e.getHp()));
                        barraHpEnemigos[i].setString(e.getHp() + " HP");
                        barraMpEnemigos[i].setMaximum(max2);
                        barraMpEnemigos[i].setValue(Math.max(0, e.getMp()));
                        barraMpEnemigos[i].setString(e.getMp() + "MP");
                    } else {
                        lblEnemigoNombre[i].setText("Vacío");
                        barraHpEnemigos[i].setValue(0);
                        barraMpEnemigos[i].setValue(0);
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
            @Override
            public void appendLog(String linea) {
                SwingUtilities.invokeLater(() -> {
                    areaLog.append(linea + "\n");
                    areaLog.setCaretPosition(areaLog.getDocument().getLength());
                });
            }
        }
        

