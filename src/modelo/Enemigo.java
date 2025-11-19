package modelo;

public class Enemigo extends Personaje implements Agresivo, Jefe {

    private final Tipo_Enemigo tipo;

    // constructor que sigue la regla de ciertos rangos permitidos, es mas que todo para los enemigos normales
    public Enemigo(String nombre, int hp, int mp, int ataque, int defensa, int velocidad, Tipo_Enemigo tipo) {
        super(nombre, hp, mp, ataque, defensa, velocidad);
        this.tipo = tipo;
        // if (!tipo.validarAtributos(hp, mp, ataque, defensa)) {
        //     throw new IllegalArgumentException("Atributos fuera del rango permitido para el tipo " + tipo.name());
        // }
    }

    // Este segundo constructor es para aquellos enemigos que sobrepasan el rango permitido esta pensado para enemigos como jefes
    public Enemigo(String nombre, int hp, int mp, int ataque, int defensa, int velocidad, Tipo_Enemigo tipo, boolean skipValidation) {
        super(nombre, hp, mp, ataque, defensa, velocidad);
        this.tipo = tipo;
        if (!skipValidation && !tipo.validarAtributos(hp, mp, ataque, defensa)) {
            throw new IllegalArgumentException("Atributos fuera del rango permitido para el tipo " + tipo.name());
        }
    }

    // obtiene el estado de los personajes
    public String getEstado() {
        return nombre + " [" + tipo.name() + "] | HP: " + hp + " | MP: " + mp +
               " | Ataque: " + ataque + " | Defensa: " + defensa + " | Velocidad: " + velocidad +
               "\nDescripción: " + tipo.getDescripcion();
    }


    public static Enemigo crearEnemigo(Tipo_Enemigo tipo, String nombre) {
        int hp = (int) (Math.random() * (tipo.getMaxHp() - tipo.getMinHp() + 1)) + tipo.getMinHp();
        int mp = (int) (Math.random() * (tipo.getMaxMp() - tipo.getMinMp() + 1)) + tipo.getMinMp();
        int ataque = (int) (Math.random() * (tipo.getMaxAtaque() - tipo.getMinAtaque() + 1)) + tipo.getMinAtaque();
        int defensa = (int) (Math.random() * (tipo.getMaxDefensa() - tipo.getMinDefensa() + 1)) + tipo.getMinDefensa();
        int velocidad = (int) (Math.random() * 20 + 10);
        return new Enemigo(nombre, hp, mp, ataque, defensa, velocidad, tipo);
    }

    public Tipo_Enemigo getTipo() {
        return tipo;
    }

    // @Override
    // public void elegirAccion() {
    //     // 
    // }

    // Implementación que respeta las interfaces (void) 
    // ya que en la clase Agresivo.java estamos manejando datos void, se crearon 2 versiones de las acciones, una que respeta el void 
    // otra que muestra el texto (String)

    @Override
    public void atacar(Personaje objetivo) {
        aplicarDañoActualizar(objetivo, 1);
    }

    // versión auxiliar que devuelve un mensaje
    public String atacarResultado(Personaje objetivo) {
        if (objetivo == null || !objetivo.esta_vivo()) {
            return nombre + " no puede atacar: objetivo inválido o muerto.";
        }
        int daño = aplicarDañoActualizar(objetivo, 1);
        if (!objetivo.esta_vivo()) {
            return nombre + " derrotó a " + objetivo.getNombre() + " causando " + daño + " de daño.";
        }
        return nombre + " atacó a " + objetivo.getNombre() + " causando " + daño + " de daño.";
    }

    // nueva funcion que sirve para aplicar la logica del daño
    private int aplicarDañoActualizar(Personaje objetivo, int multiplicador) {
        if (objetivo == null || !objetivo.esta_vivo()) return 0;
        int daño = this.ataque - objetivo.getDefensa();
        if (daño < 1) daño = 1; // Daño mínimo de 1
        objetivo.recibir_daño(daño);
        return daño;
    }

    // Permite al enemigo elegir y atacar a un héroe vivo del array proporcionado

    @Override
    public void atacarAleatorio(Heroe[] heroes) {
        if (!this.puedeActuar()) return;
        Heroe objetivo = buscarHeroeVivo(heroes);
        if (objetivo != null) {
            atacar(objetivo);
        }
    }

    // resultado de ataque aleatorio (donde muestra la info)
    public String atacarAleatorioResultado(Heroe[] heroes) {
        if (!this.puedeActuar()) return nombre + " no puede actuar este turno.";
        Heroe objetivo = buscarHeroeVivo(heroes);
        if (objetivo == null) return nombre + " no encontró héroes vivos para atacar.";
        return atacarResultado(objetivo);
    }

    // al igual que con atacar, usarHabilidadEspecial es paret de una unterface la cual es un void por lo que se debe respetar
    // por lo que hacemos un void que cumpla con el implements de Agresivo.java y aparte un String que guarde en texto la info
    @Override
    public void usarHabilidadEspecial(Personaje objetivo) {
        aplicarDañoActualizar(objetivo, 2);
    }

    public String usarHabilidadEspecialResultado(Personaje objetivo) {
        if (objetivo == null || !objetivo.esta_vivo()) {
            return nombre + " no puede usar habilidad: objetivo inválido o muerto.";
        }
        int dano = aplicarDañoActualizar(objetivo, 2);
        if (!objetivo.esta_vivo()) {
            return nombre + " usó su habilidad especial y derrotó a " + objetivo.getNombre() + " causando " + dano + " de daño.";
        }
        return nombre + " usó su habilidad especial contra " + objetivo.getNombre() + " causando " + dano + " de daño.";
    }

    // Busca y devuelve un héroe vivo aleatorio del array proporcionado

    public Heroe buscarHeroeVivo(Heroe[] heroes) {
        if (heroes == null || heroes.length == 0) return null;
        int vivos = 0;
        for (Heroe h : heroes) if (h != null && h.esta_vivo()) vivos++;
        if (vivos == 0) return null;
        int elegido = (int) (Math.random() * vivos);
        int idx = 0;
        for (Heroe h : heroes) {
            if (h != null && h.esta_vivo()) {
                if (idx == elegido) return h;
                idx++;
            }
        }
        return null;
    }

    @Override
    public int TurnosParaAtacar() {
        return 2;
    }

    public void setDaño(int daño) {
        if (daño < 0) this.ataque = 0;
        else this.ataque = daño;
    }

    @Override
    public String toString() {
        return "Enemigo: " + nombre + " | " + tipo.name() + " | HP: " + hp +
               " | MP: " + mp + " | Ataque: " + ataque +
               " | Defensa: " + defensa + " | Velocidad: " + velocidad;
    }

    @Override
    public void AtacarATodos() {
        // comportamiento por defecto vacio el controlador decide el contexto y cómo aplicarlo
    }

    
    public int getPorcentajeHP() {
        throw new UnsupportedOperationException("Unimplemented method 'getPorcentajeHP'");
    }

    @Override
    public void elegirAccion() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'elegirAccion'");
    }
}
