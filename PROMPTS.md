### Arquivo 2: `PROMPTS.md` (Seu Roteiro de Copiar/Colar)
*Copie cada bloco abaixo e mande para o chat da IA sequencialmente.*

```markdown
# Roteiro de Desenvolvimento com IA

## Prompt 1: Infraestrutura e Leitura de Dados
"Atue como Senior Java Dev. Leia `PROJECT_SPECS.md`.
1. Verifique se o `pom.xml` tem todas as libs listadas.
2. Crie o pacote `model` com `NetworkConfig` (para o JSON) e `Node`.
3. Crie `NetworkLoader` para ler o JSON usando Jackson.
4. Crie o arquivo `config.json` de exemplo em `src/main/resources`.
Gere o código para ler o arquivo e carregar na memória. Crie um teste unitário simples para verificar se leu o número certo de nós."

## Prompt 2: Validação Robusta (Graph Core)
"Agora implemente a classe `NetworkTopology`.
1. Use `SimpleGraph` do JGraphT.
2. Implemente o método `validate()` seguindo RIGOROSAMENTE as 4 regras da seção 3.2 do `PROJECT_SPECS.md`.
3. O código deve lançar exceção se o grafo for desconexo ou tiver nós inválidos.
4. Crie um teste que tenta carregar um JSON inválido e espera a exceção."

## Prompt 3: UI Acadêmica (Visualização)
"Vamos configurar a visualização antes da lógica de busca.
1. Na classe `NetworkTopology`, adicione o método `show()` que inicializa o GraphStream.
2. Aplique as configurações de qualidade (Swing, Antialias, AutoLayout) descritas na seção 5.1.
3. Aplique a String CSS exata da seção 5.2 do `PROJECT_SPECS.md`.
4. Crie métodos `resetVisuals()`, `setNodeState(id, state)` e `highlightEdge(id1, id2)` que apenas alteram os atributos `ui.class` dos elementos."

## Prompt 4: Motor de Simulação com Animação
"Implemente o `SimulationManager` e a lógica de `Flooding`.
1. O método `runSearch` deve rodar em uma Thread separada para não travar a UI.
2. Ao enviar mensagem de A para B:
   - Chame `highlightEdge(A, B)` (que deve ter o sleep de 100ms).
   - Mude a cor de B para 'visited'.
   - Imprima o log no console.
3. Se encontrar o recurso:
   - Mude para 'found'.
   - Pause 2 segundos.
   - Encerre a busca e mostre as métricas."

## Prompt 5: Estratégias Avançadas
"Implemente agora o `RandomWalk` e as versões `Informed` (com Cache).
Garanta que a lógica visual (cores e sleeps) funcione para todos os algoritmos.
Lembre-se da regra de Cache: ao encontrar, a mensagem volta e atualiza o mapa de cache dos nós."