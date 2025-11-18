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
        if (listener != null) {
            try { listener.onEvent(message); } catch (Exception e) { System.out.println(message); }
        } else {
            System.out.println(message);
        }
    }
}
