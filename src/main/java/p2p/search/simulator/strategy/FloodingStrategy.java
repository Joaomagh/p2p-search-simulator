package p2p.search.simulator.strategy;

import p2p.search.simulator.model.Message;
import p2p.search.simulator.model.Node;
import p2p.search.simulator.simulation.SimulationManager;

public class FloodingStrategy implements SearchStrategy {
    
    @Override
    public void processQuery(Node currentNode, Message message, SimulationManager simulationManager, String senderId) {
        if (message.getTtl() <= 0) {
            return;
        }
        
        if (simulationManager.hasSeenMessage(message.getId(), currentNode.getId())) {
            return;
        }
        
        if (currentNode.hasResource(message.getResource())) {
            simulationManager.completeSuccess(currentNode, message);
        }
        
        Message forwardMessage = message.decrementTTL().addToPath(currentNode.getId());
        if (forwardMessage.getTtl() <= 0) {
            return;
        }
        
        for (String neighborId : currentNode.getNeighbors()) {
            if (neighborId.equals(senderId)) {
                continue;
            }
            Message neighborMessage = forwardMessage.toBuilder()
                .target(neighborId)
                .build();
            simulationManager.sendMessage(neighborMessage, currentNode.getId());
        }
    }
    
    @Override
    public String getName() {
        return "Flooding";
    }
    
    @Override
    public boolean isInformed() {
        return false;
    }
}
