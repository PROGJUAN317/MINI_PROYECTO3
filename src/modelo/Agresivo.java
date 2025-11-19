package modelo;

public interface Agresivo {

    void atacar(Personaje objetivo);

    void usarHabilidadEspecial(Personaje objetivo);

    void atacarAleatorio(Heroe[] heroes);

}
