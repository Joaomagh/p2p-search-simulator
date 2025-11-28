package p2p.search.simulator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class NetworkConfig {
    
    @JsonProperty("num_nodes")
    private int numNodes;
    
    @JsonProperty("min_neighbors")
    private int minNeighbors;
    
    @JsonProperty("max_neighbors")
    private int maxNeighbors;
    
    @JsonProperty("resources")
    private Map<String, List<String>> resources;
    
    @JsonProperty("edges")
    private List<List<String>> edges;
    
    public NetworkConfig() {}
    
    public int getNumNodes() {
        return numNodes;
    }
    
    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }
    
    public int getMinNeighbors() {
        return minNeighbors;
    }
    
    public void setMinNeighbors(int minNeighbors) {
        this.minNeighbors = minNeighbors;
    }
    
    public int getMaxNeighbors() {
        return maxNeighbors;
    }
    
    public void setMaxNeighbors(int maxNeighbors) {
        this.maxNeighbors = maxNeighbors;
    }
    
    public Map<String, List<String>> getResources() {
        return resources;
    }
    
    public void setResources(Map<String, List<String>> resources) {
        this.resources = resources;
    }
    
    public List<List<String>> getEdges() {
        return edges;
    }
    
    public void setEdges(List<List<String>> edges) {
        this.edges = edges;
    }
    
    @Override
    public String toString() {
        return String.format("NetworkConfig[nodes=%d, minNeighbors=%d, maxNeighbors=%d, resources=%d, edges=%d]",
            numNodes, minNeighbors, maxNeighbors, 
            resources != null ? resources.size() : 0, 
            edges != null ? edges.size() : 0);
    }
}
