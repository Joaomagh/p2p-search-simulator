package p2p.search.simulator.strategy;

import p2p.search.simulator.model.Message;
import p2p.search.simulator.model.Node;
import p2p.search.simulator.simulation.SimulationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Estratégia Random Walk informada: tenta envio direto via cache antes de escolher vizinho aleatório.
 */
public class InformedRandomWalkStrategy implements SearchStrategy {

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

        Message forward = message.decrementTTL().addToPath(currentNode.getId());
        if (forward.getTtl() <= 0) {
            return;
        }

        List<String> candidates = new ArrayList<>(currentNode.getNeighbors());
        String previousHop = getPreviousHop(message);
        if (previousHop != null && candidates.size() > 1) {
            candidates.remove(previousHop);
        }

        if (candidates.isEmpty()) {
            return;
        }

        String nextHop = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        Message neighborMessage = forward.toBuilder()
            .target(nextHop)
            .build();
        simulationManager.sendMessage(neighborMessage, currentNode.getId());
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

    private String getPreviousHop(Message message) {
        List<String> path = message.getPathHistory();
        if (path.size() < 2) {
            return null;
        }
        return path.get(path.size() - 2);
    }

    @Override
    public String getName() {
        return "Informed Random Walk";
    }

    @Override
    public boolean isInformed() {
        return true;
    }
}
