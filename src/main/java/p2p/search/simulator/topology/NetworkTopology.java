package p2p.search.simulator.topology;

import p2p.search.simulator.model.NetworkConfig;
import p2p.search.simulator.model.Node;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import p2p.search.simulator.visualization.NetworkVisualizer;

import java.util.*;


public class NetworkTopology {
    
    private final Graph<String, DefaultEdge> graph;
    private final Map<String, Node> nodes;
    private final int minNeighbors;
    private final int maxNeighbors;
    
    /**
     * Constrói a topologia da rede a partir da configuração.
     * 
     * @param config configuraÃ§Ã£o da rede carregada do JSON
     */
    public NetworkTopology(NetworkConfig config) {
        this.minNeighbors = config.getMinNeighbors();
        this.maxNeighbors = config.getMaxNeighbors();
        this.graph = new SimpleGraph<>(DefaultEdge.class);
        this.nodes = new HashMap<>();
        
        buildTopology(config);
        validate();
    }
    
    private void buildTopology(NetworkConfig config) {
        for (Map.Entry<String, List<String>> entry : config.getResources().entrySet()) {
            String nodeId = entry.getKey();
            List<String> resources = entry.getValue();
            
            Node node = new Node(nodeId, resources);
            nodes.put(nodeId, node);
            graph.addVertex(nodeId);
        }
        
        for (List<String> edge : config.getEdges()) {
            if (edge.size() != 2) {
                throw new IllegalArgumentException("Invalid edge format: " + edge);
            }
            
            String node1 = edge.get(0);
            String node2 = edge.get(1);
            
            if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
                throw new IllegalArgumentException(
                    String.format("Edge references non-existent node: [%s, %s]", node1, node2)
                );
            }
            
            graph.addEdge(node1, node2);
            
            nodes.get(node1).addNeighbor(node2);
            nodes.get(node2).addNeighbor(node1);
        }
    }
    
    /**
     * Valida as 4 regras críticas da topologia:
     * 1. Conectividade - grafo deve ser conexo
     * 2. Grau - todos os não devem respeitar min/max neighbors
     * 3. Recursos - todos os não devem ter pelo menos um recurso
     * 4. Self-loops - não pode haver arestas de um não para ele mesmo
     * 
     * @throws IllegalStateException se alguma validação falhar
     */
    public void validate() {
        validateConnectivity();
        validateDegree();
        validateResources();
        validateSelfLoops();
    }
    
    private void validateConnectivity() {
        ConnectivityInspector<String, DefaultEdge> inspector = 
            new ConnectivityInspector<>(graph);
        
        if (!inspector.isConnected()) {
            List<Set<String>> components = inspector.connectedSets();
            throw new IllegalStateException(
                String.format(
                    "Network is not connected! Found %d disconnected components: %s",
                    components.size(), components
                )
            );
        }
    }
    
    private void validateDegree() {
        for (Node node : nodes.values()) {
            int degree = node.getDegree();
            
            if (degree < minNeighbors) {
                throw new IllegalStateException(
                    String.format(
                        "Node '%s' has %d neighbors, but minimum is %d",
                        node.getId(), degree, minNeighbors
                    )
                );
            }
            
            if (degree > maxNeighbors) {
                throw new IllegalStateException(
                    String.format(
                        "Node '%s' has %d neighbors, but maximum is %d",
                        node.getId(), degree, maxNeighbors
                    )
                );
            }
        }
    }
    
    private void validateResources() {
        for (Node node : nodes.values()) {
            if (node.getResources().isEmpty()) {
                throw new IllegalStateException(
                    String.format(
                        "Node '%s' has empty resource list. Every node must have at least one resource.",
                        node.getId()
                    )
                );
            }
        }
    }
    
    private void validateSelfLoops() {
        for (String nodeId : graph.vertexSet()) {
            if (graph.containsEdge(nodeId, nodeId)) {
                throw new IllegalStateException(
                    String.format(
                        "Self-loop detected: node '%s' cannot connect to itself",
                        nodeId
                    )
                );
            }
        }
    }
    
    public Graph<String, DefaultEdge> getGraph() {
        return graph;
    }
    
    public Optional<Node> getNode(String nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }
    
    public Collection<Node> getAllNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }
    
    public Set<String> getNodeIds() {
        return Collections.unmodifiableSet(nodes.keySet());
    }
    
    public int getNodeCount() {
        return nodes.size();
    }
    
    public int getEdgeCount() {
        return graph.edgeSet().size();
    }
    
    public int getMinNeighbors() {
        return minNeighbors;
    }
    
    public int getMaxNeighbors() {
        return maxNeighbors;
    }

    public NetworkVisualizer show() {
        return new NetworkVisualizer(this);
    }

    public List<String> shortestPath(String source, String target) {
        if (!graph.containsVertex(source) || !graph.containsVertex(target)) {
            return Collections.emptyList();
        }

        var algorithm = new DijkstraShortestPath<>(graph);
        var path = algorithm.getPath(source, target);
        if (path == null) {
            return Collections.emptyList();
        }
        return path.getVertexList();
    }
    
    @Override
    public String toString() {
        return String.format(
            "NetworkTopology[nodes=%d, edges=%d, minNeighbors=%d, maxNeighbors=%d]",
            getNodeCount(), getEdgeCount(), minNeighbors, maxNeighbors
        );
    }
}
