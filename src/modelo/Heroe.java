package modelo;

public class Heroe extends Personaje implements Sanador, Tanque, Hechicero {

    private final Tipo_Heroe tipo;

    public Heroe(String nombre, Tipo_Heroe tipo, int hp, int mp, int ataque, int defensa, int velocidad) {
        super(nombre, hp, mp, ataque, defensa, velocidad);
        this.tipo = tipo;

        // if (!tipo.validarAtributos(hp, mp, ataque, defensa)) {
        //     throw new IllegalArgumentException("Atributos fuera del rango para el tipo: " + tipo);
        // }
    }

    public Tipo_Heroe getTipo() {
        return tipo;
    }


    // metodos de combate/defensa

    @Override
    public boolean aumentarDefensa(int extra) {
        if (!(tipo == Tipo_Heroe.GUERRERO || tipo == Tipo_Heroe.PALADIN)) return false;
        if (mp < 10) return false;

        mp -= 10;
        defensa += extra;
        return true;
    }

    @Override
    public boolean defender(Personaje aliado) {
        if (!(tipo == Tipo_Heroe.GUERRERO || tipo == Tipo_Heroe.PALADIN)) return false;
        if (mp < 10) return false;

        mp -= 10;

        if (aliado.estaSiendoDefendido()) aliado.removerDefensa();

        aliado.recibirDefensa(this);
        return true;
    }

    @Override
    public boolean provocarEnemigo(Personaje enemigo) {
        if (!(tipo == Tipo_Heroe.GUERRERO || tipo == Tipo_Heroe.PALADIN)) return false;
        if (mp < 5) return false;

        mp -= 5;

        if (enemigo.estaProvocado()) enemigo.removerProvocacion();
        enemigo.serProvocado(this);
        return true;
    }

    public boolean provocarTodos(Personaje[] enemigos) {
        if (!(tipo == Tipo_Heroe.GUERRERO || tipo == Tipo_Heroe.PALADIN)) return false;

        int vivos = 0;
        for (Personaje e : enemigos) {
            if (e != null && e.esta_vivo()) vivos++;
        }

        int costo = vivos * 3;
        if (mp < costo) return false;

        mp -= costo;

        for (Personaje e : enemigos) {
            if (e != null && e.esta_vivo()) {
                if (e.estaProvocado()) e.removerProvocacion();
                e.serProvocado(this);
            }
        }

        return true;
    }

    // metodos del sanador

    @Override
    public boolean curar(Personaje objetivo) {
        if (!(tipo == Tipo_Heroe.DRUIDA || tipo == Tipo_Heroe.PALADIN)) return false;
        if (mp < 15) return false;

        mp -= 15;
        objetivo.setHp(objetivo.getHp() + 30);
        return true;
    }

    @Override
    public boolean revivir(Personaje objetivo) {
        if (tipo != Tipo_Heroe.PALADIN) return false;
        if (objetivo.esta_vivo()) return false;
        if (mp < 25) return false;

        mp -= 25;
        objetivo.setHp(50);
        return true;
    }

    @Override
    public boolean restaurarMana(Personaje objetivo) {
        if (tipo != Tipo_Heroe.DRUIDA) return false;
        if (mp < 20) return false;

        mp -= 20;
        objetivo.setMp(objetivo.getMp() + 25);
        return true;
    }

    @Override
    public boolean eliminarEfectoNegativo(Personaje objetivo) {
        if (!(tipo == Tipo_Heroe.DRUIDA || tipo == Tipo_Heroe.PALADIN)) return false;

        objetivo.limpiarEfectos();
        return true;
    }

    // metodos del hechicero

    @Override
    public boolean LanzaHechizoSueño(Personaje objetivo) {
        if (!(tipo == Tipo_Heroe.MAGO || tipo == Tipo_Heroe.DRUIDA)) return false;
        if (mp < 20) return false;

        mp -= 20;
        objetivo.recibir_daño(40);
        return true;
    }

    @Override
    public boolean LanzaHechizoRefuerzo(Personaje objetivo) {
        if (!(tipo == Tipo_Heroe.MAGO || tipo == Tipo_Heroe.DRUIDA)) return false;
        if (mp < 20) return false;

        mp -= 20;
        objetivo.aumentarAtaque(60);
        return true;
    }

    @Override
    public boolean LanzaHechizoParalisis(Personaje objetivo) {
        if (!(tipo == Tipo_Heroe.MAGO || tipo == Tipo_Heroe.DRUIDA)) return false;
        if (mp < 25) return false;

        mp -= 25;
        objetivo.aplicarParalisis(1);
        return true;
    }

    // metodos de ataque basicos

    public int atacar(Personaje objetivo) {
        if (objetivo == null || !objetivo.esta_vivo()) return -1;

        int daño = ataque - objetivo.getDefensa();
        if (daño < 1) daño = 1;

        objetivo.recibir_daño(daño);
        return daño; // Vista decide cómo mostrarlo
    }

    @Override
    public String toString() {
        return "Heroe{" + nombre +
                ", tipo=" + tipo +
                ", HP=" + hp +
                ", MP=" + mp +
                ", ATK=" + ataque +
                ", DEF=" + defensa +
                ", VEL=" + velocidad + "}";
    }

    @Override
    public void elegirAccion() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'elegirAccion'");
    }
}
