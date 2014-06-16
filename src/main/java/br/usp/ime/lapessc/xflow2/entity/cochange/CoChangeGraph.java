package br.usp.ime.lapessc.xflow2.entity.cochange;

import java.util.Collection;
import java.util.List;

import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.RawDependency;
import br.usp.ime.lapessc.xflow2.entity.TaskDependencyGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class CoChangeGraph {

	private UndirectedGraph<CoChangeGraphVertex, CoChangeGraphEdge> jungGraph;
	
	public CoChangeGraph(List<DependencyGraph> dependencyGraphs){
				
		jungGraph =	new UndirectedSparseGraph<CoChangeGraphVertex, CoChangeGraphEdge>();
		
		for(DependencyGraph dependencyGraph : dependencyGraphs){
			
			TaskDependencyGraph taskDependency = (TaskDependencyGraph) dependencyGraph;
			for(RawDependency<FileDependencyObject,FileDependencyObject,Commit> rawDep : taskDependency.getRawDependencies()){
				
				CoChangeGraphVertex v1 = new CoChangeGraphVertex(rawDep.getClient(), jungGraph);
				CoChangeGraphVertex v2 = new CoChangeGraphVertex(rawDep.getSupplier(), jungGraph);
				
				CoChangeGraphEdge edge = this.findEdge(v1,v2);
				
				if (edge == null){
	
					CoChangeHistory coChangeHistory = new CoChangeHistory();
					coChangeHistory.addCommit(rawDep.getLabel());
					
					int edgeIndex = jungGraph.getEdgeCount();
					
					edge = new CoChangeGraphEdge(edgeIndex, coChangeHistory, jungGraph);
					
					jungGraph.addEdge(edge, v1, v2);
				}
				else{
					edge.getCoChangeHistory().addCommit(rawDep.getLabel());
				}
			}
		}
	}
	
	public CoChangeGraphEdge findEdge(FileDependencyObject fdo1, FileDependencyObject fdo2){
		return jungGraph.findEdge(
				new CoChangeGraphVertex(fdo1,jungGraph), 
				new CoChangeGraphVertex(fdo2,jungGraph));
	}
	
	public CoChangeGraphEdge findEdge(CoChangeGraphVertex v1, CoChangeGraphVertex v2){
		return jungGraph.findEdge(v1,v2);
	}

	public Collection<CoChangeGraphVertex> getVertices(){
		return jungGraph.getVertices();
	}
	
	public Collection<CoChangeGraphEdge> getEdges() {
		return jungGraph.getEdges();
	}
	
	public int getEdgeCount(){
		return jungGraph.getEdgeCount();
	}
	
	public int getVertexCount(){
		return jungGraph.getVertexCount();
	}
	
	
}
