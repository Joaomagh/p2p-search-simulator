package p2p.search.simulator.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import p2p.search.simulator.loader.NetworkLoader;
import p2p.search.simulator.model.NetworkConfig;
import p2p.search.simulator.simulation.SimulationManager;
import p2p.search.simulator.topology.NetworkTopology;

import java.io.IOException;

/**
 * Demonstra a eficiência do anti-echo comparando o comportamento
 * antes e depois da implementação.
 */
class StrategyComparisonTest {

    private NetworkTopology topology;
    private SimulationManager simulationManager;

    @BeforeEach
    void setUp() throws IOException {
        NetworkLoader loader = new NetworkLoader();
        NetworkConfig config = loader.loadFromResource("config.json");
        topology = new NetworkTopology(config);
        simulationManager = new SimulationManager(topology);
    }

    @Test
    void demonstrateAllStrategies() {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║  COMPARAÇÃO: Todas as Estratégias (COM Anti-Echo)   ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");
        
        // Flooding
        System.out.println("1️⃣  FLOODING");
        System.out.println("─────────────────────────────────────────");
        SimulationManager.SearchResult flooding = 
            simulationManager.runSearch("n1", "fileR", 10, new FloodingStrategy());
        printResult(flooding);
        
        // Informed Flooding
        simulationManager.reset();
        System.out.println("\n2️⃣  INFORMED FLOODING");
        System.out.println("─────────────────────────────────────────");
        SimulationManager.SearchResult informedFlooding = 
            simulationManager.runSearch("n1", "fileR", 10, new InformedFloodingStrategy());
        printResult(informedFlooding);
        
        // Random Walk (multiple attempts)
        System.out.println("\n3️⃣  RANDOM WALK (20 tentativas)");
        System.out.println("─────────────────────────────────────────");
        int rwSuccess = 0;
        int rwTotalMessages = 0;
        for (int i = 0; i < 20; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult rw = 
                simulationManager.runSearch("n1", "fileR", 20, new RandomWalkStrategy());
            if (rw.isSuccess()) {
                rwSuccess++;
                rwTotalMessages += rw.getTotalMessages();
            }
        }
        System.out.println("  Taxa de sucesso: " + rwSuccess + "/20 (" + (rwSuccess * 5) + "%)");
        if (rwSuccess > 0) {
            double rwAvg = (double) rwTotalMessages / rwSuccess;
            System.out.println("  Mensagens médias: " + String.format("%.1f", rwAvg));
        }
        
        // Informed Random Walk (multiple attempts)
        System.out.println("\n4️⃣  INFORMED RANDOM WALK (20 tentativas)");
        System.out.println("─────────────────────────────────────────");
        int irwSuccess = 0;
        int irwTotalMessages = 0;
        for (int i = 0; i < 20; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult irw = 
                simulationManager.runSearch("n1", "fileR", 20, new InformedRandomWalkStrategy());
            if (irw.isSuccess()) {
                irwSuccess++;
                irwTotalMessages += irw.getTotalMessages();
            }
        }
        System.out.println("  Taxa de sucesso: " + irwSuccess + "/20 (" + (irwSuccess * 5) + "%)");
        if (irwSuccess > 0) {
            double irwAvg = (double) irwTotalMessages / irwSuccess;
            System.out.println("  Mensagens médias: " + String.format("%.1f", irwAvg));
        }
        
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                      RESUMO                           ║");
        System.out.println("╠═══════════════════════════════════════════════════════╣");
        System.out.println("║ O anti-echo elimina mensagens desnecessárias:         ║");
        System.out.println("║  • Flooding: não reenvia para o sender                ║");
        System.out.println("║  • Random Walk: exclui sender da escolha aleatória    ║");
        System.out.println("║  • Resultado: menos mensagens, mais eficiência!       ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");
    }

    private void printResult(SimulationManager.SearchResult result) {
        if (result.isSuccess()) {
            System.out.println("  ✓ SUCESSO");
            System.out.println("  Mensagens: " + result.getTotalMessages());
            System.out.println("  Hops: " + result.getHops());
            System.out.println("  Duração: " + result.getDurationMs() + "ms");
            System.out.println("  Caminho: " + result.getPath());
        } else {
            System.out.println("  ✗ FALHOU");
        }
    }
}
