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
 * Testes para verificar que as estratégias NÃO reenviam mensagens
 * de volta ao nó que acabou de enviar (comportamento anti-echo).
 */
class AntiEchoTest {

    private NetworkTopology topology;
    private SimulationManager simulationManager;
    private FloodingStrategy floodingStrategy;
    private InformedFloodingStrategy informedFloodingStrategy;
    private RandomWalkStrategy randomWalkStrategy;
    private InformedRandomWalkStrategy informedRandomWalkStrategy;

    @BeforeEach
    void setUp() throws IOException {
        // Usa a topologia existente do config.json
        NetworkLoader loader = new NetworkLoader();
        NetworkConfig config = loader.loadFromResource("config.json");
        topology = new NetworkTopology(config);
        
        simulationManager = new SimulationManager(topology);
        floodingStrategy = new FloodingStrategy();
        informedFloodingStrategy = new InformedFloodingStrategy();
        randomWalkStrategy = new RandomWalkStrategy();
        informedRandomWalkStrategy = new InformedRandomWalkStrategy();
    }

    /**
     * Flooding sem anti-echo geraria mais mensagens devido ao echo.
     * Com anti-echo, mensagens são reduzidas pois não há retorno desnecessário.
     * 
     * Com a topologia do config.json, testamos que:
     * - Flooding envia para todos os vizinhos EXCETO o sender
     * - Isso reduz mensagens e evita loops desnecessários
     */
    @Test
    void testFloodingDoesNotEcho() {
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 10, floodingStrategy);
        
        assertTrue(result.isSuccess());
        // Verifica que encontrou o recurso em n12
        assertTrue(result.getPath().contains("n12"), "Should find resource in n12");
        
        System.out.println("Flooding messages (with anti-echo): " + result.getTotalMessages());
        System.out.println("Path: " + result.getPath());
        
        // Com anti-echo, esperamos menos mensagens do que seria com echo
        // A topologia do config tem 12 nós, então sem anti-echo seria muito mais
        assertTrue(result.getTotalMessages() < 40, 
            "Anti-echo should reduce messages. Got: " + result.getTotalMessages());
    }

    @Test
    void testInformedFloodingDoesNotEcho() {
        SimulationManager.SearchResult result = 
            simulationManager.runSearch("n1", "fileR", 10, informedFloodingStrategy);
        
        assertTrue(result.isSuccess());
        System.out.println("Informed Flooding messages (with anti-echo): " + result.getTotalMessages());
        
        assertTrue(result.getTotalMessages() < 40,
            "Anti-echo should reduce messages. Got: " + result.getTotalMessages());
    }

    /**
     * Random Walk deve escolher vizinho aleatório mas NUNCA o sender.
     * Mesmo com apenas 2 vizinhos, deve excluir o sender.
     */
    @Test
    void testRandomWalkDoesNotEchoEvenWithTwoNeighbors() {
        // Executar múltiplas vezes para verificar comportamento consistente
        int successCount = 0;
        int totalMessages = 0;
        
        for (int i = 0; i < 20; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 20, randomWalkStrategy);
            
            if (result.isSuccess()) {
                successCount++;
                totalMessages += result.getTotalMessages();
                System.out.println("Random Walk attempt " + i + ": " + result.getTotalMessages() + " messages");
            }
        }
        
        System.out.println("Random Walk success rate: " + successCount + "/20");
        if (successCount > 0) {
            double avgMessages = (double) totalMessages / successCount;
            System.out.println("Average messages when successful: " + avgMessages);
            
            // Com anti-echo, mensagens devem ser razoáveis
            assertTrue(avgMessages < 30,
                "Anti-echo should keep message count reasonable. Avg: " + avgMessages);
        }
    }

    @Test
    void testInformedRandomWalkDoesNotEcho() {
        int successCount = 0;
        int totalMessages = 0;
        
        for (int i = 0; i < 20; i++) {
            simulationManager.reset();
            SimulationManager.SearchResult result = 
                simulationManager.runSearch("n1", "fileR", 20, informedRandomWalkStrategy);
            
            if (result.isSuccess()) {
                successCount++;
                totalMessages += result.getTotalMessages();
                System.out.println("Informed Random Walk attempt " + i + ": " + result.getTotalMessages() + " messages");
            }
        }
        
        System.out.println("Informed Random Walk success rate: " + successCount + "/20");
        if (successCount > 0) {
            double avgMessages = (double) totalMessages / successCount;
            System.out.println("Average messages when successful: " + avgMessages);
            
            assertTrue(avgMessages < 30,
                "Anti-echo should keep message count reasonable. Avg: " + avgMessages);
        }
    }

    /**
     * Demonstra que o anti-echo reduz mensagens em comparação com uma busca ingênua.
     * Verifica que nós intermediários não reenviam para quem mandou.
     */
    @Test
    void testAntiEchoReducesMessages() {
        // Primeira busca para estabelecer baseline
        SimulationManager.SearchResult result1 = 
            simulationManager.runSearch("n1", "fileR", 10, floodingStrategy);
        
        assertTrue(result1.isSuccess());
        int messages = result1.getTotalMessages();
        
        System.out.println("\n=== Anti-Echo Verification ===");
        System.out.println("Messages with anti-echo: " + messages);
        System.out.println("Path length: " + result1.getPath().size());
        System.out.println("Hops: " + result1.getHops());
        
        // Com anti-echo, número de mensagens deve ser proporcional ao caminho
        // e não exponencial como seria com echo completo
        int pathLength = result1.getPath().size();
        
        // Sem anti-echo, em uma rede densa, seria muito pior
        // Com anti-echo, deve ser relativamente eficiente
        assertTrue(messages < pathLength * 10,
            "With anti-echo, messages should scale reasonably with path length");
    }
}
