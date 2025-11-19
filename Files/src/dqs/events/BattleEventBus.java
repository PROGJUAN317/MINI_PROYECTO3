package dqs.events;

/**
 * Bus estático simple para enrutar mensajes del dominio hacia un
 * listener configurado por el controlador. Si no hay listener se
 * hace fallback a System.out.println para mantener compatibilidad
 * con ejecuciones en consola sin cambios adicionales.
 */
public final class BattleEventBus {
    private static BattleEventListener listener = null;

    private BattleEventBus() {}

    public static void setListener(BattleEventListener l) {
        listener = l;
    }

    public static BattleEventListener getListener() {
        return listener;
    }

    public static void log(String message) {
        if (listener == null) {
            // No hacer fallback a System.out: exigir que exista un listener
            throw new IllegalStateException("No BattleEventListener registered. Call BattleEventBus.setListener(listener) before publishing events.");
        }
        // Entregar el mensaje al listener y dejar que éste maneje errores/format
        listener.onEvent(message);
    }
}
