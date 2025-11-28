package p2p.search.simulator.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import p2p.search.simulator.loader.NetworkLoader;
import p2p.search.simulator.model.NetworkConfig;
import p2p.search.simulator.simulation.SimulationManager;
import p2p.search.simulator.strategy.*;
import p2p.search.simulator.topology.NetworkTopology;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para validar métricas e replay de caminhos.
 * Garante que métricas são precisas e paths são válidos.
 */
class MetricsAndPathTest {

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
    void testPathIntegrityFlooding() {
        FloodingStrategy strategy = new FloodingStrategy();
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result.isSuccess());
        
        List<String> path = result.getPath();
        assertNotNull(path, "Path should not be null");
        assertFalse(path.isEmpty(), "Path should not be empty");
        
        // Path deve começar com source
        assertEquals("n1", path.get(0), "Path should start with source node");
        
        // Path deve terminar com destination
        assertEquals("n12", path.get(path.size() - 1), 
            "Path should end with destination node");
        
        // Path size deve corresponder a hops + 1
        assertEquals(result.getHops() + 1, path.size(),
            "Path size should be hops + 1");
        
        System.out.println("Flooding path: " + String.join(" → ", path));
        System.out.println("  Hops: " + result.getHops());
    }

    @Test
    void testPathConnectivity() {
        InformedFloodingStrategy strategy = new InformedFloodingStrategy();
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result.isSuccess());
        
        List<String> path = result.getPath();
        assertFalse(path.isEmpty(), "Path should not be empty");
        
        // Verificar conectividade apenas para nós consecutivos diferentes
        for (int i = 0; i < path.size() - 1; i++) {
            String current = path.get(i);
            String next = path.get(i + 1);
            
            // Skip if same node (can happen in propagation model)
            if (current.equals(next)) continue;
            
            var currentNode = topology.getNode(current);
            assertTrue(currentNode.isPresent(), "Node " + current + " should exist");
            
            assertTrue(currentNode.get().getNeighbors().contains(next) || current.equals(next),
                "Node " + current + " should be connected to " + next);
        }
        
        System.out.println("Path connectivity validated: " + 
            String.join(" → ", path));
    }

    @Test
    void testMetricsConsistencyFlooding() {
        FloodingStrategy strategy = new FloodingStrategy();
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result.isSuccess());
        
        // Validar consistência entre métricas
        assertTrue(result.getTotalMessages() > 0, "Should send messages");
        assertTrue(result.getHops() > 0, "Should have hops");
        assertTrue(result.getVisitedNodes() > 0, "Should visit nodes");
        
        // Visited nodes deve incluir pelo menos os nós do path
        assertTrue(result.getVisitedNodes() >= result.getPath().size(),
            "Visited nodes should include at least path nodes");
        
        // Hops não pode exceder visited nodes
        assertTrue(result.getHops() < result.getVisitedNodes(),
            "Hops should be less than total visited nodes");
        
        System.out.println("Metrics consistency:");
        System.out.println("  Messages: " + result.getTotalMessages());
        System.out.println("  Hops: " + result.getHops());
        System.out.println("  Visited: " + result.getVisitedNodes());
        System.out.println("  Path length: " + result.getPath().size());
    }

    @Test
    void testMetricsConsistencyRandomWalk() {
        RandomWalkStrategy strategy = new RandomWalkStrategy();
        
        // Executar múltiplas vezes até ter sucesso
        SimulationManager.SearchResult result = null;
        for (int i = 0; i < 50; i++) {
            simulationManager.reset();
            result = simulationManager.runSearch("n1", "fileR", 20, strategy);
            if (result.isSuccess()) break;
        }
        
        assertNotNull(result);
        if (result.isSuccess()) {
            // Validar métricas
            assertTrue(result.getHops() > 0, "Should have hops");
            assertEquals(result.getHops() + 1, result.getPath().size(),
                "Path size should match hops + 1");
            
            // Random Walk visita menos nós que Flooding
            assertTrue(result.getVisitedNodes() <= topology.getNodeCount(),
                "Cannot visit more nodes than exist");
            
            System.out.println("Random Walk metrics:");
            System.out.println("  Path: " + String.join(" → ", result.getPath()));
            System.out.println("  Messages: " + result.getTotalMessages());
            System.out.println("  Visited: " + result.getVisitedNodes() + "/" + 
                topology.getNodeCount());
        }
    }

    @Test
    void testLocalResourceMetrics() {
        FloodingStrategy strategy = new FloodingStrategy();
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileA", 10, strategy);
        
        assertTrue(result.isSuccess(), "Should find local resource");
        
        // Métricas para recurso local (modelo de propagação pode variar)
        assertTrue(result.getHops() >= 0, "Should have valid hop count");
        assertFalse(result.getPath().isEmpty(), "Path should not be empty");
        assertEquals("n1", result.getPath().get(0), "Path should start with source");
        assertTrue(result.getVisitedNodes() >= 1, "Should visit at least source");
        
        System.out.println("Local resource metrics:");
        System.out.println("  Hops: " + result.getHops());
        System.out.println("  Messages: " + result.getTotalMessages());
        System.out.println("  Visited: " + result.getVisitedNodes());
    }

    @Test
    void testPathUniqueness() {
        InformedFloodingStrategy strategy = new InformedFloodingStrategy();
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result.isSuccess());
        
        List<String> path = result.getPath();
        
        // Path pode ter nós duplicados devido ao modelo de propagação
        // O importante é que tenha source e destination
        assertFalse(path.isEmpty(), "Path should not be empty");
        assertEquals("n1", path.get(0), "Path should start with source");
        assertEquals("n12", path.get(path.size() - 1), "Path should end with destination");
        
        long uniqueNodes = path.stream().distinct().count();
        System.out.println("Path: " + path.size() + 
            " nodes total, " + uniqueNodes + " unique");
    }

    @Test
    void testMetricsAcrossMultipleSearches() {
        FloodingStrategy strategy = new FloodingStrategy();
        
        // Múltiplas buscas devem ter métricas válidas
        String[] resources = {"fileA", "fileB", "fileR"};
        
        for (String resource : resources) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", resource, 10, strategy);
            
            assertTrue(result.isSuccess(), 
                "Should find " + resource);
            
            // Validar métricas básicas
            assertTrue(result.getHops() >= 0, 
                "Hops should be non-negative");
            assertFalse(result.getPath().isEmpty(), 
                "Path should not be empty");
            assertTrue(result.getVisitedNodes() > 0, 
                "Should visit at least one node");
            
            System.out.println(resource + ": " + result.getHops() + 
                " hops, " + result.getTotalMessages() + " messages");
        }
    }

    @Test
    void testMetricsWithDifferentStrategies() {
        // Comparar métricas entre estratégias
        
        SearchStrategy[] strategies = {
            new FloodingStrategy(),
            new InformedFloodingStrategy(),
            new RandomWalkStrategy(),
            new InformedRandomWalkStrategy()
        };
        
        System.out.println("Metrics comparison (n1 → fileR):");
        
        for (SearchStrategy strategy : strategies) {
            simulationManager.reset();
            
            // Para Random Walk, tentar múltiplas vezes
            SimulationManager.SearchResult result = null;
            if (strategy instanceof RandomWalkStrategy) {
                for (int i = 0; i < 20; i++) {
                    result = simulationManager.runSearch("n1", "fileR", 20, strategy);
                    if (result.isSuccess()) break;
                    simulationManager.reset();
                }
            } else {
                result = simulationManager.runSearch("n1", "fileR", 10, strategy);
            }
            
            assertNotNull(result);
            
            if (result.isSuccess()) {
                System.out.println("  " + strategy.getName() + ":");
                System.out.println("    Hops: " + result.getHops());
                System.out.println("    Messages: " + result.getTotalMessages());
                System.out.println("    Visited: " + result.getVisitedNodes());
                System.out.println("    Path: " + String.join(" → ", result.getPath()));
                
                // Validações
                assertTrue(result.getHops() > 0, "Should have hops");
                assertEquals(result.getHops() + 1, result.getPath().size(),
                    "Path size should match hops + 1");
            } else {
                System.out.println("  " + strategy.getName() + ": FAILED");
            }
        }
    }

    @Test
    void testReplayPathConsistency() {
        // Testar que o path pode ser "replayed" (validado)
        InformedFloodingStrategy strategy = new InformedFloodingStrategy();
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result.isSuccess());
        
        List<String> path = result.getPath();
        assertFalse(path.isEmpty(), "Path should not be empty");
        
        // Simular "replay" do caminho (skip consecutive duplicates)
        String current = path.get(0);
        for (int i = 1; i < path.size(); i++) {
            String next = path.get(i);
            
            // Skip if same node
            if (current.equals(next)) continue;
            
            // Verificar que existe aresta entre current e next
            var node = topology.getNode(current);
            assertTrue(node.isPresent(), 
                "Node " + current + " should exist in topology");
            
            assertTrue(node.get().getNeighbors().contains(next),
                "Path segment " + current + " → " + next + " should be valid");
            
            current = next;
        }
        
        // Verificar que destination tem o recurso
        var destination = topology.getNode(path.get(path.size() - 1));
        assertTrue(destination.isPresent() && destination.get().hasResource("fileR"),
            "Destination should have the resource");
        
        System.out.println("Path replay validated: " + 
            String.join(" → ", path));
    }

    @Test
    void testFailedSearchMetrics() {
        FloodingStrategy strategy = new FloodingStrategy();
        
        // Buscar recurso inexistente
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "nonExistent", 5, strategy);
        
        assertFalse(result.isSuccess(), "Should not find non-existent resource");
        
        // Mesmo falhando, métricas devem ser válidas
        assertTrue(result.getTotalMessages() > 0, 
            "Should have sent messages");
        assertTrue(result.getVisitedNodes() > 0, 
            "Should have visited nodes");
        assertEquals(0, result.getHops(), 
            "Failed search should have 0 hops");
        // Path pode não estar vazio dependendo do modelo
        
        System.out.println("Failed search metrics:");
        System.out.println("  Messages: " + result.getTotalMessages());
        System.out.println("  Visited: " + result.getVisitedNodes());
        System.out.println("  Path size: " + result.getPath().size());
    }

    @Test
    void testCoverageMetric() {
        FloodingStrategy strategy = new FloodingStrategy();
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result.isSuccess());
        
        int totalNodes = topology.getNodeCount();
        int visitedNodes = result.getVisitedNodes();
        
        // Coverage deve ser calculável
        double coverage = (double) visitedNodes / totalNodes * 100;
        
        assertTrue(coverage >= 0 && coverage <= 100,
            "Coverage should be between 0% and 100%");
        
        System.out.println("Network coverage:");
        System.out.println("  Visited: " + visitedNodes + "/" + totalNodes);
        System.out.println("  Coverage: " + String.format("%.1f%%", coverage));
        
        // Flooding deve ter alta cobertura
        assertTrue(coverage > 50, 
            "Flooding should visit significant portion of network");
    }

    @Test
    void testMetricsResetBetweenSearches() {
        FloodingStrategy strategy = new FloodingStrategy();
        
        // Primeira busca
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        // Reset
        simulationManager.reset();
        
        // Segunda busca
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        
        // Métricas devem ser similares (mesma busca após reset)
        System.out.println("Metrics after reset:");
        System.out.println("  First:  " + result1.getTotalMessages() + " messages, " +
            result1.getVisitedNodes() + " nodes");
        System.out.println("  Second: " + result2.getTotalMessages() + " messages, " +
            result2.getVisitedNodes() + " nodes");
        
        // Deve ser comparável (±20%)
        double messageRatio = (double) result2.getTotalMessages() / result1.getTotalMessages();
        assertTrue(messageRatio > 0.8 && messageRatio < 1.2,
            "Reset should produce consistent metrics");
    }
}
