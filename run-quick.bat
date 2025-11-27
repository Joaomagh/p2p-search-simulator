@echo off
REM Script rápido - assume que o projeto já está compilado

if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.6"
)

"%JAVA_HOME%\bin\java" -jar "target/p2p-simulator.jar" %*
