package dqs.controlador;

import java.util.Set;

public interface VistaBatalla {
    void refreshUI(); // coincide con el método existente en la vista
    void appendLog(String linea);
    void habilitarAcciones(Set<AccionSeleccionada> acciones);
}
