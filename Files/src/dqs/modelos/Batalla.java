package dqs.modelos;

import dqs.events.BattleEventBus;

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
    // public void agregarHeroe(Heroe heroe, int posicion) {
    //     if (posicion >= 0 && posicion < equipoHeroes.length) {
    //         equipoHeroes[posicion] = heroe;
    //     } else {
    //         throw new IllegalArgumentException("Posición inválida para el equipo de héroes.");
    //     }
    // }

    public void agregarEnemigo(Enemigo enemigo, int posicion) {
        if (posicion >= 0 && posicion < equipoEnemigos.length) {
            equipoEnemigos[posicion] = enemigo;
        } else {
            throw new IllegalArgumentException("Posición inválida para el equipo de enemigos.");
        }
    }

    // Método para crear y agregar héroes directamente al arreglo
    // public void crearYAgregarHeroe(int posicion) {
    //     if (posicion >= 0 && posicion < equipoHeroes.length) {
    //         System.out.println("\n=== Creando héroe para la posición " + (posicion + 1) + " ===");
    //         equipoHeroes[posicion] = Heroe.crearHeroePorConsola();
    //         System.out.println("¡Héroe agregado exitosamente!");
    //     } else {
    //         throw new IllegalArgumentException("Posición inválida para el equipo de héroes.");
    //     }
    // }

    // Método para crear y agregar enemigos directamente al arreglo
    public void crearYAgregarEnemigo(int posicion) {
        if (posicion >= 0 && posicion < equipoEnemigos.length) {
            BattleEventBus.log("\n=== Creando enemigo para la posición " + (posicion + 1) + " ===");
            // Selección aleatoria de tipo de enemigo para evitar siempre el primer tipo
            Tipo_Enemigo[] tipos = Tipo_Enemigo.values();
            Tipo_Enemigo elegido = tipos[(int)(Math.random() * tipos.length)];
            equipoEnemigos[posicion] = Enemigo.crearEnemigo(elegido, "Enemigo " + (posicion + 1));
            BattleEventBus.log("¡Enemigo agregado exitosamente!");
        } else {
            throw new IllegalArgumentException("Posición inválida para el equipo de enemigos.");
        }
    }

    /**
     * Crear un enemigo de forma interactiva: pide posición, lista tipos,
     * pide nombre y registra el enemigo en la posición indicada.
     */
    public void crearEnemigoInteractivo(java.util.Scanner scanner) {
    BattleEventBus.log("Ingrese la posición (1-3): ");
        int posicion;
        try {
            String line = scanner.nextLine();
            posicion = Integer.parseInt(line) - 1;
        } catch (NumberFormatException e) {
            BattleEventBus.log(" Posición inválida (entrada no numérica).");
            return;
        }

        if (posicion >= 0 && posicion < equipoEnemigos.length) {
            BattleEventBus.log("Seleccione el tipo de enemigo:");
            Tipo_Enemigo[] tipos = Tipo_Enemigo.values();
            for (int i = 0; i < tipos.length; i++) {
                BattleEventBus.log((i + 1) + ". " + tipos[i].name() + " - " + tipos[i].getDescripcion());
            }
            BattleEventBus.log("Tipo: ");
            int tipoIndex;
            try {
                String tline = scanner.nextLine();
                tipoIndex = Integer.parseInt(tline) - 1;
            } catch (NumberFormatException ex) {
                BattleEventBus.log(" Tipo inválido (entrada no numérica).");
                return;
            }

            if (tipoIndex >= 0 && tipoIndex < Tipo_Enemigo.values().length) {
                BattleEventBus.log("Nombre del enemigo: ");
                String nombre = scanner.nextLine();
                Enemigo enemigo = Enemigo.crearEnemigo(Tipo_Enemigo.values()[tipoIndex], nombre);
                agregarEnemigo(enemigo, posicion);
                BattleEventBus.log(" Enemigo creado exitosamente!");
                enemigo.mostrarEstado();
            } else {
                BattleEventBus.log(" Tipo inválido.");
            }
        } else {
                BattleEventBus.log(" Posición inválida.");
        }
    }

    // Método para crear todo el equipo de héroes
    // public void crearEquipoHeroes() {
    //     System.out.println("\n=== CREACIÓN DEL EQUIPO DE HÉROES ===");
    //     for (int i = 0; i < equipoHeroes.length; i++) {
    //         crearYAgregarHeroe(i);
    //     }
    //     System.out.println("\n¡Equipo de héroes completo!");
    // }

    /**
     * Interfaz para crear un héroe interactivamente desde la entrada
     * estándar. Recibe un `Scanner` (proveído por la capa de UI) para
     * leer la posición solicitada y reutiliza `crearYAgregarHeroe`.
     *
     * Este método traslada a `Batalla` la responsabilidad de validación
     * y creación de héroes (antes estaba duplicada en `App`).
     */
    // public void crearHeroeInteractivo(java.util.Scanner scanner) {
    //     System.out.print("Ingrese la posición (1-4): ");
    //     int posicion;
    //     try {
    //         String line = scanner.nextLine();
    //         posicion = Integer.parseInt(line) - 1;
    //     } catch (Exception e) {
    //         System.out.println(" Posición inválida (entrada no numérica).");
    //         return;
    //     }

    //     if (posicion >= 0 && posicion < equipoHeroes.length) {
    //         crearYAgregarHeroe(posicion);
    //     } else {
    //         System.out.println(" Posición inválida.");
    //     }
    // }
    // public void crearHeroeInteractivo(java.util.Scanner scanner) {
    //     System.out.print("Ingrese la posición (1-4): ");
    //     int posicion;
    //     try {
    //         String line = scanner.nextLine();
    //         posicion = Integer.parseInt(line) - 1;
    //     } catch (NumberFormatException e) {
    //         System.out.println(" Posición inválida (entrada no numérica).");
    //         return;
    //     }

    //     if (posicion >= 0 && posicion < equipoHeroes.length) {
    //         crearYAgregarHeroe(posicion);
    //     } else {
    //         System.out.println(" Posición inválida.");
    //     }
    // }

    // Método para crear todo el equipo de enemigos
    public void crearEquipoEnemigos() {
    BattleEventBus.log("\n=== CREACIÓN DEL EQUIPO DE ENEMIGOS ===");
        // Primero crear enemigos normales aleatorios
        for (int i = 0; i < equipoEnemigos.length; i++) {
            crearYAgregarEnemigo(i);
        }

        // Insertar un jefe en una posición aleatoria del arreglo de enemigos
        int posicionJefe = (int)(Math.random() * equipoEnemigos.length);
        Tipo_JefeEnemigo[] tiposJefes = Tipo_JefeEnemigo.values();
        Tipo_JefeEnemigo jefeElegido = tiposJefes[(int)(Math.random() * tiposJefes.length)];
        // Crear jefe con la fábrica y sobrescribir la posición
        JefeEnemigo jefe = JefeFactory.crearJefe(jefeElegido, "Jefe " + (posicionJefe + 1));
        equipoEnemigos[posicionJefe] = jefe;

    BattleEventBus.log("\n¡Equipo de enemigos completo! (incluye jefe en la posición " + (posicionJefe + 1) + ")");
    }

    /**
     * Crea un equipo de héroes por defecto (útil para pruebas rápidas).
     * Llena las 4 posiciones con héroes de tipos y atributos por defecto.
     */
    public void crearEquipoHeroesPorDefecto() {
    BattleEventBus.log("\n=== CREANDO EQUIPO DE HÉROES POR DEFECTO PARA PRUEBAS ===");
        // Asegurarse de que los atributos respeten los rangos definidos en Tipo_Heroe
        equipoHeroes[0] = new Heroe("Aldor", Tipo_Heroe.GUERRERO, 220, 20, 100, 25, 20);
        // MERINA (MAGO): ataque y defensa ajustados para cumplir los rangos del mago
        equipoHeroes[1] = new Heroe("Merina", Tipo_Heroe.MAGO, 90, 200, 60, 20, 18);
        equipoHeroes[2] = new Heroe("Tharok", Tipo_Heroe.PALADIN, 160, 80, 120, 30, 16);
        // LYRA (DRUIDA): ataque y defensa ajustados para cumplir los rangos del druida
        equipoHeroes[3] = new Heroe("Lyra", Tipo_Heroe.DRUIDA, 120, 140, 80, 25, 22);
    BattleEventBus.log("Equipo de héroes por defecto creado.");
    }

    // Método para mostrar los equipos
    public void mostrarEquipos() {
    BattleEventBus.log("\n=== EQUIPOS DE BATALLA ===");
        
    BattleEventBus.log("\nEQUIPO DE HÉROES:");
        for (int i = 0; i < equipoHeroes.length; i++) {
            if (equipoHeroes[i] != null) {
                BattleEventBus.log((i + 1) + ". " + equipoHeroes[i].toString());
            } else {
                BattleEventBus.log((i + 1) + ". [Vacío]");
            }
        }
        
    BattleEventBus.log("\nEQUIPO DE ENEMIGOS:");
        for (int i = 0; i < equipoEnemigos.length; i++) {
            if (equipoEnemigos[i] != null) {
                BattleEventBus.log((i + 1) + ". " + equipoEnemigos[i].toString());
            } else {
                BattleEventBus.log((i + 1) + ". [Vacío]");
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
    BattleEventBus.log("Batalla iniciada (estado reseteado)." );
    }

    /**
     * Marca la batalla como finalizada.
     */
    public void finalizar() {
    this.batallaTerminada = true;
    BattleEventBus.log("Batalla finalizada por el controlador.");
    }


}
