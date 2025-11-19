package modelo;

public interface Tanque {

    boolean defender(Personaje aliado);

    boolean provocarEnemigo(Personaje enemigo);

    boolean aumentarDefensa(int cantidad);

}
