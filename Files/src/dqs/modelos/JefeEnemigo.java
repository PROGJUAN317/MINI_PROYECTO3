package dqs.modelos;

/**
 * Subclase de Enemigo que representa a un Jefe con comportamiento especial.
 * - Puede ejecutar una habilidad especial cada N turnos (por defecto 2)
 * - Puede atacar a todos los héroes (área)
 */
public class JefeEnemigo extends Enemigo {
    private int turnosHastaEspecial;
    private final int cooldownEspecial;

    public JefeEnemigo(String nombre, int hp, int mp, int ataque, int defensa, int velocidad, Tipo_Enemigo tipo, int cooldownEspecial) {
        // Llama al constructor de Enemigo que permite omitir validación si es necesario
        super(nombre, hp, mp, ataque, defensa, velocidad, tipo, true);
        this.cooldownEspecial = Math.max(1, cooldownEspecial);
        this.turnosHastaEspecial = this.cooldownEspecial; // empezará a contar
    }

    /**
     * Ejecuta la lógica del jefe en su turno. Si el jefe está paralizado o dormido
     * pierde el turno (se maneja en Personaje.puedeActuar()). Si el contador llega a 0,
     * ejecuta la habilidad especial y resetea el contador; en otro caso ataca aleatoriamente.
     */
    public void actuar(Heroe[] heroes) {
        if (!this.puedeActuar()) return; // imprime motivo y decrementa contadores

        if (turnosHastaEspecial <= 0) {
            // usar habilidad especial contra un héroe aleatorio
            Heroe objetivo = buscarHeroeVivo(heroes);
            if (objetivo != null) {
                usarHabilidadEspecial(objetivo);
            }
            this.turnosHastaEspecial = this.cooldownEspecial;
        } else {
            // comportamiento normal: atacar a un héroe aleatorio
            this.atacarAleatorio(heroes);
            this.turnosHastaEspecial--;
        }
    }

    @Override
    public void usarHabilidadEspecial(Personaje objetivo) {
        // Por defecto habilidad especial: daño elevado a un objetivo
        if (objetivo != null && objetivo.esta_vivo()) {
            int daño = this.getAtaque() * 3 - objetivo.getDefensa();
            if (daño < 1) daño = 1;
            objetivo.recibir_daño(daño);
            System.out.println(this.getNombre() + " (JEFE) usa su habilidad especial contra " + objetivo.getNombre() + " causando " + daño + " puntos de daño!");
        }
    }

    @Override
    public int TurnosParaAtacar() {
        return this.cooldownEspecial;
    }

    @Override
    public void AtacarATodos() {
        // Implementación simple: atacar a todos los héroes vivos (se requiere contexto externo)
        System.out.println(this.getNombre() + " intenta atacar a todos, pero necesita contexto de objetivos.");
    }
}
