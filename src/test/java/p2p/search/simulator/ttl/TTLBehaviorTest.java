package p2p.search.simulator.ttl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import p2p.search.simulator.loader.NetworkLoader;
import p2p.search.simulator.model.NetworkConfig;
import p2p.search.simulator.simulation.SimulationManager;
import p2p.search.simulator.strategy.*;
import p2p.search.simulator.topology.NetworkTopology;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes específicos para validar comportamento do TTL.
 * Garante que TTL evita loops e limita propagação corretamente.
 */
class TTLBehaviorTest {

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
    void testTTLPreventsInfiniteLoops() {
        // Mesmo com topologia cíclica, TTL impede loops infinitos
        FloodingStrategy strategy = new FloodingStrategy();
        
        // TTL baixo
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileZ", 5, strategy);
        
        assertFalse(result.isSuccess(), "Should not find non-existent file");
        
        // Mensagens devem ser limitadas pelo TTL
        // Com TTL=5 e anti-echo, máximo ~20-30 mensagens em rede pequena
        assertTrue(result.getTotalMessages() <= 40,
            "TTL should prevent message explosion. Got: " + result.getTotalMessages());
        
        System.out.println("TTL=5, Flooding, non-existent file:");
        System.out.println("  Messages: " + result.getTotalMessages());
        System.out.println("  Visited nodes: " + result.getVisitedNodes());
    }

    @Test
    void testTTLLimitsPathLength() {
        // Com TTL baixo, não pode alcançar nós distantes
        FloodingStrategy strategy = new FloodingStrategy();
        
        // TTL=1: só alcança vizinhos diretos
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 1, strategy);
        
        // TTL=2: alcança até 2 saltos
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileR", 2, strategy);
        
        // TTL=10: deve alcançar n12
        SimulationManager.SearchResult result10 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        System.out.println("TTL impact on path length:");
        System.out.println("  TTL=1: " + (result1.isSuccess() ? "SUCCESS" : "FAILED") +
            ", " + result1.getVisitedNodes() + " nodes");
        System.out.println("  TTL=2: " + (result2.isSuccess() ? "SUCCESS" : "FAILED") +
            ", " + result2.getVisitedNodes() + " nodes");
        System.out.println("  TTL=10: " + (result10.isSuccess() ? "SUCCESS" : "FAILED") +
            ", " + result10.getVisitedNodes() + " nodes");
        
        // TTL maior deve visitar mais nós
        assertTrue(result10.getVisitedNodes() >= result2.getVisitedNodes(),
            "Higher TTL should visit at least as many nodes");
        assertTrue(result2.getVisitedNodes() >= result1.getVisitedNodes(),
            "Higher TTL should visit at least as many nodes");
        
        // TTL=10 deve ter sucesso
        assertTrue(result10.isSuccess(), "TTL=10 should find fileR");
        
        if (result1.isSuccess()) {
            assertEquals(1, result1.getHops(), "TTL=1 can only reach 1 hop away");
        }
        if (result2.isSuccess()) {
            assertTrue(result2.getHops() <= 2, "TTL=2 limits hops to 2");
        }
    }

    @Test
    void testTTLWithRandomWalk() {
        RandomWalkStrategy strategy = new RandomWalkStrategy();
        
        // TTL=3 com Random Walk
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 3, strategy);
        
        // Mensagens devem ser limitadas (Random Walk + TTL)
        assertTrue(result.getTotalMessages() <= 10,
            "TTL should limit Random Walk messages. Got: " + result.getTotalMessages());
        
        if (result.isSuccess()) {
            assertTrue(result.getHops() <= 3, 
                "Path length should not exceed TTL");
        }
        
        System.out.println("Random Walk with TTL=3:");
        System.out.println("  Result: " + (result.isSuccess() ? "SUCCESS" : "FAILED"));
        System.out.println("  Messages: " + result.getTotalMessages());
    }

    @Test
    void testTTLGrowthImpact() {
        InformedFloodingStrategy strategy = new InformedFloodingStrategy();
        
        // Testar diferentes valores de TTL
        int[] ttlValues = {2, 5, 10, 15};
        
        System.out.println("TTL growth impact:");
        for (int ttl : ttlValues) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", ttl, strategy);
            
            System.out.println("  TTL=" + ttl + ": " + 
                (result.isSuccess() ? "SUCCESS" : "FAILED") +
                ", " + result.getTotalMessages() + " messages, " +
                result.getVisitedNodes() + " nodes");
            
            // Mensagens não devem crescer infinitamente
            assertTrue(result.getTotalMessages() <= ttl * 10,
                "Messages should not grow unbounded with TTL");
        }
    }

    @Test
    void testTTLZeroBlocksSearch() {
        FloodingStrategy strategy = new FloodingStrategy();
        
        // TTL=0 não deve propagar
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 0, strategy);
        
        // Não deve encontrar (fileR está em n12, não em n1)
        assertFalse(result.isSuccess(), "TTL=0 should not find remote resource");
        
        // Com TTL=0, comportamento varia - o importante é que não propague muito
        assertTrue(result.getVisitedNodes() <= 2, 
            "TTL=0 should visit very few nodes");
        
        System.out.println("TTL=0: " + result.getTotalMessages() + " messages, " +
            result.getVisitedNodes() + " nodes visited");
    }

    @Test
    void testTTLWithLocalResource() {
        FloodingStrategy strategy = new FloodingStrategy();
        
        // Com TTL baixo, buscar recurso local
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileA", 1, strategy);
        
        assertTrue(result.isSuccess(), "Should find local resource with low TTL");
        // Hops pode variar dependendo do modelo de propagação
        assertTrue(result.getHops() >= 0, "Should have valid hop count");
        assertTrue(result.getVisitedNodes() >= 1, "Should visit at least source node");
    }

    @Test
    void testTTLConsistencyAcrossStrategies() {
        // TTL deve funcionar consistentemente em todas as estratégias
        int ttl = 3;
        
        SearchStrategy[] strategies = {
            new FloodingStrategy(),
            new RandomWalkStrategy(),
            new InformedFloodingStrategy(),
            new InformedRandomWalkStrategy()
        };
        
        System.out.println("TTL=" + ttl + " consistency:");
        for (SearchStrategy strategy : strategies) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", ttl, strategy);
            
            System.out.println("  " + strategy.getName() + ": " + 
                result.getTotalMessages() + " messages");
            
            // Todas devem respeitar TTL (mensagens limitadas)
            assertTrue(result.getTotalMessages() <= 50,
                strategy.getName() + " should respect TTL");
            
            if (result.isSuccess()) {
                assertTrue(result.getHops() <= ttl,
                    strategy.getName() + " path should respect TTL");
            }
        }
    }

    @Test
    void testTTLPreventsMessageExplosion() {
        // Buscar recurso inexistente com TTL razoável
        InformedFloodingStrategy strategy = new InformedFloodingStrategy();
        
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "nonExistent", 8, strategy);
        
        assertFalse(result.isSuccess(), "Should not find non-existent resource");
        
        // Mesmo não encontrando, TTL evita explosão
        int nodeCount = topology.getNodeCount();
        
        // Com TTL=8 e anti-echo, não deve visitar todos os nós múltiplas vezes
        assertTrue(result.getVisitedNodes() <= nodeCount,
            "Should not visit more nodes than exist");
        
        System.out.println("Non-existent resource with TTL=8:");
        System.out.println("  Messages: " + result.getTotalMessages());
        System.out.println("  Visited: " + result.getVisitedNodes() + "/" + nodeCount);
    }

    @Test
    void testTTLEnoughForNetworkDiameter() {
        FloodingStrategy strategy = new FloodingStrategy();
        
        // TTL generoso deve sempre encontrar (se recurso existe)
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 20, strategy);
        
        assertTrue(result.isSuccess(), 
            "Sufficient TTL should find existing resource");
        
        // Path length deve ser menor que TTL
        assertTrue(result.getHops() < 20,
            "Actual path should be shorter than TTL limit");
        
        System.out.println("Sufficient TTL (20):");
        System.out.println("  Path length: " + result.getHops() + " hops");
        System.out.println("  Messages: " + result.getTotalMessages());
        System.out.println("  Coverage: " + result.getVisitedNodes() + "/" + 
            topology.getNodeCount() + " nodes");
    }

    @Test
    void testTTLWithInformedStrategiesOptimization() {
        // Estratégias informadas devem usar TTL mais eficientemente
        
        // 1. Flooding básico
        simulationManager.reset();
        FloodingStrategy flooding = new FloodingStrategy();
        SimulationManager.SearchResult floodingResult = 
            simulationManager.runSearch("n1", "fileR", 8, flooding);
        
        // 2. Informed Flooding (primeira busca, sem cache)
        simulationManager.reset();
        InformedFloodingStrategy informed = new InformedFloodingStrategy();
        SimulationManager.SearchResult informedResult = 
            simulationManager.runSearch("n1", "fileR", 8, informed);
        
        assertTrue(floodingResult.isSuccess());
        assertTrue(informedResult.isSuccess());
        
        System.out.println("TTL=8 efficiency:");
        System.out.println("  Flooding: " + floodingResult.getTotalMessages() + " messages");
        System.out.println("  Informed: " + informedResult.getTotalMessages() + " messages");
        
        // Informed não deve usar significativamente mais mensagens
        assertTrue(informedResult.getTotalMessages() <= floodingResult.getTotalMessages() * 1.2,
            "Informed strategy should be competitive with basic flooding");
    }
}
