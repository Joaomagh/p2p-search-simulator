package p2p.search.simulator.strategy;

import p2p.search.simulator.model.Message;
import p2p.search.simulator.model.Node;
import p2p.search.simulator.simulation.SimulationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RandomWalkStrategy implements SearchStrategy {

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
			return;
		}

		Message forward = message.decrementTTL().addToPath(currentNode.getId());
		if (forward.getTtl() <= 0) {
			return;
		}

		// Pega vizinhos não tentados ainda neste nó
		List<String> candidates = new ArrayList<>(currentNode.getNeighbors());
		
		if (senderId != null) {
			candidates.remove(senderId);
		}
		
		// Remove vizinhos já tentados anteriormente
		Set<String> tried = forward.getTriedNeighborsFor(currentNode.getId());
		candidates.removeAll(tried);

		if (candidates.isEmpty()) {
			// Backtracking: volta ao nó anterior via pathHistory
			List<String> path = forward.getPathHistory();
			if (path.size() >= 2) {
				// Pega o penúltimo nó (antes do atual)
				String backtrackNode = path.get(path.size() - 2);
				Message backtrackMessage = forward.toBuilder()
					.target(backtrackNode)
					.build();
				simulationManager.sendMessage(backtrackMessage, currentNode.getId());
			}
			return;
		}

		// Escolhe um vizinho aleatório ainda não tentado
		String nextHop = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
		
		// Marca esse vizinho como tentado para este nó
		Message markedMessage = forward.markNeighborTried(currentNode.getId(), nextHop);
		
		Message neighborMessage = markedMessage.toBuilder()
			.target(nextHop)
			.build();
		simulationManager.sendMessage(neighborMessage, currentNode.getId());
	}

	@Override
	public String getName() {
		return "Random Walk";
	}
}
