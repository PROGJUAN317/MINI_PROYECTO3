package modelo;

public interface Sanador {

    boolean curar(Personaje objetivo);

    boolean revivir(Personaje objetivo);

    boolean restaurarMana(Personaje objetivo);

    boolean eliminarEfectoNegativo(Personaje objetivo);

}
