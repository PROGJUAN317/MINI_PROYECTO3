package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import modelo.*;
import vista.*;


public class ControladorBatalla{
    
    private Batalla batalla;
    private Heroe[] heroes;
    private Enemigo[] enemigos;
    private VistaJuego vista;
    private Object[] ordenTurnos;


    public ControladorBatalla(Batalla batalla, Heroe[] heroes, Enemigo[] enemigos, VistaJuego vista){
        this.heroes = heroes;
        this.enemigos = enemigos;
        this.vista = vista;
        this.batalla = batalla;
    }


    // funcion para generar el orden de ataque segun la velocidad
    private ArrayList<Object> ordenAtaque(){
        ArrayList<Object> todos = new ArrayList<>();

        for (Heroe h : heroes){
            todos.add(h);
        }

        for(Enemigo e : enemigos){
            todos.add(e);
        }

        ArrayList<Object> orden = new ArrayList<>();

        while(!todos.isEmpty()){
            Object masRapido = todos.get(0);

            for(Object o : todos){

                int velO = (o instanceof Heroe)? ((Heroe)o).getVelocidad() : ((Enemigo)o).getVelocidad();

                int velMR = (masRapido instanceof Heroe)? ((Heroe)masRapido).getVelocidad() : ((Enemigo)masRapido).getVelocidad(); 
                
                if(velO > velMR){
                    masRapido = o;
                }
            }

            orden.add(masRapido);
            todos.remove(masRapido);
        }
        return orden;
    }

    // funcion que da inicio a la batalla
    public void iniciarBatalla() {

        vista.mostrarMensaje("¡La batalla comienza!");
        vista.mostrarHeroes(heroes);
        vista.mostrarEnemigos(enemigos);
        vista.mostrarMensaje("El orden de ataque es: " + ordenAtaque());

        // empieza un bucle si hay enemigos y heroes vivos
        while (hayHeroesVivos() && hayEnemigosVivos()) {

            for (Heroe heroe : heroes) {
                if (heroe.esta_vivo()) {
                    turnoHeroe(heroe);

                    if (!hayEnemigosVivos()) {
                        vista.mostrarVictoria();
                        return;
                    }
                }
            }

            // El enemigo ataca después
            for (Enemigo enemigo : enemigos) {
                if (enemigo.esta_vivo()) {
                    turnoEnemigo(enemigo);

                    if (!hayHeroesVivos()) {
                        vista.mostrarDerrota();
                        return;
                    }
                }
            }
        }
    }

    // controla el turno de los heroes 
    private void turnoHeroe(Heroe heroe) {
        vista.mostrarEstado(heroe, primerEnemigoVivo());

        int accion = vista.elegirAccion(heroe);

        switch (accion) {
            case 1: // Atacar
                int idx = vista.seleccionarEnemigo(enemigos);
                Enemigo objetivo = enemigos[idx];
                heroe.atacar(objetivo);
                vista.mostrarMensaje(heroe.getNombre() + " atacó a " + objetivo.getNombre());
                vista.actualizarBarras();
                break;


            case 2: // Habilidad
                vista.mostrarMensaje("no esta implementada por temas de presupuesto");
                break;

            default:
                vista.mostrarMensaje("Opción inválida.");
                turnoHeroe(heroe);
        }
    }

    // controla el turno "Automatico" de los enemigos
    private void turnoEnemigo(Enemigo enemigo) {
        Heroe objetivo = primerHeroeVivo();
        enemigo.atacar(objetivo);
        vista.mostrarMensaje(enemigo.getNombre() + " atacó a " + objetivo.getNombre());
    }

    private boolean hayHeroesVivos() {
        for (Heroe h : heroes)
            if (h.esta_vivo()) return true;
        return false;
    }

    private boolean hayEnemigosVivos() {
        for (Enemigo e : enemigos)
            if (e.esta_vivo()) return true;
        return false;
    }

    private Heroe primerHeroeVivo() {
        for (Heroe h : heroes) if (h.esta_vivo()) return h;
        return null;
    }

    private Enemigo primerEnemigoVivo() {
        for (Enemigo e : enemigos) if (e.esta_vivo()) return e;
        return null;
    }   

    // inicia llamandose asi mismo y al inciarBatalla
    public void iniciar(){
        vista.iniciar(this);
        iniciarBatalla();
    }


}
