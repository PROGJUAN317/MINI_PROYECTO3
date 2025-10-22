package dqs.modelo;

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

    // metodos para agregar heroes y enemigos al equipo
    public void agregarHeroe(Heroe heroe, int posicion) {
        if (posicion >= 0 && posicion < equipoHeroes.length) {
            equipoHeroes[posicion] = heroe;
        } else {
            throw new IllegalArgumentException("Posición inválida para el equipo de héroes.");
        }
    }

    public void agregarEnemigo(Enemigo enemigo, int posicion) {
        if (posicion >= 0 && posicion < equipoEnemigos.length) {
            equipoEnemigos[posicion] = enemigo;
        } else {
            throw new IllegalArgumentException("Posición inválida para el equipo de enemigos.");
        }
    }

    // Método para crear y agregar héroes directamente al arreglo
    public void crearYAgregarHeroe(int posicion) {
        if (posicion >= 0 && posicion < equipoHeroes.length) {
            System.out.println("\n=== Creando héroe para la posición " + (posicion + 1) + " ===");
            equipoHeroes[posicion] = Heroe.crearHeroePorConsola();
            System.out.println("¡Héroe agregado exitosamente!");
        } else {
            throw new IllegalArgumentException("Posición inválida para el equipo de héroes.");
        }
    }

    // Método para crear y agregar enemigos directamente al arreglo
    public void crearYAgregarEnemigo(int posicion) {
        if (posicion >= 0 && posicion < equipoEnemigos.length) {
            System.out.println("\n=== Creando enemigo para la posición " + (posicion + 1) + " ===");
            // Usar el primer tipo disponible como valor por defecto y un nombre generado automáticamente.
            equipoEnemigos[posicion] = Enemigo.crearEnemigo(Tipo_Enemigo.values()[0], "Enemigo " + (posicion + 1));
            System.out.println("¡Enemigo agregado exitosamente!");
        } else {
            throw new IllegalArgumentException("Posición inválida para el equipo de enemigos.");
        }
    }

    /**
     * Crear un enemigo de forma interactiva: pide posición, lista tipos,
     * pide nombre y registra el enemigo en la posición indicada.
     */
    public void crearEnemigoInteractivo(java.util.Scanner scanner) {
        System.out.print("Ingrese la posición (1-3): ");
        int posicion;
        try {
            String line = scanner.nextLine();
            posicion = Integer.parseInt(line) - 1;
        } catch (Exception e) {
            System.out.println(" Posición inválida (entrada no numérica).");
            return;
        }

        if (posicion >= 0 && posicion < equipoEnemigos.length) {
            System.out.println("Seleccione el tipo de enemigo:");
            Tipo_Enemigo[] tipos = Tipo_Enemigo.values();
            for (int i = 0; i < tipos.length; i++) {
                System.out.println((i + 1) + ". " + tipos[i].name() + " - " + tipos[i].getDescripcion());
            }
            System.out.print("Tipo: ");
            int tipoIndex;
            try {
                String tline = scanner.nextLine();
                tipoIndex = Integer.parseInt(tline) - 1;
            } catch (Exception ex) {
                System.out.println(" Tipo inválido (entrada no numérica).");
                return;
            }

            if (tipoIndex >= 0 && tipoIndex < Tipo_Enemigo.values().length) {
                System.out.print("Nombre del enemigo: ");
                String nombre = scanner.nextLine();
                Enemigo enemigo = Enemigo.crearEnemigo(Tipo_Enemigo.values()[tipoIndex], nombre);
                agregarEnemigo(enemigo, posicion);
                System.out.println(" Enemigo creado exitosamente!");
                enemigo.mostrarEstado();
            } else {
                System.out.println(" Tipo inválido.");
            }
        } else {
            System.out.println(" Posición inválida.");
        }
    }

    // Método para crear todo el equipo de héroes
    public void crearEquipoHeroes() {
        System.out.println("\n=== CREACIÓN DEL EQUIPO DE HÉROES ===");
        for (int i = 0; i < equipoHeroes.length; i++) {
            crearYAgregarHeroe(i);
        }
        System.out.println("\n¡Equipo de héroes completo!");
    }

    /**
     * Interfaz para crear un héroe interactivamente desde la entrada
     * estándar. Recibe un `Scanner` (proveído por la capa de UI) para
     * leer la posición solicitada y reutiliza `crearYAgregarHeroe`.
     *
     * Este método traslada a `Batalla` la responsabilidad de validación
     * y creación de héroes (antes estaba duplicada en `App`).
     */
    public void crearHeroeInteractivo(java.util.Scanner scanner) {
        System.out.print("Ingrese la posición (1-4): ");
        int posicion;
        try {
            String line = scanner.nextLine();
            posicion = Integer.parseInt(line) - 1;
        } catch (Exception e) {
            System.out.println(" Posición inválida (entrada no numérica).");
            return;
        }

        if (posicion >= 0 && posicion < equipoHeroes.length) {
            crearYAgregarHeroe(posicion);
        } else {
            System.out.println(" Posición inválida.");
        }
    }

    // Método para crear todo el equipo de enemigos
    public void crearEquipoEnemigos() {
        System.out.println("\n=== CREACIÓN DEL EQUIPO DE ENEMIGOS ===");
        for (int i = 0; i < equipoEnemigos.length; i++) {
            crearYAgregarEnemigo(i);
        }
        System.out.println("\n¡Equipo de enemigos completo!");
    }

    // Método para mostrar los equipos
    public void mostrarEquipos() {
        System.out.println("\n=== EQUIPOS DE BATALLA ===");
        
        System.out.println("\nEQUIPO DE HÉROES:");
        for (int i = 0; i < equipoHeroes.length; i++) {
            if (equipoHeroes[i] != null) {
                System.out.println((i + 1) + ". " + equipoHeroes[i].toString());
            } else {
                System.out.println((i + 1) + ". [Vacío]");
            }
        }
        
        System.out.println("\nEQUIPO DE ENEMIGOS:");
        for (int i = 0; i < equipoEnemigos.length; i++) {
            if (equipoEnemigos[i] != null) {
                System.out.println((i + 1) + ". " + equipoEnemigos[i].toString());
            } else {
                System.out.println((i + 1) + ". [Vacío]");
            }
        }
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
        System.out.println("Batalla iniciada (estado reseteado)." );
    }

    /**
     * Marca la batalla como finalizada.
     */
    public void finalizar() {
        this.batallaTerminada = true;
        System.out.println("Batalla finalizada por el controlador.");
    }


}
