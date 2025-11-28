package p2p.search.simulator.strategy;

import p2p.search.simulator.model.Message;
import p2p.search.simulator.model.Node;
import p2p.search.simulator.simulation.SimulationManager;

public interface SearchStrategy {
    
    void processQuery(Node currentNode, Message message, SimulationManager simulationManager, String senderId);
    
    String getName();
    
    default boolean isInformed() {
        return false;
    }
}
