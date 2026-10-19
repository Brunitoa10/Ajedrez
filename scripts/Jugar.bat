@echo off
REM Ejecuta la aplicacion desde el jar (requiere Java 21+ instalado).
REM Si el jar no existe, primero lo compila con Maven.
cd /d "%~dp0\.."
if not exist target\dist\ajedrez.jar (
  call mvn -q clean package -DskipTests
  if errorlevel 1 exit /b 1
)
start "" javaw -jar target\dist\ajedrez.jar
