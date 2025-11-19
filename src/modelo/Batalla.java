package modelo;

public class Batalla {

    private final Heroe[] equipoHeroes;
    private final Enemigo[] equipoEnemigos;
    private int turnoActual;
    private boolean batallaTerminada;

    // Constructor
    public Batalla(){
        this.equipoHeroes = new Heroe[4];
        this.equipoEnemigos = new Enemigo[3];
        this.turnoActual = 0;
        this.batallaTerminada = false;
    }

    // metodos para agregar heroes y enemigos 
    public void agregarHeroe(Heroe heroe, int posicion) {
        if (posicion < 0 || posicion >= equipoHeroes.length) { 
            throw new IllegalArgumentException("Posición inválida para el equipo de héroes.");
        }
        equipoHeroes[posicion] = heroe;
    }

    public void agregarEnemigo(Enemigo enemigo, int posicion) {
        if (posicion < 0 || posicion >= equipoEnemigos.length) {
            throw new IllegalArgumentException("Posición inválida para el equipo de héroes.");
        }
        equipoEnemigos[posicion] = enemigo;
        }

    // Getters
    public Heroe[] getEquipoHeroes() { return equipoHeroes; }
    public Enemigo[] getEquipoEnemigos() { return equipoEnemigos; }
    public boolean isBatallaTerminada() { return batallaTerminada; }
    public int getTurnoActual() { return turnoActual; }

    // Setters
    public void setBatallaTerminada(boolean batallaTerminada){
         this.batallaTerminada = batallaTerminada; }
         
    public void setTurnoActual(int turnoActual) {
         this.turnoActual = turnoActual; }

         
         /**
          * Método auxiliar para iniciar una batalla desde un controlador.
          * Implementación mínima: resetea el estado de la batalla.
          */
          public void iniciar() {
              this.turnoActual = 1;
              this.batallaTerminada = false;
            }
            
            /**
             * Marca la batalla como finalizada.
             */
            public void finalizar() {
                this.batallaTerminada = true;
            }
            
            public void siguienteTurno() {
                if (!batallaTerminada) {
                    turnoActual++;
                }
            }

}
