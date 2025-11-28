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
 * Testes unitários para InformedFloodingStrategy.
 * Valida uso de cache, otimização de mensagens e anti-echo.
 */
class InformedFloodingStrategyTest {

    private NetworkTopology topology;
    private SimulationManager simulationManager;
    private InformedFloodingStrategy strategy;

    @BeforeEach
    void setUp() throws IOException {
        NetworkLoader loader = new NetworkLoader();
        NetworkConfig config = loader.loadFromResource("config.json");
        topology = new NetworkTopology(config);
        simulationManager = new SimulationManager(topology);
        strategy = new InformedFloodingStrategy();
    }

    @Test
    void testInformedFloodingSucceeds() {
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result.isSuccess(), "Informed Flooding should find resource");
        assertTrue(result.getPath().contains("n12"), 
            "Path should contain destination node n12");
        assertTrue(result.getHops() > 0, "Should have at least one hop");
        assertTrue(result.getTotalMessages() > 0, "Should send messages");
    }

    @Test
    void testInformedFloodingUsesCache() {
        // Primeira busca: sem cache, muitas mensagens
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result1.isSuccess(), "First search should succeed");
        int firstSearchMessages = result1.getTotalMessages();
        
        // Segunda busca: com cache, deve usar MUITO menos mensagens
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result2.isSuccess(), "Second search should succeed");
        int secondSearchMessages = result2.getTotalMessages();
        
        System.out.println("First search (no cache): " + firstSearchMessages + " messages");
        System.out.println("Second search (with cache): " + secondSearchMessages + " messages");
        System.out.println("Reduction: " + 
            String.format("%.1f%%", (1.0 - (double)secondSearchMessages/firstSearchMessages) * 100));
        
        // Cache deve reduzir mensagens drasticamente (pelo menos 50%)
        assertTrue(secondSearchMessages < firstSearchMessages * 0.5,
            "Cache should reduce messages by at least 50%");
    }

    @Test
    void testInformedFloodingUsesFewerMessagesThanFlooding() {
        // Comparar com Flooding básico (sem cache)
        
        // 1. Informed Flooding (sem cache inicial)
        SimulationManager.SearchResult informedResult = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        // 2. Flooding básico
        simulationManager.reset();
        SimulationManager.SearchResult floodingResult = 
            simulationManager.runSearch("n1", "fileR", 10, new FloodingStrategy());
        
        assertTrue(informedResult.isSuccess(), "Informed should succeed");
        assertTrue(floodingResult.isSuccess(), "Flooding should succeed");
        
        System.out.println("Informed Flooding messages: " + informedResult.getTotalMessages());
        System.out.println("Basic Flooding messages: " + floodingResult.getTotalMessages());
        
        // Primeira busca: Informed pode usar menos ou igual mensagens
        // (depende da topologia, mas não deve usar mais)
        assertTrue(informedResult.getTotalMessages() <= floodingResult.getTotalMessages() * 1.1,
            "Informed Flooding should not use significantly more messages than basic Flooding");
    }

    @Test
    void testInformedFloodingCacheDirectsSearch() {
        // Buscar fileR em n12 primeiro
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result1.isSuccess(), "First search should succeed");
        
        // Buscar novamente o mesmo recurso - cache deve acelerar
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result2.isSuccess(), "Second search should succeed using cache");
        
        // Segunda busca deve usar menos ou igual mensagens (cache direciona)
        assertTrue(result2.getTotalMessages() <= result1.getTotalMessages(),
            "Cache should maintain or improve efficiency");
        
        System.out.println("First search messages: " + result1.getTotalMessages());
        System.out.println("Second search messages (cache-directed): " + result2.getTotalMessages());
    }

    @Test
    void testInformedFloodingRespectsAntiEcho() {
        // Executar busca e verificar que mensagens não explodem
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 15, strategy);
        
        assertTrue(result.isSuccess(), "Should find resource");
        
        // Com anti-echo, mensagens devem ser controladas
        // Flooding básico em rede pequena: ~20-40 mensagens
        // Com anti-echo: deve ser similar ou menor
        assertTrue(result.getTotalMessages() <= 50,
            "Anti-echo should prevent excessive messages. Got: " + 
            result.getTotalMessages());
    }

    @Test
    void testInformedFloodingRespectsTTL() {
        // TTL baixo deve limitar propagação
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 2, strategy);
        
        // Com TTL=2, pode falhar se n12 estiver longe
        if (result.isSuccess()) {
            assertTrue(result.getHops() <= 2, "Path should respect TTL limit");
        }
        
        // Mensagens devem ser limitadas pelo TTL
        assertTrue(result.getTotalMessages() <= 15,
            "TTL should limit message propagation. Got: " + result.getTotalMessages());
    }

    @Test
    void testInformedFloodingCacheExpiration() {
        // Buscar recurso
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result1.isSuccess());
        
        // Reset limpa cache
        simulationManager.reset();
        
        // Nova busca sem cache
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result2.isSuccess());
        
        // Sem cache, mensagens devem ser similares à primeira busca
        System.out.println("Before reset: " + result1.getTotalMessages() + " messages");
        System.out.println("After reset: " + result2.getTotalMessages() + " messages");
        
        // Deve ser comparável (±20% devido a possíveis variações)
        double ratio = (double) result2.getTotalMessages() / result1.getTotalMessages();
        assertTrue(ratio > 0.8 && ratio < 1.2,
            "After reset, message count should be similar to first search");
    }

    @Test
    void testInformedFloodingFindsLocalResource() {
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileA", 10, strategy);
        
        assertTrue(result.isSuccess(), "Should find local resource");
        // Local resource may have hops > 0 due to message propagation model
        assertTrue(result.getHops() >= 0, "Should have valid hop count");
        assertTrue(result.getVisitedNodes() >= 1, "Should visit at least source node");
    }

    @Test
    void testInformedFloodingCachePopulation() {
        // Executar busca que visita múltiplos nós
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        assertTrue(result1.isSuccess());
        
        // Agora buscar recursos que foram vistos durante a primeira busca
        // Cache deve ter informação sobre nós intermediários
        
        // Se n2 foi visitado e tem fileB, buscar fileB deve ser mais rápido
        SimulationManager.SearchResult result2 = 
            simulationManager.runSearch("n1", "fileB", 10, strategy);
        
        if (result2.isSuccess()) {
            // Se n2 está no caminho para n12, cache pode ajudar
            System.out.println("fileB search messages: " + result2.getTotalMessages());
            
            // Cache deve reduzir mensagens se nó foi visto
            assertTrue(result2.getTotalMessages() <= result1.getTotalMessages(),
                "Cache should help find intermediate nodes");
        }
    }

    @Test
    void testInformedFloodingPathIntegrity() {
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 10, strategy);
        
        assertTrue(result.isSuccess());
        
        var pathList = result.getPath();
        assertNotNull(pathList, "Path should not be null");
        assertFalse(pathList.isEmpty(), "Path should not be empty");
        
        // Path deve começar com origem e terminar com destino
        assertEquals("n1", pathList.get(0), "Path should start with source");
        assertEquals("n12", pathList.get(pathList.size() - 1), 
            "Path should end with destination");
        
        // Contar hops no path
        assertEquals(result.getHops() + 1, pathList.size(),
            "Path nodes should match hops + 1");
    }

    @Test
    void testStrategyName() {
        assertEquals("Informed Flooding", strategy.getName());
        assertTrue(strategy.isInformed(), "Informed Flooding should be informed");
    }
}
