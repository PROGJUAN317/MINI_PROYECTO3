package dqs.vista;

import dqs.controlador.VistaBatalla;
import dqs.controlador.AccionSeleccionada;
import dqs.controlador.ControladorVistaBatalla;
import dqs.events.BattleEventListener;

import java.util.Scanner;
import java.util.Set;

/**
 * Vista de consola que implementa la interfaz `VistaBatalla`.
 * Es responsable de mostrar mensajes y leer opciones del usuario
 * cuando la aplicación se ejecuta en modo CLI.
 */
public class VistaBatallaCLI implements VistaBatalla, BattleEventListener {
    private ControladorVistaBatalla controlador;
    private final Scanner scanner = new Scanner(System.in);

    public VistaBatallaCLI() {}

    public void setControlador(ControladorVistaBatalla controlador) {
        this.controlador = controlador;
    }

    @Override
    public void refreshUI() {
        // En consola no necesitamos refrescar una UI, pero podemos imprimir un separador
        System.out.println("\n[REFRESH]");
    }

    @Override
    public void appendLog(String linea) {
        System.out.println(linea);
    }

    @Override
    public void habilitarAcciones(Set<AccionSeleccionada> acciones) {
        if (controlador == null) return;
        // Intentar obtener las opciones textuales desde el controlador/manager
        String[] opciones = null;
        try {
            opciones = controlador.getOpcionesParaHeroeActual();
        } catch (Exception ignored) {}

        if (opciones == null || opciones.length == 0) {
            // Fallback: mostrar los nombres de los enums como antes
            System.out.println("Seleccione acción:");
            int i = 1;
            AccionSeleccionada[] mapa = acciones.toArray(new AccionSeleccionada[0]);
            for (AccionSeleccionada a : mapa) {
                System.out.println(i + ". " + a.name());
                i++;
            }
            System.out.print("Opción: ");
            try {
                String line = scanner.nextLine();
                int opt = Integer.parseInt(line.trim());
                if (opt >= 1 && opt <= mapa.length) {
                    controlador.seleccionarAccion(mapa[opt - 1]);
                } else {
                    System.out.println("Opción inválida.");
                    habilitarAcciones(acciones);
                }
            } catch (Exception e) {
                System.out.println("Entrada inválida.");
                habilitarAcciones(acciones);
            }
            return;
        }

        // Mostrar las opciones textuales y enviar el índice seleccionado al controlador
        System.out.println("Seleccione acción:");
        for (int i = 0; i < opciones.length; i++) {
            System.out.println((i + 1) + ". " + opciones[i]);
        }
        System.out.print("Opción: ");
        try {
            String line = scanner.nextLine();
            int opt = Integer.parseInt(line.trim());
            if (opt >= 1 && opt <= opciones.length) {
                controlador.seleccionarAccionPorIndice(opt - 1);
            } else {
                System.out.println("Opción inválida.");
                habilitarAcciones(acciones);
            }
        } catch (Exception e) {
            System.out.println("Entrada inválida.");
            habilitarAcciones(acciones);
        }
    }

    @Override
    public int solicitarSeleccion(String titulo, String[] opciones) {
        System.out.println(titulo);
        for (int i = 0; i < opciones.length; i++) {
            System.out.println((i + 1) + ". " + opciones[i]);
        }
        System.out.print("Seleccione (número) o 0 para cancelar: ");
        try {
            String line = scanner.nextLine();
            int opt = Integer.parseInt(line.trim());
            if (opt == 0) return -1;
            if (opt >= 1 && opt <= opciones.length) return opt - 1;
        } catch (Exception ignored) {}
        System.out.println("Entrada inválida.");
        return solicitarSeleccion(titulo, opciones);
    }

    @Override
    public void onEvent(String message) {
        appendLog(message);
    }
}
