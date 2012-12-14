package it.cnr.alfresco.repository.workflow;

import java.awt.Dimension;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class JungViz {

	private static final int width = 800;
	
	private static final int height = 600;

	public DirectedOrderedSparseMultigraph<String, String> g = new DirectedOrderedSparseMultigraph<String, String>();
	
	
	public String[] getGraph() {
		
		Graph<String, String> myGraph = this.g;
		FRLayout<String,String> layout = new FRLayout<String,String>( myGraph );
		layout.setSize(new Dimension(width,height));
		layout.setAttractionMultiplier(0.99);
		layout.setRepulsionMultiplier(0.99);
		layout.setMaxIterations(1000);
		
		/*for(String link :myGraph.getEdges()){
			Pair<MyNode> coppia = myGraph.getEndpoints(link);
			System.out.println( "edge "+ coppia.getFirst().toString() +" "+ coppia.getSecond().toString());
		}*/
		String[] str=new String[myGraph.getVertices().size()];
		int i=0;
		for(String node : myGraph.getVertices()){
			double x = layout.getX(node);
			double y = layout.getY(node);
			//System.out.println( node.toString() + " " +x +"," + y );
			str[i]=(node.toString() + "," +x +"," + y );
			i++;
		}
		
		return str;
	}
	
	public void addVertex(String vertex) {
			g.addVertex(vertex);
	}

	public void addEdge(String edgeLabel, String da, String a) {
		g.addEdge(edgeLabel, da, a, EdgeType.DIRECTED);
	}

	
}
