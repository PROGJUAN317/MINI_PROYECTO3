package dqs.vista;

import dqs.controlador.BattleEventListener;

/**
 * Implementación sencilla que envía los eventos de dominio a la
 * consola estándar. Útil para ejecuciones en modo CLI.
 */
public class ConsoleBattleEventListener implements BattleEventListener {
    @Override
    public void onEvent(String message) {
        System.out.println(message);
    }
}
