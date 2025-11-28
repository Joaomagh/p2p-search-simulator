package p2p.search.simulator.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import p2p.search.simulator.loader.NetworkLoader;
import p2p.search.simulator.model.NetworkConfig;
import p2p.search.simulator.simulation.SimulationManager;
import p2p.search.simulator.strategy.InformedFloodingStrategy;
import p2p.search.simulator.strategy.InformedRandomWalkStrategy;
import p2p.search.simulator.topology.NetworkTopology;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes específicos para validar comportamento do cache.
 * Garante que cache direciona busca corretamente e melhora performance.
 */
class CacheBehaviorTest {

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
    void testCacheReducesMessagesInformedFlooding() {
        InformedFloodingStrategy strategy = new InformedFloodingStrategy();
        
        // Primeira busca: sem cache, explora rede
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result1.isSuccess(), "First search should succeed");
        int firstMessages = result1.getTotalMessages();
        
        // Segunda busca: com cache, usa caminho direto
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result2.isSuccess(), "Second search should succeed");
        int secondMessages = result2.getTotalMessages();
        
        System.out.println("Cache impact on Informed Flooding:");
        System.out.println("  First search: " + firstMessages + " messages");
        System.out.println("  Second search: " + secondMessages + " messages");
        System.out.println("  Reduction: " + 
            String.format("%.1f%%", (1.0 - (double)secondMessages/firstMessages) * 100));
        
        // Cache deve reduzir mensagens drasticamente
        assertTrue(secondMessages < firstMessages * 0.5,
            "Cache should reduce messages by at least 50%");
    }

    @Test
    void testCacheDirectsToCorrectNode() {
        InformedFloodingStrategy strategy = new InformedFloodingStrategy();
        
        // Buscar fileR em n12
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result1.isSuccess(), "First search should succeed");
        assertEquals("n12", result1.getPath().get(result1.getPath().size() - 1),
            "fileR should be in n12");
        
        // Buscar novamente fileR - cache deve direcionar
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result2.isSuccess(), "Second search should succeed");
        assertEquals("n12", result2.getPath().get(result2.getPath().size() - 1),
            "fileR should still be in n12");
        
        // Segunda busca deve ser mais eficiente com cache
        assertTrue(result2.getTotalMessages() <= result1.getTotalMessages(),
            "Cache should make second search at least as efficient");
        
        System.out.println("Cache directing:");
        System.out.println("  First search: " + result1.getTotalMessages() + " messages");
        System.out.println("  Second search (cached): " + result2.getTotalMessages() + " messages");
    }

    @Test
    void testCachePropagatesAlongPath() {
        InformedFloodingStrategy strategy = new InformedFloodingStrategy();
        
        // Executar busca que popula cache em múltiplos nós
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result1.isSuccess());
        
        // Buscar recursos que estão em nós intermediários
        // Se n2 está no caminho e tem fileB, buscar fileB deve ser rápido
        SimulationManager.SearchResult resultB = 
            simulationManager.runSearch("n1", "fileB", 10, strategy);
        
        if (resultB.isSuccess()) {
            System.out.println("Intermediate node search:");
            System.out.println("  fileB found in " + resultB.getHops() + " hops");
            System.out.println("  " + resultB.getTotalMessages() + " messages");
            
            // Cache de nós intermediários ajuda
            assertTrue(resultB.getTotalMessages() < result1.getTotalMessages(),
                "Cache should help find intermediate nodes");
        }
    }

    @Test
    void testCacheResetClearsInformation() {
        InformedFloodingStrategy strategy = new InformedFloodingStrategy();
        
        // Primeira busca popula cache
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result1.isSuccess());
        int firstMessages = result1.getTotalMessages();
        
        // Segunda busca usa cache (menos mensagens)
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result2.isSuccess());
        int cachedMessages = result2.getTotalMessages();
        
        assertTrue(cachedMessages < firstMessages, "Cache should reduce messages");
        
        // Reset limpa cache
        simulationManager.reset();
        
        // Terceira busca sem cache (similar à primeira)
        SimulationManager.SearchResult result3 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result3.isSuccess());
        int afterResetMessages = result3.getTotalMessages();
        
        System.out.println("Cache reset impact:");
        System.out.println("  First search: " + firstMessages + " messages");
        System.out.println("  Cached search: " + cachedMessages + " messages");
        System.out.println("  After reset: " + afterResetMessages + " messages");
        
        // Após reset, deve ser similar à primeira busca
        double ratio = (double) afterResetMessages / firstMessages;
        assertTrue(ratio > 0.8 && ratio < 1.2,
            "After reset, should behave like first search (±20%)");
    }

    @Test
    void testCacheImprovesInformedRandomWalk() {
        InformedRandomWalkStrategy strategy = new InformedRandomWalkStrategy();
        
        // Primeira busca sem cache
        simulationManager.reset();
        boolean firstSuccess = false;
        int firstMessages = 0;
        
        for (int i = 0; i < 5; i++) {
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 20, strategy);
            if (result.isSuccess()) {
                firstSuccess = true;
                firstMessages = result.getTotalMessages();
                break;
            }
        }
        
        if (firstSuccess) {
            // Buscas subsequentes com cache
            int cachedSuccesses = 0;
            int cachedMessagesSum = 0;
            
            for (int i = 0; i < 10; i++) {
                SimulationManager.SearchResult result = 
                    simulationManager.runSearch("n1", "fileR", 20, strategy);
                if (result.isSuccess()) {
                    cachedSuccesses++;
                    cachedMessagesSum += result.getTotalMessages();
                }
            }
            
            System.out.println("Informed Random Walk with cache:");
            System.out.println("  First search: " + firstMessages + " messages");
            System.out.println("  Cached searches: " + cachedSuccesses + "/10 succeeded");
            
            if (cachedSuccesses > 0) {
                double avgCachedMessages = (double) cachedMessagesSum / cachedSuccesses;
                System.out.println("  Average cached: " + String.format("%.1f", avgCachedMessages));
                
                // Cache deve melhorar eficiência
                assertTrue(avgCachedMessages <= firstMessages * 1.2,
                    "Cache should maintain or improve efficiency");
            }
        }
    }

    @Test
    void testCacheMultipleDestinations() {
        InformedFloodingStrategy strategy = new InformedFloodingStrategy();
        
        // Buscar recursos em diferentes nós
        SimulationManager.SearchResult resultR = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(resultR.isSuccess());
        
        SimulationManager.SearchResult resultB = 
            simulationManager.runSearch("n1", "fileB", 10, strategy);
        assertTrue(resultB.isSuccess());
        
        SimulationManager.SearchResult resultK = 
            simulationManager.runSearch("n1", "fileK", 10, strategy);
        assertTrue(resultK.isSuccess());
        
        // Buscar novamente - cache deve acelerar
        SimulationManager.SearchResult resultR2 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(resultR2.isSuccess());
        assertTrue(resultR2.getTotalMessages() < resultR.getTotalMessages(),
            "Cache should speed up repeated search");
        
        System.out.println("Multiple destinations with cache:");
        System.out.println("  fileR: " + resultR.getTotalMessages() + " → " + 
            resultR2.getTotalMessages() + " messages");
        System.out.println("  fileB: " + resultB.getTotalMessages() + " messages");
        System.out.println("  fileK: " + resultK.getTotalMessages() + " messages");
    }

    @Test
    void testCacheConsistencyAcrossSearches() {
        InformedFloodingStrategy strategy = new InformedFloodingStrategy();
        
        // Múltiplas buscas para o mesmo recurso
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result1.isSuccess());
        
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result2.isSuccess());
        
        SimulationManager.SearchResult result3 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result3.isSuccess());
        
        // Cache deve manter eficiência consistente
        assertTrue(result2.getTotalMessages() <= result1.getTotalMessages(),
            "Second search should be as efficient as or better than first");
        assertTrue(result3.getTotalMessages() <= result2.getTotalMessages(),
            "Third search should be as efficient as or better than second");
        
        // Path deve ser consistente com cache
        assertEquals(result2.getPath(), result3.getPath(),
            "Cache should provide consistent paths");
        
        System.out.println("Cache consistency:");
        System.out.println("  Search 1: " + result1.getTotalMessages() + " messages");
        System.out.println("  Search 2: " + result2.getTotalMessages() + " messages");
        System.out.println("  Search 3: " + result3.getTotalMessages() + " messages");
    }

    @Test
    void testCacheWithDifferentSources() {
        InformedFloodingStrategy strategy = new InformedFloodingStrategy();
        
        // n1 busca fileR (popula cache em n1 e caminho)
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result1.isSuccess());
        
        // n2 busca fileR (pode se beneficiar de cache se nós compartilham)
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n2", "fileR", 10, strategy);
        assertTrue(result2.isSuccess());
        
        System.out.println("Different sources:");
        System.out.println("  n1 → fileR: " + result1.getTotalMessages() + " messages, " +
            result1.getHops() + " hops");
        System.out.println("  n2 → fileR: " + result2.getTotalMessages() + " messages, " +
            result2.getHops() + " hops");
        
        // Ambos devem ter sucesso
        assertTrue(result1.isSuccess() && result2.isSuccess(),
            "Both searches should succeed");
    }
}
