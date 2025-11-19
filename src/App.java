import java.util.Scanner;

import controlador.ControladorBatalla;
import modelo.Batalla;
import modelo.Enemigo;
import modelo.Heroe;
import modelo.Tipo_Enemigo;
import modelo.Tipo_Heroe;
import vista.VistaGUI;
import vista.VistaJuego;
import vista.VistaTerminal;

public class App {
    public static void main(String[] args) throws Exception {

        VistaJuego vista;
        Batalla batalla = new Batalla();

        // preguntamos al usuario si desea verlo por GUI o terminal 
        Scanner sc = new Scanner(System.in);
        System.out.println("1. Terminal");
        System.out.println("2. GUI");
        int op = sc.nextInt();

        // dependiendo de la opcion llama a la vista terminal o la gui 
        if (op == 1) vista = new VistaTerminal();
        else vista = new VistaGUI();

        // creacion de heroes y enemigos
        Heroe[] heroes = {
            new Heroe("Angelo", Tipo_Heroe.GUERRERO, 50, 25, 18, 30, 55),
            new Heroe("Yangus", Tipo_Heroe.GUERRERO, 40, 5, 20, 35, 25),
            new Heroe("Hero", Tipo_Heroe.GUERRERO, 40, 5, 20, 35, 25),
            new Heroe("Jessica", Tipo_Heroe.GUERRERO, 40, 5, 20, 35, 25),
        };

        Enemigo[] enemigos = {
            new Enemigo("Slime", 30, 0, 23, 6, 30, Tipo_Enemigo.GOLEM),
            new Enemigo("Dracky", 25, 0, 12, 10, 21, Tipo_Enemigo.NOMUERTO),
            new Enemigo("Golem", 25, 0, 12, 10, 21, Tipo_Enemigo.NOMUERTO),
            new Enemigo("Gengar", 25, 0, 12, 10, 21, Tipo_Enemigo.NOMUERTO),
        };


        // objeto controlador que permitira llamar el iniciarBatalla para inciar nuestro juego
        ControladorBatalla controlador = new ControladorBatalla(batalla, heroes, enemigos, vista);

        controlador.iniciarBatalla();
    }
}