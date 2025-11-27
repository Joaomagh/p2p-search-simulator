Especificação Técnica: Simulador P2P (Academic Standard)

1. Visão Geral

Simulador de redes P2P não estruturadas desenvolvido em Java 17. O sistema deve carregar topologias de rede a partir de arquivos JSON, validar rigorosamente a conectividade e integridade do grafo, e simular algoritmos de busca de recursos (Flooding, Random Walk e variantes informadas).

O diferencial do projeto é a visualização gráfica de alta qualidade ("Academic Style"), utilizando animações em tempo real para demonstrar o funcionamento dos algoritmos.

2. Stack Tecnológica

Linguagem: Java 17 (LTS).

Gerenciamento de Dependências: Maven.

Bibliotecas:

Jackson (com.fasterxml.jackson.core): Para leitura robusta de arquivos JSON.

JGraphT (org.jgrapht): Para modelagem matemática do grafo e algoritmos de conectividade.

GraphStream (org.graphstream): Para renderização gráfica e animação da rede.

JUnit 5: Para testes unitários automatizados.

3. Arquitetura do Sistema

A simulação deve ocorrer em memória, utilizando o padrão de troca de mensagens assíncronas (sem Sockets reais).

Core: SimulationManager gerencia o ciclo de vida da busca.

Entidades: Node (com lógica de recebimento) e Message (objeto que trafega).

3.1 Entrada de Dados (JSON)

O sistema deve aceitar um arquivo config.json com a estrutura exata abaixo:

{
  "num_nodes": 12,
  "min_neighbors": 2,
  "max_neighbors": 4,
  "resources": {
    "n1": ["r1", "r2"],
    "n2": ["r3"]
  },
  "edges": [
    ["n1", "n2"],
    ["n1", "n3"]
  ]
}


3.2 Validação de Topologia (Crítico)

Ao carregar o grafo, a classe NetworkTopology DEVE lançar IllegalStateException se qualquer uma das condições abaixo for violada:

Conectividade: O grafo não pode estar particionado (desconexo). Utilize ConnectivityInspector do JGraphT.

Graus: Nenhum nó pode ter grau menor que min_neighbors ou maior que max_neighbors.

Recursos: Todo nó deve possuir pelo menos um recurso (lista não vazia).

Self-Loops: Não pode haver arestas de um nó para ele mesmo.

4. Algoritmos de Busca (Strategy Pattern)

Implementar a interface SearchStrategy com as seguintes variações:

Flooding (Inundação): O nó repassa a mensagem para todos os vizinhos (exceto o remetente).

Random Walk (Passeio Aleatório): O nó escolhe aleatoriamente um único vizinho para repassar.

Informed Flooding: Verifica o cache local. Se souber onde está o recurso, envia mensagem direta (Direct Message) ao alvo. Caso contrário, executa Flooding.

Informed Random Walk: Verifica o cache local. Se souber, envia direto. Caso contrário, executa Random Walk.

Regras de Execução:

TTL (Time To Live): Decrementado a cada salto. Se chegar a 0, a mensagem é descartada.

Cache Update (Backtracking): Em buscas informadas, quando o recurso é encontrado, uma mensagem de resposta (RESPONSE) percorre o caminho inverso até a origem. Todos os nós nesse caminho devem atualizar seus caches com a localização do recurso.

5. Visualização "Academic Style" (GraphStream)

A interface deve ser minimalista, com fundo branco e cores sóbrias, adequada para prints em artigos acadêmicos.

5.1 Configurações de Renderização

O código deve forçar a renderização Swing de alta qualidade:

System.setProperty("org.graphstream.ui", "swing")

Atributos do grafo: ui.quality, ui.antialias.

Layout: viewer.enableAutoLayout() para organização orgânica dos nós.

5.2 Folha de Estilo (CSS) Obrigatória

Aplique a seguinte String CSS ao atributo ui.stylesheet do grafo:

graph {
    fill-color: #ffffff; /* Fundo Branco (Academic Standard) */
    padding: 40px;
}
node {
    size: 20px;
    fill-color: #ecf0f1; /* Cinza Claro (Idle) */
    stroke-mode: plain;
    stroke-color: #bdc3c7;
    stroke-width: 2px;
    text-mode: normal;
    text-style: bold;
    text-size: 14;
    text-color: #2c3e50;
    text-alignment: at-right;
    text-offset: 5px, 0px;
}
/* Estados Dinâmicos */
node.source {
    fill-color: #3498db; /* Azul Profissional (Origem) */
    stroke-color: #2980b9;
    size: 25px;
}
node.visited {
    fill-color: #e67e22; /* Laranja (Processando/Visitado) */
    stroke-color: #d35400;
}
node.found {
    fill-color: #2ecc71; /* Verde (Sucesso/Alvo) */
    stroke-color: #27ae60;
    size: 30px;
    shadow-mode: plain;
    shadow-color: #999;
    shadow-offset: 3px, -3px;
}
edge {
    shape: line;
    fill-color: #95a5a6; /* Cinza Médio */
    size: 1.5px;
    arrow-shape: none;
}
edge.active {
    fill-color: #e74c3c; /* Vermelho (Mensagem Viajando) */
    size: 3px;
}


5.3 Lógica de Animação

Para permitir que o usuário acompanhe o fluxo da mensagem:

Envio (Edge Flash): Ao enviar de A para B, mude a classe da aresta para active, aguarde 100ms (Thread.sleep) e restaure.

Visita (Node State): Ao receber mensagem, mude a classe do nó para visited.

Sucesso: Se o nó tiver o recurso, mude para found e pause a simulação por 2000ms.

Console: Imprima logs passo a passo: [Step 1] N1 -> N2 (TTL: 5).

6. Métricas Finais

Ao encerrar a simulação, o sistema deve exibir no console:

Total de mensagens trocadas.

Total de nós envolvidos (visitados).