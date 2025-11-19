package modelo;

public class JefeEnemigo extends Enemigo {

    private int turnosHastaEspecial;
    private final int cooldownEspecial;

    public JefeEnemigo(
            String nombre, int hp, int mp, int ataque, int defensa,
            int velocidad, Tipo_Enemigo tipo, int cooldownEspecial
    ) {
        super(nombre, hp, mp, ataque, defensa, velocidad, tipo, true);
        this.cooldownEspecial = Math.max(1, cooldownEspecial);
        this.turnosHastaEspecial = this.cooldownEspecial;
    }

    /**
     * Actuación del jefe SIN manejar consola ni elegir objetivos.
     * El controlador debe decidir el objetivo.
     *
     * @return true si debe usar habilidad especial, false si debe ataque normal.
     */
    public boolean debeUsarHabilidadEspecial() {
        if (!this.puedeActuar()) {
            return false;
        }
        return turnosHastaEspecial <= 0;
    }

    /**
     * Lógica del turno del jefe, sin impresiones ni selección interna.
     * El controlador llamará a este método después de ejecutar la acción.
     */
    public void terminarTurno() {
        if (turnosHastaEspecial <= 0) {
            turnosHastaEspecial = cooldownEspecial;
        } else {
            turnosHastaEspecial--;
        }
    }

    /**
     * Habilidad especial básica.
     * No imprime nada. El controlador o vista informan al usuario.
     *
     * @return daño aplicado (para que el controlador lo comunique a la vista)
     */
    @Override
    public void usarHabilidadEspecial(Personaje objetivo) {
        if (objetivo == null || !objetivo.esta_vivo()) {
            return;
        }

        int daño = this.getAtaque() * 3 - objetivo.getDefensa();
        if (daño < 1) daño = 1;

        objetivo.recibir_daño(daño);
        return;
    }

    /**
     * Devuelve cada cuántos turnos ejecuta su habilidad especial
     */
    @Override
    public int TurnosParaAtacar() {
        return cooldownEspecial;
    }

    /**
     * Ataque grupal. No imprime.
     * Devuelve el daño base que se aplicará a cada enemigo.
     */
    @Override
    public void AtacarATodos() {
        // Daño base multiplicado por 2 para ataque de área (ejemplo)
        int daño = this.getAtaque() * 2;
        return;
    }
}
