# MINI_PROYECTO3

Nota importante para colaboradores
---------------------------------

Este proyecto usa una estructura de carpetas donde el código fuente Java se encuentra en `Files/src`.
Por compatibilidad con el servidor de lenguaje Java en VS Code, es recomendable crear un archivo local de configuración
que indique la carpeta fuente al editor.

1) Crear el archivo de configuración local (no lo subas al repositorio):

```json
// .vscode/settings.json
{
	"java.project.sourcePaths": ["Files/src"]
}
```

2) Por qué: si no se indica la carpeta fuente, VS Code puede reportar errores de paquete (por ejemplo: "The declared package 'dqs.modelo' does not match the expected package ''").

3) Importante: este archivo es local y puede variar entre colaboradores — por eso **no** debería subirse al repositorio. A continuación se ha actualizado `.gitignore` para evitar conflictos.

Si prefieres no usar VS Code, puedes compilar desde la raíz del proyecto ajustando la ruta de `javac` y ubicando la carpeta de origen correctamente.
