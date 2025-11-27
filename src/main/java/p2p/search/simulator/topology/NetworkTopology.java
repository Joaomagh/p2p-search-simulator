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

/**
 * Representa a topologia da rede P2P usando JGraphT.
 * ResponsÃ¡vel por construir o grafo e validar as regras crÃ­ticas da rede.
 */
public class NetworkTopology {
    
    private final Graph<String, DefaultEdge> graph;
    private final Map<String, Node> nodes;
    private final int minNeighbors;
    private final int maxNeighbors;
    
    /**
     * ConstrÃ³i a topologia da rede a partir da configuraÃ§Ã£o.
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
    
    /**
     * ConstrÃ³i o grafo e os nÃ³s a partir da configuraÃ§Ã£o.
     */
    private void buildTopology(NetworkConfig config) {
        // 1. Criar nÃ³s e adicionar ao grafo
        for (Map.Entry<String, List<String>> entry : config.getResources().entrySet()) {
            String nodeId = entry.getKey();
            List<String> resources = entry.getValue();
            
            Node node = new Node(nodeId, resources);
            nodes.put(nodeId, node);
            graph.addVertex(nodeId);
        }
        
        // 2. Criar arestas
        for (List<String> edge : config.getEdges()) {
            if (edge.size() != 2) {
                throw new IllegalArgumentException("Invalid edge format: " + edge);
            }
            
            String node1 = edge.get(0);
            String node2 = edge.get(1);
            
            // Verifica se os nÃ³s existem
            if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
                throw new IllegalArgumentException(
                    String.format("Edge references non-existent node: [%s, %s]", node1, node2)
                );
            }
            
            // Adiciona aresta ao grafo (JGraphT impede duplicatas automaticamente)
            graph.addEdge(node1, node2);
            
            // Atualiza vizinhos nos nÃ³s
            nodes.get(node1).addNeighbor(node2);
            nodes.get(node2).addNeighbor(node1);
        }
    }
    
    /**
     * Valida as 4 regras crÃ­ticas da topologia:
     * 1. Conectividade - grafo deve ser conexo
     * 2. Grau - todos os nÃ³s devem respeitar min/max neighbors
     * 3. Recursos - todos os nÃ³s devem ter pelo menos um recurso
     * 4. Self-loops - nÃ£o pode haver arestas de um nÃ³ para ele mesmo
     * 
     * @throws IllegalStateException se alguma validaÃ§Ã£o falhar
     */
    public void validate() {
        validateConnectivity();
        validateDegree();
        validateResources();
        validateSelfLoops();
    }
    
    /**
     * REGRA 1: Valida que o grafo Ã© conexo (todos os nÃ³s sÃ£o alcanÃ§Ã¡veis).
     * Usa ConnectivityInspector do JGraphT.
     */
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
    
    /**
     * REGRA 2: Valida que todos os nÃ³s respeitam os limites de grau.
     * Nenhum nÃ³ pode ter neighbors < min ou > max.
     */
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
    
    /**
     * REGRA 3: Valida que todos os nÃ³s tÃªm lista de recursos nÃ£o-vazia.
     */
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
    
    /**
     * REGRA 4: Valida que nÃ£o existem self-loops (aresta de um nÃ³ para ele mesmo).
     */
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
    
    // Getters
    
    /**
     * Retorna o grafo JGraphT.
     */
    public Graph<String, DefaultEdge> getGraph() {
        return graph;
    }
    
    /**
     * Retorna um nÃ³ pelo ID.
     */
    public Optional<Node> getNode(String nodeId) {
        return Optional.ofNullable(nodes.get(nodeId));
    }
    
    /**
     * Retorna todos os nÃ³s da rede.
     */
    public Collection<Node> getAllNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }
    
    /**
     * Retorna todos os IDs dos nÃ³s.
     */
    public Set<String> getNodeIds() {
        return Collections.unmodifiableSet(nodes.keySet());
    }
    
    /**
     * Retorna o nÃºmero de nÃ³s na rede.
     */
    public int getNodeCount() {
        return nodes.size();
    }
    
    /**
     * Retorna o nÃºmero de arestas na rede.
     */
    public int getEdgeCount() {
        return graph.edgeSet().size();
    }
    
    public int getMinNeighbors() {
        return minNeighbors;
    }
    
    public int getMaxNeighbors() {
        return maxNeighbors;
    }

    /**
     * Exibe a topologia usando o visualizador acadêmico.
     */
    public NetworkVisualizer show() {
        return new NetworkVisualizer(this);
    }

    /**
     * Retorna o caminho mais curto entre dois nós como lista de IDs.
     */
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
