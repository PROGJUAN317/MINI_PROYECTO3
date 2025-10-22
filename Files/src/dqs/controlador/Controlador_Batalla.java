package dqs.controlador;

public class Controlador_Batalla {
    private Batalla batalla;

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
