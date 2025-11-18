package dqs.controlador;

/**
 * Interfaz para recibir eventos de dominio generados por el modelo.
 * Implementaciones pueden enrutar esos mensajes hacia la vista o
 * hacia la consola según convenga.
 */
public interface BattleEventListener {
    void onEvent(String message);
}
