package step2;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.mxgraph.model.mxICell;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public class GraphVisualizer extends JFrame {

	private static final long serialVersionUID = 1L;

	public GraphVisualizer(Graph<String, DefaultEdge> graph) {
        super("Call Graph Visualization");
        mxGraph jGraph = new mxGraph();
        Object parent = jGraph.getDefaultParent();

        jGraph.getModel().beginUpdate();
        try {
            // Create a map to store the vertices as mxICell instances
            Map<String, mxICell> vertexMap = new HashMap<>();

            // Insert vertices
            for (String vertex : graph.vertexSet()) {
                // Create a visual representation for the vertex
                mxICell v = (mxICell) jGraph.insertVertex(parent, null, vertex, 100, 100, 80, 30);
                vertexMap.put(vertex, v);
            }

            // Insert edges
            for (DefaultEdge edge : graph.edgeSet()) {
                String source = graph.getEdgeSource(edge);
                String target = graph.getEdgeTarget(edge);
                // Use the mxICell objects for source and target from the map
                jGraph.insertEdge(parent, null, "", vertexMap.get(source), vertexMap.get(target));
            }
        } finally {
            jGraph.getModel().endUpdate();
        }

        mxGraphComponent graphComponent = new mxGraphComponent(jGraph);
        getContentPane().add(graphComponent);
    }

    public static void displayGraph(Graph<String, DefaultEdge> callGraph) {
        GraphVisualizer frame = new GraphVisualizer(callGraph);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 320);
        frame.setVisible(true);
    }
}