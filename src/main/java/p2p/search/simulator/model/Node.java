package p2p.search.simulator.model;

import p2p.search.simulator.simulation.SimulationManager;
import p2p.search.simulator.strategy.SearchStrategy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Representa um nÃ³ (peer) na rede P2P.
 * Cada nÃ³ possui um ID, lista de recursos e um cache para buscas informadas.
 */
public class Node {
    
    private final String id;
    private final Set<String> resources;
    private final Set<String> neighbors;
    
    // Cache para algoritmos informados: Map<Resource, NodeID que possui o recurso>
    private final Map<String, String> cache;
    
    // EstratÃ©gia de busca configurada para este nÃ³
    private SearchStrategy searchStrategy;
    
    public Node(String id, List<String> resources) {
        this.id = id;
        this.resources = new HashSet<>(resources);
        this.neighbors = new HashSet<>();
        this.cache = new ConcurrentHashMap<>();
    }
    
    /**
     * Adiciona um vizinho a este nÃ³.
     */
    public void addNeighbor(String neighborId) {
        neighbors.add(neighborId);
    }
    
    /**
     * Verifica se este nÃ³ possui o recurso especificado.
     */
    public boolean hasResource(String resource) {
        return resources.contains(resource);
    }
    
    /**
     * Adiciona uma entrada no cache: recurso -> nÃ³ que o possui.
     * Usado pelos algoritmos informados apÃ³s uma busca bem-sucedida.
     */
    public void addToCache(String resource, String nodeId) {
        cache.put(resource, nodeId);
    }
    
    /**
     * Consulta o cache para saber se conhece onde estÃ¡ um recurso.
     */
    public Optional<String> getCachedLocation(String resource) {
        return Optional.ofNullable(cache.get(resource));
    }
    
    /**
     * Limpa o cache deste nÃ³.
     */
    public void clearCache() {
        cache.clear();
    }
    
    /**
     * Define a estratÃ©gia de busca para este nÃ³.
     */
    public void setSearchStrategy(SearchStrategy strategy) {
        this.searchStrategy = strategy;
    }
    
    /**
     * Retorna a estratÃ©gia de busca configurada.
     */
    public SearchStrategy getSearchStrategy() {
        return searchStrategy;
    }
    
    /**
     * Recebe e processa uma mensagem.
     * Delega o processamento para a estratÃ©gia de busca configurada.
     */
    public void receiveMessage(Message message, SimulationManager simulationManager) {
        if (searchStrategy == null) {
            throw new IllegalStateException(
                "No search strategy configured for node " + id
            );
        }
        
        switch (message.getType()) {
            case QUERY:
                // Delega para a estratÃ©gia processar a query
                searchStrategy.processQuery(this, message, simulationManager);
                break;
                
            case RESPONSE:
                // Processa resposta (para algoritmos informados atualizarem o cache)
                if (searchStrategy.isInformed() && message.isSuccess()) {
                    addToCache(message.getResource(), message.getSource());
                }
                
                // Se nÃ£o for o nÃ³ de origem, propaga a resposta de volta
                if (!this.id.equals(message.getTarget())) {
                    simulationManager.sendMessage(message, this.id);
                }
                break;
        }
    }
    
    // Getters
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
