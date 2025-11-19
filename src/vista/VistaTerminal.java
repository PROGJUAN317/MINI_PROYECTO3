package vista;

import java.util.Scanner;

import controlador.ControladorBatalla;
import modelo.*;

public class VistaTerminal implements VistaJuego {
    
    private Scanner sc = new Scanner(System.in);

    // metodos de VistaJuego
    @Override
    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    @Override
    public void mostrarEstado(Heroe heroe, Enemigo enemigo) {
        System.out.println("-------------- HEROES -----------\n");
        System.out.println(heroe.getNombre() + " //HP " + heroe.getHp() + " //MP " + heroe.getMp() + " //Ataque " + heroe.getAtaque());

        System.out.println("-------- ENEMGIOS ---------\n");
        System.out.println(enemigo.getNombre() + " //HP " + enemigo.getHp() + " //MP " + enemigo.getMp() + " //Ataque " + enemigo.getAtaque());
    }

    @Override
    public void mostrarVictoria() {
        System.out.println("Has derrotado a todos los enemigos, FELICIDADES HAS GANADO");
    }

    @Override
    public void mostrarDerrota() {
        System.out.println("Han derrotado a todos los heroes, NOS CONDENASTE A TODOS");
    }

    @Override
    public void esperarEnter() {
        System.out.println("Presiona ENTER para continuar");
        sc.nextLine();
    }

    @Override
    public int elegirAccion(Heroe heroe) {
        System.out.println("Elige una accion para " + heroe.getNombre() +"\n");
        System.out.println("1. Atacar\n");
        System.out.println("2. Habilidad");
        return leerEntero();
    }

    @Override
    public int seleccionarEnemigo(Enemigo[] enemigos) {

        System.out.println("\nElige un enemigo:");
        for (int i = 0; i < enemigos.length; i++) {
            if (enemigos[i].getHp() > 0) {
                System.out.println((i + 1) + ". " + enemigos[i].getNombre() + " (HP: " + enemigos[i].getHp() + ")");
            }
        }
        return leerEntero() - 1; 
    }

    @Override
    public int seleccionarHeroe(Heroe[] heroes) {

        System.out.println("\nElige un héroe:");
        for (int i = 0; i < heroes.length; i++) {
            if (heroes[i].getHp() > 0) {
                System.out.println((i + 1) + ". " + heroes[i].getNombre() + " (HP: " + heroes[i].getHp() + ")");
            }
        }
        return leerEntero() - 1;
    }

    @Override
    public int seleccionarHeroeMuerto(Heroe[] heroes) {

        System.out.println("\nElige un héroe muerto:");
        for (int i = 0; i < heroes.length; i++) {
            if (heroes[i].getHp() <= 0) {
                System.out.println((i + 1) + ". " + heroes[i].getNombre() + " (HP: 0)");
            }
        }
        return leerEntero() - 1;
    }

    @Override
    public int leerEntero() {
        while (true) {
            try {
                System.out.print("→ ");
                return Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un número válido.");
            }
        }
    }

    @Override
    public void mostrarHeroes(Heroe[] heroe) {
        System.out.println(" --------- Heroes -------");
        for ( Heroe h : heroe){
            System.out.println(h.getNombre() + " - " + h.getTipo() + "\n");
        }   
    }

    @Override
    public void mostrarEnemigos(Enemigo[] enemigo) {
        System.out.println(" --------- Enemigos -------");
        for ( Enemigo e : enemigo){
            System.out.println(e.getNombre() + " - " + e.getTipo() + "\n");
        }  
    }

    @Override
    public void iniciar(ControladorBatalla controlador) {

    }

    @Override
    public void actualizarBarras() {
    }
    
    }
    


    


