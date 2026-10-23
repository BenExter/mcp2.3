# Validación de Truchosafío 4 · 0.2.0-dev

- Gradle 8.9 con Java 21: `build` completado; JAR remapeado para Fabric 1.21.1.
- Lógica de escudo: 77 aserciones correctas.
- Integridad estática de recursos: 2451 aserciones correctas.
- Cargadores auténticos de GeckoLib 4.7.7: 8 modelos y 48 animaciones cargados.
- Servidor dedicado Fabric: arranque correcto; comandos de invocación de las siete criaturas ejecutados.
- Prueba de servidor fallida: `14 actual mechanisms destroyed`. No se ha establecido todavía si es un defecto de la mecánica o del montaje de la prueba. El test de servidor no está incluido en `build` y se ejecuta aparte mediante `runSmokeServer`.
- Sin prueba de renderizado, controles cliente, combate con jugadores ni multijugador real.
- DEDsafío 1.3.0 no pudo inspeccionarse: el adjunto excedió el límite de transferencia de 32 MiB. Compatibilidad no verificada.
- No se recreó la resurrección con fogata y cuchara.

## Archivos

El JAR publicado corresponde a la compilación comprobada de desarrollo; requiere Minecraft Java 1.21.1, Fabric, Fabric API y GeckoLib. El ZIP fuente incluye scripts de compilación, pruebas y los ocho modelos `.bbmodel`. La prueba fallida impide considerar esta una versión estable o lista para un evento hardcore.
