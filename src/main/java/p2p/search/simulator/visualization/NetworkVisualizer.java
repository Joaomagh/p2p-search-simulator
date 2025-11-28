package p2p.search.simulator.visualization;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.jgrapht.graph.DefaultEdge;
import p2p.search.simulator.topology.NetworkTopology;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Responsavel pela visualização da rede usando GraphStream.
 */
public class NetworkVisualizer {

    static {
        System.setProperty("org.graphstream.ui", "swing");
    }

    private static final String ACADEMIC_CSS = "graph {" +
        " fill-color: #ffffff;" +
        " padding: 40px;" +
        "}" +
        "node {" +
        " size: 20px;" +
        " fill-color: #ecf0f1;" +
        " stroke-mode: plain;" +
        " stroke-color: #bdc3c7;" +
        " stroke-width: 2px;" +
        " text-mode: normal;" +
        " text-style: bold;" +
        " text-size: 14;" +
        " text-color: #2c3e50;" +
        " text-alignment: at-right;" +
        " text-offset: 5px, 0px;" +
        "}" +
        "node.source {" +
        " fill-color: #3498db;" +
        " stroke-color: #2980b9;" +
        " size: 25px;" +
        "}" +
        "node.visited {" +
        " fill-color: #e67e22;" +
        " stroke-color: #d35400;" +
        "}" +
        "node.found {" +
        " fill-color: #2ecc71;" +
        " stroke-color: #27ae60;" +
        " size: 30px;" +
        " shadow-mode: plain;" +
        " shadow-color: #999;" +
        " shadow-offset: 3px, -3px;" +
        "}" +
        "edge {" +
        " shape: line;" +
        " fill-color: #95a5a6;" +
        " size: 1.5px;" +
        " arrow-shape: none;" +
        "}" +
        "edge.active {" +
        " fill-color: #e74c3c;" +
        " size: 3px;" +
        "}";

    private final Graph graph;
    private final Viewer viewer;
    private final ViewPanel viewPanel;
    private final Map<String, String> edgeIds = new HashMap<>();

    public NetworkVisualizer(NetworkTopology topology) {
        this.graph = new SingleGraph("P2P-Academic-Network");
        configureGraphStyle();
        buildVisualization(topology);

    this.viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
    this.viewPanel = (ViewPanel) viewer.addDefaultView(false);
    }

    private void configureGraphStyle() {
        graph.setAttribute("ui.stylesheet", ACADEMIC_CSS);
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
    }

    private void buildVisualization(NetworkTopology topology) {
        org.jgrapht.Graph<String, DefaultEdge> jGraph = topology.getGraph();

        for (String nodeId : jGraph.vertexSet()) {
            Node gsNode = graph.addNode(nodeId);
            gsNode.setAttribute("ui.label", nodeId);
            gsNode.setAttribute("ui.class", NodeVisualState.IDLE.getCssClass());
            topology.getNode(nodeId).ifPresent(modelNode ->
                gsNode.setAttribute("resources", String.join(", ", modelNode.getResources()))
            );
        }

        for (DefaultEdge edge : jGraph.edgeSet()) {
            String source = jGraph.getEdgeSource(edge);
            String target = jGraph.getEdgeTarget(edge);
            String graphEdgeId = source + "__" + target;
            graph.addEdge(graphEdgeId, source, target, false);
            edgeIds.put(makeKey(source, target), graphEdgeId);
        }
    }

    public void resetVisuals() {
        graph.edges().forEach(e -> e.removeAttribute("ui.class"));
        graph.nodes().forEach(n -> n.setAttribute("ui.class", NodeVisualState.IDLE.getCssClass()));
    }

    public void setNodeState(String nodeId, NodeVisualState state) {
        Optional.ofNullable(graph.getNode(nodeId))
            .ifPresent(node -> node.setAttribute("ui.class", state.getCssClass()));
    }

    public void markAsSource(String nodeId) {
        setNodeState(nodeId, NodeVisualState.SOURCE);
    }

    public void highlightEdge(String from, String to, long millis) {
        String key = makeKey(from, to);
        String edgeId = edgeIds.get(key);
        if (edgeId == null) {
            // tenta criar se ainda não existir (caso de autogerado)
            graph.addEdge(key, from, to, false);
            edgeIds.put(key, key);
            edgeId = key;
        }

        org.graphstream.graph.Edge edge = graph.getEdge(edgeId);
        if (edge != null) {
            edge.setAttribute("ui.class", "active");
            sleep(millis);
            edge.removeAttribute("ui.class");
        }
    }

    public void animatePath(java.util.List<String> path, long millis) {
        if (path == null || path.size() < 2) {
            return;
        }

        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i + 1);
            highlightEdge(from, to, millis);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public ViewPanel getViewPanel() {
        return viewPanel;
    }

    public void close() {
        viewer.close();
    }

    private String makeKey(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "__" + b : b + "__" + a;
    }

    public Graph getGraph() {
        return graph;
    }

    public enum NodeVisualState {
        IDLE("idle"),
        SOURCE("source"),
        VISITED("visited"),
        FOUND("found");

        private final String cssClass;

        NodeVisualState(String cssClass) {
            this.cssClass = cssClass;
        }

        public String getCssClass() {
            return cssClass;
        }
    }
}
