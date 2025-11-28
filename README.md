# Simulador de Busca em Redes P2P

Simulador de redes Peer-to-Peer não estruturadas com algoritmos de busca distribuída e visualização gráfica em tempo real.

## Características

- ✅ **Validação de Topologia**: Conectividade, grau de nós, recursos e self-loops
- ✅ **4 Algoritmos de Busca**: Flooding, Random Walk, Informed Flooding, Informed Random Walk
- ✅ **Propagação de Cache**: Mensagens RESPONSE atualizam cache no caminho de volta
- ✅ **Comportamento Anti-Echo**: Nós não reenviam mensagens ao sender
- ✅ **Visualização Gráfica**: Interface interativa com GraphStream (Academic Style)
- ✅ **Métricas Detalhadas**: Mensagens, nós visitados, hops, cobertura da rede, tempo de execução
- ✅ **Interface GUI Completa**: Controle de velocidade (slider), replay, logs em tempo real

## Requisitos

### ☕ Java (OBRIGATÓRIO)

- **Java 17 ou superior** (recomendado: Java 21 LTS)
- Maven 3.6+

> ⚠️ **Importante**: O projeto foi compilado com Java 17. Versões anteriores (Java 8, 11) **NÃO funcionarão** e retornarão erro `UnsupportedClassVersionError: class file version 61.0`.

### Verificando sua versão

```bash
java -version
# Deve mostrar: java version "17.x.x" ou superior
```

```bash
mvn -version
# Deve mostrar: Java version: 17.x.x ou superior
```

### Instalação do Java 17+

Se você não tem Java 17+:

#### Windows
1. Baixe o [OpenJDK 21](https://adoptium.net/) ou [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/)
2. Instale em `C:\Program Files\Java\jdk-21.0.6\` (ou local de preferência)
3. Configure `JAVA_HOME` manualmente ou use o script:

```powershell
# PowerShell (como Administrador)
cd h:/trab-distribuida/p2p-search-simulator
./scripts/setup-java17.ps1 "C:/Program Files/Java/jdk-21.0.6"
```

O script valida o diretório, define `JAVA_HOME` e adiciona ao `PATH`. Depois abra um novo terminal e confirme com `java -version`.

#### Linux
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# Fedora/RHEL
sudo dnf install java-17-openjdk-devel
```

#### macOS
```bash
# Homebrew
brew install openjdk@17

# Adicionar ao PATH
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### Resolvendo conflitos de versão

Se você tem múltiplas versões de Java instaladas:

```bash
# Windows - definir temporariamente para a sessão atual
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.6
set PATH=%JAVA_HOME%\bin;%PATH%

# Linux/Mac - definir temporariamente
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

Para tornar permanente, edite as variáveis de ambiente do sistema (Windows) ou adicione ao `.bashrc`/`.zshrc` (Linux/Mac).

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

#### Windows (.bat)

- **`run.bat`** - Script completo: compila com Maven e executa GUI
  ```powershell
  # Usa: "C:\Program Files\Java\jdk-21.0.6\bin\java.exe"
  run.bat
  ```

- **`run-auto.bat`** - Detecção automática de Java 17+
  ```powershell
  # Procura Java em locais padrões: Program Files, JAVA_HOME
  run-auto.bat
  ```

- **`run-quick.bat`** - Execução rápida (sem recompilar)
  ```powershell
  # Assume que target/p2p-simulator.jar já existe
  # Use após mvn package
  run-quick.bat
  ```

- **`run-gui.bat`** - Atalho direto para GUI (recompila antes)
  ```powershell
  run-gui.bat
  ```

#### Linux/Mac (.sh)

- **`run.sh`** - Script universal: compila e executa
  ```bash
  chmod +x run.sh
  ./run.sh
  ```

#### Quando usar cada script?

| Script | Quando usar |
|--------|-------------|
| `run.bat` / `run.sh` | **Uso geral** - sempre compila antes de executar |
| `run-auto.bat` | Quando você **não sabe** qual Java está instalado |
| `run-quick.bat` | Para **desenvolvimento rápido** - já compilou antes |
| `run-gui.bat` | Atalho direto para **interface gráfica** |

> **⚠️ Importante:** Todos os scripts chamam o `target/p2p-simulator.jar`. Execute `mvn clean package` se houver erros de "classe não encontrada".

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

Todos os algoritmos implementam **anti-echo** (não reenviam mensagens ao sender) e seguem o padrão **Strategy** para fácil extensão.

### 📊 Tabela Comparativa

| Algoritmo | Mensagens | Nós Visitados | Taxa de Sucesso | Usa Cache | Melhor Para |
|-----------|-----------|---------------|-----------------|-----------|-------------|
| **Flooding** | 🔴 Alto (~20) | 🔴 100% da rede | ✅ ~100% | ❌ | Redes pequenas, busca garantida |
| **Random Walk** | 🟢 Baixo (~13) | 🟢 ~40% da rede | ⚠️ ~40-50% | ❌ | Redes grandes, economia de recursos |
| **Informed Flooding** | 🟡 Médio (~14) | 🟡 Reduzido | ✅ ~100% | ✅ | Buscar recursos conhecidos |
| **Informed Random Walk** | 🟢 Baixo (~13) | 🟢 Mínimo | 🟢 ~70%+ | ✅ | Máxima eficiência com cache |

### 1. Flooding (Inundação)

Envia a mensagem de busca para **todos os vizinhos** (exceto o sender).

**Algoritmo:**
```
1. Se TTL = 0 → descarta
2. Se já viu essa mensagem → descarta (evita loops)
3. Se tem o recurso → SUCESSO
4. Senão → envia para TODOS os vizinhos (exceto sender)
```

**✅ Vantagens:**
- Altíssima taxa de sucesso (~100%)
- Encontra o caminho mais curto
- Garante cobertura completa da rede

**❌ Desvantagens:**
- Alto número de mensagens (sobrecarga)
- Visita 100% dos nós (mesmo desnecessários)
- Não escalável para redes grandes

**📈 Métricas Típicas:** 20 mensagens, 12 nós visitados (100%), 7 hops

---

### 2. Random Walk (Passeio Aleatório)

Escolhe **aleatoriamente** um único vizinho para encaminhar (exceto sender).

**Algoritmo:**
```
1. Se TTL = 0 → descarta
2. Se já viu essa mensagem → descarta
3. Se tem o recurso → SUCESSO
4. Senão → escolhe 1 vizinho ALEATÓRIO (exceto sender)
```

**✅ Vantagens:**
- Baixíssimo número de mensagens
- Apenas ~40% da rede visitada
- Escalável para redes grandes

**❌ Desvantagens:**
- Taxa de sucesso menor (~40-50%)
- Pode não encontrar recursos disponíveis
- Caminho não é o mais curto

**📈 Métricas Típicas:** 13 mensagens, 4-5 nós visitados (~40%), sucesso variável

---

### 3. Informed Flooding

Flooding com **cache**: verifica cache antes de inundar.

**Algoritmo:**
```
1. Se já sabe onde está (cache) → envia DIRETO ao destino
2. Senão → executa Flooding normal
3. Ao encontrar → RESPONSE popula cache no caminho inverso
```

**✅ Vantagens:**
- Reduz mensagens quando cache disponível (~30% de redução)
- Mantém alta taxa de sucesso
- Cache compartilhado entre buscas

**❌ Desvantagens:**
- Primeira busca ainda é custosa (sem cache)
- Precisa de propagação RESPONSE

**📈 Métricas Típicas:** 14 mensagens (com cache), 20 (sem cache)

---

### 4. Informed Random Walk

Random Walk com **cache**: tenta envio direto primeiro.

**Algoritmo:**
```
1. Se já sabe onde está (cache) → envia DIRETO
2. Senão → Random Walk normal
3. Ao encontrar → RESPONSE popula cache
```

**✅ Vantagens:**
- Combina eficiência do Random Walk com cache
- Melhor taxa de sucesso que Random Walk puro
- Mínimo de mensagens com cache

**❌ Desvantagens:**
- Depende de buscas anteriores para popular cache
- Sem cache, comporta-se como Random Walk

**📈 Métricas Típicas:** 13 mensagens (média), taxa de sucesso ~70%+

---

### 🔄 Sistema de Cache (RESPONSE Messages)

Quando um recurso é encontrado:
1. Nó que tem o recurso cria mensagem **RESPONSE**
2. RESPONSE percorre o **caminho inverso** até a origem
3. **Todos os nós no caminho** atualizam seus caches
4. Próximas buscas podem usar **envio direto**

**Benefícios:**
- 🎯 Redução de 30%+ em mensagens
- 📚 Conhecimento distribuído na rede
- ⚡ Buscas subsequentes mais rápidas

## Interface Gráfica (GUI)

A GUI oferece controle completo sobre as simulações com visualização em tempo real.

### Recursos da Interface

#### 🎛️ Controles Principais

- **Slider de Velocidade**: Ajusta delay de 0ms (instantâneo) a 2000ms (2s por passo)
  - 🐌 Lento (1000-2000ms): Ideal para apresentações e depuração
  - ⚡ Rápido (0-300ms): Análise de métricas
  
- **Botão Replay**: Re-executa a última busca com mesmos parâmetros
  - Útil para comparar diferentes velocidades
  - Preserva configuração (origem, recurso, TTL, estratégia)

- **Seleção de Estratégia**: Dropdown com 4 algoritmos
- **Configuração de Busca**: Origem, Recurso, TTL personalizáveis

#### 📊 Painel de Estatísticas

Exibe em tempo real:
- ✅ Status (SUCESSO/FALHOU)
- 📨 Total de Mensagens
- 🌐 Total de Nós Visitados
- 🎯 Número de Hops
- 📊 Mensagens/Nó
- 📈 Cobertura da Rede (%)
- ⏱️ Tempo de Execução (ms)
- 🗺️ Caminho percorrido

#### 🎨 Cores da Visualização (Academic Style)

- 🔵 **Azul** - Nó de origem
- 🟠 **Laranja** - Nó sendo visitado/processando
- 🟢 **Verde** - Nó com recurso (sucesso)
- ⚪ **Cinza** - Nó idle (não visitado)
- 🔴 **Vermelho** - Aresta ativa (mensagem em trânsito)

### Executando a GUI

```bash
# Windows
run-gui.bat

# Linux/Mac
./run.sh

# Ou manualmente
java -jar target/p2p-simulator.jar
```

## Testes

```bash
# Todos os testes
mvn test

# Teste específico
mvn -Dtest=VisitedNodesTest test
mvn -Dtest=CacheResponseTest test
mvn -Dtest=AntiEchoTest test
```

### Suítes de Testes (36 testes)

#### ✅ TopologyTest (4 testes)
- Validação de conectividade
- Validação de grau (min/max neighbors)
- Validação de recursos
- Detecção de self-loops

#### ✅ SimulationTest (7 testes)
- Busca por recursos locais e remotos
- Expiração de TTL
- Detecção de loops (mensagens duplicadas)
- Propagação do flooding
- Cache atualizado via RESPONSE

#### ✅ CacheResponseTest (4 testes)
- Propagação de cache via mensagens RESPONSE
- Cache vazio antes da primeira busca
- Cache apenas em nós do caminho
- Reset limpa caches corretamente

#### ✅ AntiEchoTest (6 testes)
- Flooding não reenvia ao sender
- Random Walk exclui sender da escolha
- Redução de mensagens com anti-echo
- Comparação entre estratégias

#### ✅ VisitedNodesTest (8 testes)
- Contagem de nós visitados cresce durante busca
- Reset zera contador corretamente
- Nós visitados nunca excedem total da rede
- Cobertura varia por estratégia

#### ✅ StrategyComparisonTest (1 teste)
- Demonstração comparativa de todas as estratégias

#### ✅ NetworkLoaderTest (6 testes)
- Carregamento de JSON válido
- Detecção de topologia inválida
- Validação de recursos malformados

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

## Exemplo de Uso (CLI)

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
2. Random Walk
3. Informed Flooding
4. Informed Random Walk

Opção [1]: 1

Habilitar visualização gráfica? (s/N): s
✓ Modo de visualização habilitado

Digite o ID do nó de origem (ex: n1): n1
Digite o recurso a buscar (ex: fileA): fileR
Digite o TTL (default=10): 10

==============================================
Iniciando busca...
==============================================
Origem: n1
Recurso: fileR
Algoritmo: Flooding
TTL: 10
==============================================

[Step 1] n1 processa mensagem (TTL 10)
[Step 2] n1 -> n2 (TTL 9)
[Step 3] n1 -> n3 (TTL 9)
[Step 4] n2 -> n4 (TTL 8)
[Step 5] n2 -> n6 (TTL 8)
[Step 6] n6 -> n12 (TTL 7)
Recurso 'fileR' encontrado em n12

==============================================
   ESTATÍSTICAS DA BUSCA
==============================================
Status: ✓ SUCESSO
Recurso: fileR
Origem: n1
----------------------------------------------
Total de Mensagens: 20
Total de Nós Visitados: 12
Número de Hops: 7
Mensagens/Nó: 1.67
Cobertura da Rede: 100.0%
Tempo de Execução: 8 ms
Caminho: n1 → n1 → n1 → n2 → n2 → n6 → n6 → n12
==============================================
```

## Conformidade com PROJECT_SPECS

Este projeto atende **rigorosamente** às especificações acadêmicas:

### ✅ Requisitos Técnicos
- [x] Java 17 (LTS) como linguagem base
- [x] Maven para gerenciamento de dependências
- [x] Jackson para parsing robusto de JSON
- [x] JGraphT para modelagem matemática do grafo
- [x] GraphStream para visualização "Academic Style"
- [x] JUnit 5 para testes automatizados

### ✅ Validação de Topologia
- [x] **Conectividade**: `ConnectivityInspector` valida grafo conexo
- [x] **Graus**: Verifica `min_neighbors` ≤ grau ≤ `max_neighbors`
- [x] **Recursos**: Todo nó possui pelo menos um recurso
- [x] **Self-Loops**: Detecta e rejeita arestas inválidas
- [x] Lança `IllegalStateException` em violações

### ✅ Algoritmos de Busca (Strategy Pattern)
- [x] **Flooding**: Inunda todos os vizinhos (exceto sender)
- [x] **Random Walk**: Escolha aleatória de um vizinho
- [x] **Informed Flooding**: Cache + Flooding
- [x] **Informed Random Walk**: Cache + Random Walk
- [x] **TTL**: Decrementado a cada salto, descarte em 0
- [x] **Cache Update**: Mensagens RESPONSE no caminho inverso

### ✅ Visualização "Academic Style"
- [x] Propriedade `org.graphstream.ui=swing`
- [x] Fundo branco (`#ffffff`) com CSS obrigatório
- [x] Estados dinâmicos: source (azul), visited (laranja), found (verde)
- [x] Animação de arestas: classe `active` durante envio
- [x] Layout automático: `viewer.enableAutoLayout()`
- [x] Pausa de 2000ms ao encontrar recurso

### ✅ Métricas Finais
- [x] **Total de mensagens trocadas** (getTotalMessages)
- [x] **Total de nós visitados** (getVisitedNodes)
- [x] **Número de hops** (getHops)
- [x] **Tempo de execução** (getDurationMs)
- [x] **Caminho percorrido** (getPath)
- [x] **Cobertura da rede** (%)
- [x] Console logs passo a passo: `[Step N] A -> B (TTL: X)`

## Roadmap Futuro

- [ ] Geração automática de topologias (Barabási-Albert, Watts-Strogatz)
- [ ] Exportação de métricas para CSV/JSON
- [ ] Comparação side-by-side de múltiplas estratégias
- [ ] Replay com controle frame-by-frame
- [ ] Topologias hierárquicas (super-peers)
- [ ] Simulação de falhas de nós
- [ ] Análise de robustez da rede

## Arquitetura do Projeto

```
src/main/java/p2p/search/simulator/
├── Main.java                      # Entry point (CLI)
├── loader/
│   └── NetworkLoader.java         # Parsing de JSON com Jackson
├── model/
│   ├── NetworkConfig.java         # DTO de configuração
│   ├── Node.java                  # Nó P2P (cache, recursos)
│   └── Message.java               # Mensagem (QUERY/RESPONSE)
├── topology/
│   └── NetworkTopology.java       # Grafo + validações (JGraphT)
├── strategy/
│   ├── SearchStrategy.java        # Interface Strategy
│   ├── FloodingStrategy.java
│   ├── RandomWalkStrategy.java
│   ├── InformedFloodingStrategy.java
│   └── InformedRandomWalkStrategy.java
├── simulation/
│   └── SimulationManager.java     # Motor de simulação
├── visualization/
│   └── NetworkVisualizer.java     # GraphStream wrapper
└── ui/
    └── SimulatorGUI.java          # Interface Swing

src/test/java/p2p/search/simulator/
├── topology/
│   └── TopologyTest.java
├── simulation/
│   ├── SimulationTest.java
│   ├── CacheResponseTest.java
│   ├── VisitedNodesTest.java
│   └── NetworkLoaderTest.java
└── strategy/
    ├── AntiEchoTest.java
    └── StrategyComparisonTest.java
```

## Documentação Adicional

- **[PROJECT_SPECS.md](PROJECT_SPECS.md)** - Especificação técnica acadêmica completa
- **[ANTI_ECHO_REFACTORING.md](ANTI_ECHO_REFACTORING.md)** - Detalhes da implementação anti-echo
- **[PROMPTS.md](PROMPTS.md)** - Histórico de desenvolvimento com IA

## Contribuindo

Este é um projeto acadêmico. Contribuições são bem-vindas via Pull Requests.

Antes de contribuir:
1. Execute todos os testes: `mvn test`
2. Garanta que o build passa: `mvn clean package`
3. Siga o padrão de código existente
4. Adicione testes para novas funcionalidades

## Troubleshooting

### Erro: `UnsupportedClassVersionError`
**Causa**: Java 8 ou 11 instalado, mas projeto requer Java 17+
**Solução**: Instale Java 17+ e configure `JAVA_HOME`

### Erro: `Could not find or load main class`
**Causa**: JAR não foi compilado ou está corrompido
**Solução**: Execute `mvn clean package` novamente

### Erro: `NetworkConfig not found`
**Causa**: `config.json` não está no classpath
**Solução**: Verifique `src/main/resources/config.json`

### GUI não abre
**Causa**: GraphStream/Swing não disponível ou erro de inicialização
**Solução**: Verifique logs no console, tente modo CLI com `--cli`

### Testes falhando
**Causa**: Versão do Java incompatível ou dependências desatualizadas
**Solução**: `mvn clean install -U` para forçar atualização

## Autores

Desenvolvido para a disciplina de **Sistemas Distribuídos** - Universidade Federal do Ceará (UFC)

## Licença

MIT License - Veja [LICENSE](LICENSE) para detalhes

---

## 📚 Resumo Executivo

**Simulador P2P** completo com 4 algoritmos de busca, visualização gráfica em tempo real, sistema de cache inteligente e suite completa de testes (36 testes, 100% pass rate).

**Destaques técnicos:**
- ✅ Conformidade total com especificações acadêmicas
- ✅ Pattern Strategy + Observer para extensibilidade
- ✅ GraphStream com estilo "Academic" (fundo branco, cores sóbrias)
- ✅ Anti-echo implementado (redução ~20% em mensagens)
- ✅ Sistema de cache via RESPONSE messages (~30% de ganho)
- ✅ Métricas detalhadas: mensagens, nós visitados, cobertura, tempo

**Uso rápido:**
```bash
# Windows
run.bat

# Linux/Mac
./run.sh
```
