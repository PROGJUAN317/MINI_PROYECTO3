package dqs.modelo;

/**
 * Implementación simple de un jefe como subclase de Enemigo.
 * Aprovecha la lógica base de Enemigo pero puede sobrescribir
 * comportamiento especial del jefe.
 */
public class JefeEnemigo extends Enemigo {
    private final Tipo_JefeEnemigo tipoJefe;

    public JefeEnemigo(String nombre, int hp, int mp, int ataque, int defensa, int velocidad, Tipo_JefeEnemigo tipoJefe) {
        // Reutilizamos Tipo_Enemigo.GIGANTE como marcador base (no usado en lógica de jefe)
    super(nombre, hp, mp, ataque, defensa, velocidad, Tipo_Enemigo.DRAGON);
        this.tipoJefe = tipoJefe;
    }

    public Tipo_JefeEnemigo getTipoJefe() {
        return tipoJefe;
    }

    @Override
    public void usarHabilidadEspecial(Personaje objetivo) {
        if (objetivo == null) {
            System.out.println(getNombre() + " intenta usar su habilidad de jefe, pero no hay objetivo.");
            return;
        }

        System.out.println(getNombre() + " usa su habilidad de jefe: " + tipoJefe.name());
        int daño = this.getAtaque() * 3 - objetivo.getDefensa();
        if (daño < 1) daño = 1;
        objetivo.recibir_daño(daño);

        if (!objetivo.esta_vivo()) {
            System.out.println(objetivo.getNombre() + " ha sido derrotado por el jefe " + getNombre() + "!");
        }
    }

    @Override
    public void AtacarATodos() {
        // Implementación de ejemplo: no conocemos el contexto de objetivos aquí,
        // por tanto dejamos la implementación vacía y se usará el método de
        // BatallaManager que llame a usarHabilidadEspecial contra todos.
        System.out.println(getNombre() + " intenta usar AtacarATodos(), se requiere contexto de objetivos.");
    }
}
