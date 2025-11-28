package p2p.search.simulator.model;

import p2p.search.simulator.simulation.SimulationManager;
import p2p.search.simulator.strategy.SearchStrategy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Node {
    
    private final String id;
    private final Set<String> resources;
    private final Set<String> neighbors;
    private final Map<String, String> cache;
    private SearchStrategy searchStrategy;
    
    public Node(String id, List<String> resources) {
        this.id = id;
        this.resources = new HashSet<>(resources);
        this.neighbors = new HashSet<>();
        this.cache = new ConcurrentHashMap<>();
    }
    
    public void addNeighbor(String neighborId) {
        neighbors.add(neighborId);
    }
    
    public boolean hasResource(String resource) {
        return resources.contains(resource);
    }
    
    public void addToCache(String resource, String nodeId) {
        cache.put(resource, nodeId);
    }
    
    public Optional<String> getCachedLocation(String resource) {
        return Optional.ofNullable(cache.get(resource));
    }
    
    public void clearCache() {
        cache.clear();
    }
    
    public void setSearchStrategy(SearchStrategy strategy) {
        this.searchStrategy = strategy;
    }
    
    public SearchStrategy getSearchStrategy() {
        return searchStrategy;
    }
    
    public void receiveMessage(Message message, SimulationManager simulationManager, String senderId) {
        if (searchStrategy == null) {
            throw new IllegalStateException(
                "No search strategy configured for node " + id
            );
        }
        
        switch (message.getType()) {
            case QUERY:
                searchStrategy.processQuery(this, message, simulationManager, senderId);
                break;
                
            case RESPONSE:
                if (message.isSuccess()) {
                    addToCache(message.getResource(), message.getSource());
                }
                simulationManager.continueResponse(this, message);
                break;
        }
    }
    
    public String getId() {
        return id;
    }
    
    public Set<String> getResources() {
        return Collections.unmodifiableSet(resources);
    }
    
    public Set<String> getNeighbors() {
        return Collections.unmodifiableSet(neighbors);
    }
    
    public Map<String, String> getCache() {
        return Collections.unmodifiableMap(cache);
    }
    
    public int getDegree() {
        return neighbors.size();
    }
    
    @Override
    public String toString() {
        return String.format("Node[id=%s, resources=%d, neighbors=%d, cache=%d]",
            id, resources.size(), neighbors.size(), cache.size());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
