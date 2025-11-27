@echo off
REM Script para executar o simulador P2P no Windows

REM Define o JAVA_HOME se não estiver configurado
if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.6"
)

REM Usa o Java do JAVA_HOME
set "JAVA_CMD=%JAVA_HOME%\bin\java"

echo Compilando o projeto...
call mvn clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Executando o simulador...
    echo Usando: %JAVA_CMD%
    echo.
    REM Configura encoding UTF-8 e executa em modo CLI
    "%JAVA_CMD%" -Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -jar "target/p2p-simulator.jar" --cli %*
) else (
    echo Erro na compilacao!
    exit /b 1
)
