package p2p.search.simulator.simulation;

import p2p.search.simulator.model.Message;
import p2p.search.simulator.model.Node;
import p2p.search.simulator.strategy.SearchStrategy;
import p2p.search.simulator.topology.NetworkTopology;
import p2p.search.simulator.visualization.NetworkVisualizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SimulationManager {

    private static final long EDGE_HIGHLIGHT_DELAY_MS = 100;
    private static final long SUCCESS_PAUSE_MS = 2000;

    private final NetworkTopology topology;
    private final Queue<PendingMessage> messageQueue = new LinkedList<>();
    private final Set<String> seenMessages = ConcurrentHashMap.newKeySet();
    private final Set<String> visitedNodes = ConcurrentHashMap.newKeySet();
    private final AtomicInteger messageCount = new AtomicInteger(0);
    private final AtomicInteger stepCounter = new AtomicInteger(0);

    private NetworkVisualizer visualizer;
    private long visualizationDelay = 300;
    private Consumer<String> logConsumer = msg -> {};

    private volatile boolean searchCompleted;
    private volatile boolean searchSucceeded;
    private List<String> resultPath = new ArrayList<>();

    public SimulationManager(NetworkTopology topology) {
        this.topology = topology;
    }

    public void setLogConsumer(Consumer<String> logConsumer) {
        this.logConsumer = logConsumer != null ? logConsumer : msg -> {};
    }

    public SearchResult runSearch(String sourceNodeId,
                                  String resource,
                                  int ttl,
                                  SearchStrategy strategy) {
        Objects.requireNonNull(strategy, "Strategy cannot be null");

        topology.getNode(sourceNodeId)
            .orElseThrow(() -> new IllegalArgumentException("Nó inexistente: " + sourceNodeId));

        configureStrategy(strategy);
        resetInternalState(resource, sourceNodeId);

        if (visualizer != null) {
            visualizer.resetVisuals();
            visualizer.markAsSource(sourceNodeId);
        }

        Message query = new Message.Builder()
            .type(Message.Type.QUERY)
            .source(sourceNodeId)
            .target(sourceNodeId)
            .resource(resource)
            .ttl(ttl)
            .build();

        sendMessage(query, null);
        long start = System.currentTimeMillis();
        processMessages();
        long duration = System.currentTimeMillis() - start;

        if (!searchSucceeded) {
            if (resultPath.isEmpty()) {
                resultPath.add(sourceNodeId);
            }
            log(String.format("Recurso '%s' não encontrado (TTL esgotado).", resource));
        }
        
        if (!searchCompleted) {
            searchCompleted = true;
        }

        return new SearchResult(
            searchSucceeded,
            Math.max(0, resultPath.size() - 1),
            messageCount.get(),
            visitedNodes.size(),
            duration,
            resource,
            sourceNodeId,
            List.copyOf(resultPath)
        );
    }

    private void configureStrategy(SearchStrategy strategy) {
        Collection<Node> nodes = topology.getAllNodes();
        for (Node node : nodes) {
            node.setSearchStrategy(strategy);
        }
    }

    private void resetInternalState(String resource, String source) {
        messageQueue.clear();
        seenMessages.clear();
        visitedNodes.clear();
        messageCount.set(0);
        stepCounter.set(0);
        searchCompleted = false;
        searchSucceeded = false;
        resultPath = new ArrayList<>();
    }

    private void processMessages() {
        while (!messageQueue.isEmpty()) {
            PendingMessage pending = messageQueue.poll();
            if (pending == null) {
                continue;
            }

            Message message = pending.message();
            String senderId = pending.senderId();

            if (message.getType() == Message.Type.QUERY && message.getTtl() <= 0) {
                log(String.format("TTL expirou antes de alcançar %s", message.getTarget()));
                continue;
            }

            Optional<Node> maybeTarget = topology.getNode(message.getTarget());
            if (maybeTarget.isEmpty()) {
                continue;
            }

            Node targetNode = maybeTarget.get();

            if (visualizer != null && senderId != null && !senderId.equals(message.getTarget())) {
                visualizer.highlightEdge(senderId, message.getTarget(), EDGE_HIGHLIGHT_DELAY_MS);
            }

            logStep(senderId, message.getTarget(), message);

            messageCount.incrementAndGet();
            visitedNodes.add(message.getTarget());

            if (visualizer != null) {
                visualizer.setNodeState(message.getTarget(), NetworkVisualizer.NodeVisualState.VISITED);
                sleep(visualizationDelay);
            }

            Message enriched = message;
            if (message.getType() == Message.Type.QUERY) {
                enriched = message.addToPath(message.getTarget());
            }
            targetNode.receiveMessage(enriched, this, senderId);

            if (visualizer != null && !searchSucceeded) {
                visualizer.setNodeState(message.getTarget(), NetworkVisualizer.NodeVisualState.IDLE);
            }
        }

        if (!searchSucceeded && !searchCompleted) {
            searchCompleted = true;
        }
    }

    private void logStep(String from, String to, Message message) {
        int step = stepCounter.incrementAndGet();
        String logMessage;
        if (from == null || from.equals(to)) {
            logMessage = String.format("[Step %d] %s processa mensagem (TTL %d)", step, to, message.getTtl());
        } else {
            logMessage = String.format("[Step %d] %s -> %s (TTL %d)", step, from, to, message.getTtl());
        }
        log(logMessage);
    }

    private void log(String message) {
        if (logConsumer != null) {
            logConsumer.accept(message);
        }
    }

    public void sendMessage(Message message) {
        sendMessage(message, null);
    }

    public void sendMessage(Message message, String senderId) {
        messageQueue.add(new PendingMessage(message, senderId));
    }

    public boolean hasSeenMessage(String messageId, String nodeId) {
        String key = messageId + ":" + nodeId;
        return !seenMessages.add(key);
    }

    public void completeSuccess(Node node, Message message) {
        if (searchSucceeded) {
            return;
        }
        searchSucceeded = true;

        List<String> path = new ArrayList<>(message.getPathHistory());
        if (path.isEmpty() || !path.get(path.size() - 1).equals(node.getId())) {
            path.add(node.getId());
        }
        resultPath = path;

        log(String.format("Recurso '%s' encontrado em %s", message.getResource(), node.getId()));

        if (visualizer != null) {
            visualizer.setNodeState(node.getId(), NetworkVisualizer.NodeVisualState.FOUND);
            sleep(SUCCESS_PAUSE_MS);
        }

        startResponseFlow(node, message);
    }

    private void startResponseFlow(Node node, Message originalQuery) {
        Message response = originalQuery.createResponse(node.getId(), true);
        List<String> reversePath = response.getPathHistory();

        if (reversePath.size() < 2) {
            searchCompleted = true;
            return;
        }

        messageQueue.clear();

        for (int i = 0; i < reversePath.size(); i++) {
            String current = reversePath.get(i);
            
            Message hop = response.toBuilder()
                .target(current)
                .build();
            
            sendMessage(hop, i > 0 ? reversePath.get(i - 1) : null);
        }
        
        searchCompleted = true;
    }

    public void continueResponse(Node currentNode, Message responseMessage) {
    }

    public void reset() {
        messageQueue.clear();
        seenMessages.clear();
        visitedNodes.clear();
        messageCount.set(0);
        stepCounter.set(0);
        searchCompleted = false;
        searchSucceeded = false;
        resultPath = new ArrayList<>();
        topology.getAllNodes().forEach(Node::clearCache);
        if (visualizer != null) {
            visualizer.resetVisuals();
        }
    }

    public void enableVisualization(NetworkVisualizer visualizer) {
        this.visualizer = visualizer;
    }

    public void disableVisualization() {
        this.visualizer = null;
    }

    public void setVisualizationDelay(long millis) {
        this.visualizationDelay = millis;
    }

    public NetworkTopology getTopology() {
        return topology;
    }

    public int getMessageCount() {
        return messageCount.get();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record PendingMessage(Message message, String senderId) { }

    public static class SearchResult {
        private final boolean success;
        private final int hops;
        private final int totalMessages;
        private final int visitedNodes;
        private final long durationMs;
        private final String resource;
        private final String sourceNode;
        private final List<String> path;

        public SearchResult(boolean success,
                            int hops,
                            int totalMessages,
                            int visitedNodes,
                            long durationMs,
                            String resource,
                            String sourceNode,
                            List<String> path) {
            this.success = success;
            this.hops = hops;
            this.totalMessages = totalMessages;
            this.visitedNodes = visitedNodes;
            this.durationMs = durationMs;
            this.resource = resource;
            this.sourceNode = sourceNode;
            this.path = path;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getHops() {
            return hops;
        }

        public int getTotalMessages() {
            return totalMessages;
        }

        public int getVisitedNodes() {
            return visitedNodes;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public String getResource() {
            return resource;
        }

        public String getSourceNode() {
            return sourceNode;
        }

        public List<String> getPath() {
            return path;
        }
    }
}
