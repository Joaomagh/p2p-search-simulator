# Simulador de Busca em Redes P2P

Simulador de redes Peer-to-Peer não estruturadas com algoritmos de busca distribuída.

## Características

- ✅ **Validação de Topologia**: Conectividade, grau de nós, recursos e self-loops
- ✅ **Algoritmos de Busca**: Flooding (implementado), Random Walk, Informed variants (em breve)
- ✅ **Visualização Gráfica**: Interface interativa com GraphStream
- ✅ **Métricas Detalhadas**: Mensagens totais, hops, taxa mensagens/nó

## Requisitos

- **Java 17 ou superior** (recomendado Java 21)
- Maven 3.6+

### Verificando sua versão do Java

```bash
java -version
```

Se você tiver Java 8, precisará instalar o Java 17+ ou configurar `JAVA_HOME` para apontar para a versão correta.

#### Configurando automaticamente (Windows)

Para evitar o erro `UnsupportedClassVersionError`, rode uma vez o script abaixo (PowerShell):

```powershell
cd h:/trab-distribuida/p2p-search-simulator
./scripts/setup-java17.ps1 "C:/Program Files/Java/jdk-21.0.6"
```

Ele valida o diretório informado, define `JAVA_HOME` no escopo do usuário e adiciona `bin` ao `PATH`. Depois abra um novo terminal e confirme com `java -version`.

## Compilação

```bash
mvn clean package
```

Isso criará um JAR executável em `target/p2p-simulator.jar` com todas as dependências incluídas.

## Execução

### Modo Interativo (Recomendado)

```bash
# Windows
run.bat

# Linux/Mac
./run.sh
```

### Modo CLI

**IMPORTANTE:** Certifique-se de estar usando Java 17+. Se o comando `java -version` mostrar Java 8, configure `JAVA_HOME`:

```bash
# Windows - configure o JAVA_HOME para Java 17+
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.6

# Ou use o caminho completo do Java
"C:\Program Files\Java\jdk-21.0.6\bin\java.exe" -jar target/p2p-simulator.jar
```

```bash
# Modo interativo (recomendado)
java -jar target/p2p-simulator.jar

# Com parâmetros (em desenvolvimento)
java -jar target/p2p-simulator.jar config.json flooding visual
```

### Parâmetros

```
java -jar target/p2p-simulator.jar [config_path] [algorithm] [visualization]
```

- `config_path`: Caminho do arquivo JSON (default: `config.json`)
- `algorithm`: `flooding`, `randomwalk`, `informed-flooding`, `informed-randomwalk`
- `visualization`: `visual` ou `true` para habilitar interface gráfica

### Scripts Disponíveis

- **`run.bat`**: Script principal (compila e executa com Java 21)
- **`run-auto.bat`**: Detecta automaticamente o Java 17+ instalado
- **`run-quick.bat`**: Execução rápida (assume que já está compilado)
- **`run-gui.bat`** / **`run.sh`**: atalhos para abrir diretamente a interface gráfica

> **Importante:** todos os scripts chamam o `target/p2p-simulator.jar` recém-gerado pelo Maven. Execute `mvn clean package` (ou `mvn clean package -DskipTests` se preferir) antes de usá-los, caso contrário o jar anterior pode não existir ou estar desatualizado.

## Configuração da Rede

Edite `src/main/resources/config.json`:

```json
{
  "num_nodes": 12,
  "min_neighbors": 2,
  "max_neighbors": 4,
  "resources": {
    "n1": ["fileA", "fileB"],
    "n2": ["fileC"]
  },
  "edges": [
    ["n1", "n2"],
    ["n1", "n3"]
  ]
}
```

### Regras de Validação

1. **Conectividade**: O grafo deve ser conexo (todos os nós alcançáveis)
2. **Grau**: Cada nó deve ter entre `min_neighbors` e `max_neighbors` vizinhos
3. **Recursos**: Cada nó deve ter pelo menos um recurso
4. **Self-Loops**: Não são permitidas arestas de um nó para ele mesmo

## Algoritmos Implementados

### Flooding

Envia a mensagem de busca para **todos os vizinhos** do nó atual.

**Regras:**
1. Se TTL = 0 → descarta mensagem
2. Se mensagem já foi vista → descarta (evita loops)
3. Se nó possui o recurso → retorna sucesso
4. Senão → decrementa TTL e envia para todos os vizinhos

**Vantagens:**
- Alta taxa de sucesso
- Encontra o caminho mais curto

**Desvantagens:**
- Alto número de mensagens
- Pode sobrecarregar a rede

## Testes

```bash
mvn test
```

### Testes Implementados

- ✅ Validação de topologia (conectividade, grau, recursos, self-loops)
- ✅ Busca por recursos locais e remotos
- ✅ Expiração de TTL
- ✅ Detecção de loops (mensagens duplicadas)
- ✅ Propagação do flooding

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/
│   │   └── p2p/search/simulator/
│   │       ├── Main.java                    # Classe principal
│   │       ├── loader/
│   │       │   └── NetworkLoader.java       # Carregamento de JSON
│   │       ├── model/
│   │       │   ├── NetworkConfig.java       # DTO de configuração
│   │       │   ├── Node.java                # Nó da rede P2P
│   │       │   └── Message.java             # Mensagem entre nós
│   │       ├── topology/
│   │       │   └── NetworkTopology.java     # Grafo e validações
│   │       ├── strategy/
│   │       │   ├── SearchStrategy.java      # Interface Strategy
│   │       │   └── FloodingStrategy.java    # Implementação Flooding
│   │       ├── simulation/
│   │       │   └── SimulationManager.java   # Motor de simulação
│   │       └── visualization/
│   │           └── NetworkVisualizer.java   # Interface GraphStream
│   └── resources/
│       └── config.json                      # Configuração de exemplo
└── test/
    └── java/
        └── p2p/search/simulator/
            ├── topology/
            │   └── TopologyTest.java        # Testes de topologia
            └── simulation/
                └── SimulationTest.java      # Testes de simulação
```

## Exemplo de Uso

```
==============================================
   Simulador de Busca em Redes P2P
==============================================

Carregando configuração: config.json
✓ Configuração carregada

Construindo topologia da rede...
✓ Topologia criada: NetworkTopology[nodes=12, edges=14, minNeighbors=2, maxNeighbors=4]
✓ Validações: Conectividade, Grau, Recursos, Self-loops

Escolha o algoritmo de busca:
1. Flooding (padrão)
2. Random Walk (em breve)
3. Informed Flooding (em breve)
4. Informed Random Walk (em breve)

Opção [1]: 1
✓ Estratégia configurada: Flooding

Habilitar visualização gráfica? (s/N): s

✓ Modo de visualização habilitado

Digite o ID do nó de origem (ex: n1): n1
Digite o recurso a buscar (ex: fileA): fileC
Digite o TTL (default=10): 10

==============================================
Iniciando busca...
==============================================
Origem: n1
Recurso: fileC
Algoritmo: Flooding
TTL: 10
==============================================

==============================================
   ESTATÍSTICAS DA BUSCA
==============================================
Status: ✓ SUCESSO
Recurso: fileC
Origem: n1
----------------------------------------------
Total de Mensagens: 15
Número de Hops: 2
Mensagens/Nó: 1.25
Tempo de Execução: 7523 ms
==============================================
```

## Cores da Visualização

- 🔵 **Azul (Idle)**: Nó em estado normal
- 🟠 **Laranja (Processing)**: Nó processando mensagem
- 🟢 **Verde (Success)**: Nó encontrou o recurso
- 🔴 **Vermelho (Failed)**: Busca falhou

## Próximos Passos

- [ ] Implementar Random Walk Strategy
- [ ] Implementar Informed Flooding Strategy
- [ ] Implementar Informed Random Walk Strategy
- [ ] Sistema de cache com propagação de resposta
- [ ] Geração automática de topologias
- [ ] Comparação de algoritmos
- [ ] Exportação de métricas para CSV

## Autores

Desenvolvido para a disciplina de Sistemas Distribuídos - UFC

## Licença

MIT License
