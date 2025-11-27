# Exemplos de Execução do Simulador P2P

## Executar com entrada interativa
```bash
java -jar target/p2p-simulator.jar
```

## Executar com Java 17+ explícito (se você tiver múltiplas versões)
```bash
# Windows
"C:\Program Files\Java\jdk-21.0.6\bin\java.exe" -jar target/p2p-simulator.jar

# Linux/Mac
/usr/lib/jvm/java-17-openjdk/bin/java -jar target/p2p-simulator.jar
```

## Executar com parâmetros (modo não-interativo ainda não suportado completamente)
```bash
# Formato: [config] [algorithm] [visualization]
java -jar target/p2p-simulator.jar config.json flooding visual
```

## Via script Windows
```cmd
run.bat
```

## Via script Linux/Mac
```bash
chmod +x run.sh
./run.sh
```

## Teste rápido sem visualização

Inputs de exemplo para testar (copie e cole quando solicitado):
```
1              # Algoritmo: Flooding
n              # Sem visualização
n1             # Nó de origem
fileC          # Recurso a buscar
10             # TTL
```

## Teste com visualização

Inputs de exemplo:
```
1              # Algoritmo: Flooding
s              # Com visualização
n1             # Nó de origem
fileM          # Recurso a buscar (em um nó distante)
10             # TTL
```
