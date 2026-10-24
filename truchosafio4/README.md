# Truchosafío 4 — Flashbang · 0.3.0-flashbang

Mod para Minecraft Java 1.21.1, Fabric y Java 21. Esta edición contiene únicamente Flashbang.

Conserva su aspecto 2D, textura de frente/espalda/perfiles, sonido, fogonazo de pantalla y daño. La lógica de la criatura, el overlay, la textura y el modelo mantienen el comportamiento de la entrega anterior. Se eliminaron las demás criaturas, objetos, sistemas y controles.

## Instalación

Reemplaza el JAR anterior por `truchosafio4-flashbang-0.3.0-flashbang.jar` en `mods/`, en clientes y servidor. Instala Fabric API para 1.21.1 y GeckoLib 4.7.7. Utiliza una sola versión de Truchosafío a la vez; el identificador sigue siendo `truchosafio4`.

## Invocación y configuración

- `/truchosafio spawn flashbang`
- `/truchosafio spawn flashbang minecraft:the_nether ~ ~ ~`
- `/summon truchosafio4:flashbang`
- `/truchosafio natural flashbang overworld on` o `off`
- `/truchosafio natural flashbang nether on` o `off`
- `/truchosafio reload`

Los comandos propios requieren OP nivel 2. También se incluye su huevo de invocación en creativo.

El spawn natural empieza desactivado. La configuración está en `config/truchosafio4.json`; otras dimensiones existentes y biomas pueden configurarse por sus identificadores JSON. Al leer una configuración de una entrega anterior, solo se conserva la regla de Flashbang en memoria; los ajustes eliminados no se ejecutan y desaparecen al guardar la configuración mediante los comandos.

## Compilar y comprobar

Con JDK 21 y Python 3: `bash gradlew --no-daemon --max-workers=2 clean build`.

En Windows: `gradlew.bat clean build`, con Python disponible como `python3` para la validación de recursos.

`build` incluye la integridad estática de recursos y la carga mediante los cargadores reales de GeckoLib. `runSmokeServer` ejecuta las pruebas de integración en un servidor Fabric de desarrollo separado (`run-smoke/`). Para repetirlas, utiliza Java 21, una configuración de servidor de prueba en esa carpeta, y acepta la EULA de Minecraft en su archivo `eula.txt` antes de ejecutarlas. La configuración usada está en `config-ejemplo/servidor-pruebas.properties`. La prueba optativa de servidor está excluida del JAR distribuido.

El modelo editable está en `modelos-blockbench/flashbang.bbmodel`, con textura y animaciones incrustadas. Los recursos canónicos de juego están en `src/main/resources/assets/truchosafio4/`. El modelo y las tres animaciones se pueden volver a exportar con `python3 tools/exportar_blockbench.py`.

Consulta `INFORME_VALIDACION.md` para los resultados exactos y las pruebas de cliente pendientes. La compatibilidad con DEDsafío no se ha verificado.
