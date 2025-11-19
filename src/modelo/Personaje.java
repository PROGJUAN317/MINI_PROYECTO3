package modelo;

public abstract class Personaje {

    protected String nombre;
    protected int hp;
    protected int mp;
    protected int ataque;
    protected int defensa;
    protected int velocidad;
    protected boolean esta_vivo = true;
    protected boolean esta_paralizado = false;

    protected int turnosParalisis = 0; // turnos restantes de parálisis
    protected boolean siendo_defendido = false;
    protected int turnosSueno = 0; // turnos restantes de sueño
    protected boolean esta_provocado = false;
    protected Personaje provocador = null;
    protected Personaje defensor = null;
    protected boolean esta_durmiendo = false;
    public String getNombre() { return nombre; }
    public int getHp() { return hp; }
    public int getMp() { return mp; }
    public int getAtaque() { return ataque; }
    public int getDefensa() { return defensa; }
    public int getVelocidad() { return velocidad; }
    public void setHp(int valor) {
        hp = Math.max(0, valor);
        esta_vivo = hp > 0;
        if (!esta_vivo) removerDefensa();
    }

    public void setMp(int valor) {
        mp = Math.max(0, valor);
    }

    public Personaje(String nombre, int hp, int mp, int ataque, int defensa, int velocidad) {
        this.nombre = nombre;
        this.hp = hp;
        this.mp = mp;
        this.ataque = ataque;
        this.defensa = defensa;
        this.velocidad = velocidad;
        this.esta_vivo = hp > 0;
    }

    public boolean esta_vivo() { return esta_vivo; }
    public boolean estaParalizado() { return esta_paralizado; }
    public boolean estaDurmiendo() { return esta_durmiendo; }
    public boolean estaSiendoDefendido() { return siendo_defendido; }
    public boolean estaProvocado() { return esta_provocado; }

    public int recibir_daño(int cantidad) {

        int defensaTotal = defensa;

        // Si está siendo defendido por un tanque, aplicar defensa combinada
        if (siendo_defendido && defensor != null && defensor.esta_vivo) {
            defensaTotal += defensor.getDefensa();
        }

        int dañoFinal = cantidad - defensaTotal;

        // Daño mínimo de 1
        if (dañoFinal < 1) dañoFinal = 1;

        setHp(hp - dañoFinal);

        // Si muere, se borra la defensa
        if (!esta_vivo) removerDefensa();

        return dañoFinal;  // La vista decide cómo mostrarlo
    }

    protected void aumentarAtaque(int aumento) {
        if (aumento > 0) ataque += aumento;
    }

    // efectos

    public void aplicarParalisis(int turnos) {
        if (turnos > 0) {
            this.turnosParalisis = Math.max(this.turnosParalisis, turnos);
        }
    }

    public boolean aplicarDormir() {
        if (!esta_vivo) return false;
        esta_durmiendo = true;
        return true;
    }

    public void limpiarEfectos() {
        esta_paralizado = false;
        esta_durmiendo  = false;
        esta_provocado  = false;
        provocador = null;
    }

    public boolean puedeActuar() {

        // Parálisis
        if (turnosParalisis > 0) {
            turnosParalisis--;
            return false;
        }

        // Sueño
        if (turnosSueno > 0) {
            turnosSueno--;
            return false;
        }

        return true;
    }

    public boolean puedeAtacar() {
        return esta_vivo && (turnosParalisis == 0) && (turnosSueno == 0);
    }

    // defensa

    public boolean recibirDefensa(Personaje tanque) {
        if (!esta_vivo) return false;
        siendo_defendido = true;
        defensor = tanque;
        return true;
    }

    public void removerDefensa() {
        siendo_defendido = false;
        defensor = null;
    }

    public Personaje getDefensor() { return defensor; }

    // provocacion

    public boolean serProvocado(Personaje tanque) {
        if (!esta_vivo) return false;
        esta_provocado = true;
        provocador = tanque;
        return true;
    }

    public void removerProvocacion() {
        esta_provocado = false;
        provocador = null;
    }

    public Personaje getProvocador() { return provocador; }

    public Personaje seleccionarObjetivo(Personaje[] posibles) {
        if (esta_provocado && provocador != null && provocador.esta_vivo) {
            return provocador;
        }
        for (Personaje p : posibles) {
            if (p != null && p.esta_vivo()) return p;
        }
        return null;
    }

    public int atacar(Personaje[] posibles) {
        Personaje obj = seleccionarObjetivo(posibles);
        if (obj == null) return -1;

        int daño = ataque - obj.getDefensa();
        if (daño < 1) daño = 1;

        obj.recibir_daño(daño);
        return daño;
    }


    public abstract void elegirAccion();
}
