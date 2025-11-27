package p2p.search.simulator.strategy;

import p2p.search.simulator.model.Message;
import p2p.search.simulator.model.Node;
import p2p.search.simulator.simulation.SimulationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementa o algoritmo Random Walk: encaminha a mensagem para apenas um vizinho aleatório.
 */
public class RandomWalkStrategy implements SearchStrategy {

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

	private String getPreviousHop(Message message) {
		List<String> path = message.getPathHistory();
		if (path.size() < 2) {
			return null;
		}
		return path.get(path.size() - 2);
	}

	@Override
	public String getName() {
		return "Random Walk";
	}
}
