package dqs.modelos;

import dqs.events.BattleEventBus;

import java.util.Scanner;

public class Heroe extends Personaje implements Sanador, Tanque, Hechicero {
    private final Tipo_Heroe tipo;

    public Heroe(String nombre, Tipo_Heroe tipo, int hp, int mp, int ataque, int defensa, int velocidad) {
        super(nombre, hp, mp, ataque, defensa, velocidad);
        this.tipo = tipo;

        if(!tipo.validarAtributos(hp, mp, ataque, defensa)) {
            throw new IllegalArgumentException(
                "Atributos fuera del rango permitido para el tipo " + tipo.name() +
                "\nHP: " + tipo.getMinHP() + " - " + tipo.getMaxHP() +
                " |MP: " + tipo.getMinMP() + " - " + tipo.getMaxMP() +
                " |Ataque: " + tipo.getMinAtaque() + " - " + tipo.getMaxAtaque() +
                " |Defensa: " + tipo.getMinDefensa() + " - " + tipo.getMaxDefensa()
            );
        }
    }
    
    //METODO PARA CREAR UN HEROE PIDIENDO DATOS POR CONSOLA
    public static Heroe crearHeroePorConsola() {
        Scanner sc = new Scanner(System.in);

    BattleEventBus.log("Crear héroe: ");
    BattleEventBus.log("Nombre: ");
        String nombre = sc.nextLine();

    BattleEventBus.log("Seleccione el tipo de heroe: ");
        for (Tipo_Heroe t : Tipo_Heroe.values()) {
                BattleEventBus.log("- " + t.name() + ": " + t.getDescripcion());
        }

        Tipo_Heroe tipo = null;
        while (tipo == null) {
            BattleEventBus.log("Ingrese el tipo (MAGO/GUERRERO/PALADIN/DRUIDA): ");
            String entrada = sc.nextLine().toUpperCase();
            try {
                tipo = Tipo_Heroe.valueOf(entrada);
            } catch (IllegalArgumentException e) {
                BattleEventBus.log("Tipo inválido. Intente de nuevo.");
            } 
        }

    BattleEventBus.log("\nIngrese los atributos dentro de los rasgos permitidos:");
    BattleEventBus.log("HP: " + tipo.getMinHP() + " - " + tipo.getMaxHP());
    BattleEventBus.log("MP: " + tipo.getMinMP() + " - " + tipo.getMaxMP());
    BattleEventBus.log("Ataque: " + tipo.getMinAtaque() + " - " + tipo.getMaxAtaque());
    BattleEventBus.log("Defensa: " + tipo.getMinDefensa() + " - " + tipo.getMaxDefensa());

        int hp = pedirEnRango(sc, "HP", tipo.getMinHP(), tipo.getMaxHP());
        int mp = pedirEnRango(sc, "MP", tipo.getMinMP(), tipo.getMaxMP());
        int ataque = pedirEnRango(sc, "Ataque", tipo.getMinAtaque(), tipo.getMaxAtaque());
        int defensa = pedirEnRango(sc, "Defensa", tipo.getMinDefensa(), tipo.getMaxDefensa());
        int velocidad = (int)(Math.random() * 20 + 10);

        return new Heroe(nombre, tipo, hp, mp, ataque, defensa, velocidad);
    }

//METODO AUXILIAR PARA PEDIR NUMEROS DENTRO DE UN RANGO
 private static int pedirEnRango(Scanner sc, String atributo, int min, int max) {
        int valor;
        while (true) {
            BattleEventBus.log(atributo + ": ");
            try {
                valor = Integer.parseInt(sc.nextLine());
                if (valor >= min && valor <= max) {
                    break;
                } else {
                    BattleEventBus.log(" El valor debe estar entre " + min + " y " + max + ".");
                }
            } catch (NumberFormatException e) {
                BattleEventBus.log(" Ingresa un número válido.");
            }
        }
        return valor;
    }

    public void mostrarEstado() {
    BattleEventBus.log("\n " + nombre + " [" + tipo.name() + "]");
    BattleEventBus.log("HP: " + hp + "\n"+ " MP: " + mp + "\n" +
               " | Ataque: " + ataque +"\n" + " | Defensa: " + defensa + "\n" +
               " | Velocidad: " + velocidad);
    BattleEventBus.log("""
                           
               Descripción: """ + tipo.getDescripcion());
    BattleEventBus.log("--------------------------------------");
    }

    public Tipo_Heroe getTipo() {
        return tipo;
    }

    @Override
    public void elegirAccion() {
    BattleEventBus.log(nombre + " (" + tipo.name() + ") esta eligiendo su acción...");
    }
    
    // MÉTODOS DE LA INTERFAZ TANQUE
    @Override
    public void aumentarDefensa(int defensa) {
        if (tipo == Tipo_Heroe.GUERRERO || tipo == Tipo_Heroe.PALADIN) {
            if (mp >= 10) {
                mp -= 10;
                this.defensa += defensa;
                BattleEventBus.log(nombre + " aumenta su defensa en " + defensa + " puntos.");
            } else {
                BattleEventBus.log(nombre + " no tiene suficiente MP para aumentar defensa.");
            }
        } else {
            BattleEventBus.log(nombre + " no puede aumentar defensa.");
        }
    }

    // Método para defender a un aliado
    @Override
    public void defender(Personaje aliado) {
        if (tipo == Tipo_Heroe.GUERRERO || tipo == Tipo_Heroe.PALADIN) {
            if (mp >= 10) {
                mp -= 10;
                
                // Remover defensa anterior si existe
                if (aliado.estaSiendoDefendido()) {
                    aliado.removerDefensa();
                }
                
                // Activar nueva defensa
                aliado.recibirDefensa(this);
                BattleEventBus.log(nombre + " está defendiendo a " + aliado.getNombre() + 
                                 "! Los próximos ataques tendrán defensa combinada.");
            } else {
                BattleEventBus.log(nombre + " no tiene suficiente MP para defender.");
            }
        } else {
            BattleEventBus.log(nombre + " no puede defender a otros.");
        }
    }
    
    @Override
    public void provocarEnemigo(Personaje enemigo) {
        if (tipo == Tipo_Heroe.GUERRERO || tipo == Tipo_Heroe.PALADIN) {
            if (mp >= 5) {
                mp -= 5;
                
                // Remover provocación anterior si existe
                if (enemigo.estaProvocado()) {
                    enemigo.removerProvocacion();
                }
                
                // Aplicar nueva provocación
                enemigo.serProvocado(this);
                BattleEventBus.log(nombre + " provoca a " + enemigo.getNombre() + 
                                 "! El enemigo debe atacar al tanque en su próximo turno.");
            } else {
                BattleEventBus.log(nombre + " no tiene suficiente MP para provocar.");
            }
        } else {
            BattleEventBus.log(nombre + " no puede provocar enemigos.");
        }
    }
    
    // Método para dejar de defender a un aliado
    public void dejarDeDefender(Personaje aliado) {
        if (aliado.estaSiendoDefendido() && aliado.getDefensor() == this) {
            aliado.removerDefensa();
            BattleEventBus.log(nombre + " ha dejado de defender a " + aliado.getNombre());
        }
    }
    
    // Método para provocar a todos los enemigos en un arreglo (área de efecto)
    public void provocarTodosLosEnemigos(Personaje[] enemigos) {
        if (tipo == Tipo_Heroe.GUERRERO || tipo == Tipo_Heroe.PALADIN) {    
            int costoPorEnemigo = 3;
            int enemigosVivos = 0;
            
            // Contar enemigos vivos
            for (Personaje enemigo : enemigos) {
                if (enemigo != null && enemigo.esta_vivo()) {
                    enemigosVivos++;
                }
            }
            
            int costoTotal = costoPorEnemigo * enemigosVivos;
            
            if (mp >= costoTotal) {
                mp -= costoTotal;
                BattleEventBus.log(nombre + " provoca a todos los enemigos vivos!");
                
                for (Personaje enemigo : enemigos) {
                    if (enemigo != null && enemigo.esta_vivo()) {
                        if (enemigo.estaProvocado()) {
                            enemigo.removerProvocacion();
                        }
                        enemigo.serProvocado(this);
                    }
                }
            } else {
                BattleEventBus.log(nombre + " no tiene suficiente MP para provocar a todos los enemigos. Costo: " + costoTotal);
            }
        } else {
                BattleEventBus.log(nombre + " no puede provocar enemigos.");
        }
    }
    // MÉTODOS DE LA INTERFAZ SANADOR
    @Override
    public void curar(Personaje objetivo) {
        if (tipo == Tipo_Heroe.DRUIDA || tipo == Tipo_Heroe.PALADIN) {
            if (mp >= 15) {
                mp -= 15;
                int curacion = 30;
                objetivo.setHp(objetivo.getHp() + curacion);
                BattleEventBus.log(nombre + " ha curado a " + objetivo.getNombre() + " por " + curacion + " puntos de vida.");
            } else {
                BattleEventBus.log(nombre + " no tiene suficiente MP para curar.");
            }
        } else {
            BattleEventBus.log(nombre + " no puede curar.");
        }
    }

    @Override
    public void revivir(Personaje objetivo) {
        if (tipo == Tipo_Heroe.PALADIN) {
            if (!objetivo.esta_vivo() && mp >= 25) {
                mp -= 25;
                objetivo.setHp(50);
                // Asegurar que el personaje revivido pueda actuar: limpiar efectos y marcas
                objetivo.turnosParalisis = 0;
                objetivo.turnosSueno = 0;
                objetivo.removerProvocacion();
                objetivo.removerDefensa();
                objetivo.esta_vivo = true; // asegurar bandera activa
                BattleEventBus.log(nombre + " ha revivido a " + objetivo.getNombre() + " con 50 puntos de vida.");
            } else if(objetivo.esta_vivo()) {
                BattleEventBus.log(objetivo.getNombre() + " ya está vivo.");
            } else {
                BattleEventBus.log(nombre + " no tiene suficiente MP para revivir.");
            }
        } else {
            BattleEventBus.log(nombre + " no puede revivir a otros.");
        }
    }
    @Override
    public void restaurarMana(Personaje objetivo) {
        if (tipo == Tipo_Heroe.DRUIDA) {
            if (mp >= 20) {
                mp -= 10;
                objetivo.setMp(objetivo.getMp() + 25);
                BattleEventBus.log(nombre + " ha restaurado 25 puntos de MP a " + objetivo.getNombre() + ".");
            } else {
                BattleEventBus.log(nombre + " no tiene suficiente MP para restaurar.");
            }
        } else {
            BattleEventBus.log(nombre + " no puede restaurar mana.");
        }
    }

    @Override
    public void eliminarEfectoNegativo(Personaje objetivo) {
        if (tipo == Tipo_Heroe.DRUIDA || tipo == Tipo_Heroe.PALADIN) {
            BattleEventBus.log(nombre + " elimina efectos negativos de " + objetivo.getNombre() + ".");
            // Lógica para limpiar estados negativos
        } else {
            BattleEventBus.log(nombre + " no puede eliminar efectos negativos de " + objetivo.getNombre() + ".");
        }
    }

    //METODOS DE LA INTERFAZ HECHICERO

    @Override
    public void LanzaHechizoSueño(Personaje objetivo) {
        // Ahora el hechizo de sueño aplica el estado de sueño por 3 turnos
        if (tipo == Tipo_Heroe.MAGO || tipo == Tipo_Heroe.DRUIDA) {
            int costoMana = 20;
            if (mp >= costoMana) {
                mp -= costoMana;
                objetivo.aplicarSueno(3); // dormir 3 turnos
            } else {
                BattleEventBus.log(nombre + " no tiene suficiente MP para lanzar el hechizo.");
            }
        } else {
            BattleEventBus.log(nombre + " no puede lanzar hechizos.");
        }
    }

    @Override
    public void LanzaHechizoRefuerzo(Personaje objetivo) {
        if (tipo == Tipo_Heroe.MAGO || tipo == Tipo_Heroe.DRUIDA) {
            int costoMana = 20;
            if (mp >= costoMana) {
                mp -= costoMana;
                int aumento = 60;
                objetivo.aumentarAtaque(aumento);
                BattleEventBus.log(nombre + " lanza el hechizo Refuerzo a " + objetivo.getNombre() +
                                 " aumentando su ataque en " + aumento + " puntos.");
            } else {
                BattleEventBus.log(nombre + " no tiene suficiente MP para lanzar el hechizo.");
            }
        } else {
            BattleEventBus.log(nombre + " no puede lanzar hechizos.");
        }
    }

    //Metodo para paralizar a un enemigo
    @Override
    public void LanzaHechizoParalisis(Personaje objetivo) {
        // La parálisis ahora aplica 1 turno de incapacidad mediante aplicarParalisis
        if (tipo == Tipo_Heroe.MAGO || tipo == Tipo_Heroe.DRUIDA) {
            int costoMana = 25;
            if (mp >= costoMana) {
                mp -= costoMana;
                objetivo.aplicarParalisis(1);
            } else {
                BattleEventBus.log(nombre + " no tiene suficiente MP para lanzar el hechizo.");
            }
        } else {
            BattleEventBus.log(nombre + " no puede lanzar hechizos.");
        }
    }

    // MÉTODO DE ATAQUE
    public void atacar(Personaje objetivo) {
        if (objetivo != null && objetivo.esta_vivo()) {
            int daño = this.ataque - objetivo.getDefensa();
            if (daño < 1) daño = 1; // Daño mínimo de 1
            
            objetivo.recibir_daño(daño);
            BattleEventBus.log(this.nombre + " (" + tipo.name() + ") ataca a " + objetivo.getNombre() + 
                             " causando " + daño + " puntos de daño!");
            
            if (!objetivo.esta_vivo()) {
                BattleEventBus.log(objetivo.getNombre() + " ha sido derrotado!");
            }
        } else {
            BattleEventBus.log(this.nombre + " no puede atacar a un objetivo inválido o muerto.");
        }
    }

    // Método para atacar en el contexto de una batalla (busca automáticamente enemigos)
    public void atacarEnemigo(Enemigo[] enemigos) {
        Enemigo objetivo = buscarEnemigoVivo(enemigos);
        if (objetivo != null) {
            atacar(objetivo);
        } else {
            BattleEventBus.log(this.nombre + " no encuentra enemigos vivos para atacar.");
        }
    }

    // Método auxiliar para buscar un enemigo vivo
    private Enemigo buscarEnemigoVivo(Enemigo[] enemigos) {
        for (Enemigo enemigo : enemigos) {
            if (enemigo != null && enemigo.esta_vivo()) {
                return enemigo;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Heroe: " + nombre + " | " + tipo.name() + " | HP: " + hp +
         " | MP: " + mp +
         " | Ataque: " + ataque +
         " | Defensa: " + defensa +
         " | Velocidad: " + velocidad;
    }

    public int getPorcentajeHP() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPorcentajeHP'");
    }
}
