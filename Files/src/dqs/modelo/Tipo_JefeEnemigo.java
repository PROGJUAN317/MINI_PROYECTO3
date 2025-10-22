package dqs.modelo;

public enum Tipo_JefeEnemigo {
    REY_DRAGON (300, 500, 100, 200, 500, 100, 300, 600, 200, 400, "Un dragón feroz que escupe fuego."),
    NIGROMANTE (300, 450, 150, 250, 350, 70, 250, 500, 100, 200, "Maestro de las artes oscuras y la necromancia."),
    JEFE_ORCO(250, 400, 50, 100, 400, 80, 200, 400, 150, 300, "Líder de los orcos con gran poder mágico."),
    GIGANTE(200, 350, 30, 80, 450, 90, 250, 500, 100, 250, "Una criatura enorme con fuerza descomunal."),
    DEMONIO(350, 600, 200, 300, 600, 120, 400, 700, 250, 450, "Ser infernal con habilidades mágicas devastadoras.");

    private int minHp, maxHp;
    private int minMp, maxMp;
    private int minAtaque, maxAtaque;
    private int minDefensa, maxDefensa;
    private int minVelocidad, maxVelocidad;
    private String descripcion;

    private Tipo_JefeEnemigo(int minHp, int maxHp, int minMp, int maxMp, int minAtaque, int maxAtaque, int minDefensa, int maxDefensa, int minVelocidad, int maxVelocidad, String descripcion) {
        this.minHp = minHp;
        this.maxHp = maxHp;
        this.minMp = minMp;
        this.maxMp = maxMp;
        this.minAtaque = minAtaque;
        this.maxAtaque = maxAtaque;
        this.minDefensa = minDefensa;
        this.maxDefensa = maxDefensa;
        this.minVelocidad = minVelocidad;
        this.maxVelocidad = maxVelocidad;
        this.descripcion = descripcion;
    }

    public int getMinHp() {
        return minHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getMinMp() {
        return minMp;
    }

    public int getMaxMp() {
        return maxMp;
    }

    public int getMinAtaque() {
        return minAtaque;
    }

    public int getMaxAtaque() {
        return maxAtaque;
    }

    public int getMinDefensa() {
        return minDefensa;
    }

    public int getMaxDefensa() {
        return maxDefensa;
    }

    public int getMinVelocidad() {
        return minVelocidad;
    }

    public int getMaxVelocidad() {
        return maxVelocidad;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
