package dqs.events;

/**
 * Interfaz para recibir eventos de dominio generados por el modelo.
 */
public interface BattleEventListener {
    void onEvent(String message);
}
