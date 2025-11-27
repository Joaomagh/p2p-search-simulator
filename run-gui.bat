@echo off
REM ====================================================
REM Script para executar o Simulador P2P (Interface Grafica)
REM ====================================================

set "JAVA_CMD=C:\Program Files\Java\jdk-21.0.6\bin\java.exe"
set "JAR_FILE=target\p2p-simulator.jar"

echo ====================================================
echo   Simulador de Busca em Redes P2P - GUI
echo ====================================================
echo.

REM Verifica se o JAR existe
if not exist "%JAR_FILE%" (
    echo [ERRO] JAR nao encontrado: %JAR_FILE%
    echo.
    echo Execute: mvn clean package
    echo.
    pause
    exit /b 1
)

REM Executa o simulador com GUI (padrao)
echo Iniciando interface grafica...
echo.
"%JAVA_CMD%" -jar "%JAR_FILE%"

if errorlevel 1 (
    echo.
    echo [ERRO] Falha ao executar o simulador
    pause
)
