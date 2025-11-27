package p2p.search.simulator.strategy;

import p2p.search.simulator.model.Message;
import p2p.search.simulator.model.Node;
import p2p.search.simulator.simulation.SimulationManager;

/**
 * ImplementaÃ§Ã£o da estratÃ©gia de busca por Flooding (InundaÃ§Ã£o).
 * 
 * LÃ³gica:
 * 1. Se TTL = 0, descarta a mensagem (drop)
 * 2. Se jÃ¡ viu essa mensagem (UUID), descarta (evita loops)
 * 3. Se tem o recurso -> Envia RESPONSE com sucesso
 * 4. SenÃ£o -> Decrementa TTL e envia para todos os vizinhos
 */
public class FloodingStrategy implements SearchStrategy {
    
    @Override
    public void processQuery(Node currentNode, Message message, SimulationManager simulationManager) {
        // REGRA 1: Se TTL = 0, descarta
        if (message.getTtl() <= 0) {
            return;  // Drop
        }
        
        // REGRA 2: Se jÃ¡ viu essa mensagem, descarta (evita loops)
        if (simulationManager.hasSeenMessage(message.getId(), currentNode.getId())) {
            return;  // Drop
        }
        
        // REGRA 3: Se tem o recurso -> Sucesso! (propaga caches e anima backtracking)
        if (currentNode.hasResource(message.getResource())) {
            simulationManager.completeSuccess(currentNode, message);
            return;
        }
        
        // REGRA 4: Senão -> Decrementa TTL e envia para TODOS os vizinhos (Flooding)
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
    
    @Override
    public String getName() {
        return "Flooding";
    }
    
    @Override
    public boolean isInformed() {
        return false;
    }
}
