@echo off
REM Script alternativo para executar o simulador P2P no Windows
REM Este script tenta encontrar automaticamente o Java 17+

setlocal enabledelayedexpansion

echo ========================================
echo   Simulador P2P - Verificando Java
echo ========================================
echo.

REM Verifica se JAVA_HOME está configurado e é Java 17+
if defined JAVA_HOME (
    "%JAVA_HOME%\bin\java" -version 2>&1 | findstr /R "version.*\"1[7-9]\." >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        set "JAVA_CMD=%JAVA_HOME%\bin\java"
        echo [OK] Usando JAVA_HOME: %JAVA_HOME%
        goto :compile
    )
    
    "%JAVA_HOME%\bin\java" -version 2>&1 | findstr /R "version.*\"[2-9][0-9]\." >nul 2>&1
    if !ERRORLEVEL! EQU 0 (
        set "JAVA_CMD=%JAVA_HOME%\bin\java"
        echo [OK] Usando JAVA_HOME: %JAVA_HOME%
        goto :compile
    )
)

REM Procura por Java 17+ em locais comuns
set "JAVA_PATHS=C:\Program Files\Java\jdk-21.0.6;C:\Program Files\Java\jdk-17;C:\Program Files\Eclipse Adoptium\jdk-17*;C:\Program Files\Eclipse Adoptium\jdk-21*"

for %%p in (%JAVA_PATHS%) do (
    if exist "%%p\bin\java.exe" (
        set "JAVA_CMD=%%p\bin\java"
        echo [OK] Java encontrado em: %%p
        goto :compile
    )
)

REM Se não encontrou, tenta usar o java do PATH e avisa
echo [AVISO] Java 17+ nao encontrado em JAVA_HOME ou locais padroes.
echo [AVISO] Tentando usar o Java do PATH (pode falhar se for Java 8)...
set "JAVA_CMD=java"

:compile
echo.
echo ========================================
echo   Compilando o projeto
echo ========================================
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRO] Falha na compilacao!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Executando o simulador
echo ========================================
echo.
"%JAVA_CMD%" -jar "target/p2p-simulator.jar" %*

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRO] Falha na execucao!
    echo.
    echo Possiveis causas:
    echo - Java 17+ nao esta instalado
    echo - JAVA_HOME aponta para Java 8 ou inferior
    echo.
    echo Solucao:
    echo 1. Instale o Java 17 ou superior
    echo 2. Configure JAVA_HOME para apontar para o Java 17+
    echo    set JAVA_HOME=C:\Program Files\Java\jdk-17
    echo.
    pause
)

endlocal
