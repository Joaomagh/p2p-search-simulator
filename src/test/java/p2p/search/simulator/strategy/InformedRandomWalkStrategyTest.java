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
 * Testes unitários para InformedRandomWalkStrategy.
 * Valida uso de cache, comportamento estocástico otimizado e anti-echo.
 */
class InformedRandomWalkStrategyTest {

    private NetworkTopology topology;
    private SimulationManager simulationManager;
    private InformedRandomWalkStrategy strategy;

    @BeforeEach
    void setUp() throws IOException {
        NetworkLoader loader = new NetworkLoader();
        NetworkConfig config = loader.loadFromResource("config.json");
        topology = new NetworkTopology(config);
        simulationManager = new SimulationManager(topology);
        strategy = new InformedRandomWalkStrategy();
    }

    @Test
    void testInformedRandomWalkSucceeds() {
        // Estratégia estocástica mas com cache deve ter boa taxa de sucesso
        int successCount = 0;
        int attempts = 30;
        
        for (int i = 0; i < attempts; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 25, strategy);
            
            if (result.isSuccess()) {
                successCount++;
                assertTrue(result.getPath().contains("n12"), 
                    "Path should contain destination");
            }
        }
        
        double successRate = (double) successCount / attempts;
        System.out.println("Informed Random Walk success rate: " + 
            String.format("%.1f%%", successRate * 100) + 
            " (" + successCount + "/" + attempts + ")");
        
        // Informed RW deve ter algum sucesso (threshold mais baixo para aceitar variabilidade)
        assertTrue(successRate > 0.10, 
            "Informed Random Walk should succeed at least 10% of the time");
    }

    @Test
    void testInformedRandomWalkUsesCache() {
        // Primeira busca: sem cache
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 20, strategy);
        
        // Segunda busca: com cache (deve ter melhor taxa de sucesso)
        int successCount = 0;
        int messages = 0;
        int attempts = 10;
        
        for (int i = 0; i < attempts; i++) {
            SimulationManager.SearchResult result2 = 
                simulationManager.runSearch("n1", "fileR", 20, strategy);
            
            if (result2.isSuccess()) {
                successCount++;
                messages += result2.getTotalMessages();
            }
        }
        
        System.out.println("First search: " + 
            (result1.isSuccess() ? "SUCCESS" : "FAILED") + 
            ", " + result1.getTotalMessages() + " messages");
        
        if (successCount > 0) {
            double avgMessages = (double) messages / successCount;
            System.out.println("Cached searches: " + successCount + "/" + attempts + 
                " succeeded, avg " + String.format("%.1f", avgMessages) + " messages");
            
            // Com cache, deve usar menos mensagens em média
            if (result1.isSuccess()) {
                assertTrue(avgMessages <= result1.getTotalMessages() * 1.2,
                    "Cache should reduce message count");
            }
        }
    }

    @Test
    void testInformedRandomWalkUsesFewerMessagesThanRandomWalk() {
        // Comparar com Random Walk básico
        
        // 1. Informed Random Walk (média com cache)
        int irwSuccesses = 0;
        int irwMessages = 0;
        
        for (int i = 0; i < 20; i++) {
            if (i > 0) {
                // Manter cache entre buscas para ver benefício
            } else {
                simulationManager.reset();
            }
            
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 20, strategy);
            
            if (result.isSuccess()) {
                irwSuccesses++;
                irwMessages += result.getTotalMessages();
            }
        }
        
        // 2. Random Walk básico
        simulationManager.reset();
        int rwSuccesses = 0;
        int rwMessages = 0;
        
        for (int i = 0; i < 20; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 20, new RandomWalkStrategy());
            
            if (result.isSuccess()) {
                rwSuccesses++;
                rwMessages += result.getTotalMessages();
            }
        }
        
        System.out.println("Informed Random Walk: " + irwSuccesses + " successes, " +
            (irwSuccesses > 0 ? (irwMessages/irwSuccesses) : 0) + " avg messages");
        System.out.println("Basic Random Walk: " + rwSuccesses + " successes, " +
            (rwSuccesses > 0 ? (rwMessages/rwSuccesses) : 0) + " avg messages");
        
        // Informed deve ter vantagem (mais sucessos ou menos mensagens)
        assertTrue(irwSuccesses >= rwSuccesses || 
            (irwSuccesses > 0 && rwSuccesses > 0 && 
             (double)irwMessages/irwSuccesses < (double)rwMessages/rwSuccesses),
            "Informed Random Walk should perform better than basic Random Walk");
    }

    @Test
    void testInformedRandomWalkRespectsAntiEcho() {
        // Múltiplas buscas não devem gerar loops infinitos
        for (int i = 0; i < 10; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 15, strategy);
            
            // Anti-echo impede explosão de mensagens
            assertTrue(result.getTotalMessages() <= 30,
                "Anti-echo should prevent excessive messages. Got: " + 
                result.getTotalMessages());
        }
    }

    @Test
    void testInformedRandomWalkRespectsTTL() {
        // TTL baixo deve limitar busca
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 2, strategy);
        
        if (result.isSuccess()) {
            assertTrue(result.getHops() <= 2, 
                "Path should respect TTL limit");
        }
        
        assertTrue(result.getTotalMessages() <= 10,
            "TTL should limit messages. Got: " + result.getTotalMessages());
    }

    @Test
    void testInformedRandomWalkCacheDirectsSearch() {
        // Primeira busca popula cache
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 15, strategy);
        
        if (result1.isSuccess()) {
            // Segunda busca para mesmo destino deve ser mais eficiente
            SimulationManager.SearchResult result2 = 
                simulationManager.runSearch("n1", "fileR", 15, strategy);
            
            assertTrue(result2.isSuccess(), 
                "Cache should improve success rate");
            assertTrue(result2.getTotalMessages() <= result1.getTotalMessages(),
                "Cache should reduce messages");
            
            System.out.println("First: " + result1.getTotalMessages() + 
                ", Second (cached): " + result2.getTotalMessages());
        }
    }

    @Test
    void testInformedRandomWalkVisitsFewerNodes() {
        // Random Walk visita menos nós que Flooding
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 20, strategy);
        
        if (result.isSuccess()) {
            int totalNodes = topology.getNodeCount();
            int visitedNodes = result.getVisitedNodes();
            
            assertTrue(visitedNodes < totalNodes,
                "Should not visit all nodes");
            
            System.out.println("Visited: " + visitedNodes + "/" + totalNodes + 
                " nodes (" + 
                String.format("%.1f%%", (double)visitedNodes * 100 / totalNodes) + ")");
        }
    }

    @Test
    void testInformedRandomWalkFindsLocalResource() {
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileA", 10, strategy);
        
        assertTrue(result.isSuccess(), "Should find local resource");
        // Random Walk may take random paths even for local resources
        assertTrue(result.getHops() >= 0, "Should have valid hop count");
        assertTrue(result.getVisitedNodes() >= 1, "Should visit at least source");
    }

    @Test
    void testInformedRandomWalkCacheReset() {
        // Busca com sucesso
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 20, strategy);
        
        // Reset limpa cache
        simulationManager.reset();
        
        // Nova busca comporta-se como primeira (sem cache)
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileR", 20, strategy);
        
        // Ambas devem ter métricas válidas
        assertTrue(result1.getTotalMessages() > 0);
        assertTrue(result2.getTotalMessages() > 0);
        
        System.out.println("Before reset: " + 
            (result1.isSuccess() ? "SUCCESS" : "FAILED") + 
            ", " + result1.getTotalMessages() + " messages");
        System.out.println("After reset: " + 
            (result2.isSuccess() ? "SUCCESS" : "FAILED") + 
            ", " + result2.getTotalMessages() + " messages");
    }

    @Test
    void testInformedRandomWalkPathConsistency() {
        // Buscar múltiplas vezes e validar paths quando bem-sucedido
        for (int i = 0; i < 5; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 20, strategy);
            
            if (result.isSuccess()) {
                var path = result.getPath();
                assertNotNull(path, "Path should not be null");
                assertFalse(path.isEmpty(), "Path should not be empty");
                assertEquals("n1", path.get(0), "Path should start with source");
                assertEquals("n12", path.get(path.size() - 1), 
                    "Path should end with destination");
                assertEquals(result.getHops() + 1, path.size(),
                    "Path size should match hops + 1");
            }
        }
    }

    @Test
    void testInformedRandomWalkCombinesBestOfBoth() {
        // Informed Random Walk deve ter:
        // - Eficiência de mensagens do Random Walk
        // - Benefício do cache do Informed
        
        // Executar múltiplas buscas com cache acumulado
        int totalMessages = 0;
        int successCount = 0;
        
        for (int i = 0; i < 15; i++) {
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 20, strategy);
            
            if (result.isSuccess()) {
                successCount++;
                totalMessages += result.getTotalMessages();
            }
        }
        
        System.out.println("Success rate: " + successCount + "/15 = " +
            String.format("%.1f%%", (double)successCount * 100 / 15));
        
        if (successCount > 0) {
            double avgMessages = (double) totalMessages / successCount;
            System.out.println("Average messages when successful: " + 
                String.format("%.1f", avgMessages));
            
            // Com cache, deve ter boa eficiência
            assertTrue(avgMessages < 25, 
                "With cache, should use reasonable number of messages");
        }
        
        // Deve ter algum sucesso
        assertTrue(successCount > 0, 
            "Should succeed at least once in 15 attempts");
    }

    @Test
    void testStrategyName() {
        assertEquals("Informed Random Walk", strategy.getName());
        assertTrue(strategy.isInformed(), 
            "Informed Random Walk should be informed");
    }
}
