package p2p.search.simulator.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import p2p.search.simulator.loader.NetworkLoader;
import p2p.search.simulator.model.NetworkConfig;
import p2p.search.simulator.model.Node;
import p2p.search.simulator.strategy.FloodingStrategy;
import p2p.search.simulator.strategy.InformedFloodingStrategy;
import p2p.search.simulator.topology.NetworkTopology;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes demonstrando a propagação de cache via mensagens RESPONSE.
 */
class CacheResponseTest {

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
    void demonstrateCachePropagationViaResponse() {
        System.out.println("\n=== DEMONSTRAÇÃO: Cache via RESPONSE ===\n");
        
        // Fase 1: Busca inicial SEM cache
        System.out.println("FASE 1: Primeira busca (n1 → fileR)");
        System.out.println("---------------------------------------");
        
        Node n1 = topology.getNode("n1").orElseThrow();
        Node n2 = topology.getNode("n2").orElseThrow();
        Node n6 = topology.getNode("n6").orElseThrow();
        
        System.out.println("Cache ANTES da busca:");
        System.out.println("  n1: " + n1.getCache());
        System.out.println("  n2: " + n2.getCache());
        System.out.println("  n6: " + n6.getCache());
        
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, new FloodingStrategy());
        
        System.out.println("\nResultado da busca:");
        System.out.println("  Status: " + (result1.isSuccess() ? "✓ SUCESSO" : "✗ FALHOU"));
        System.out.println("  Caminho: " + result1.getPath());
        System.out.println("  Mensagens: " + result1.getTotalMessages());
        
        System.out.println("\nCache DEPOIS da busca (via RESPONSE):");
        System.out.println("  n1: " + n1.getCache());
        System.out.println("  n2: " + n2.getCache());
        System.out.println("  n6: " + n6.getCache());
        
        // Verifica que o cache foi atualizado
        assertTrue(result1.isSuccess());
        for (String nodeId : result1.getPath()) {
            Node node = topology.getNode(nodeId).orElseThrow();
            assertEquals("n12", node.getCachedLocation("fileR").orElse(null),
                "Nó " + nodeId + " deveria ter cache apontando para n12");
        }
        
        // Fase 2: Busca COM cache (Informed Flooding)
        System.out.println("\n\nFASE 2: Segunda busca COM cache (n2 → fileR usando Informed Flooding)");
        System.out.println("-----------------------------------------------------------------------");
        
        simulationManager.reset();
        
        System.out.println("Cache de n2 ANTES: " + n2.getCache());
        System.out.println("  → n2 já sabe que fileR está em n12!");
        
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n2", "fileR", 10, new InformedFloodingStrategy());
        
        System.out.println("\nResultado da busca informada:");
        System.out.println("  Status: " + (result2.isSuccess() ? "✓ SUCESSO" : "✗ FALHOU"));
        System.out.println("  Caminho: " + result2.getPath());
        System.out.println("  Mensagens: " + result2.getTotalMessages());
        
        // Comparação
        System.out.println("\n=== COMPARAÇÃO ===");
        System.out.println("Busca SEM cache (Flooding):         " + result1.getTotalMessages() + " mensagens");
        System.out.println("Busca COM cache (Informed Flooding): " + result2.getTotalMessages() + " mensagens");
        System.out.println("Redução: " + 
            String.format("%.1f%%", 100.0 * (result1.getTotalMessages() - result2.getTotalMessages()) / result1.getTotalMessages()));
        
        // Verifica que a busca informada foi mais eficiente
        assertTrue(result2.isSuccess());
        assertTrue(result2.getTotalMessages() < result1.getTotalMessages(),
            "Busca informada deveria usar MENOS mensagens");
    }

    @Test
    void verifyCacheIsEmptyBeforeFirstSearch() {
        Node n1 = topology.getNode("n1").orElseThrow();
        Node n2 = topology.getNode("n2").orElseThrow();
        
        assertTrue(n1.getCache().isEmpty(), "Cache deveria estar vazio inicialmente");
        assertTrue(n2.getCache().isEmpty(), "Cache deveria estar vazio inicialmente");
    }

    @Test
    void verifyCacheOnlyUpdatesNodesInPath() {
        // Busca de n1 para fileR (em n12)
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 10, new FloodingStrategy());
        
        assertTrue(result.isSuccess());
        
        // Nós NO caminho devem ter cache
        for (String nodeId : result.getPath()) {
            Node node = topology.getNode(nodeId).orElseThrow();
            assertNotNull(node.getCachedLocation("fileR").orElse(null),
                "Nó " + nodeId + " está no caminho e deveria ter cache");
        }
        
        // Nós FORA do caminho não devem ter cache
        for (String nodeId : topology.getNodeIds()) {
            if (!result.getPath().contains(nodeId)) {
                Node node = topology.getNode(nodeId).orElseThrow();
                assertNull(node.getCachedLocation("fileR").orElse(null),
                    "Nó " + nodeId + " não está no caminho e NÃO deveria ter cache");
            }
        }
    }

    @Test
    void verifyCachePersistsAcrossResets() {
        // Primeira busca popula cache
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, new FloodingStrategy());
        
        assertTrue(result1.isSuccess());
        
        Node n1 = topology.getNode("n1").orElseThrow();
        assertNotNull(n1.getCachedLocation("fileR").orElse(null));
        
        // Reset do simulador (limpa caches)
        simulationManager.reset();
        
        // Cache foi limpo
        assertNull(n1.getCachedLocation("fileR").orElse(null),
            "Cache deveria ser limpo após reset()");
    }
}
