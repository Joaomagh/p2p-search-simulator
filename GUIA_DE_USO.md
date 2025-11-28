# Guia de Uso - Simulador de Busca P2P

## 📋 Pré-requisitos

- **Java 17 ou superior** instalado
- **Maven 3.6+** (para compilar o projeto)
- IDE compatível (IntelliJ IDEA, Eclipse, VS Code) - **opcional**

### Verificar Instalação do Java

```bash
java -version
```

Deve exibir versão 17 ou superior.

---

## 🚀 Como Executar a Aplicação

### Método 1: Executar Diretamente pela IDE (Recomendado)

#### No IntelliJ IDEA / VS Code:

1. Abra o projeto na IDE
2. Localize o arquivo: `src/main/java/p2p/search/simulator/ui/SimulatorGUI.java`
3. **Clique com botão direito** no arquivo
4. Selecione **"Run 'SimulatorGUI.main()'"** (ou pressione `Shift+F10`)
5. A interface gráfica será aberta automaticamente

#### No Eclipse:

1. Abra o projeto no Eclipse
2. Navegue até: `src/main/java/p2p/search/simulator/ui/SimulatorGUI.java`
3. **Clique com botão direito** → **Run As** → **Java Application**

---

### Método 2: Executar via Maven

```bash
# 1. Navegar até o diretório do projeto
cd caminho/para/p2p-search-simulator

# 2. Compilar o projeto
mvn clean compile

# 3. Executar a GUI
mvn exec:java -Dexec.mainClass="p2p.search.simulator.ui.SimulatorGUI"
```

---

### Método 3: Gerar e Executar o JAR

```bash
# 1. Gerar o JAR executável
mvn clean package -DskipTests

# 2. Executar o JAR
java -jar target/p2p-simulator.jar
```

---

## 🖥️ Usando a Interface Gráfica

### 1️⃣ Configuração da Rede

**Arquivo de Configuração (network.json)**
- Já vem pré-carregado com uma topologia de 12 nós
- Localização: `src/main/resources/network.json`
- Clique em **"Load Network"** para recarregar se necessário

**Informações exibidas:**
- **Nodes**: Quantidade de nós na rede (ex: 12)
- **Edges**: Quantidade de conexões (ex: 15)
- **Min/Max Neighbors**: Grau mínimo e máximo dos nós (ex: 2/3)

---

### 2️⃣ Parâmetros de Busca

#### **Resource (Recurso)**
Digite o nome do arquivo que deseja buscar:
- Exemplos disponíveis na rede padrão:
  - `fileA` - Localizado em `n1`
  - `fileB` - Localizado em `n2`
  - `fileR` - Localizado em `n12`
  - `fileK` - Localizado em `n10`

#### **Source Node (Nó de Origem)**
Selecione o nó que iniciará a busca:
- Dropdown com todos os nós disponíveis: `n1`, `n2`, ..., `n12`
- **Dica**: Escolha `n1` como origem e `fileR` como recurso para ver uma busca longa

#### **TTL (Time-To-Live)**
Limite de saltos que a mensagem pode percorrer:
- **Valor baixo** (1-3): Busca local, poucos nós visitados
- **Valor médio** (5-8): Busca equilibrada
- **Valor alto** (10-20): Busca abrangente, cobre toda a rede
- **Recomendado**: 10 para ver a rede completa

#### **Search Strategy (Estratégia de Busca)**

| Estratégia | Descrição | Quando Usar |
|-----------|-----------|-------------|
| **Flooding** | Envia mensagem para todos os vizinhos (exceto origem) | Alta taxa de sucesso, mas usa muitas mensagens |
| **Random Walk** | Escolhe um vizinho aleatório por salto | Eficiente em mensagens, taxa de sucesso variável |
| **Informed Flooding** | Flooding + cache de buscas anteriores | Segunda busca é mais eficiente (70% menos mensagens) |
| **Informed Random Walk** | Random Walk + cache | Combina eficiência com conhecimento prévio |

---

### 3️⃣ Visualização

#### **Enable Visualization**
- ✅ **Marcado**: Abre janela gráfica mostrando a rede
  - Visualiza propagação das mensagens em tempo real
  - Nós e arestas destacados durante a busca
  - **Atenção**: Torna a execução ~2s mais lenta (animação)

- ⬜ **Desmarcado**: Execução rápida, apenas resultados textuais
  - Ideal para testar múltiplas buscas rapidamente
  - Recomendado para comparar estratégias

#### **Visualization Delay (ms)**
- Controla velocidade da animação (100-1000ms)
- **100ms**: Rápido, pode ser difícil acompanhar
- **300ms**: Padrão, equilibrado
- **500-1000ms**: Lento, didático para apresentações

---

### 4️⃣ Executar a Busca

1. Configure todos os parâmetros
2. Clique em **"Run Search"**
3. Aguarde a execução (barra de progresso aparece)
4. Analise os resultados

---

## 📊 Interpretando os Resultados

### Painel de Log (Área Central)

Mostra o passo-a-passo da busca:

```
[Step 1] n1 processa mensagem (TTL 10)
[Step 2] n1 -> n2 (TTL 9)
[Step 3] n1 -> n3 (TTL 9)
...
Recurso 'fileR' encontrado em n12
```

### Estatísticas (Painel Direito)

| Métrica | Significado | Valor Ideal |
|---------|-------------|-------------|
| **Success** | Se o recurso foi encontrado | ✅ True |
| **Hops** | Quantidade de saltos até encontrar | Menor = melhor |
| **Total Messages** | Mensagens enviadas total | Menor = mais eficiente |
| **Visited Nodes** | Nós que processaram a mensagem | Depende da estratégia |
| **Duration** | Tempo de execução (ms) | Menor = mais rápido |
| **Path** | Caminho percorrido até o recurso | Lista de nós |

---

## 🧪 Experimentos Sugeridos

### Experimento 1: Comparar Estratégias

**Objetivo**: Ver diferença de mensagens entre estratégias

1. Configure: `Source=n1`, `Resource=fileR`, `TTL=10`
2. Execute com **Flooding** → Anote mensagens (ex: 20)
3. Execute com **Random Walk** → Anote mensagens (ex: 12-15)
4. Execute com **Informed Flooding** → Anote mensagens (ex: 20 na 1ª, 6 na 2ª)

**Resultado esperado**: Flooding garante sucesso mas usa mais mensagens. Random Walk é eficiente mas pode falhar.

---

### Experimento 2: Efeito do Cache

**Objetivo**: Ver como cache melhora eficiência

1. Configure: `Source=n1`, `Resource=fileR`, `TTL=10`, `Strategy=Informed Flooding`
2. **Primeira busca**: Anote mensagens (ex: 20)
3. **Segunda busca** (sem reiniciar): Anote mensagens (ex: 6)
4. Clique em **"Reset Simulation"**
5. **Terceira busca**: Anote mensagens (ex: 20 novamente)

**Resultado esperado**: Cache reduz mensagens em ~70% na segunda busca.

---

### Experimento 3: Impacto do TTL

**Objetivo**: Ver como TTL limita a busca

1. Configure: `Source=n1`, `Resource=fileR` (distante)
2. Execute com `TTL=2` → Provavelmente falha
3. Execute com `TTL=5` → Pode falhar ou ter sucesso
4. Execute com `TTL=10` → Sucesso garantido

**Resultado esperado**: TTL baixo impede alcançar recursos distantes.

---

### Experimento 4: Random Walk - Natureza Estocástica

**Objetivo**: Ver variação nas buscas aleatórias

1. Configure: `Source=n1`, `Resource=fileR`, `TTL=10`, `Strategy=Random Walk`
2. Execute **10 vezes**
3. Anote:
   - Quantas vezes teve sucesso (ex: 4/10 = 40%)
   - Média de mensagens nas bem-sucedidas (ex: 13)

**Resultado esperado**: Taxa de sucesso ~30-50%, mensagens variam entre 12-15.

---

## 🔄 Funcionalidades Adicionais

### Reset Simulation
- **Botão**: "Reset Simulation"
- **Efeito**: Limpa cache de todos os nós, zera métricas
- **Uso**: Entre experimentos para garantir condições iniciais limpas

### Load Network
- **Botão**: "Load Network"
- **Efeito**: Recarrega `network.json` do disco
- **Uso**: Após modificar o arquivo de configuração manualmente

---

## ⚠️ Troubleshooting

### Problema: "java: command not found"
**Solução**: Instale o Java 17+ e configure a variável `JAVA_HOME`

### Problema: Janela gráfica não abre
**Solução**: 
1. Verifique se visualization está marcado
2. Tente desmarcar e executar novamente
3. Verifique logs no terminal

### Problema: Random Walk sempre falha
**Solução**: 
- É esperado! Random Walk é estocástico
- Aumente o TTL para 15-20
- Execute múltiplas vezes
- Use Flooding se precisa garantir sucesso

### Problema: "Port already in use" (se executar múltiplas instâncias)
**Solução**: Feche outras instâncias da aplicação

---

## 📝 Modificando a Topologia

Para criar sua própria rede, edite `src/main/resources/network.json`:

```json
{
  "minNeighbors": 2,
  "maxNeighbors": 3,
  "resources": {
    "n1": ["fileA", "fileB"],
    "n2": ["fileC"],
    "n3": ["fileD"]
  },
  "edges": [
    ["n1", "n2"],
    ["n2", "n3"],
    ["n3", "n1"]
  ]
}
```

**Regras**:
- Grafo deve ser conexo (todos os nós alcançáveis)
- Cada nó deve ter entre `minNeighbors` e `maxNeighbors` vizinhos
- Cada nó deve ter pelo menos 1 recurso
- Sem self-loops (nó conectado a si mesmo)

Após editar, clique em **"Load Network"** na GUI.

---

## 📚 Recursos Adicionais

- **Testes automatizados**: `mvn test` (98 testes)
- **Modo CLI**: Execute `Main.java` para interface texto
- **Documentação técnica**: Veja `GUIA_ACADEMICO.md`

---

## 💡 Dicas de Uso

1. **Para demonstrações**: Use `visualization=true` e `delay=500ms`
2. **Para análise**: Use `visualization=false` e rode múltiplos experimentos
3. **Para comparar estratégias**: Mantenha todos os parâmetros iguais, mude apenas a estratégia
4. **Para ver cache**: Use estratégias "Informed" e execute 2x seguidas
5. **Para entender TTL**: Comece com valores baixos e aumente gradualmente

---

**Versão**: 1.0  
**Data**: Novembro 2025  
**Compatível com**: Java 17+, Maven 3.6+
