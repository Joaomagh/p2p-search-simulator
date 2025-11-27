package p2p.search.simulator;

import p2p.search.simulator.loader.NetworkLoader;
import p2p.search.simulator.model.NetworkConfig;
import p2p.search.simulator.simulation.SimulationManager;
import p2p.search.simulator.strategy.FloodingStrategy;
import p2p.search.simulator.strategy.InformedFloodingStrategy;
import p2p.search.simulator.strategy.InformedRandomWalkStrategy;
import p2p.search.simulator.strategy.RandomWalkStrategy;
import p2p.search.simulator.strategy.SearchStrategy;
import p2p.search.simulator.topology.NetworkTopology;
import p2p.search.simulator.ui.SimulatorGUI;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.IOException;
import java.util.Scanner;

/**
 * Classe principal do simulador P2P.
 */
public class Main {

    private static final String DEFAULT_CONFIG = "config.json";
    private static final int DEFAULT_TTL = 10;

    public static void main(String[] args) {
        boolean useGUI = true;

        for (String arg : args) {
            if (arg.equalsIgnoreCase("--cli") || arg.equalsIgnoreCase("-c")) {
                useGUI = false;
                break;
            }
        }

        if (useGUI) {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }
                SimulatorGUI gui = new SimulatorGUI();
                gui.setVisible(true);
            });
        } else {
            runCLI(args);
        }
    }

    private static void runCLI(String[] args) {
        System.out.println("==============================================");
        System.out.println("   Simulador de Busca em Redes P2P");
        System.out.println("==============================================\n");

        try {
            // 1. Carregar configuração
            String configPath = args.length > 0 ? args[0] : DEFAULT_CONFIG;
            NetworkConfig config = loadConfiguration(configPath);

            // 2. Criar topologia
            System.out.println("Construindo topologia da rede...");
            NetworkTopology topology = new NetworkTopology(config);
            System.out.println("✓ Topologia criada: " + topology);
            System.out.println("✓ Validações: Conectividade, Grau, Recursos, Self-loops\n");

            // 3. Criar gerenciador de simulação
            SimulationManager simulationManager = new SimulationManager(topology);
            simulationManager.setLogConsumer(System.out::println);

            // 4. Scanner para entrada do usuário
            Scanner scanner = new Scanner(System.in);

            // 5. Escolher algoritmo
            SearchStrategy strategy = chooseAlgorithm(args, scanner);

            // 6. Escolher modo de visualização
            boolean visualMode = chooseVisualizationMode(args, scanner);

            if (visualMode) {
                System.out.println("\n✓ Modo de visualização habilitado");
                simulationManager.enableVisualization(topology.show());
                simulationManager.setVisualizationDelay(500);
            } else {
                simulationManager.disableVisualization();
            }

            // 7. Escolher parâmetros da busca
            System.out.print("\nDigite o ID do nó de origem (ex: n1): ");
            String sourceNodeId = scanner.nextLine().trim();

            System.out.print("Digite o recurso a buscar (ex: fileA): ");
            String resource = scanner.nextLine().trim();

            System.out.print("Digite o TTL (default=" + DEFAULT_TTL + "): ");
            String ttlInput = scanner.nextLine().trim();
            int ttl = ttlInput.isEmpty() ? DEFAULT_TTL : Integer.parseInt(ttlInput);

            // 8. Executar simulação
            System.out.println("\n==============================================");
            System.out.println("Iniciando busca...");
            System.out.println("==============================================");
            System.out.println("Origem: " + sourceNodeId);
            System.out.println("Recurso: " + resource);
            System.out.println("Algoritmo: " + strategy.getName());
            System.out.println("TTL: " + ttl);
            System.out.println("==============================================\n");

            SimulationManager.SearchResult result =
                simulationManager.runSearch(sourceNodeId, resource, ttl, strategy);

            // 9. Exibir estatísticas
            printStatistics(result, topology.getNodeCount());

            // 10. Aguardar fechamento (se modo visual)
            if (visualMode) {
                System.out.println("\nPressione ENTER para finalizar...");
                scanner.nextLine();
            }

            scanner.close();

        } catch (Exception e) {
            System.err.println("Erro durante a execução: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("\nSimulação finalizada.");
    }

    /**
     * Carrega a configuração da rede.
     */
    private static NetworkConfig loadConfiguration(String configPath) throws IOException {
        System.out.println("Carregando configuração: " + configPath);
        NetworkLoader loader = new NetworkLoader();

        NetworkConfig config;
        if (configPath.equals(DEFAULT_CONFIG)) {
            config = loader.loadFromResource(configPath);
        } else {
            config = loader.loadFromFile(configPath);
        }

        System.out.println("✓ Configuração carregada\n");
        return config;
    }

    /**
     * Permite escolher o algoritmo de busca.
     */
    private static SearchStrategy chooseAlgorithm(String[] args, Scanner scanner) {
        if (args.length > 1) {
            String alg = args[1].toLowerCase();
            return createStrategy(alg);
        }

        System.out.println("Escolha o algoritmo de busca:");
        System.out.println("1. Flooding (padrão)");
        System.out.println("2. Random Walk");
        System.out.println("3. Informed Flooding");
        System.out.println("4. Informed Random Walk");
        System.out.print("\nOpção [1]: ");

        String choice = scanner.nextLine().trim();
        if (choice.isEmpty()) {
            choice = "1";
        }

        return createStrategy(choice);
    }

    /**
     * Cria a estratégia baseada na escolha.
     */
    private static SearchStrategy createStrategy(String choice) {
        switch (choice.toLowerCase()) {
            case "1":
            case "flooding":
                return new FloodingStrategy();
            case "2":
            case "random":
            case "randomwalk":
                return new RandomWalkStrategy();
            case "3":
            case "informed-flooding":
                return new InformedFloodingStrategy();
            case "4":
            case "informed-random":
            case "informedrandomwalk":
                return new InformedRandomWalkStrategy();
            default:
                System.out.println("Algoritmo não reconhecido, usando Flooding como padrão.");
                return new FloodingStrategy();
        }
    }

    /**
     * Pergunta se deseja modo de visualização.
     */
    private static boolean chooseVisualizationMode(String[] args, Scanner scanner) {
        if (args.length > 2) {
            return args[2].equalsIgnoreCase("visual") || args[2].equalsIgnoreCase("true");
        }

        System.out.print("\nHabilitar visualização gráfica? (s/N): ");
        String response = scanner.nextLine().trim().toLowerCase();

        return response.equals("s") || response.equals("sim") || response.equals("y") || response.equals("yes");
    }

    /**
     * Imprime as estatísticas da busca.
     */
    private static void printStatistics(SimulationManager.SearchResult result, int totalNodes) {
        System.out.println("\n==============================================");
        System.out.println("   ESTATÍSTICAS DA BUSCA");
        System.out.println("==============================================");
        System.out.println("Status: " + (result.isSuccess() ? "✓ SUCESSO" : "✗ FALHOU"));
        System.out.println("Recurso: " + result.getResource());
        System.out.println("Origem: " + result.getSourceNode());
        System.out.println("----------------------------------------------");
        System.out.println("Total de Mensagens: " + result.getTotalMessages());
        System.out.println("Número de Hops: " + result.getHops());
        System.out.println("Mensagens/Nó: " + String.format("%.2f", (double) result.getTotalMessages() / totalNodes));
        System.out.println("Tempo de Execução: " + result.getDurationMs() + " ms");
        if (!result.getPath().isEmpty()) {
            System.out.println("Caminho: " + String.join(" → ", result.getPath()));
        }
        System.out.println("==============================================");
    }
}
