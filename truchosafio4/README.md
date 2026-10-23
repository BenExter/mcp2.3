# Truchosafío 4 — Hardcore Creatures · 0.2.0-dev

Mod Java para Minecraft 1.21.1, Fabric y GeckoLib 4. No es un datapack.

**Estado: compilación Gradle y remapeo Fabric completados. El servidor dedicado arrancó; una prueba de destrucción de mecanismos falló y queda pendiente de diagnóstico. Versión de desarrollo, sin validación gráfica ni multijugador.** Consulta `INFORME_VALIDACION.md`.

## Funcionalidad en el código

- Flashbang: dos planos ortogonales de grosor exactamente cero con regiones distintas de frente, espalda y ambos perfiles; geometría orientada, no billboard. Es arte 2D en dos tarjetas cruzadas, no un volumen sólido. Fogonazo, overlay con desvanecimiento, sonido y daño resueltos en servidor. Falta comprobar caras, orientación, transparencia e impacto visual dentro de Minecraft.
- Rojo: persecución y embestida con trayectoria sostenida y detección de proximidad durante el desplazamiento.
- Negro: roba hasta siete corazones máximos por UUID, con cooldown; almacenamiento por mundo y reaplicación al entrar o reaparecer. Escritura atómica y rechazo de datos dañados, sin sustituir los sistemas de vidas de otros mods.
- Amarillo: posesión temporal con invisibilidad del fantasma, náusea, lentitud y marca; hasta dos saltos a jugadores cercanos con línea de visión, demora e inmunidad temporal. No toma el teclado ni provoca ataques PvP del huésped. Libera el estado al morir, desconectarse, cambiar de dimensión o agotar el tiempo.
- Blanco: comparte objetivo entre miembros próximos y alterna rodeo con aproximación de ataque. La aparición natural permite grupos.
- Qumara: jefe de 1000 puntos de vida por defecto (máximo configurable 1024 por el atributo vanilla), control OP nivel 2, IA apagada durante el control, movimiento por paquetes, pisotón rompeescudos, vacío, escudo de 14 mecanismos y rugido que invoca raptores. Aura venenosa configurable, barra de jefe, partículas y fases de escudo persistidas.
- Los 14 mecanismos orbitan al jefe. La inmunidad depende de los mecanismos vivos registrados y no de un conteo de entidades en chunks cargados. Se rompe al destruir el último; no se regenera en el mismo jefe.
- Ocho modelos GeckoLib, texturas del proyecto fuente, animaciones de locomoción y habilidades; marcha articulada de Qumara y raptor. Ocho proyectos `.bbmodel` derivados con textura incrustada y animaciones.
- Aparición JSON por especie/dimensión/bioma, con todos los spawns naturales inicialmente desactivados. Respeta dificultad Pacífico, `doMobSpawning`, distancia a jugadores, chunks cargados, colisiones y límites de entidades cargadas por dimensión y alrededor de cada jugador.
- No registra ninguna dimensión del Limbo.
- No implementa ni recrea el ritual de resurrección con fogata/cuchara. El usuario indica que ya existe en su instalación.

## Compilar

Requisitos: JDK 21 completo (no solamente JRE), Python 3 para las comprobaciones y acceso a los repositorios oficiales declarados en Gradle.

Linux/macOS: `bash gradlew --no-daemon --max-workers=2 build`.
Windows: `gradlew.bat build` con JDK 21 y Python 3 disponibles. El paso `validateResources` usa `python3`; puede ejecutarse en WSL o ajustar la instalación de Python para ofrecer ese ejecutable.

El JAR esperado tras una compilación exitosa es `build/libs/truchosafio4-hardcore-0.2.0-dev.jar`. El archivo `-sources.jar` es código fuente, no un mod ejecutable. Instalar Fabric API y GeckoLib tanto en servidor como en clientes. No se incluyen dependencias ni DEDsafío en los paquetes fuente.

En este entorno cloud: `bash tools/instalar_jdk.sh`, `source tools/entorno.sh`, después la orden de Gradle anterior. Los scripts respetan la verificación TLS y los checksums. `tools/comprobar_local.sh` ejecuta las comprobaciones que no necesitan Minecraft ni Fabric.

## Administración

OP nivel 2:

- `/truchosafio spawn flashbang|rojo|negro|amarillo|blanco|qumara|raptor`
- `/truchosafio spawn rojo minecraft:overworld ~ ~ ~`
- `/truchosafio natural flashbang overworld on` y `off`; también `nether` y las otras especies.
- `/truchosafio reload` valida el JSON y conserva la configuración activa si la nueva es inválida.
- `/qumara spawn`, `/qumara control` (jefe más cercano), `/qumara release`.
- `/truchosafio qumara aura on|off`, mientras se controla el jefe.
- `/truchosafio hearts get <jugador>` y `/truchosafio hearts restore <jugador> <cantidad>`.

Configuración: `config/truchosafio4.json`; ejemplo en `config-ejemplo/`. Los comandos de aparición manual ignoran el interruptor de aparición natural. Los comandos abreviados de spawn natural cubren Overworld y Nether; otras dimensiones existentes se configuran por sus identificadores JSON.

Qumara: WASD, clic izquierdo (pisotón), clic derecho (vacío), R (escudo), F (raptores), Shift (desmontar). R/F son reasignables. Los mixins cliente interceptan ataque/uso/minado durante la montura; desactivan el intercambio vanilla de manos durante el control. Debe comprobarse la coexistencia de teclas con otros mods.

## Modelos editables

Abre `modelos-blockbench/*.bbmodel` en Blockbench. Se exportan mediante `python3 tools/exportar_blockbench.py`; contienen textura y keyframes. La apertura y exportación en la interfaz de Blockbench no se han probado aquí.

Los recursos canónicos del juego son `src/main/resources/assets/truchosafio4/geo/*.geo.json`, `animations/*.animation.json` y `textures/`. También puedes importar el modelo Bedrock directamente e importar la animación con el complemento GeckoLib correspondiente. No reemplaces los archivos de juego con un `.bbmodel`.

## Compatibilidad DEDsafío

El objetivo actualizado por el usuario es DEDsafío 4 Fan 1.3.0 para 1.21.1. El adjunto excede el límite de transferencia de 32 MiB de la herramienta y no pudo inspeccionarse. **Compatibilidad no verificada** con 1.3.0 ni 1.2.0. No hay integración especulativa con sus comandos internos, baneos, resurrección o dimensiones. Se requiere una prueba conjunta con el JAR real y sus dependencias.

Las texturas y el sonido existentes se conservan del ZIP suministrado; no se copiaron recursos de DEDsafío. El código original declaraba MIT; los permisos del sonido original deben conservarse según su procedencia.
