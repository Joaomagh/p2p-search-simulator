package p2p.search.simulator.strategy;

import p2p.search.simulator.model.Message;
import p2p.search.simulator.model.Node;
import p2p.search.simulator.simulation.SimulationManager;

/**
 * Interface para estratÃ©gias de busca na rede P2P.
 * Implementa o padrÃ£o Strategy.
 */
public interface SearchStrategy {
    
    /**
     * Processa uma mensagem QUERY e decide para quais vizinhos encaminhÃ¡-la.
     * 
     * @param currentNode nÃ³ atual processando a mensagem
     * @param message mensagem sendo processada
     * @param simulationManager gerenciador da simulaÃ§Ã£o
     */
    void processQuery(Node currentNode, Message message, SimulationManager simulationManager);
    
    /**
     * Retorna o nome da estratÃ©gia.
     */
    String getName();
    
    /**
     * Indica se a estratÃ©gia Ã© informada (usa cache).
     */
    default boolean isInformed() {
        return false;
    }
}
