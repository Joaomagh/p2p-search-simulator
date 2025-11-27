package p2p.search.simulator.topology;

import p2p.search.simulator.loader.NetworkLoader;
import p2p.search.simulator.model.NetworkConfig;
import p2p.search.simulator.model.Node;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitÃ¡rios para a classe NetworkTopology.
 */
class TopologyTest {
    
    @Test
    void testLoadValidTopologyFromResource() throws IOException {
        // Carrega o config.json de example
        NetworkLoader loader = new NetworkLoader();
        NetworkConfig config = loader.loadFromResource("config.json");
        
        // Cria a topologia (deve validar com sucesso)
        NetworkTopology topology = new NetworkTopology(config);
        
        // VerificaÃ§Ãµes bÃ¡sicas
        assertNotNull(topology);
        assertEquals(12, topology.getNodeCount());
        assertEquals(14, topology.getEdgeCount());
        assertEquals(2, topology.getMinNeighbors());
        assertEquals(4, topology.getMaxNeighbors());
    }
    
    @Test
    void testAllNodesHaveValidDegree() throws IOException {
        NetworkLoader loader = new NetworkLoader();
        NetworkConfig config = loader.loadFromResource("config.json");
        NetworkTopology topology = new NetworkTopology(config);
        
        // Verifica que todos os nÃ³s respeitam min/max neighbors
        for (Node node : topology.getAllNodes()) {
            int degree = node.getDegree();
            assertTrue(degree >= topology.getMinNeighbors(), 
                String.format("Node %s has degree %d < min %d", 
                    node.getId(), degree, topology.getMinNeighbors()));
            assertTrue(degree <= topology.getMaxNeighbors(),
                String.format("Node %s has degree %d > max %d",
                    node.getId(), degree, topology.getMaxNeighbors()));
        }
    }
    
    @Test
    void testAllNodesHaveResources() throws IOException {
        NetworkLoader loader = new NetworkLoader();
        NetworkConfig config = loader.loadFromResource("config.json");
        NetworkTopology topology = new NetworkTopology(config);
        
        // Verifica que todos os nÃ³s tÃªm pelo menos um recurso
        for (Node node : topology.getAllNodes()) {
            assertFalse(node.getResources().isEmpty(),
                "Node " + node.getId() + " should have at least one resource");
        }
    }
    
    @Test
    void testTopologyIsConnected() throws IOException {
        NetworkLoader loader = new NetworkLoader();
        NetworkConfig config = loader.loadFromResource("config.json");
        
        // Se conseguir criar a topologia, significa que estÃ¡ conectada
        // (a validaÃ§Ã£o lanÃ§a exceÃ§Ã£o se nÃ£o estiver)
        assertDoesNotThrow(() -> new NetworkTopology(config));
    }
    
    @Test
    void testDisconnectedTopologyThrowsException() {
        // Cria uma configuraÃ§Ã£o com grafo desconexo
        NetworkConfig config = new NetworkConfig();
        config.setMinNeighbors(1);
        config.setMaxNeighbors(2);
        
        Map<String, List<String>> resources = new HashMap<>();
        resources.put("n1", List.of("fileA"));
        resources.put("n2", List.of("fileB"));
        resources.put("n3", List.of("fileC"));
        resources.put("n4", List.of("fileD"));
        config.setResources(resources);
        
        // Cria duas componentes desconexas: (n1-n2) e (n3-n4)
        List<List<String>> edges = new ArrayList<>();
        edges.add(List.of("n1", "n2"));
        edges.add(List.of("n3", "n4"));
        config.setEdges(edges);
        
        // Deve lanÃ§ar exceÃ§Ã£o
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> new NetworkTopology(config)
        );
        
        assertTrue(exception.getMessage().contains("not connected"));
    }
    
    @Test
    void testNodeWithTooFewNeighborsThrowsException() {
        NetworkConfig config = new NetworkConfig();
        config.setMinNeighbors(2);
        config.setMaxNeighbors(4);
        
        Map<String, List<String>> resources = new HashMap<>();
        resources.put("n1", List.of("fileA"));
        resources.put("n2", List.of("fileB"));
        resources.put("n3", List.of("fileC"));
        config.setResources(resources);
        
        // n3 terÃ¡ apenas 1 vizinho (menos que o mÃ­nimo de 2)
        List<List<String>> edges = new ArrayList<>();
        edges.add(List.of("n1", "n2"));
        edges.add(List.of("n1", "n3"));
        config.setEdges(edges);
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> new NetworkTopology(config)
        );
        
        assertTrue(exception.getMessage().contains("neighbors"));
        assertTrue(exception.getMessage().contains("minimum"));
    }
    
    @Test
    void testNodeWithTooManyNeighborsThrowsException() {
        NetworkConfig config = new NetworkConfig();
        config.setMinNeighbors(1);
        config.setMaxNeighbors(2);
        
        Map<String, List<String>> resources = new HashMap<>();
        resources.put("n1", List.of("fileA"));
        resources.put("n2", List.of("fileB"));
        resources.put("n3", List.of("fileC"));
        resources.put("n4", List.of("fileD"));
        config.setResources(resources);
        
        // n1 terÃ¡ 3 vizinhos (mais que o mÃ¡ximo de 2)
        List<List<String>> edges = new ArrayList<>();
        edges.add(List.of("n1", "n2"));
        edges.add(List.of("n1", "n3"));
        edges.add(List.of("n1", "n4"));
        edges.add(List.of("n2", "n3"));
        config.setEdges(edges);
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> new NetworkTopology(config)
        );
        
        assertTrue(exception.getMessage().contains("neighbors"));
        assertTrue(exception.getMessage().contains("maximum"));
    }
    
    @Test
    void testNodeWithoutResourcesThrowsException() {
        NetworkConfig config = new NetworkConfig();
        config.setMinNeighbors(1);
        config.setMaxNeighbors(2);
        
        Map<String, List<String>> resources = new HashMap<>();
        resources.put("n1", List.of("fileA"));
        resources.put("n2", Collections.emptyList());  // Lista vazia!
        config.setResources(resources);
        
        List<List<String>> edges = new ArrayList<>();
        edges.add(List.of("n1", "n2"));
        config.setEdges(edges);
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> new NetworkTopology(config)
        );
        
        assertTrue(exception.getMessage().contains("empty resource list"));
    }
    
    @Test
    void testGetNodeById() throws IOException {
        NetworkLoader loader = new NetworkLoader();
        NetworkConfig config = loader.loadFromResource("config.json");
        NetworkTopology topology = new NetworkTopology(config);
        
        // Testa busca de nÃ³ existente
        Optional<Node> node1 = topology.getNode("n1");
        assertTrue(node1.isPresent());
        assertEquals("n1", node1.get().getId());
        
        // Testa busca de nÃ³ inexistente
        Optional<Node> nodeX = topology.getNode("nX");
        assertFalse(nodeX.isPresent());
    }
    
    @Test
    void testGraphStructure() throws IOException {
        NetworkLoader loader = new NetworkLoader();
        NetworkConfig config = loader.loadFromResource("config.json");
        NetworkTopology topology = new NetworkTopology(config);
        
        // Verifica que o grafo tem todos os nÃ³s
        assertEquals(12, topology.getGraph().vertexSet().size());
        
        // Verifica que o grafo tem todas as arestas
        assertEquals(14, topology.getGraph().edgeSet().size());
        
        // Verifica que nÃ£o hÃ¡ self-loops
        for (String nodeId : topology.getGraph().vertexSet()) {
            assertFalse(topology.getGraph().containsEdge(nodeId, nodeId),
                "Graph should not contain self-loop for node " + nodeId);
        }
    }
}
