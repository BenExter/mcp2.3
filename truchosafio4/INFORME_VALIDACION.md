# Validación · Flashbang 0.3.0-flashbang

## Comprobado

- `clean build`: compilación y remapeo correctos con Java 21, Gradle 8.9 y Fabric Loom 1.7.4, para Minecraft 1.21.1.
- 54 aserciones de integridad de recursos correctas.
- Cargadores auténticos de GeckoLib 4.7.7: un modelo y tres animaciones procesados correctamente.
- Auditoría del JAR remapeado: exactamente 15 clases del mod, un modelo, una textura de entidad y un modelo de huevo de Flashbang; sin otras criaturas, sistemas, mixins de control ni código de pruebas.
- Servidor dedicado Fabric: arranque y parada correctos; 20 aserciones correctas sobre registro exclusivo de entidad/huevo, comandos, invocación en Overworld y Nether, salud, serialización NBT, activación/desactivación del spawn y recarga JSON.
- Comparación con la entrega anterior: lógica de Flashbang, overlay, geometría, textura y audio idénticos byte a byte. Solo se retiraron las animaciones de habilidades ajenas que no se utilizaban.

## Sin comprobar

- Cliente gráfico, renderizado en pantalla, impacto del fogonazo y daño sobre jugadores reales.
- Aparición natural durante una sesión con jugadores, autenticación de cuentas y pruebas multijugador.
- Compatibilidad con DEDsafío 1.3.0 o 1.2.0.

La prueba dedicada se ejecutó localmente en modo offline, sin jugadores conectados. El log contiene avisos de autenticación externa y ausencia de data fixer personalizado; no impidieron el arranque ni las aserciones. Los resultados anteriores de mecanismos y fantasmas no aplican a esta edición: esos componentes fueron eliminados.
