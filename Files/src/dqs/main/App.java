package dqs.main;
import dqs.vista.*;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada minimal que lanza la vista gráfica principal.
 *
 * Esta clase delega la responsabilidad de construir y mostrar la ventana
 * en {@link dqs.vista.VistaIniciarBatallaNueva}. Se asegura de que la
 * creación de la UI ocurra en el Event Dispatch Thread usando
 * {@link SwingUtilities#invokeLater(Runnable)}.
 */
public class App {

    public static void main(String[] args) {
        System.out.println("  ¡Bienvenido al Sistema de Batallas RPG!");
        System.out.println("==========================================");

        mostrarVistaIniciarBatallaNueva();
    }

    /**
     * Crea y muestra la vista principal en el EDT. Cualquier excepción se
     * imprime en la salida de error para facilitar depuración.
     */
    @SuppressWarnings("CallToPrintStackTrace")
    private static void mostrarVistaIniciarBatallaNueva() {
        try {
            SwingUtilities.invokeLater(() -> new VistaIniciarBatallaNueva());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al iniciar la vista: " + e.getMessage());
        }
    }
}
