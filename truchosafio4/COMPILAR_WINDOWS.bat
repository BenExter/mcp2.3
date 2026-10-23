@echo off
setlocal
cd /d "%~dp0"
echo ========================================
echo TRUCHOSAFIO 4 - COMPILACION 1.21.1
 echo ========================================
java -version 2>&1
where java >nul 2>nul
if errorlevel 1 (
  echo ERROR: Instala Java JDK 21 (no solo JRE).
  pause
  exit /b 1
)
if not exist gradlew.bat (
  echo ERROR: falta el wrapper gradlew.bat
  pause
  exit /b 1
)
call gradlew.bat clean build --stacktrace
if errorlevel 1 (
  echo ERROR: no se pudo compilar. Copia el mensaje de error completo.
  pause
  exit /b 1
)
echo.
echo COMPILACION COMPLETA. Abre build\libs
explorer build\libs
pause
