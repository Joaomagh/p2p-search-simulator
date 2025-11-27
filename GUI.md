# Interface Gráfica do Simulador P2P

## 🎨 Visão Geral

A interface gráfica (GUI) oferece uma experiência visual e intuitiva para executar simulações de busca em redes P2P, **resolvendo completamente os problemas de encoding UTF-8 do console Windows**.

## ✨ Vantagens da GUI

- ✅ **Sem problemas de encoding**: Caracteres especiais (ç, ã, õ, á) exibidos corretamente
- ✅ **Interface amigável**: Formulários claros e organizados
- ✅ **Feedback visual**: Estatísticas em painel dedicado
- ✅ **Log detalhado**: Área de texto com histórico completo da execução
- ✅ **Controle total**: Botões para carregar rede e executar buscas
- ✅ **Seleção de arquivos**: File chooser para escolher config.json customizado

## 🚀 Como Usar

### 1. Executar a GUI

#### Forma mais simples (Windows):
```bash
.\run-gui.bat
```

#### Forma alternativa:
```bash
java -jar target\p2p-simulator.jar
```

### 2. Usando a Interface

#### Painel "Configuração da Rede"
1. **Arquivo JSON**: Mantém `config.json` ou clique em "..." para escolher outro arquivo
2. Clique em **"Carregar Rede"**
3. Aguarde a mensagem verde: "Rede carregada com sucesso!"

#### Painel "Parâmetros de Busca"
1. **Algoritmo**: Selecione "Flooding" (outros em breve)
2. **Nó de Origem**: Digite o ID do nó (ex: `n1`, `n2`)
3. **Recurso**: Digite o recurso buscado (ex: `fileA`, `fileB`)
4. **TTL**: Ajuste o Time-To-Live (1-100, padrão 10)
5. **Visualização**: Marque para abrir janela GraphStream
6. Clique em **"Executar Busca"**

#### Painel "Log de Execução"
- Mostra todas as etapas da simulação em tempo real
- Inclui carregamento, validações e estatísticas finais
- Scroll automático para última mensagem

#### Painel "Estatísticas da Busca"
Aparece após executar uma busca, mostrando:
- **Status**: SUCESSO (verde) ou FALHOU (vermelho)
- **Total de Mensagens**: Quantidade total de mensagens trocadas
- **Número de Hops**: Distância até encontrar o recurso
- **Mensagens/Nó**: Média de mensagens por nó
- **Tempo de Execução**: Duração em milissegundos

## 📸 Layout

```
┌─────────────────────────────────────────────────────────┐
│ Configuração da Rede                                    │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Arquivo JSON: [config.json        ] [Carregar Rede] │ │
│ │ Status: Aguardando carregamento da rede...          │ │
│ └─────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────┤
│ Parâmetros de Busca                                     │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Algoritmo:      [Flooding                      ▼]   │ │
│ │ Nó de Origem:   [n1                            ]   │ │
│ │ Recurso:        [fileA                         ]   │ │
│ │ TTL:            [10                            ]   │ │
│ │ ☑ Habilitar visualização gráfica                   │ │
│ │                [Executar Busca]                     │ │
│ └─────────────────────────────────────────────────────┘ │
│ Estatísticas da Busca                                   │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Status:              SUCESSO                        │ │
│ │ Total de Mensagens:  2                              │ │
│ │ Número de Hops:      1                              │ │
│ │ Mensagens/Nó:        0,17                           │ │
│ │ Tempo de Execução:   500 ms                         │ │
│ └─────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────┤
│ Log de Execução                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Carregando configuração: config.json                │ │
│ │ ✓ Configuração carregada                            │ │
│ │ Construindo topologia da rede...                    │ │
│ │ ✓ Topologia criada: NetworkTopology[nodes=12...]    │ │
│ │ ✓ Validações: Conectividade, Grau, Recursos...      │ │
│ │ ✓ Simulador pronto!                                 │ │
│ │ ==============================================     │ │
│ │ Iniciando busca...                                  │ │
│ │ ==============================================     │ │
│ │ [...]                                               │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

## 🎯 Exemplo de Uso Completo

1. Execute `.\run-gui.bat`
2. A janela abre com `config.json` já selecionado
3. Clique em **"Carregar Rede"**
4. Log mostra: "✓ Configuração carregada", "✓ Topologia criada..."
5. Status fica verde: "Rede carregada com sucesso!"
6. Digite `n1` em "Nó de Origem"
7. Digite `fileA` em "Recurso"
8. Deixe TTL em `10`
9. Marque "Habilitar visualização gráfica"
10. Clique em **"Executar Busca"**
11. Janela GraphStream abre mostrando a rede
12. Log mostra progresso em tempo real
13. Painel "Estatísticas" aparece com resultados
14. Status: **SUCESSO** (em verde)
15. Total de Mensagens: **2**, Hops: **1**

## 🔄 Executar Múltiplas Buscas

Sem necessidade de reiniciar:

1. Após uma busca, altere os parâmetros (ex: recurso `fileB`)
2. Clique novamente em **"Executar Busca"**
3. O simulador reseta automaticamente e executa nova busca
4. Log e estatísticas são atualizados

## 🐛 Resolução de Problemas

### Janela não abre
- Verifique se o JAR foi compilado: `mvn clean package`
- Confirme que Java 17+ está instalado: `java -version`

### Erro "JAR não encontrado"
- Execute `mvn clean package` antes de `.\run-gui.bat`

### Visualização GraphStream não funciona
- Certifique-se de ter ambiente gráfico (não funciona via SSH)
- Verifique se marcou "Habilitar visualização gráfica"

### Botão "Executar Busca" desabilitado
- Primeiro clique em "Carregar Rede"
- Aguarde status verde: "Rede carregada com sucesso!"

## 💡 Dicas

- **Modo CLI ainda disponível**: Execute `.\run.bat` ou `java -jar target\p2p-simulator.jar --cli`
- **Arquivos JSON customizados**: Use botão "..." para navegar até outro arquivo
- **TTL recomendado**: 10 é suficiente para redes pequenas (12 nós)
- **Visualização lenta**: Para redes grandes (100+ nós), desabilite visualização para execução mais rápida

## 📝 Notas Técnicas

- Framework: **Java Swing**
- Look & Feel: Sistema nativo do Windows
- Thread model: `SwingWorker` para operações longas (não trava a UI)
- Encoding: UTF-8 nativo do Swing (sem problemas de console)
- Fonte do log: Consolas 12pt (monospace para melhor legibilidade)
