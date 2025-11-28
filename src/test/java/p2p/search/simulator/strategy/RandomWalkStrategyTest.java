package p2p.search.simulator.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import p2p.search.simulator.loader.NetworkLoader;
import p2p.search.simulator.model.NetworkConfig;
import p2p.search.simulator.simulation.SimulationManager;
import p2p.search.simulator.topology.NetworkTopology;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para RandomWalkStrategy.
 * Valida comportamento estocástico, anti-echo e TTL.
 */
class RandomWalkStrategyTest {

    private NetworkTopology topology;
    private SimulationManager simulationManager;
    private RandomWalkStrategy strategy;

    @BeforeEach
    void setUp() throws IOException {
        NetworkLoader loader = new NetworkLoader();
        NetworkConfig config = loader.loadFromResource("config.json");
        topology = new NetworkTopology(config);
        simulationManager = new SimulationManager(topology);
        strategy = new RandomWalkStrategy();
    }

    @Test
    void testRandomWalkSucceedsEventually() {
        // Random Walk é estocástico, mas com múltiplas tentativas deve ter sucesso
        int successCount = 0;
        int attempts = 50;
        
        for (int i = 0; i < attempts; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 25, strategy);
            
            if (result.isSuccess()) {
                successCount++;
                
                // Validações quando encontra
                assertTrue(result.getPath().contains("n12"), 
                    "Path should contain destination node");
                assertTrue(result.getHops() > 0, 
                    "Should have at least one hop");
                assertTrue(result.getVisitedNodes() > 0, 
                    "Should visit at least one node");
                assertTrue(result.getTotalMessages() > 0, 
                    "Should send at least one message");
            }
        }
        
        // Random Walk deve ter taxa de sucesso razoável (> 15% é aceitável para topologia complexa)
        double successRate = (double) successCount / attempts;
        System.out.println("Random Walk success rate: " + 
            String.format("%.1f%%", successRate * 100) + 
            " (" + successCount + "/" + attempts + ")");
        
        assertTrue(successRate > 0.15, 
            "Random Walk should succeed at least 15% of the time with TTL=25");
    }

    @Test
    void testRandomWalkUsesFewerMessagesThanFlooding() {
        // Comparar mensagens: Random Walk deve usar menos que Flooding
        
        // 1. Random Walk (média de múltiplas tentativas)
        int rwTotalMessages = 0;
        int rwSuccesses = 0;
        
        for (int i = 0; i < 20; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 20, strategy);
            
            if (result.isSuccess()) {
                rwTotalMessages += result.getTotalMessages();
                rwSuccesses++;
            }
        }
        
        assertTrue(rwSuccesses > 0, "Random Walk should succeed at least once");
        double rwAvgMessages = (double) rwTotalMessages / rwSuccesses;
        
        // 2. Flooding (determinístico)
        simulationManager.reset();
        SimulationManager.SearchResult floodingResult = 
            simulationManager.runSearch("n1", "fileR", 10, new FloodingStrategy());
        
        System.out.println("Random Walk avg messages: " + String.format("%.1f", rwAvgMessages));
        System.out.println("Flooding messages: " + floodingResult.getTotalMessages());
        
        // Random Walk deve usar menos mensagens que Flooding
        assertTrue(rwAvgMessages < floodingResult.getTotalMessages(),
            "Random Walk should use fewer messages than Flooding");
    }

    @Test
    void testRandomWalkRespectsAntiEcho() {
        // Executar múltiplas vezes e verificar que não há comportamento de echo
        // Se houvesse echo, haveria loops infinitos ou muitas mensagens
        
        for (int i = 0; i < 10; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 15, strategy);
            
            // Com TTL=15, mesmo com echo, não deve ultrapassar limite razoável
            assertTrue(result.getTotalMessages() <= 30,
                "Anti-echo should prevent excessive messages. Got: " + 
                result.getTotalMessages());
        }
    }

    @Test
    void testRandomWalkRespectsTTL() {
        // Com TTL baixo, busca deve falhar ou ter caminho curto
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 2, strategy);
        
        if (result.isSuccess()) {
            // Se teve sucesso, caminho deve ser curto
            assertTrue(result.getHops() <= 2,
                "With TTL=2, path should not exceed 2 hops");
        }
        
        // Mesmo falhando, não deve enviar muitas mensagens
        assertTrue(result.getTotalMessages() <= 10,
            "TTL should limit message count. Got: " + result.getTotalMessages());
    }

    @Test
    void testRandomWalkVisitsLinearPath() {
        // Random Walk visita nós ao longo do caminho, não todos
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 20, strategy);
        
        if (result.isSuccess()) {
            int totalNodes = topology.getNodeCount();
            int visitedNodes = result.getVisitedNodes();
            
            // Random Walk visita menos nós que Flooding
            assertTrue(visitedNodes < totalNodes,
                "Random Walk should not visit all nodes");
            
            System.out.println("Random Walk visited: " + visitedNodes + 
                "/" + totalNodes + " nodes (" + 
                String.format("%.1f%%", (double) visitedNodes * 100 / totalNodes) + ")");
        }
    }

    @Test
    void testRandomWalkFindsLocalResource() {
        // Buscar recurso no próprio nó de origem
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileA", 10, strategy);
        
        assertTrue(result.isSuccess(), "Should find local resource");
        // Random Walk pode fazer saltos mesmo com recurso local (comportamento estocástico)
        // O importante é que encontre
        assertTrue(result.getHops() >= 0, "Should have valid hop count");
        assertTrue(result.getVisitedNodes() >= 1, "Should visit at least source node");
        assertEquals("n1", result.getPath().get(0), "Path should start with source");
    }

    @Test
    void testRandomWalkWithInsufficientTTL() {
        // TTL=1 não deve alcançar nós distantes
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 1, strategy);
        
        // Provavelmente vai falhar (n12 está longe)
        // Mas se tiver sorte e o vizinho direto for o caminho certo, pode suceder
        
        // O importante é que respeite o TTL
        assertTrue(result.getTotalMessages() <= 5,
            "TTL=1 should severely limit messages");
        
        if (result.isSuccess()) {
            assertEquals(1, result.getHops(), "With TTL=1, can only reach direct neighbors");
        }
    }

    @Test
    void testRandomWalkMultipleSearchesIndependent() {
        // Múltiplas buscas devem ser independentes (reset funciona)
        
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 15, strategy);
        
        simulationManager.reset();
        
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileR", 15, strategy);
        
        // Resultados podem ser diferentes (estocástico)
        // Mas ambos devem ter métricas válidas
        assertTrue(result1.getTotalMessages() > 0, "First search should send messages");
        assertTrue(result2.getTotalMessages() > 0, "Second search should send messages");
        
        System.out.println("Search 1: " + result1.getTotalMessages() + " messages, " +
            (result1.isSuccess() ? "SUCCESS" : "FAILED"));
        System.out.println("Search 2: " + result2.getTotalMessages() + " messages, " +
            (result2.isSuccess() ? "SUCCESS" : "FAILED"));
    }

    @Test
    void testStrategyName() {
        assertEquals("Random Walk", strategy.getName());
        assertFalse(strategy.isInformed(), "Random Walk is not informed");
    }
}
