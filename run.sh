#!/bin/bash
# Script para executar o simulador P2P

echo "Compilando o projeto..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "Executando o simulador..."
    echo ""
    java -cp target/classes:target/dependency/* p2p.search.simulator.Main "$@"
else
    echo "Erro na compilação!"
    exit 1
fi
