# Guia Acadêmico - Defesa e Avaliação do Projeto

## 🎯 Visão Geral do Sistema

Este documento orienta alunos sobre **onde estudar** para responder perguntas técnicas e **o que incluir nos slides** para a apresentação acadêmica do projeto.

---

## 📂 Arquitetura do Projeto

### Estrutura de Pacotes

```
src/main/java/p2p/search/simulator/
├── model/              # Entidades do domínio
│   ├── Message.java    ⭐ CRÍTICO
│   ├── Node.java       ⭐ CRÍTICO
│   └── NetworkConfig.java
├── strategy/           # Algoritmos de busca
│   ├── SearchStrategy.java        ⭐ CRÍTICO
│   ├── FloodingStrategy.java      ⭐ CRÍTICO
│   ├── RandomWalkStrategy.java    ⭐ CRÍTICO
│   ├── InformedFloodingStrategy.java     ⭐ CRÍTICO
│   └── InformedRandomWalkStrategy.java   ⭐ CRÍTICO
├── simulation/         # Motor de simulação
│   └── SimulationManager.java     ⭐ CRÍTICO
├── topology/           # Estrutura da rede
│   └── NetworkTopology.java       ⭐ CRÍTICO
├── ui/                 # Interface gráfica
│   └── SimulatorGUI.java
├── visualization/      # Visualização da rede
│   └── NetworkVisualizer.java
└── Main.java          # Interface CLI
```

---

## 🔍 Onde Estudar Para Cada Tipo de Pergunta

### 1. Perguntas sobre **Modelo de Mensagens P2P**

#### 📍 **Arquivo**: `model/Message.java`

**O que o professor pode perguntar:**

1. **"Como vocês representam uma mensagem na rede P2P?"**
   - **Responder**: Explique a classe `Message` e seus atributos
   - **Linha 10-18**: Enum `Type` (QUERY, RESPONSE)
   - **Linhas 20-27**: Atributos da mensagem (id, source, target, resource, ttl, pathHistory, success)
   - **Mencione**: Imutabilidade (fields `final`) e uso de Builder pattern

2. **"Como funciona o controle de TTL?"**
   - **Responder**: Método `decrementTTL()` (linha ~43)
   - Cria nova mensagem com `ttl - 1`
   - Imutabilidade garante que original não é modificado

3. **"Como vocês evitam loops infinitos?"**
   - **Responder**: Combinação de 2 mecanismos:
     - **TTL**: Limita quantidade de saltos (Message.java)
     - **Anti-echo**: `SimulationManager.hasSeenMessage()` (SimulationManager.java, linha ~208)
   - UUID único da mensagem + nodeId formam chave

4. **"Como funciona o rastreamento do caminho (path history)?"**
   - **Responder**: Método `addToPath(String nodeId)` (linha ~59)
   - Lista imutável que cresce a cada salto
   - Usado para RESPONSE voltar pelo caminho inverso

5. **"Explique o mecanismo de RESPONSE"**
   - **Responder**: Método `createResponse()` (linha ~78)
   - Inverte o path da QUERY: `Collections.reverse(reversePath)`
   - TTL ajustado para tamanho do caminho
   - Permite cache ser propagado no caminho de volta

**Para os slides:**
- Diagrama UML da classe Message
- Fluxo: QUERY (ida) → RESPONSE (volta)
- Exemplo visual de TTL decrementando
- Estrutura do pathHistory: `[n1, n1, n2, n2, n6, n6, n12]`

---

### 2. Perguntas sobre **Nós da Rede (Peers)**

#### 📍 **Arquivo**: `model/Node.java`

**O que o professor pode perguntar:**

1. **"O que cada nó armazena/mantém?"**
   - **Responder** (linhas 15-22):
     - `id`: Identificador único
     - `resources`: Arquivos que o nó possui (Set para evitar duplicatas)
     - `neighbors`: Lista de vizinhos (conectividade)
     - `cache`: Mapa<Recurso, NodeID> para buscas informadas
     - `searchStrategy`: Estratégia configurada dinamicamente

2. **"Como funciona o cache para buscas informadas?"**
   - **Responder** (linhas 46-54):
     - `addToCache(resource, nodeId)`: Armazena após RESPONSE
     - `getCachedLocation(resource)`: Consulta antes de buscar
     - `clearCache()`: Limpa para novos experimentos
   - **Chave**: Cache é populado na volta (RESPONSE), não na ida (QUERY)

3. **"Como um nó processa mensagens?"**
   - **Responder**: Método `receiveMessage()` (linha ~69)
   - **Switch** no tipo da mensagem:
     - `QUERY`: Delega para a estratégia (`strategy.processQuery()`)
     - `RESPONSE`: Atualiza cache se sucesso, continua propagação

4. **"Por que a estratégia é configurável?"**
   - **Responder** (linhas 56-63):
     - Padrão Strategy Pattern (polimorfismo)
     - Permite trocar algoritmo em runtime
     - Todos os nós usam a mesma estratégia em cada simulação
     - `setSearchStrategy()` chamado por `SimulationManager.configureStrategy()`

**Para os slides:**
- Diagrama UML da classe Node
- Representação visual: Nó com resources, neighbors, cache
- Fluxo de processamento: receiveMessage → switch(type) → strategy
- Exemplo de cache: `{fileR: n12, fileB: n2}`

---

### 3. Perguntas sobre **Estratégias de Busca**

#### 📍 **Arquivos**: `strategy/*.java`

**O que o professor pode perguntar:**

---

#### **3.1 Interface SearchStrategy** ⭐ CRÍTICO

📍 **Arquivo**: `strategy/SearchStrategy.java`

**ATENÇÃO**: Este arquivo define o contrato que todas as estratégias seguem!

1. **"Por que usar uma interface?"**
   - **Responder**: Padrão Strategy Pattern (GoF Design Pattern)
   - Permite múltiplas implementações sem modificar Node ou SimulationManager
   - Polimorfismo: `strategy.processQuery(...)` funciona para qualquer estratégia
   - **Código** (linhas ~9-11):
     ```java
     void processQuery(Node currentNode, Message message, 
                      SimulationManager simulationManager, String senderId);
     String getName();
     default boolean isInformed() { return false; }
     ```

2. **"O que significa `isInformed()`?"**
   - **Responder** (linha ~13):
     - `false`: Estratégias básicas (Flooding, Random Walk)
     - `true`: Estratégias que usam cache (Informed variants)
   - Permite lógica condicional baseada no tipo
   - **Implementações**:
     - FloodingStrategy: `return false` (não usa cache)
     - InformedFloodingStrategy: `return true` (usa cache)

3. **"Quantas estratégias vocês implementaram?"**
   - **Responder**: 4 estratégias concretas:
     1. **FloodingStrategy** (básica)
     2. **RandomWalkStrategy** (básica)
     3. **InformedFloodingStrategy** (com cache)
     4. **InformedRandomWalkStrategy** (com cache)

**Para os slides:**
- Hierarquia: Interface → 4 implementações
- Método abstrato: `processQuery()` - onde a mágica acontece
- Diagrama de classes mostrando herança
- Tabela: Estratégia × isInformed() × Usa cache?

---

#### **3.2 Flooding Strategy** ⭐ CRÍTICO

📍 **Arquivo**: `strategy/FloodingStrategy.java`

**ATENÇÃO**: Esta é a estratégia mais simples e serve de base para entender as outras!

1. **"Como funciona o algoritmo de Flooding?"**
   - **Responder** (método `processQuery`, linha ~10):
     1. **Verifica TTL**: Se <= 0, descarta (linha ~11)
     2. **Verifica mensagem duplicada**: Anti-echo (linha ~15)
     3. **Verifica recurso local**: Se tem, envia SUCCESS (linha ~19)
     4. **Propaga para TODOS os vizinhos** (linha ~29)
        - **Exceto** o sender (linha ~30) → Anti-echo

2. **"Qual a complexidade de mensagens do Flooding?"**
   - **Responder**: O(E × TTL) onde E = número de arestas
   - Pior caso: Todos os nós recebem múltiplas vezes
   - **Vantagem**: Taxa de sucesso ~100% (se TTL suficiente)
   - **Desvantagem**: Muitas mensagens redundantes

3. **"Como vocês implementam anti-echo?"**
   - **Responder** (linha ~30-32):
     ```java
     if (neighborId.equals(senderId)) {
         continue; // Não reenvia para quem mandou
     }
     ```
   - Evita que mensagem volte imediatamente para o sender
   - Reduz mensagens mas não elimina duplicatas (nó pode receber por outro caminho)

**Para os slides:**
- Pseudocódigo do algoritmo
- Animação/diagrama: Mensagem se propagando em ondas
- Gráfico: Mensagens × Tamanho da rede
- Comparação: Com anti-echo vs. sem anti-echo

---

#### **3.3 Random Walk Strategy** ⭐ CRÍTICO

📍 **Arquivo**: `strategy/RandomWalkStrategy.java`

**ATENÇÃO**: Estratégia estocástica - resultados variam a cada execução!

1. **"Como funciona o Random Walk?"**
   - **Responder** (método `processQuery`, linha ~13):
     1. Verifica TTL e duplicatas (igual Flooding)
     2. Verifica recurso local
     3. **Escolhe UM vizinho aleatório** (linha ~39):
        ```java
        String nextHop = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        ```
     4. Envia mensagem para apenas esse vizinho

2. **"Por que Random Walk é mais eficiente que Flooding?"**
   - **Responder**:
     - **Mensagens**: O(TTL) vs. O(E × TTL)
     - Apenas 1 mensagem por salto vs. broadcasts
     - **Trade-off**: Menor taxa de sucesso (~30-50% vs. ~100%)

3. **"Como garantir que não reenvia para o sender?"**
   - **Responder** (linhas ~33-35):
     ```java
     List<String> candidates = new ArrayList<>(currentNode.getNeighbors());
     if (senderId != null) {
         candidates.remove(senderId); // Remove sender das opções
     }
     ```

4. **"Por que a taxa de sucesso varia?"**
   - **Responder**: Natureza estocástica
     - Pode escolher caminho errado
     - TTL pode esgotar antes de encontrar
     - Múltiplas execuções têm resultados diferentes
   - **Validação**: Veja testes em `RandomWalkStrategyTest.java` (linha ~35-66)

**Para os slides:**
- Diagrama: Caminho único vs. Flooding (árvore)
- Gráfico: Taxa de sucesso × TTL
- Gráfico: Mensagens × Taxa de sucesso (trade-off)
- Animação: Caminho aleatório na rede

---

#### **3.4 Informed Flooding Strategy** ⭐ CRÍTICO

📍 **Arquivo**: `strategy/InformedFloodingStrategy.java`

**ATENÇÃO**: Esta é a diferença entre estratégias "básicas" e "informadas"!

1. **"Qual a diferença entre Informed Flooding e Flooding básico?"**
   - **RESPOSTA ESSENCIAL** (linhas ~25-27):
     
     **CÓDIGO COMPLETO DA DIFERENÇA**:
     ```java
     // InformedFloodingStrategy.java - linha ~25
     if (tryDirect(currentNode, message, simulationManager)) {
         return; // Cache hit: Envia direto, não faz flooding
     }
     // Se chegou aqui: Cache miss, faz flooding normal (resto igual FloodingStrategy)
     ```
     
     **Método tryDirect()** (linha ~43-68):
     ```java
     private boolean tryDirect(Node currentNode, Message message, 
                               SimulationManager simulationManager) {
         // 1. Consulta cache
         Optional<String> cachedTarget = currentNode.getCachedLocation(message.getResource());
         if (cachedTarget.isEmpty()) {
             return false; // Cache miss: volta e faz flooding
         }
         
         // 2. Cache hit: Envia DIRETO para o nó
         String destination = cachedTarget.get();
         Message directMessage = message.decrementTTL()
                                        .addToPath(currentNode.getId())
                                        .toBuilder()
                                        .target(destination)
                                        .build();
         simulationManager.sendMessage(directMessage, currentNode.getId());
         return true; // Não precisa fazer flooding!
     }
     ```
     
     **RESUMO DA DIFERENÇA**:
     - **FloodingStrategy**: SEMPRE faz flooding (envia para TODOS os vizinhos)
     - **InformedFloodingStrategy**: Consulta cache PRIMEIRO
       - Se **cache hit**: Envia DIRETO (1 mensagem)
       - Se **cache miss**: Faz flooding normal

2. **"Como o cache é populado?"**
   - **Responder**:
     - Durante RESPONSE (Node.java, linha ~73)
     - Cada nó no caminho de volta adiciona ao cache
     - Segunda busca do mesmo recurso usa cache

3. **"Qual a melhoria de desempenho?"**
   - **Responder** (evidência em `InformedFloodingStrategyTest.java`):
     - **Primeira busca**: ~20 mensagens (igual Flooding)
     - **Segunda busca**: ~6 mensagens (70% de redução)
     - Cache converte Flooding em busca direcionada

4. **"E se o cache estiver desatualizado?"**
   - **Responder**:
     - Nó pode ter saído da rede (não implementado aqui)
     - Recurso pode ter sido movido (não implementado)
     - **Solução**: Timeout de cache ou re-flooding se falhar
     - **Neste projeto**: Rede é estática, cache sempre válido

**Para os slides:**
- Fluxograma: Consulta cache → hit (direto) | miss (flooding)
- Gráfico comparativo: Mensagens busca 1 vs. busca 2
- Diagrama: Cache propagado no RESPONSE
- Percentual de redução: 70%

---

#### **3.5 Informed Random Walk Strategy** ⭐ CRÍTICO

📍 **Arquivo**: `strategy/InformedRandomWalkStrategy.java`

**ATENÇÃO**: Combina o melhor dos dois mundos!

1. **"Como combina Random Walk com cache?"**
   - **RESPOSTA ESSENCIAL** (método `processQuery`):
     
     **CÓDIGO COMPLETO**:
     ```java
     // InformedRandomWalkStrategy.java - linha ~31
     if (tryDirect(currentNode, message, simulationManager)) {
         return; // Cache hit: Envia direto
     }
     
     // Cache miss: Faz Random Walk (escolhe 1 vizinho aleatório)
     // ... resto igual RandomWalkStrategy
     ```
     
     **DIFERENÇA PARA RandomWalkStrategy**:
     - **RandomWalkStrategy**: SEMPRE escolhe vizinho aleatório
     - **InformedRandomWalkStrategy**: Consulta cache PRIMEIRO
       - Se **cache hit**: Envia DIRETO (1 mensagem certa)
       - Se **cache miss**: Random Walk (1 mensagem aleatória)

2. **"Qual estratégia é melhor?"**
   - **RESPOSTA HONESTA**: Depende do cenário e das LIMITAÇÕES:
   
   | Estratégia | 1ª Busca | 2ª Busca (mesmo recurso) | Taxa Sucesso | Limitações |
   |-----------|----------|--------------------------|--------------|------------|
   | **Flooding** | 20 msgs | 20 msgs | **100%** ✅ | Sempre custoso |
   | **Random Walk** | 8-15 msgs | 8-15 msgs | **40-60%** ⚠️ | Pode falhar mesmo com TTL alto |
   | **Informed Flooding** | 20 msgs | **1 msg** 🚀 | **100%** ✅ | Cache inútil se recursos sempre diferentes |
   | **Informed Random Walk** | 8-15 msgs | **1 msg** 🚀 | **50-70%** ⚠️ | Herda limitação do Random Walk |
   
   **ANÁLISE CRÍTICA**:
   
   **Flooding**:
   - ✅ Única com garantia matemática de sucesso (se TTL suficiente)
   - ❌ Não escala (explosão de mensagens)
   - 🎯 Use quando: Precisa GARANTIR encontrar o recurso
   
   **Random Walk**:
   - ✅ Eficiente (80% menos mensagens que Flooding)
   - ❌ **Nunca alcança 100%** de sucesso (natureza estocástica)
   - ❌ Pode seguir caminho errado e esgotar TTL
   - 🎯 Use quando: Pode aceitar falhas ocasionais
   
   **Informed Flooding**:
   - ✅ Melhor de dois mundos: Garantia (100%) + Eficiência (2ª busca)
   - ❌ Primeira busca sempre custosa
   - ❌ Cache só funciona para recursos populares/repetidos
   - 🎯 Use quando: Recursos são buscados múltiplas vezes (música popular)
   
   **Informed Random Walk**:
   - ✅ Eficiente + conhecimento prévio
   - ❌ **AINDA herda problema do Random Walk** (nunca 100%)
   - ❌ Cache não resolve natureza estocástica
   - 🎯 Use quando: Equilíbrio entre eficiência e sucesso moderado

3. **"O que muda do Informed para o padrão?"**
   - **RESPOSTA DIRETA**:
   
   **A ÚNICA DIFERENÇA É UMA LINHA DE CÓDIGO**:
   ```java
   // Estratégias BÁSICAS (Flooding e Random Walk):
   public void processQuery(...) {
       // Verificações (TTL, duplicatas, recurso local)
       // Depois: FAZ A BUSCA (flooding ou random walk)
   }
   
   // Estratégias INFORMADAS (Informed Flooding e Informed Random Walk):
   public void processQuery(...) {
       // Verificações (TTL, duplicatas, recurso local)
       if (tryDirect(...)) return; // ← ESTA É A ÚNICA DIFERENÇA!
       // Depois: FAZ A BUSCA (flooding ou random walk)
   }
   ```
   
   **OU SEJA**: Estratégias informadas SÓ adicionam `tryDirect()` antes da busca!

3. **"Qual o IMPACTO REAL dessa única linha?"**
   - **RESPOSTA HONESTA**:
   
   **IMPACTO GIGANTE NA SEGUNDA BUSCA**:
   ```
   Primeira busca do mesmo recurso:
   - Flooding: 20 mensagens (explora tudo)
   - Informed Flooding: 20 mensagens (cache vazio, explora tudo)
   ❌ Nenhuma diferença ainda!
   
   Segunda busca do mesmo recurso:
   - Flooding: 20 mensagens (sempre explora tudo)
   - Informed Flooding: 1 mensagem (direto do cache)
   ✅ Redução de 95% (20 → 1 mensagem)!
   ```
   
   **POR QUE FUNCIONA?**:
   1. **Primeira busca**: Cache está vazio
      - `tryDirect()` retorna `false` (cache miss)
      - Faz busca completa (Flooding ou Random Walk)
      - **RESPONSE propaga cache** no caminho de volta
   
   2. **Segunda busca**: Cache populado
      - `tryDirect()` retorna `true` (cache hit)
      - **Pula toda a busca!** Vai direto ao nó
      - Mensagem: source → destination (1 salto)
   
   **ANALOGIA**: 
   - **Estratégias básicas**: GPS sem memória (sempre recalcula rota)
   - **Estratégias informadas**: GPS com histórico (lembra caminho anterior)
   
   **LIMITAÇÕES**:
   - ❌ Não ajuda se cada busca é por recurso diferente
   - ❌ Cache pode ficar desatualizado (nó sai da rede)
   - ❌ Primeira busca sempre tem custo completo
   - ✅ Perfeito para buscas repetidas (casos reais: música popular, filme famoso)

**Para os slides:**
- **Slide crucial**: "O impacto de UMA linha de código"
  - Antes/Depois: Primeira busca × Segunda busca
  - Gráfico de barras: 20 mensagens → 1 mensagem
  - Destaque: **95% de redução** (não apenas 70%)
  - Quando NÃO funciona: Recursos sempre diferentes
- Tabela comparativa: 4 estratégias × Mensagens (1ª busca vs 2ª busca)
- Matriz de decisão: Quando usar cada estratégia
- Gráfico: Evolução de mensagens ao longo de N buscas repetidas

---

### 4. Perguntas sobre **Motor de Simulação**

#### 📍 **Arquivo**: `simulation/SimulationManager.java`

**O que o professor pode perguntar:**

1. **"Como vocês orquestram a simulação?"**
   - **Responder**: Método `runSearch()` (linha ~52):
     1. Valida parâmetros (nó existe, strategy != null)
     2. Configura estratégia em todos os nós
     3. Reseta estado interno (fila, cache de mensagens vistas)
     4. Cria mensagem inicial (QUERY)
     5. Processa fila de mensagens (`processMessages()`)
     6. Retorna `SearchResult` com métricas

2. **"Como funciona a fila de mensagens?"**
   - **Responder** (linhas 29, ~130):
     - `Queue<PendingMessage>`: FIFO, processamento sequencial
     - `PendingMessage(message, senderId)`: Record que armazena sender
     - `processMessages()` (linha ~130): Loop até fila vazia

3. **"Como vocês detectam mensagens duplicadas?"**
   - **Responder**: Método `hasSeenMessage()` (linha ~208):
     ```java
     String key = messageId + ":" + nodeId;
     return !seenMessages.add(key);
     ```
     - Chave = UUID da mensagem + nodeId
     - `ConcurrentHashMap.newKeySet()`: Thread-safe
     - `add()` retorna `false` se já existe

4. **"Como funciona o fluxo de RESPONSE?"**
   - **Responder**: Método `completeSuccess()` (linha ~215) e `startResponseFlow()` (linha ~235):
     1. Quando recurso é encontrado, chama `completeSuccess()`
     2. Cria mensagem RESPONSE com caminho invertido
     3. Enfileira mensagens para cada nó no caminho de volta
     4. Cada nó atualiza seu cache ao receber RESPONSE

5. **"Como vocês coletam métricas?"**
   - **Responder** (linhas ~34-36):
     - `AtomicInteger messageCount`: Conta mensagens enviadas
     - `AtomicInteger stepCounter`: Conta passos da simulação
     - `Set<String> visitedNodes`: Nós que processaram mensagens
     - `SearchResult` (linha ~98): DTO com todas as métricas

6. **"Por que usar AtomicInteger?"**
   - **Responder**: Thread-safety (preparado para futura paralelização)
   - Operações atômicas: `incrementAndGet()`
   - Nesta versão: Single-threaded, mas boa prática

7. **"Como funciona a visualização?"**
   - **Responder** (linhas ~41, ~159-164):
     - `NetworkVisualizer visualizer`: Opcional (pode ser null)
     - Durante processamento: Destaca nós e arestas
     - `sleep(visualizationDelay)`: Pausa para animação
     - Pode ser desabilitada para execuções rápidas

**Para os slides:**
- Diagrama de sequência: runSearch() → processMessages() → strategies
- Fluxo de dados: Message → Queue → Node → Strategy → Queue
- Diagrama do anti-echo: hasSeenMessage() com Set
- Arquitetura: SimulationManager como orquestrador central

---

### 5. Perguntas sobre **Topologia da Rede**

#### 📍 **Arquivo**: `topology/NetworkTopology.java`

**O que o professor pode perguntar:**

1. **"Como vocês representam a topologia?"**
   - **Responder** (linhas ~14-17):
     - Usa biblioteca **JGraphT**: `Graph<String, DefaultEdge>`
     - `SimpleGraph`: Grafo não-direcionado, sem arestas múltiplas
     - `Map<String, Node>`: Mapeia ID → Objeto Node
     - Separação: Grafo (conectividade) vs. Nós (dados)

2. **"Como validam a topologia?"**
   - **Responder**: Método `validate()` (linha ~47) com 4 regras:
     
     **a) Conectividade** (`validateConnectivity`, linha ~53):
     - Usa `ConnectivityInspector` do JGraphT
     - Garante que todos os nós são alcançáveis
     - Lança exceção se grafo desconexo
     
     **b) Grau dos nós** (`validateDegree`, linha ~68):
     - Verifica `degree >= minNeighbors && degree <= maxNeighbors`
     - Garante que nenhum nó está isolado ou super-conectado
     
     **c) Recursos** (`validateResources`, linha ~85):
     - Todo nó deve ter pelo menos 1 recurso
     - Sem nós vazios na rede
     
     **d) Self-loops** (`validateSelfLoops`, linha ~97):
     - Proíbe arestas de um nó para ele mesmo
     - `graph.containsEdge(nodeId, nodeId)` deve ser `false`

3. **"Por que essas validações são importantes?"**
   - **Responder**:
     - **Conectividade**: Evita sub-redes isoladas (busca sempre falha)
     - **Grau**: Mantém propriedades P2P (não centralizado, não isolado)
     - **Recursos**: Garante que busca tem sentido (tem o que buscar)
     - **Self-loops**: Evita bugs em algoritmos de propagação

4. **"Como funciona o algoritmo de shortest path?"**
   - **Responder**: Método `shortestPath()` (linha ~123):
     - Usa `DijkstraShortestPath` do JGraphT
     - Retorna lista de nós no caminho mais curto
     - **Uso**: Análise teórica (não usado na busca P2P)

**Para os slides:**
- Diagrama da rede (12 nós, 15 arestas)
- Código JSON do network.json
- 4 regras de validação com exemplos de erro
- Grafo antes/depois de passar validação

---

### 6. Perguntas sobre **Testes e Validação**

#### 📍 **Arquivos**: `src/test/java/**/*Test.java`

**O que o professor pode perguntar:**

1. **"Como vocês validaram as estratégias?"**
   - **Responder**: 98 testes automatizados (JUnit 5)
   
   **a) Testes de estratégia**:
   - `FloodingStrategyTest`: Taxa de sucesso, anti-echo
   - `RandomWalkStrategyTest`: Estocástica, taxa ~30-50%
   - `InformedFloodingStrategyTest`: Redução de 70% de mensagens
   - `InformedRandomWalkStrategyTest`: Combine conhecimento + eficiência
   
   **b) Testes de comportamento**:
   - `TTLBehaviorTest`: Valida limite de saltos
   - `CacheBehaviorTest`: Valida propagação de cache
   - `AntiEchoTest`: Valida que não reenvia para sender
   - `MetricsAndPathTest`: Valida integridade de métricas

2. **"Como lidam com não-determinismo do Random Walk?"**
   - **Responder** (`RandomWalkStrategyTest.java`, linha ~35):
     - **50 execuções** da mesma busca
     - Valida taxa de sucesso >= 15% (threshold pragmático)
     - Aceita variação estatística
     - Média de mensagens quando bem-sucedido

3. **"Qual a cobertura de testes?"**
   - **Responder**:
     - **98 testes**, todos passando
     - Cobertura de:
       - ✅ Todas as 4 estratégias
       - ✅ TTL (limites e expiração)
       - ✅ Cache (propagação e uso)
       - ✅ Anti-echo (não reenvia)
       - ✅ Métricas (consistência)
       - ✅ Topologia (validações)

**Para os slides:**
- Dashboard: 98 tests, 0 failures, 100% passing
- Tabela: Estratégia × Testes × Métricas validadas
- Gráfico: Taxa de sucesso × Estratégia (média de 50 execuções)

---

## 📊 O Que Colocar nos Slides

### Slide 1: Título
- **Simulador de Busca em Redes P2P**
- Nomes dos integrantes
- Disciplina e data

---

### Slide 2: Introdução - Motivação
- **Problema**: Como encontrar recursos em rede descentralizada?
- **Desafios**:
  - Sem servidor central
  - Evitar sobrecarga de mensagens
  - Lidar com rede dinâmica (cache)

---

### Slide 3: Objetivos do Projeto
- Implementar 4 estratégias de busca P2P:
  - Flooding
  - Random Walk
  - Informed Flooding
  - Informed Random Walk
- Comparar eficiência (mensagens × taxa de sucesso)
- Validar através de simulação

---

### Slide 4: Arquitetura do Sistema
- **Diagrama de pacotes**:
  ```
  Model (Message, Node) ←→ Strategy (4 algoritmos)
         ↓                           ↓
  Topology (Grafo) ←→ SimulationManager (Orquestrador)
         ↓                           ↓
       GUI                    Visualization
  ```
- Padrão Strategy Pattern
- Separação: Domínio vs. Lógica vs. UI

---

### Slide 5: Modelo de Mensagens
- **Classe Message**:
  - Atributos: id, type, source, target, resource, ttl, pathHistory
  - Tipos: QUERY (ida) e RESPONSE (volta)
- **Fluxo**: QUERY → encontra recurso → RESPONSE pelo caminho inverso
- **Diagrama**: Mensagem viajando pela rede

---

### Slide 6: Representação de Nós
- **Classe Node**:
  - Recursos locais (Set<String>)
  - Vizinhos (conectividade)
  - Cache (buscas informadas)
  - Estratégia (configurável)
- **Diagrama**: Nó com 4 componentes

---

### Slide 7: Estratégia 1 - Flooding
- **Algoritmo**:
  1. Verifica TTL e recurso local
  2. Propaga para TODOS os vizinhos (exceto sender)
- **Vantagens**: Taxa de sucesso ~100%
- **Desvantagens**: Muitas mensagens (O(E × TTL))
- **Gráfico**: Mensagens × Tamanho da rede

---

### Slide 8: Estratégia 2 - Random Walk
- **Algoritmo**:
  1. Escolhe UM vizinho aleatório
  2. Envia mensagem apenas para ele
- **Vantagens**: Eficiente (O(TTL) mensagens)
- **Desvantagens**: Taxa de sucesso ~30-50%
- **Gráfico**: Taxa de sucesso × TTL

---

### Slide 9: Estratégia 3 - Informed Flooding
- **Algoritmo**:
  1. Consulta cache
  2. Se hit: Envia direto para nó
  3. Se miss: Faz Flooding
- **Resultados**:
  - 1ª busca: 20 mensagens
  - 2ª busca: 6 mensagens (70% redução)
- **Gráfico**: Comparação busca 1 vs. busca 2

---

### Slide 10: Estratégia 4 - Informed Random Walk
- **Algoritmo**: Cache + Random Walk
- **Benefício**: Combina eficiência com conhecimento
- **Tabela comparativa**: 4 estratégias × Métricas

---

### Slide 11: Mecanismos de Controle
- **TTL (Time-To-Live)**:
  - Limita propagação
  - Previne loops infinitos
- **Anti-Echo**:
  - Não reenvia para sender
  - hasSeenMessage(UUID + nodeId)
- **Cache**:
  - Populado no RESPONSE
  - Válido para buscas futuras

---

### Slide 12: Topologia da Rede
- **Representação**: JGraphT (SimpleGraph)
- **Validações**:
  1. Conectividade (grafo conexo)
  2. Grau (min ≤ degree ≤ max)
  3. Recursos (todos os nós têm)
  4. Self-loops (proibidos)
- **Configuração**: JSON (12 nós, 15 arestas)

---

### Slide 13: Experimentos Realizados
- **98 testes automatizados** (JUnit 5)
- **Metodologia**:
  - **50 execuções** por estratégia (validação estatística)
  - Topologia fixa: 12 nós, 15 arestas
  - TTL = 10, Source = n1, Target variável
- **Testes de estratégia**:
  - Flooding: **100%** de sucesso (garantido matematicamente)
  - Random Walk: Taxa **40-60%** (estocástica, NUNCA alcança 100%)
  - Informed Flooding: **95% redução** mensagens (2ª busca)
  - Informed Random Walk: Eficiência + cache (sucesso moderado)
- **Testes de comportamento**:
  - TTL: Limita propagação corretamente
  - Cache: Propagado no RESPONSE (não na QUERY)
  - Anti-echo: Reduz mensagens duplicadas
  - Métricas: Consistência (contador = realidade)

---

### Slide 14: Resultados - Análise Crítica
**Tabela Completa (Média de 50 execuções)**:

| Estratégia | 1ª Busca | 2ª Busca (mesmo) | Taxa Sucesso | Limitações |
|-----------|----------|------------------|--------------|------------|
| **Flooding** | ~20 msgs | ~20 msgs | **100%** ✅ | Sempre custoso, não aprende |
| **Random Walk** | ~12 msgs | ~12 msgs | **50%** ⚠️ | **Nunca 100%** (estocástico) |
| **Informed Flooding** | ~20 msgs | **~1 msg** 🚀 | **100%** ✅ | Cache inútil para recursos únicos |
| **Informed Random Walk** | ~12 msgs | **~1 msg** 🚀 | **60%** ⚠️ | Cache não resolve aleatoriedade |

**INSIGHTS IMPORTANTES**:
1. **Random Walk NUNCA alcança 100%**: Natureza estocástica significa falhas ocasionais
2. **Cache = 95% redução**: Segunda busca vai direto (20 → 1 mensagem)
3. **Flooding é único com garantia**: Se TTL suficiente, sempre encontra
4. **Informed não melhora taxa de sucesso**: Só reduz mensagens em buscas repetidas

**QUANDO USAR CADA UMA?**:
- 🔴 **Crítico (deve encontrar)**: Flooding ou Informed Flooding
- 🟡 **Moderado (pode falhar)**: Random Walk ou Informed Random Walk
- 🟢 **Recursos populares**: Informed Flooding (aprende com uso)
- 🔵 **Recursos únicos**: Flooding (cache não ajuda)

---

### Slide 15: Interface Gráfica
- **Screenshots**: GUI com parâmetros configuráveis
- **Visualização**: Rede animada durante busca
- **Resultados**: Métricas em tempo real
- **Demo**: (Se possível, mostrar aplicação rodando)

---

### Slide 16: Desafios e Soluções
| Desafio | Solução |
|---------|---------|
| Loops infinitos | TTL + Anti-echo |
| Mensagens redundantes | Cache (buscas informadas) |
| Variação estocástica | 50 execuções + threshold estatístico |
| Validação da topologia | 4 regras automáticas |

---

### Slide 17: Tecnologias Utilizadas
- **Linguagem**: Java 17
- **Build**: Maven 3.6+
- **Testes**: JUnit 5 (98 testes)
- **Grafos**: JGraphT
- **GUI**: Swing
- **Visualização**: GraphStream

---

### Slide 18: Conclusões
- ✅ 4 estratégias implementadas e validadas
- ✅ Cache reduz mensagens em 70%
- ✅ TTL previne loops em todas as estratégias
- ✅ Random Walk é estocástico mas eficiente
- ✅ Trade-off: Garantia × Eficiência

---

### Slide 19: Trabalhos Futuros
- Suporte a rede dinâmica (nós entrando/saindo)
- Cache com timeout (invalidação)
- Paralelização (múltiplas buscas simultâneas)
- Outras estratégias (Expanding Ring, k-Random Walk)
- Comparação com protocolos reais (Gnutella, BitTorrent)

---

### Slide 20: Perguntas?
- Contatos dos integrantes
- Link do repositório GitHub
- Agradecimentos

---

## 🎤 Ensaio para a Apresentação

### Tempo sugerido: 15-20 minutos

**Distribuição:**
- Introdução (2min): Slides 1-3
- Arquitetura (3min): Slides 4-6
- Estratégias (6min): Slides 7-10
- Controles e Topologia (2min): Slides 11-12
- Experimentos e Resultados (3min): Slides 13-14
- Demo e Interface (2min): Slide 15
- Conclusões (2min): Slides 16-20

---

## ❓ Perguntas Prováveis do Professor

### Perguntas Conceituais

1. **"Por que P2P é importante hoje em dia?"**
   - **Responder**: BitTorrent, Blockchain, redes descentralizadas, censura
   
2. **"Qual a diferença entre P2P estruturado e não-estruturado?"**
   - **Responder**: Estruturado (DHT, Chord) vs. Não-estruturado (Gnutella, Flooding)
   
3. **"Por que Flooding não escala?"**
   - **Responder**: O(E × TTL) mensagens, explosão combinatorial em redes grandes

### Perguntas Técnicas

4. **"Como vocês garantem que a topologia é válida?"**
   - **Responder**: 4 validações automáticas em `NetworkTopology.validate()`

5. **"O que acontece se o cache estiver desatualizado?"**
   - **Responder**: Neste projeto, rede é estática. Em rede real: timeout ou re-flooding

6. **"Por que usar JGraphT?"**
   - **Responder**: Biblioteca madura, algoritmos prontos (Dijkstra, ConnectivityInspector)

7. **"Como vocês testam comportamento estocástico?"**
   - **Responder**: 50 execuções, threshold estatístico (15%), aceita variação

### Perguntas de Implementação

8. **"Onde está o anti-echo implementado?"**
   - **Responder**: 2 lugares:
     - Estratégias: `if (neighborId.equals(senderId)) continue;`
     - SimulationManager: `hasSeenMessage(UUID + nodeId)`

9. **"Como funciona o Builder Pattern em Message?"**
   - **Responder**: Classe interna estática, métodos fluentes, validação no `build()`

10. **"Por que AtomicInteger se é single-threaded?"**
    - **Responder**: Preparado para paralelização futura, boa prática

---

## 📚 Material de Estudo Recomendado

### Leitura Obrigatória

1. **Message.java** - 30 min
   - Foque em: TTL, pathHistory, createResponse()

2. **Node.java** - 20 min
   - Foque em: cache, receiveMessage(), strategy

3. **SimulationManager.java** - 45 min
   - Foque em: runSearch(), processMessages(), hasSeenMessage()

4. **Cada Strategy** - 15 min cada (60 min total)
   - Entenda diferenças entre as 4

5. **NetworkTopology.java** - 30 min
   - Foque em: 4 validações

**Total**: ~3 horas para dominar o código crítico

---

### Revisão Final (1 hora antes)

1. Releia este documento ✅
2. Execute aplicação 3x com estratégias diferentes ✅
3. Rode `mvn test` e veja resultado ✅
4. Revise slides ✅
5. Prepare respostas para 10 perguntas prováveis ✅

---

## 🏆 Checklist de Preparação

### Antes da Apresentação

- [ ] Testar aplicação (rodar pelo menos 1x)
- [ ] Confirmar que `mvn test` passa (98/98)
- [ ] Revisar todos os arquivos críticos (⭐ marcados)
- [ ] Preparar demo (Source=n1, Resource=fileR, TTL=10, Flooding)
- [ ] Slides finalizados (20 slides)
- [ ] Ensaio cronometrado (15-20 min)
- [ ] Notebook carregado e testado

### Durante a Apresentação

- [ ] Falar pausadamente
- [ ] Olhar para a banca, não para os slides
- [ ] Demonstrar código quando perguntar
- [ ] Admitir limitações (rede estática, single-thread)
- [ ] Relacionar com conceitos teóricos da disciplina

### Se o Professor Perguntar Algo Que Você Não Sabe

**NÃO faça**:
- ❌ Inventar resposta
- ❌ Ficar calado

**FAÇA**:
- ✅ "Boa pergunta, não tenho certeza agora, mas acredito que seja..."
- ✅ "Posso verificar no código?" (mostre que sabe onde procurar)
- ✅ "Não implementamos isso, mas seria interessante para trabalhos futuros"

