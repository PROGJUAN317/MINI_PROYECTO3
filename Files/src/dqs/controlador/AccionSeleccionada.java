package dqs.controlador;

/**
 * Enum público con las acciones disponibles en la interfaz de batalla.
 * Se separa del código de la vista para evitar dependencias circulares
 * y permitir que el controlador y la vista compartan el tipo.
 */
public enum AccionSeleccionada {
    NONE,
    ATTACK,
    DEFEND,
    PROVOKE,
    INCREASE_DEF,
    HEAL,
    RESTORE_MP,
    REMOVE_EFFECT,
    REVIVE,
    SLEEP,
    REINFORCE,
    PARALYZE,
    PASS
}
