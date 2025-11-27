package p2p.search.simulator.ui;

import org.graphstream.ui.swing_viewer.ViewPanel;
import p2p.search.simulator.loader.NetworkLoader;
import p2p.search.simulator.model.NetworkConfig;
import p2p.search.simulator.simulation.SimulationManager;
import p2p.search.simulator.strategy.FloodingStrategy;
import p2p.search.simulator.strategy.InformedFloodingStrategy;
import p2p.search.simulator.strategy.InformedRandomWalkStrategy;
import p2p.search.simulator.strategy.RandomWalkStrategy;
import p2p.search.simulator.strategy.SearchStrategy;
import p2p.search.simulator.topology.NetworkTopology;
import p2p.search.simulator.visualization.NetworkVisualizer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Interface gráfica principal do simulador P2P.
 */
public class SimulatorGUI extends JFrame {
    
    private static final int DEFAULT_TTL = 10;
    private static final int MIN_VIS_DELAY = 100;
    private static final int MAX_VIS_DELAY = 1500;
    private static final int DEFAULT_VIS_DELAY = 500;
    
    private NetworkTopology topology;
    private SimulationManager simulationManager;
    private NetworkVisualizer visualizer;
    
    // Componentes da UI
    private JTextField configPathField;
    private JComboBox<String> algorithmCombo;
    private JTextField sourceNodeField;
    private JTextField resourceField;
    private JSpinner ttlSpinner;
    private JCheckBox visualizationCheckBox;
    private JTextArea logArea;
    private JButton loadButton;
    private JButton runButton;
    private JLabel statusLabel;
    private JPanel statsPanel;
    private JPanel visualizationPanel;
    private JLabel visualizationPlaceholder;
    private JSlider speedSlider;
    private JLabel speedLabel;
    private JButton replayButton;
    private SimulationManager.SearchResult lastResult;
    
    public SimulatorGUI() {
        setTitle("Simulador de Busca em Redes P2P");
    setSize(1100, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents();
    }
    
    private void initComponents() {
        // Painel de configuração
        configPathField = new JTextField("config.json", 30);
        JButton browseButton = new JButton("...");
        browseButton.addActionListener(e -> browseConfigFile());
        
        loadButton = new JButton("Carregar Rede");
        loadButton.addActionListener(e -> loadNetwork());
        
        // Painel de busca
        algorithmCombo = new JComboBox<>(new String[]{
            "Flooding",
            "Random Walk",
            "Informed Flooding",
            "Informed Random Walk"
        });
        
        sourceNodeField = new JTextField("n1", 10);
        resourceField = new JTextField("fileA", 10);
        ttlSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_TTL, 1, 100, 1));
    visualizationCheckBox = new JCheckBox("Habilitar visualizacao grafica", true);
    visualizationCheckBox.addActionListener(e -> toggleVisualizationControls());

    speedSlider = new JSlider(MIN_VIS_DELAY, MAX_VIS_DELAY, DEFAULT_VIS_DELAY);
    speedSlider.setMajorTickSpacing(350);
    speedSlider.setPaintTicks(true);
    speedSlider.addChangeListener(e -> updateVisualizationSpeed());
    speedLabel = new JLabel("Delay: " + DEFAULT_VIS_DELAY + " ms");
    speedSlider.setEnabled(visualizationCheckBox.isSelected());

    replayButton = new JButton("Reproduzir caminho");
    replayButton.setEnabled(false);
    replayButton.addActionListener(e -> replayLastPath());
        
        runButton = new JButton("Executar Busca");
        runButton.setEnabled(false);
        runButton.addActionListener(e -> executeSearch());
        
        // Área de log
        logArea = new JTextArea(15, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        
        // Status
        statusLabel = new JLabel("Aguardando carregamento da rede...");
        statusLabel.setForeground(Color.BLUE);
        
        // Painel de estatísticas
    statsPanel = new JPanel();
    statsPanel.setBorder(BorderFactory.createTitledBorder("Estatisticas da Busca"));
    statsPanel.setLayout(new GridLayout(0, 2, 10, 5));
    statsPanel.setVisible(false);

        visualizationPanel = new JPanel(new BorderLayout());
        visualizationPanel.setBorder(BorderFactory.createTitledBorder("Visualizacao da Rede"));
        visualizationPlaceholder = new JLabel("Carregue a rede e habilite a visualizacao.", SwingConstants.CENTER);
        visualizationPanel.add(visualizationPlaceholder, BorderLayout.CENTER);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Painel superior - Configuração da rede
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Configuracao da Rede"));
        
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        configPanel.add(new JLabel("Arquivo JSON:"));
        configPanel.add(configPathField);
        configPanel.add(loadButton);
        
        topPanel.add(configPanel, BorderLayout.CENTER);
        topPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Painel central - Parâmetros de busca
    JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Parametros de Busca"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Linha 1: Algoritmo
        gbc.gridx = 0; gbc.gridy = 0;
        searchPanel.add(new JLabel("Algoritmo:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(algorithmCombo, gbc);
        
        // Linha 2: Nó de origem
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("No de Origem:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(sourceNodeField, gbc);
        
        // Linha 3: Recurso
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("Recurso:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(resourceField, gbc);
        
        // Linha 4: TTL
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(new JLabel("TTL:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(ttlSpinner, gbc);
        
        // Linha 5: Visualização
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        searchPanel.add(visualizationCheckBox, gbc);

        // Linha 6: Controle de velocidade
        JPanel sliderPanel = new JPanel(new BorderLayout(5, 0));
        sliderPanel.add(new JLabel("Delay (ms):"), BorderLayout.WEST);
        sliderPanel.add(speedSlider, BorderLayout.CENTER);
        sliderPanel.add(speedLabel, BorderLayout.EAST);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        searchPanel.add(sliderPanel, gbc);

        // Linha 7: Botão executar
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(runButton, gbc);
        
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(statsPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, visualizationPanel);
        splitPane.setResizeWeight(0.45);
        splitPane.setContinuousLayout(true);
        
        // Painel inferior - Log
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Log de Execucao"));
        JScrollPane scrollPane = new JScrollPane(logArea);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Adiciona tudo ao frame
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Padding
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
    
    private void browseConfigFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".json");
            }
            public String getDescription() {
                return "Arquivos JSON (*.json)";
            }
        });
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            configPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void loadNetwork() {
        String configPath = configPathField.getText().trim();
        
        if (configPath.isEmpty()) {
            showError("Por favor, especifique um arquivo de configuracao.");
            return;
        }
        
        loadButton.setEnabled(false);
        statusLabel.setText("Carregando rede...");
        statusLabel.setForeground(Color.ORANGE);
        logArea.setText("");
        
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Carregando configuracao: " + configPath);
                
                NetworkLoader loader = new NetworkLoader();
                NetworkConfig config;
                
                try {
                    if (configPath.equals("config.json")) {
                        config = loader.loadFromResource(configPath);
                    } else {
                        config = loader.loadFromFile(configPath);
                    }
                    publish("✓ Configuracao carregada");
                    
                    publish("\nConstruindo topologia da rede...");
                    topology = new NetworkTopology(config);
                    publish("✓ Topologia criada: " + topology);
                    publish("✓ Validacoes: Conectividade, Grau, Recursos, Self-loops");
                    
                    simulationManager = new SimulationManager(topology);
                    publish("\n✓ Simulador pronto!");
                    
                } catch (Exception e) {
                    throw new Exception("Erro ao carregar rede: " + e.getMessage(), e);
                }
                
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String msg : chunks) {
                    logArea.append(msg + "\n");
                }
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Rede carregada com sucesso!");
                    statusLabel.setForeground(new Color(0, 150, 0));
                    runButton.setEnabled(true);
                    loadButton.setEnabled(true);
                    
                    // Preenche lista de nós disponíveis
                    if (topology != null && !topology.getAllNodes().isEmpty()) {
                        sourceNodeField.setText(new ArrayList<>(topology.getNodeIds()).get(0));
                    }
                    
                } catch (Exception e) {
                    statusLabel.setText("Erro ao carregar rede!");
                    statusLabel.setForeground(Color.RED);
                    showError("Erro ao carregar rede:\n" + e.getCause().getMessage());
                    loadButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void executeSearch() {
        if (simulationManager == null || topology == null) {
            showError("Carregue uma rede antes de executar a busca.");
            return;
        }

    String sourceNode = sourceNodeField.getText().trim();
    String resource = resourceField.getText().trim();
    int ttl = (Integer) ttlSpinner.getValue();
    boolean enableViz = visualizationCheckBox.isSelected();

        if (sourceNode.isEmpty() || resource.isEmpty()) {
            showError("Por favor, preencha o no de origem e o recurso.");
            return;
        }

    runButton.setEnabled(false);
    replayButton.setEnabled(false);
    lastResult = null;
        statusLabel.setText("Executando busca...");
        statusLabel.setForeground(Color.ORANGE);
        statsPanel.setVisible(false);

        SwingWorker<SimulationManager.SearchResult, String> worker = new SwingWorker<>() {
            @Override
            protected SimulationManager.SearchResult doInBackground() throws Exception {
        SearchStrategy strategy = resolveSelectedStrategy();

                publish("\n==============================================");
                publish("Iniciando busca...");
                publish("==============================================");
                publish("Origem: " + sourceNode);
                publish("Recurso: " + resource);
                publish("Algoritmo: " + strategy.getName());
                publish("TTL: " + ttl);
                publish("==============================================\n");

                simulationManager.setLogConsumer(msg -> publish(msg));

                if (enableViz) {
                    visualizer = topology.show();
                    simulationManager.enableVisualization(visualizer);
                    simulationManager.setVisualizationDelay(SimulatorGUI.this.getSelectedDelay());
                    publish("✓ Visualizacao habilitada");
                    SimulatorGUI.this.attachVisualizerView();
                } else {
                    simulationManager.disableVisualization();
                    SimulatorGUI.this.showVisualizationPlaceholder("Visualizacao desativada.");
                }

                return simulationManager.runSearch(sourceNode, resource, ttl, strategy);
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String msg : chunks) {
                    logArea.append(msg + "\n");
                }
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }

            @Override
            protected void done() {
                try {
                    SimulationManager.SearchResult result = get();
                    displayResults(result);
                    statusLabel.setText("Busca concluida!");
                    statusLabel.setForeground(new Color(0, 150, 0));

                } catch (Exception e) {
                    statusLabel.setText("Erro durante a busca!");
                    statusLabel.setForeground(Color.RED);
                    showError("Erro durante a busca:\n" + e.getMessage());
                } finally {
                    runButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }
    
    private void displayResults(SimulationManager.SearchResult result) {
        lastResult = result;
        logArea.append("\n==============================================\n");
        logArea.append("   ESTATISTICAS DA BUSCA\n");
        logArea.append("==============================================\n");
        logArea.append("Status: " + (result.isSuccess() ? "✓ SUCESSO" : "✗ FALHOU") + "\n");
        logArea.append("Recurso: " + result.getResource() + "\n");
        logArea.append("Origem: " + result.getSourceNode() + "\n");
        logArea.append("----------------------------------------------\n");
        logArea.append("Total de Mensagens: " + result.getTotalMessages() + "\n");
        logArea.append("Numero de Hops: " + result.getHops() + "\n");
        logArea.append("Mensagens/No: " + String.format("%.2f",
            (double) result.getTotalMessages() / topology.getNodeCount()) + "\n");
        logArea.append("Tempo de Execucao: " + result.getDurationMs() + " ms\n");
        if (!result.getPath().isEmpty()) {
            logArea.append("Caminho: " + String.join(" → ", result.getPath()) + "\n");
        }
        logArea.append("==============================================\n");

        statsPanel.removeAll();
        boolean pathAvailable = result.getPath() != null && result.getPath().size() >= 2;

        statsPanel.add(new JLabel("Status:"));
        JLabel statusLbl = new JLabel(result.isSuccess() ? "SUCESSO" : "FALHOU");
        statusLbl.setForeground(result.isSuccess() ? new Color(0, 150, 0) : Color.RED);
        statusLbl.setFont(statusLbl.getFont().deriveFont(Font.BOLD));
        statsPanel.add(statusLbl);

        statsPanel.add(new JLabel("Total de Mensagens:"));
        statsPanel.add(new JLabel(String.valueOf(result.getTotalMessages())));

        statsPanel.add(new JLabel("Numero de Hops:"));
        statsPanel.add(new JLabel(String.valueOf(result.getHops())));

        statsPanel.add(new JLabel("Mensagens/No:"));
        statsPanel.add(new JLabel(String.format("%.2f",
            (double) result.getTotalMessages() / topology.getNodeCount())));

        statsPanel.add(new JLabel("Tempo de Execucao:"));
        statsPanel.add(new JLabel(result.getDurationMs() + " ms"));

        if (!result.getPath().isEmpty()) {
            statsPanel.add(new JLabel("Caminho:"));
            statsPanel.add(new JLabel(String.join(" → ", result.getPath())));
        }

        if (pathAvailable) {
            if (replayButton.getParent() != null) {
                replayButton.getParent().remove(replayButton);
            }
            statsPanel.add(new JLabel("Acoes:"));
            boolean visualizationReady = visualizationCheckBox.isSelected() && visualizer != null;
            replayButton.setEnabled(visualizationReady);
            replayButton.setToolTipText(visualizationReady
                ? "Reproduzir caminho desta execucao"
                : "Habilite a visualizacao grafica para reproduzir o caminho");
            statsPanel.add(replayButton);
        } else {
            replayButton.setEnabled(false);
        }

        statsPanel.setVisible(true);
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private SearchStrategy resolveSelectedStrategy() {
        String selection = (String) algorithmCombo.getSelectedItem();
        if (selection == null) {
            return new FloodingStrategy();
        }

        return switch (selection) {
            case "Random Walk" -> new RandomWalkStrategy();
            case "Informed Flooding" -> new InformedFloodingStrategy();
            case "Informed Random Walk" -> new InformedRandomWalkStrategy();
            default -> new FloodingStrategy();
        };
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void toggleVisualizationControls() {
        if (speedSlider == null || visualizationCheckBox == null) {
            return;
        }
        boolean enabled = visualizationCheckBox.isSelected();
        speedSlider.setEnabled(enabled);
        if (!enabled) {
            if (simulationManager != null) {
                simulationManager.disableVisualization();
            }
            showVisualizationPlaceholder("Visualizacao desativada.");
            if (replayButton != null) {
                replayButton.setEnabled(false);
            }
        } else if (replayButton != null) {
            boolean canReplay = lastResult != null
                && lastResult.getPath() != null
                && lastResult.getPath().size() >= 2
                && visualizer != null;
            replayButton.setEnabled(canReplay);
        }
    }

    private void updateVisualizationSpeed() {
        if (speedSlider == null) {
            return;
        }
        int delay = getSelectedDelay();
        if (speedLabel != null) {
            speedLabel.setText("Delay: " + delay + " ms");
        }
        if (simulationManager != null) {
            simulationManager.setVisualizationDelay(delay);
        }
    }

    private int getSelectedDelay() {
        return speedSlider != null ? speedSlider.getValue() : DEFAULT_VIS_DELAY;
    }

    private void replayLastPath() {
        if (visualizer == null || lastResult == null || lastResult.getPath().size() < 2) {
            showError("Execute uma busca com visualizacao para reproduzir o caminho.");
            return;
        }

        replayButton.setEnabled(false);
        SwingWorker<Void, Void> replayWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                visualizer.animatePath(lastResult.getPath(), Math.max(80, getSelectedDelay()));
                return null;
            }

            @Override
            protected void done() {
                replayButton.setEnabled(true);
            }
        };
        replayWorker.execute();
    }

    private void attachVisualizerView() {
        if (visualizer == null) {
            showVisualizationPlaceholder("Visualizacao indisponivel.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            visualizationPanel.removeAll();
            ViewPanel view = visualizer.getViewPanel();
            if (view.getParent() != null) {
                ((Container) view.getParent()).remove(view);
            }
            visualizationPanel.add(view, BorderLayout.CENTER);
            visualizationPanel.revalidate();
            visualizationPanel.repaint();
        });
    }

    private void showVisualizationPlaceholder(String message) {
        SwingUtilities.invokeLater(() -> {
            visualizationPanel.removeAll();
            visualizationPlaceholder.setText(message);
            visualizationPanel.add(visualizationPlaceholder, BorderLayout.CENTER);
            visualizationPanel.revalidate();
            visualizationPanel.repaint();
        });
    }

    @Override
    public void dispose() {
        if (visualizer != null) {
            visualizer.close();
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        // Configura Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignora e usa o padrão
        }
        
        SwingUtilities.invokeLater(() -> {
            SimulatorGUI gui = new SimulatorGUI();
            gui.setVisible(true);
        });
    }
}
