package vista;

import modelo.Heroe;
import modelo.Enemigo;
import controlador.ControladorBatalla;

public interface VistaJuego {

    // funciones que la vista se encargara de mostrar
    public void iniciar(ControladorBatalla controlador);

    void actualizarBarras();
    void mostrarMensaje(String mensaje);
    void mostrarEstado(Heroe heroe, Enemigo enemigo);
    void mostrarHeroes(Heroe[] heroe);
    void mostrarEnemigos(Enemigo[] enemigo);
    void mostrarVictoria();
    void mostrarDerrota();

    void esperarEnter();
    
    int elegirAccion(Heroe heroe);
    
    int seleccionarEnemigo(Enemigo[] enemigos);
    int seleccionarHeroe(Heroe[] heroes);
    int seleccionarHeroeMuerto(Heroe[] heroes);

    int leerEntero();


}
