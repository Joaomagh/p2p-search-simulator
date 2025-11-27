package p2p.search.simulator.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import p2p.search.simulator.loader.NetworkLoader;
import p2p.search.simulator.model.Message;
import p2p.search.simulator.model.NetworkConfig;
import p2p.search.simulator.model.Node;
import p2p.search.simulator.strategy.FloodingStrategy;
import p2p.search.simulator.topology.NetworkTopology;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para o motor de simulação e estratégia de Flooding usando a API pública.
 */
class SimulationTest {

    private NetworkTopology topology;
    private SimulationManager simulationManager;
    private FloodingStrategy floodingStrategy;

    @BeforeEach
    void setUp() throws IOException {
        NetworkLoader loader = new NetworkLoader();
        NetworkConfig config = loader.loadFromResource("config.json");
        topology = new NetworkTopology(config);
        simulationManager = new SimulationManager(topology);
        floodingStrategy = new FloodingStrategy();
    }

    @Test
    void testFloodingStrategyName() {
        assertEquals("Flooding", floodingStrategy.getName());
        assertFalse(floodingStrategy.isInformed());
    }

    @Test
    void testSearchResourceInSameNode() {
        Optional<Node> n1Opt = topology.getNode("n1");
        assertTrue(n1Opt.isPresent());
        assertTrue(n1Opt.get().hasResource("fileA"));

        SimulationManager.SearchResult result =
            simulationManager.runSearch("n1", "fileA", 5, floodingStrategy);

        assertTrue(result.isSuccess());
        assertTrue(result.getHops() <= 1);
        assertEquals("n1", result.getSourceNode());
        assertEquals("fileA", result.getResource());
    }

    @Test
    void testSearchResourceInNeighbor() {
        Optional<Node> n2Opt = topology.getNode("n2");
        assertTrue(n2Opt.isPresent());
        assertTrue(n2Opt.get().hasResource("fileC"));

        SimulationManager.SearchResult result =
            simulationManager.runSearch("n1", "fileC", 5, floodingStrategy);

        assertTrue(result.isSuccess());
        assertTrue(result.getHops() >= 1);
        assertTrue(result.getPath().size() >= 2);
    }

    @Test
    void testTTLExpiration() {
        SimulationManager.SearchResult result =
            simulationManager.runSearch("n1", "fileZ", 0, floodingStrategy);

        assertFalse(result.isSuccess());
        assertEquals(0, result.getTotalMessages());
        assertEquals(1, result.getPath().size());
    }

    @Test
    void testMessageNotDuplicated() {
        assertFalse(simulationManager.hasSeenMessage("msg-1", "n1"));
        assertTrue(simulationManager.hasSeenMessage("msg-1", "n1"));
    }

    @Test
    void testFloodingPropagation() {
        SimulationManager.SearchResult result =
            simulationManager.runSearch("n1", "fileZ", 2, floodingStrategy);

        assertFalse(result.isSuccess());
        assertTrue(result.getTotalMessages() >= 0);
    }

    @Test
    void testSimulationReset() {
        simulationManager.runSearch("n1", "fileA", 5, floodingStrategy);
        assertTrue(simulationManager.getMessageCount() > 0);

        simulationManager.reset();
        assertEquals(0, simulationManager.getMessageCount());
    }

    @Test
    void testNodeWithoutStrategyThrowsException() {
        Node node = new Node("test", java.util.List.of("fileX"));

        Message query = new Message.Builder()
            .type(Message.Type.QUERY)
            .source("test")
            .target("test")
            .resource("fileX")
            .ttl(5)
            .build();

        assertThrows(IllegalStateException.class,
            () -> node.receiveMessage(query, simulationManager));
    }
}
