package dqs.controlador;

import dqs.modelo.Batalla;

public class Controlador_Batalla {
    private final Batalla batalla;

    public Controlador_Batalla(Batalla batalla) {
        this.batalla = batalla;
    }

    public void iniciarBatalla() {
        batalla.iniciar();
    }

    public void finalizarBatalla() {
        batalla.finalizar();
    }
}
