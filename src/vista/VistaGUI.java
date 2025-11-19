package vista;

import modelo.*;
import java.awt.*;

import javax.swing.*;

import controlador.ControladorBatalla;



import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class VistaGUI extends JFrame implements VistaJuego{

    private JFrame frame;

    private ControladorBatalla controlador;

    private Map<Heroe, Integer> hpMaxHeroes = new HashMap<>();
    private Map<Enemigo, Integer> hpMaxEnemigos = new HashMap<>();

    private Map<Heroe, JProgressBar> barrasHeroes = new HashMap<>();
    private Map<Enemigo, JProgressBar> barrasEnemigos = new HashMap<>();

    private JPanel panelHeroes;
    private JPanel panelEnemigos;
    private JTextArea salida;
    private JScrollPane scroll;
    
    private JButton btnatacar;
    private JButton btnhabilidad;
    private JPanel panelEstado;
    private JTextArea areaLog;
    
    private JButton btnVolverMenu;
    private int accionElegida = -1;
    private int enemigoElegido = -1;

    private final Object lockAccion = new Object();
    private final Object lockEnemigo = new Object();


    public VistaGUI(){

        //cremaos lo que tendra nuestra interfaz
        setTitle("Dragon Quest VIII");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        

        salida = new JTextArea();
        salida.setEditable(false);
        salida.setFont(new Font("Consolas", Font.PLAIN, 14));
        salida.setBackground(new Color(30, 30, 30));
        salida.setForeground(Color.WHITE);

        JScrollPane scrollSalida = new JScrollPane(salida);
        scrollSalida.setPreferredSize(new Dimension(400, 180));
    

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

                add(scroll, BorderLayout.EAST);

                panelHeroes = new JPanel();
                panelHeroes.setLayout(new GridLayout(3, 1));
                panelHeroes.setBorder(BorderFactory.createTitledBorder("Heroes"));
                panelHeroes.setPreferredSize(new Dimension(250, 600));

                add(panelHeroes, BorderLayout.WEST);

                panelEnemigos = new JPanel();
                panelEnemigos.setLayout(new GridLayout(3, 1));
                panelEnemigos.setBorder(BorderFactory.createTitledBorder("Enemigos"));
                panelEnemigos.setPreferredSize(new Dimension(250, 600));

                add(panelEnemigos, BorderLayout.CENTER);

                JPanel panelBotones = new JPanel();

                btnatacar = new JButton("Atacar");
                btnhabilidad = new JButton("Habilidad");

                panelBotones.add(btnatacar);
                panelBotones.add(btnhabilidad);

                add(panelBotones, BorderLayout.SOUTH);

                btnVolverMenu = new JButton("Salir");
                panelBotones.add(btnVolverMenu);
                btnVolverMenu.addActionListener(e -> dispose());

                btnatacar.addActionListener(e -> elegir(1));
                btnhabilidad.addActionListener(e -> elegir(2));

                setVisible(true);

                btnVolverMenu = new JButton("Salir");
                btnVolverMenu.addActionListener(e -> dispose());

                JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                bottom.add(btnVolverMenu);

                add(panelEstado, BorderLayout.CENTER);
                add(scroll, BorderLayout.EAST);
                add(bottom, BorderLayout.SOUTH);

        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    // crea el panel heroes mas no lo muestra
    private void crearPanelHeroes(Heroe h){

        panelHeroes = new JPanel();
        panelHeroes.setLayout(new GridLayout(3, 1));
        panelHeroes.setBorder(BorderFactory.createTitledBorder("Heroes"));
        panelHeroes.setPreferredSize(new Dimension(300, 200));

    }
    
    // esta funcion si muestra a los heroes creados
    private JPanel generarHeroe(Heroe h){

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        card.setBackground(new Color(30,30,30)); 

        JLabel nombre = new JLabel(h.getNombre() + " //velocidad: " + h.getVelocidad() );
        nombre.setForeground(Color.WHITE);
        nombre.setFont(new Font("Arial", Font.BOLD, 12));

        hpMaxHeroes.putIfAbsent(h, h.getHp());
        int hpMax = hpMaxHeroes.get(h);

        JProgressBar vida = new JProgressBar(0, hpMax);
        vida.setValue(h.getHp());
        vida.setStringPainted(true);

        barrasHeroes.put(h, vida);

        card.add(nombre, BorderLayout.NORTH);
        card.add(vida, BorderLayout.CENTER);
        
        return card;
    }

    private void crearPanelEnemigos(Enemigo e){

        panelEnemigos = new JPanel();
        panelEnemigos.setLayout(new GridLayout(3, 1));
        panelEnemigos.setBorder(BorderFactory.createTitledBorder("Enemigos"));
        panelEnemigos.setPreferredSize(new Dimension(300, 200));

        JLabel nombre = new JLabel(e.getNombre() + " //velocidad: " + e.getVelocidad() );
        nombre.setForeground(Color.WHITE);
        nombre.setFont(new Font("Arial", Font.BOLD, 12));
        
        frame.add(panelHeroes, BorderLayout.WEST);
    }

    private JPanel generarEnemigo(Enemigo e, int index){

        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        card.setBackground(new Color(30,30,30)); 

        JLabel nombre = new JLabel(e.getNombre() + " //velocidad: " + e.getVelocidad() );
        nombre.setForeground(Color.WHITE);
        nombre.setFont(new Font("Arial", Font.BOLD, 14));
        
        hpMaxEnemigos.putIfAbsent(e, e.getHp());
        int hpMax = hpMaxEnemigos.get(e);

        JProgressBar vida = new JProgressBar(0, hpMax);
        vida.setValue(e.getHp());
        vida.setStringPainted(true);
    
        barrasEnemigos.put(e, vida);

        card.add(nombre, BorderLayout.NORTH);
        card.add(vida, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e1) {
                synchronized (lockEnemigo) {
                    enemigoElegido = index;
                    lockEnemigo.notify();
                }
            }
        });
        
        return card;
    }

    // funcion que actualiza las barras segun la vida del personaje 
    public void actualizarBarras(){
        for (Heroe h : barrasHeroes.keySet()){
            barrasHeroes.get(h).setValue(h.getHp());
        }

        for (Enemigo e : barrasEnemigos.keySet()){
            barrasEnemigos.get(e).setValue(e.getHp());
        }
    }



    private void crearPanelLog(){
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaLog.setBackground(new Color(20,20,20));
        areaLog.setForeground(Color.GREEN);

        scroll = new JScrollPane(areaLog);

        frame.add(scroll, BorderLayout.CENTER);
    }

    private void crearPanelBotones(){
        JPanel panel = new JPanel();

        btnatacar = new JButton("Atacar");
        btnatacar.setFont(new Font("Arial", Font.BOLD, 18));

        btnhabilidad = new JButton("Habilidad");
        btnhabilidad.setFont(new Font("Arial", Font.BOLD, 18));

        btnatacar.addActionListener(e -> elegir(1));
        btnhabilidad.addActionListener(e -> elegir(2));;

        panel.add(btnatacar);
        panel.add(btnhabilidad);

        frame.add(panel, BorderLayout.SOUTH);
    }

    private void elegir(int n) {
        synchronized (lockAccion) {
            accionElegida = n;
            lockAccion.notify();
        }
    }

    // aqui estan las funciones del VistaJuego, no usa Todas ya que la GUI no necesiat ciertas funciones que la terminal si 
    @Override
    public void mostrarMensaje(String mensaje) {
        areaLog.append(mensaje + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    @Override
    public void mostrarEstado(Heroe heroe, Enemigo enemigo) {
        mostrarMensaje("-------------- HEROES -----------\n");
        mostrarMensaje(heroe.getNombre() + " //HP " + heroe.getHp() + " //MP " + heroe.getMp() + " //Ataque " + heroe.getAtaque());

        mostrarMensaje("-------- ENEMGIOS ---------\n");
        mostrarMensaje(enemigo.getNombre() + " //HP " + enemigo.getHp() + " //MP " + enemigo.getMp() + " //Ataque " + enemigo.getAtaque());
    }

    @Override
    public void mostrarHeroes(Heroe[] heroe) {
        panelHeroes.removeAll();
        for(Heroe h : heroe){
            panelHeroes.add(generarHeroe(h));
        }
    }

    @Override
    public void mostrarEnemigos(Enemigo[] enemigo) {
        panelEnemigos.removeAll();
        for(int i = 0 ; i < enemigo.length; i++){
            panelEnemigos.add(generarEnemigo(enemigo[i], i));
        }

        panelEnemigos.revalidate();
        panelEnemigos.repaint();
    }

    @Override
    public void mostrarVictoria() {
        mostrarMensaje("Victoria!!");
    }

    @Override
    public void mostrarDerrota() {
        mostrarMensaje("Derrota :C");
    }

    @Override
    public void esperarEnter() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'esperarEnter'");
    }

    @Override
    public int elegirAccion(Heroe heroe) {
        mostrarMensaje("Tunrno de " + heroe.getNombre());

        accionElegida = -1;
        mostrarMensaje("Elige una accion");


        synchronized (lockAccion) {
            while (accionElegida == -1) {
                try { lockAccion.wait(); } catch (InterruptedException e) {}
            }
        }
        return accionElegida;
    }

    @Override
    public int seleccionarEnemigo(Enemigo[] enemigos) {
        enemigoElegido = -1;
        mostrarMensaje("Selecciona a un enemigo para atacarlo");
        synchronized (lockEnemigo) {
            while (enemigoElegido == -1) {
                try { lockEnemigo.wait(); } catch (InterruptedException e) {}
            }
        }
        return enemigoElegido;
    }

    @Override
    public int seleccionarHeroe(Heroe[] heroes) {
        mostrarMensaje("Selecciona a un heroe");
        return 0;
    }

    @Override
    public int seleccionarHeroeMuerto(Heroe[] heroes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'seleccionarHeroeMuerto'");
    }

    @Override
    public int leerEntero() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'leerEntero'");
    }


    @Override
    public void iniciar(ControladorBatalla ctrl) {
        this.controlador = ctrl;
        frame.setVisible(true);
    }
    
}
