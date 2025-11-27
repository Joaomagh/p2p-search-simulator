package p2p.search.simulator.model;

import java.util.*;

/**
 * Representa uma mensagem trocada entre nÃ³s na rede P2P.
 * Usada para simulaÃ§Ã£o de comunicaÃ§Ã£o (sem sockets/RMI).
 */
public class Message {
    
    /**
     * Tipos de mensagem.
     */
    public enum Type {
        QUERY,      // Busca por um recurso
        RESPONSE    // Resposta (recurso encontrado ou nÃ£o)
    }
    
    private final String id;              // UUID Ãºnico da mensagem
    private final Type type;               // Tipo da mensagem
    private final String source;           // ID do nÃ³ de origem
    private final String target;           // ID do nÃ³ de destino
    private final String resource;         // Recurso sendo buscado
    private final int ttl;                 // Time-To-Live (hops restantes)
    private final List<String> pathHistory; // Caminho percorrido pela mensagem
    private final boolean success;         // Se a busca foi bem-sucedida (para RESPONSE)
    
    /**
     * Construtor privado - use o Builder.
     */
    private Message(String id, Type type, String source, String target, 
                   String resource, int ttl, List<String> pathHistory, boolean success) {
        this.id = id;
        this.type = type;
        this.source = source;
        this.target = target;
        this.resource = resource;
        this.ttl = ttl;
        this.pathHistory = new ArrayList<>(pathHistory);
        this.success = success;
    }
    
    /**
     * Cria uma nova mensagem com TTL decrementado.
     */
    public Message decrementTTL() {
        return new Builder()
            .id(this.id)
            .type(this.type)
            .source(this.source)
            .target(this.target)
            .resource(this.resource)
            .ttl(this.ttl - 1)
            .pathHistory(this.pathHistory)
            .success(this.success)
            .build();
    }
    
    /**
     * Cria uma nova mensagem adicionando um nÃ³ ao path.
     */
    public Message addToPath(String nodeId) {
        List<String> newPath = new ArrayList<>(this.pathHistory);
        newPath.add(nodeId);
        
        return new Builder()
            .id(this.id)
            .type(this.type)
            .source(this.source)
            .target(this.target)
            .resource(this.resource)
            .ttl(this.ttl)
            .pathHistory(newPath)
            .success(this.success)
            .build();
    }
    
    /**
     * Cria uma mensagem de RESPONSE a partir de uma QUERY.
     */
    public Message createResponse(String responderNodeId, boolean success) {
        // O caminho de resposta Ã© o inverso do caminho da query
        List<String> reversePath = new ArrayList<>(this.pathHistory);
        Collections.reverse(reversePath);
        
        return new Builder()
            .id(this.id)  // MantÃ©m o mesmo ID da query original
            .type(Type.RESPONSE)
            .source(responderNodeId)
            .target(this.source)  // Retorna para o nÃ³ de origem da query
            .resource(this.resource)
            .ttl(reversePath.size())  // TTL suficiente para retornar
            .pathHistory(reversePath)
            .success(success)
            .build();
    }
    
    /**
     * Retorna um builder pré-populado a partir desta mensagem.
     */
    public Builder toBuilder() {
        return new Builder()
            .id(this.id)
            .type(this.type)
            .source(this.source)
            .target(this.target)
            .resource(this.resource)
            .ttl(this.ttl)
            .pathHistory(this.pathHistory)
            .success(this.success);
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public Type getType() {
        return type;
    }
    
    public String getSource() {
        return source;
    }
    
    public String getTarget() {
        return target;
    }
    
    public String getResource() {
        return resource;
    }
    
    public int getTtl() {
        return ttl;
    }
    
    public List<String> getPathHistory() {
        return Collections.unmodifiableList(pathHistory);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public int getHops() {
        return pathHistory.size();
    }
    
    @Override
    public String toString() {
        return String.format("Message[id=%s, type=%s, source=%s, target=%s, resource=%s, ttl=%d, hops=%d, success=%s]",
            id, type, source, target, resource, ttl, pathHistory.size(), success);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * Builder para construÃ§Ã£o de mensagens.
     */
    public static class Builder {
        private String id = UUID.randomUUID().toString();
        private Type type;
        private String source;
        private String target;
        private String resource;
        private int ttl;
        private List<String> pathHistory = new ArrayList<>();
        private boolean success = false;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder type(Type type) {
            this.type = type;
            return this;
        }
        
        public Builder source(String source) {
            this.source = source;
            return this;
        }
        
        public Builder target(String target) {
            this.target = target;
            return this;
        }
        
        public Builder resource(String resource) {
            this.resource = resource;
            return this;
        }
        
        public Builder ttl(int ttl) {
            this.ttl = ttl;
            return this;
        }
        
        public Builder pathHistory(List<String> pathHistory) {
            this.pathHistory = new ArrayList<>(pathHistory);
            return this;
        }
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder addToPath(String nodeId) {
            this.pathHistory.add(nodeId);
            return this;
        }
        
        public Message build() {
            if (type == null) {
                throw new IllegalStateException("Message type cannot be null");
            }
            if (source == null) {
                throw new IllegalStateException("Message source cannot be null");
            }
            if (resource == null) {
                throw new IllegalStateException("Message resource cannot be null");
            }
            
            // Adiciona o source ao path se estiver vazio
            if (pathHistory.isEmpty()) {
                pathHistory.add(source);
            }
            
            return new Message(id, type, source, target, resource, ttl, pathHistory, success);
        }
    }
}
