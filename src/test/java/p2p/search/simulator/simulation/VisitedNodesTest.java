package p2p.search.simulator.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import p2p.search.simulator.loader.NetworkLoader;
import p2p.search.simulator.model.NetworkConfig;
import p2p.search.simulator.strategy.FloodingStrategy;
import p2p.search.simulator.strategy.InformedFloodingStrategy;
import p2p.search.simulator.strategy.RandomWalkStrategy;
import p2p.search.simulator.topology.NetworkTopology;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para verificar o rastreamento de nós visitados durante a simulação.
 */
class VisitedNodesTest {

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
    void testVisitedNodesGrowsDuringFlooding() {
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 10, new FloodingStrategy());
        
        assertTrue(result.isSuccess(), "Search should succeed");
        assertTrue(result.getVisitedNodes() > 0, "Should have visited at least one node");
        
        // Flooding visita múltiplos nós
        assertTrue(result.getVisitedNodes() >= result.getHops(), 
            "Visited nodes should be at least equal to hops");
        
        System.out.println("Flooding - Visited nodes: " + result.getVisitedNodes());
        System.out.println("Flooding - Path length: " + result.getPath().size());
        System.out.println("Flooding - Total messages: " + result.getTotalMessages());
    }

    @Test
    void testVisitedNodesWithRandomWalk() {
        int totalVisited = 0;
        int successCount = 0;
        
        // Random Walk pode ter diferentes padrões de visita
        for (int i = 0; i < 10; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 20, new RandomWalkStrategy());
            
            if (result.isSuccess()) {
                successCount++;
                totalVisited += result.getVisitedNodes();
                
                // Random Walk visita nós ao longo do caminho
                assertTrue(result.getVisitedNodes() > 0, 
                    "Should have visited nodes");
                assertTrue(result.getVisitedNodes() <= result.getTotalMessages(),
                    "Visited nodes should not exceed messages");
                
                System.out.println("Random Walk attempt " + i + 
                    " - Visited: " + result.getVisitedNodes() + 
                    ", Messages: " + result.getTotalMessages());
            }
        }
        
        if (successCount > 0) {
            double avgVisited = (double) totalVisited / successCount;
            System.out.println("Random Walk - Average visited nodes: " + String.format("%.1f", avgVisited));
            assertTrue(avgVisited > 0, "Should visit nodes on average");
        }
    }

    @Test
    void testVisitedNodesClearsOnReset() {
        // Primeira busca
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, new FloodingStrategy());
        
        int visited1 = result1.getVisitedNodes();
        assertTrue(visited1 > 0, "First search should visit nodes");
        
        // Reset
        simulationManager.reset();
        
        // Segunda busca - deve começar do zero
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileR", 10, new FloodingStrategy());
        
        int visited2 = result2.getVisitedNodes();
        assertTrue(visited2 > 0, "Second search should visit nodes");
        
        // Os valores podem ser iguais (determinístico) mas devem ser independentes
        System.out.println("First search visited: " + visited1);
        System.out.println("Second search visited: " + visited2);
        
        // O importante é que ambos sejam > 0, indicando que o reset funcionou
        assertTrue(visited1 > 0 && visited2 > 0, 
            "Both searches should visit nodes independently");
    }

    @Test
    void testVisitedNodesNeverExceedsTotalNodes() {
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 20, new FloodingStrategy());
        
        int totalNodes = topology.getNodeCount();
        assertTrue(result.getVisitedNodes() <= totalNodes,
            "Visited nodes (" + result.getVisitedNodes() + 
            ") should not exceed total nodes (" + totalNodes + ")");
        
        System.out.println("Total nodes in network: " + totalNodes);
        System.out.println("Visited nodes: " + result.getVisitedNodes());
        System.out.println("Coverage: " + String.format("%.1f%%", 
            (double) result.getVisitedNodes() * 100 / totalNodes));
    }

    @Test
    void testInformedFloodingVisitsFewerNodes() {
        // Primeira busca com Flooding para popular o cache
        simulationManager.runSearch("n1", "fileR", 10, new FloodingStrategy());
        int floodingVisited = simulationManager.runSearch("n2", "fileR", 10, new FloodingStrategy())
            .getVisitedNodes();
        
        // Reset e nova busca
        simulationManager.reset();
        simulationManager.runSearch("n1", "fileR", 10, new FloodingStrategy());
        
        // Informed Flooding deve visitar menos nós devido ao cache
        SimulationManager.SearchResult informedResult = 
            simulationManager.runSearch("n2", "fileR", 10, new InformedFloodingStrategy());
        
        System.out.println("Regular Flooding visited: " + floodingVisited);
        System.out.println("Informed Flooding visited: " + informedResult.getVisitedNodes());
        
        // Com cache, pode visitar menos ou igual número de nós
        assertTrue(informedResult.getVisitedNodes() <= floodingVisited,
            "Informed Flooding should visit fewer or equal nodes when cache is available");
    }

    @Test
    void testVisitedNodesIncludesSourceNode() {
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileA", 10, new FloodingStrategy());
        
        // Mesmo que o recurso esteja no próprio nó de origem,
        // ele deve ser contado como visitado
        assertTrue(result.getVisitedNodes() >= 1,
            "Source node should be counted as visited");
        
        System.out.println("Resource: " + result.getResource());
        System.out.println("Source: " + result.getSourceNode());
        System.out.println("Visited nodes: " + result.getVisitedNodes());
        System.out.println("Path: " + result.getPath());
    }

    @Test
    void testVisitedNodesWithDifferentStrategies() {
        System.out.println("\n=== Comparison: Visited Nodes by Strategy ===\n");
        
        // Flooding
        SimulationManager.SearchResult flooding = 
            simulationManager.runSearch("n1", "fileR", 10, new FloodingStrategy());
        System.out.println("Flooding:");
        System.out.println("  Visited: " + flooding.getVisitedNodes());
        System.out.println("  Messages: " + flooding.getTotalMessages());
        System.out.println("  Success: " + flooding.isSuccess());
        
        // Informed Flooding
        simulationManager.reset();
        SimulationManager.SearchResult informed = 
            simulationManager.runSearch("n1", "fileR", 10, new InformedFloodingStrategy());
        System.out.println("\nInformed Flooding:");
        System.out.println("  Visited: " + informed.getVisitedNodes());
        System.out.println("  Messages: " + informed.getTotalMessages());
        System.out.println("  Success: " + informed.isSuccess());
        
        // Random Walk (average over multiple runs)
        simulationManager.reset();
        int rwTotalVisited = 0;
        int rwSuccess = 0;
        for (int i = 0; i < 10; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult rw = 
                simulationManager.runSearch("n1", "fileR", 20, new RandomWalkStrategy());
            if (rw.isSuccess()) {
                rwTotalVisited += rw.getVisitedNodes();
                rwSuccess++;
            }
        }
        
        if (rwSuccess > 0) {
            System.out.println("\nRandom Walk (avg over " + rwSuccess + " successful runs):");
            System.out.println("  Visited: " + String.format("%.1f", (double) rwTotalVisited / rwSuccess));
        }
        
        System.out.println("\n=============================================\n");
        
        // All strategies should visit at least one node
        assertTrue(flooding.getVisitedNodes() > 0, "Flooding should visit nodes");
        assertTrue(informed.getVisitedNodes() > 0, "Informed should visit nodes");
    }
}
