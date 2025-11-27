package p2p.search.simulator.strategy;

import p2p.search.simulator.model.Message;
import p2p.search.simulator.model.Node;
import p2p.search.simulator.simulation.SimulationManager;

import java.util.Optional;

/**
 * Estratégia Flooding com cache informado: consulta o cache antes de inundar a rede.
 */
public class InformedFloodingStrategy implements SearchStrategy {

    @Override
    public void processQuery(Node currentNode, Message message, SimulationManager simulationManager) {
        if (message.getTtl() <= 0) {
            return;
        }

        if (simulationManager.hasSeenMessage(message.getId(), currentNode.getId())) {
            return;
        }

        if (currentNode.hasResource(message.getResource())) {
            simulationManager.completeSuccess(currentNode, message);
            return;
        }

        if (tryDirect(currentNode, message, simulationManager)) {
            return;
        }

        Message forwardMessage = message.decrementTTL().addToPath(currentNode.getId());
        if (forwardMessage.getTtl() <= 0) {
            return;
        }

        for (String neighborId : currentNode.getNeighbors()) {
            Message neighborMessage = forwardMessage.toBuilder()
                .target(neighborId)
                .build();
            simulationManager.sendMessage(neighborMessage, currentNode.getId());
        }
    }

    private boolean tryDirect(Node currentNode, Message message, SimulationManager simulationManager) {
        Optional<String> cachedTarget = currentNode.getCachedLocation(message.getResource());
        if (cachedTarget.isEmpty()) {
            return false;
        }

        String destination = cachedTarget.get();
        if (destination.equals(currentNode.getId())) {
            simulationManager.completeSuccess(currentNode, message);
            return true;
        }

        Message direct = message.decrementTTL().addToPath(currentNode.getId());
        if (direct.getTtl() <= 0) {
            return false;
        }

        Message directMessage = direct.toBuilder()
            .target(destination)
            .build();
        simulationManager.sendMessage(directMessage, currentNode.getId());
        return true;
    }

    @Override
    public String getName() {
        return "Informed Flooding";
    }

    @Override
    public boolean isInformed() {
        return true;
    }
}
