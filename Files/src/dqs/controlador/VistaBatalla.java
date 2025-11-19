package dqs.controlador;

import java.util.Set;

public interface VistaBatalla {
    void refreshUI(); // coincide con el método existente en la vista
    void appendLog(String linea);
    void habilitarAcciones(Set<AccionSeleccionada> acciones);
    /**
     * Solicita al usuario seleccionar un índice de una lista de opciones.
     * Retorna el índice seleccionado (0-based) o -1 si el usuario cancela.
     */
    int solicitarSeleccion(String titulo, String[] opciones);
}
