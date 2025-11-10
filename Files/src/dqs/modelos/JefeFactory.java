package dqs.modelos;

/**
 * Fábrica para crear instancias de JefeEnemigo a partir de Tipo_JefeEnemigo
 */
public class JefeFactory {

    public static JefeEnemigo crearJefe(Tipo_JefeEnemigo tipo, String nombre) {
        int hp = (int)(Math.random() * (tipo.getMaxHp() - tipo.getMinHp() + 1)) + tipo.getMinHp();
        int mp = (int)(Math.random() * (tipo.getMaxMp() - tipo.getMinMp() + 1)) + tipo.getMinMp();
        int ataque = (int)(Math.random() * (tipo.getMaxAtaque() - tipo.getMinAtaque() + 1)) + tipo.getMinAtaque();
        int defensa = (int)(Math.random() * (tipo.getMaxDefensa() - tipo.getMinDefensa() + 1)) + tipo.getMinDefensa();
        int velocidad = (int)(Math.random() * (tipo.getMaxVelocidad() - tipo.getMinVelocidad() + 1)) + tipo.getMinVelocidad();

        // Usamos Tipo_Enemigo por compatibilidad con el constructor de Enemigo/JefeEnemigo
        Tipo_Enemigo tipoSimple = Tipo_Enemigo.DRAGON; // fallback
        // Intentamos mapear por nombre si existe un tipo equivalente
        try {
            tipoSimple = Tipo_Enemigo.valueOf("DRAGON");
        } catch (Exception e) {
            // no importa, usamos DRAGON por defecto
        }

        // cooldown especial de 2 turnos por defecto
        return new JefeEnemigo(nombre, hp, mp, ataque, defensa, velocidad, tipoSimple, 2);
    }
}
