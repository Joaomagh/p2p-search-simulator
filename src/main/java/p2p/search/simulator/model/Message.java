package p2p.search.simulator.model;

import java.util.*;

public class Message {
    
    public enum Type {
        QUERY,
        RESPONSE
    }
    
    private final String id;
    private final Type type;
    private final String source;
    private final String target;
    private final String resource;
    private final int ttl;
    private final List<String> pathHistory;
    private final boolean success;
    
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
    
    public Message createResponse(String responderNodeId, boolean success) {
        List<String> reversePath = new ArrayList<>(this.pathHistory);
        Collections.reverse(reversePath);
        
        return new Builder()
            .id(this.id)
            .type(Type.RESPONSE)
            .source(responderNodeId)
            .target(this.source)
            .resource(this.resource)
            .ttl(reversePath.size())
            .pathHistory(reversePath)
            .success(success)
            .build();
    }
    
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
            
            if (pathHistory.isEmpty()) {
                pathHistory.add(source);
            }
            
            return new Message(id, type, source, target, resource, ttl, pathHistory, success);
        }
    }
}
