## Estado MVC del proyecto MINI_PROYECTO3

Fecha: 18 de noviembre de 2025

Este documento resume el estado actual del proyecto respecto al patrón MVC, los cambios realizados para desacoplar presentación y lógica, cómo probar el comportamiento (modo GUI y CLI) y los siguientes pasos recomendados.

---

### Resumen ejecutivo

- El paquete `dqs.modelos` contiene la lógica de negocio (Personaje, Heroe, Enemigo, Batalla, JefeEnemigo, etc.).
- Para evitar que las clases del modelo impriman directamente en consola, se introdujo un mecanismo de publicación de eventos del dominio: `BattleEventBus` + `BattleEventListener`.
- Las llamadas a `System.out.println` en los modelos fueron reemplazadas por `BattleEventBus.log(...)`, por lo que el modelo *emite mensajes de dominio* pero NO realiza IO directo.
- El controlador `ControladorVistaBatalla` y la vista `VistaIniciarBatallaNueva` consumen esos mensajes: el controlador puede registrar un listener que reenvíe los mensajes a la vista (`appendLog`) o, en modo CLI, puede registrarse un `ConsoleBattleEventListener` para imprimir en consola.

Estado actual respecto a las reglas MVC:

- Modelo: contiene reglas de negocio, estados y métodos que modifican el estado (cumple).
- Vista: contiene únicamente la UI (Swing) y emite eventos hacia el controlador (cumple).
- Controlador: orquesta la batalla y actúa como intermediario (cumple).

Importante: para respetar completamente la separación, evitamos que el modelo importe clases del controlador. Para lograrlo, el bus/listener fue colocado en un lugar neutral dentro del código (paquete `dqs.controlador` actualmente). Recomendación mínima: mover el bus/listener a un paquete neutral (`dqs.events` o `dqs.util`) para eliminar cualquier dependencia conceptual del modelo al controlador.

---

### Cambios aplicados en esta iteración

- Añadidas las entidades de eventos:
  - `dqs.controlador.BattleEventListener` (interfaz)
  - `dqs.controlador.BattleEventBus` (bus estático, con fallback a System.out)
  - `dqs.controlador.ConsoleBattleEventListener` (listener que imprime en consola)
- Reemplazadas llamadas a `System.out.println`/`print` en las clases de `dqs.modelos` por `BattleEventBus.log(...)`.
- `ControladorVistaBatalla` puede registrar un listener para recibir eventos del dominio y reenviarlos a la vista.
- Se ofreció una línea fácil de cambiar para alternar entre GUI y CLI:
  - CLI (actual en la rama): registrar `ConsoleBattleEventListener` en `ControladorVistaBatalla.iniciar()`.
  - GUI: registrar el propio controlador como listener (`BattleEventBus.setListener(this)`) para que la vista reciba los mensajes.

Archivos modificados (lista relevante)

- Files/src/dqs/controlador/BattleEventListener.java (nuevo)
- Files/src/dqs/controlador/BattleEventBus.java (nuevo)
- Files/src/dqs/controlador/ConsoleBattleEventListener.java (nuevo)
- Files/src/dqs/controlador/ControladorVistaBatalla.java (registrado listener CLI, o puede usarse el controlador como listener)
- Files/src/dqs/modelos/Personaje.java (reemplazadas impresiones por BattleEventBus.log)
- Files/src/dqs/modelos/Heroe.java (reemplazadas impresiones por BattleEventBus.log)
- Files/src/dqs/modelos/Enemigo.java (reemplazadas impresiones por BattleEventBus.log)
- Files/src/dqs/modelos/Batalla.java (reemplazadas impresiones por BattleEventBus.log)
- Files/src/dqs/modelos/JefeEnemigo.java (reemplazadas impresiones por BattleEventBus.log)

---

### Cómo probar (rápido)

Compilar todo (PowerShell):

```powershell
$files = Get-ChildItem -Path 'Files/src' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
javac -d Files/bin $files
```

Ejecutar la aplicación (CLI/ejemplo):

```powershell
java -cp Files/bin dqs.main.App
```

Notas:
- Por defecto en la rama actual el controlador registra `ConsoleBattleEventListener`, por lo que verás los mensajes del modelo impresos en la consola.
- Para que la vista GUI los reciba en lugar de la consola, cambia en `ControladorVistaBatalla.iniciar()` la línea que registra el listener por:

```java
BattleEventBus.setListener(this);
```

y recompila.

---

### Recomendaciones y próximos pasos

1) Mover `BattleEventBus` y `BattleEventListener` a un paquete neutral (por ejemplo `dqs.events`) para eliminar la dependencia conceptual modelo→controlador.
2) Eliminar las funciones interactivas que queden en el modelo (`Heroe.crearHeroePorConsola()`, `Batalla.crearEnemigoInteractivo()`): deben residir en la vista o en `BatallaManager` (refactor para separación completa).
3) Considerar convertir mensajes libres en eventos estructurados (`DomainEvent`/`ActionResult`) para permitir: testing más robusto, localización y formateo por la vista.
4) Refactorizar `BatallaManager` para exponer APIs no interactivas que el controlador invoca; la UI debe ser la única fuente de IO.

---

### Resumen final

Con los cambios aplicados el flujo queda así:

Usuario (vista) → Controlador → Modelo (ejecuta lógica y publica eventos) → Bus de eventos → Listener (Controlador o Console) → Vista

Esto preserva el requisito de que el Modelo siga *emitiendo mensajes de dominio* pero evita que haga IO directo. Para llegar al 100% MVC queda la tarea de mover el bus a un paquete neutral y retirar cualquier lectura/IO aún presente en `modelos`.

Si quieres que aplique ya el parche mínimo para mover el bus a `dqs.events` (cambio de paquetes y ajustes de imports) lo hago ahora y valido compilación/ejecución.

---

Archivo generado por la iteración de refactor MVC — si quieres cambios en el contenido dime qué añadir.
