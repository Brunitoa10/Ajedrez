@echo off
REM ==============================================================
REM  Genera la version PORTABLE para Windows (no requiere Java instalado)
REM  Resultado: target\portable\Ajedrez\Ajedrez.exe  +  target\Ajedrez-portable-windows.zip
REM  Requisitos para compilar: JDK 21+ (incluye jpackage) y Maven en el PATH.
REM ==============================================================
setlocal
cd /d "%~dp0\.."

echo [1/3] Compilando y ejecutando tests...
call mvn -q clean package
if errorlevel 1 goto :error

echo [2/3] Creando imagen portable con jpackage (incluye su propio Java)...
if exist target\portable rmdir /s /q target\portable
jpackage --type app-image ^
  --name Ajedrez ^
  --app-version 1.0.0 ^
  --input target\dist ^
  --main-jar ajedrez.jar ^
  --main-class com.ajedrez.Launcher ^
  --dest target\portable ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --jlink-options "--strip-debug --no-header-files --no-man-pages"
if errorlevel 1 goto :error

echo [3/3] Comprimiendo...
powershell -NoProfile -Command "Compress-Archive -Path 'target\portable\Ajedrez' -DestinationPath 'target\Ajedrez-portable-windows.zip' -Force"

echo.
echo Listo. Copia la carpeta target\portable\Ajedrez a un pendrive y ejecuta Ajedrez.exe
echo La base de datos y la configuracion se guardan dentro de esa misma carpeta (data\ y config\).
goto :eof

:error
echo.
echo Hubo un error. Revisa que tengas JDK 21+ y Maven en el PATH.
exit /b 1
