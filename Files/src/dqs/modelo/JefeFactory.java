package dqs.modelo;

import java.util.Random;

public class JefeFactory {
    private static final Random RAND = new Random();

    public static JefeEnemigo crearJefeAleatorio(Tipo_JefeEnemigo tipo) {
        int hp = randomBetween(tipo.getMinHp(), tipo.getMaxHp());
        int mp = randomBetween(tipo.getMinMp(), tipo.getMaxMp());
        int at = randomBetween(tipo.getMinAtaque(), tipo.getMaxAtaque());
        int def = randomBetween(tipo.getMinDefensa(), tipo.getMaxDefensa());
        int vel = randomBetween(tipo.getMinVelocidad(), tipo.getMaxVelocidad());

        String nombre = "Jefe_" + tipo.name();
        return new JefeEnemigo(nombre, hp, mp, at, def, vel, tipo);
    }

    private static int randomBetween(int a, int b) {
        if (b < a) return a;
        return a + RAND.nextInt(b - a + 1);
    }
}
