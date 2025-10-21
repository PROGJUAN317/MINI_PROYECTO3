package dqs.modelo;

public interface Tanque {

    void defender(Personaje aliado);

    void provocarEnemigo(Personaje enemigo);

    void aumentarDefensa(int cantidad);

}
