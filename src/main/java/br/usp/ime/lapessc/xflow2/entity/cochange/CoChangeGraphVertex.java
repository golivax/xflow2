package br.usp.ime.lapessc.xflow2.entity.cochange;

import java.util.Collection;

import edu.uci.ics.jung.graph.UndirectedGraph;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;

public class CoChangeGraphVertex {

	private FileDependencyObject fileDependencyObject;
	private UndirectedGraph<CoChangeGraphVertex, CoChangeGraphEdge> jungGraph;
	
	public CoChangeGraphVertex(FileDependencyObject fileDependencyObject, 
			UndirectedGraph<CoChangeGraphVertex, CoChangeGraphEdge> jungGraph){
		
		this.fileDependencyObject = fileDependencyObject;
		this.jungGraph = jungGraph;
	}

	public FileDependencyObject getFileDependencyObject() {
		return fileDependencyObject;
	}
	
	public Collection<CoChangeGraphEdge> getEdges(){
		return jungGraph.getIncidentEdges(this);
	}
	
	public Collection<CoChangeGraphVertex> getNeighbors(){
		return jungGraph.getNeighbors(this);
	}
	
	public int getNeighborCount(){
		return jungGraph.getNeighborCount(this);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((fileDependencyObject == null) ? 0 : fileDependencyObject
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CoChangeGraphVertex other = (CoChangeGraphVertex) obj;
		if (fileDependencyObject == null) {
			if (other.fileDependencyObject != null)
				return false;
		} else if (!fileDependencyObject.equals(other.fileDependencyObject))
			return false;
		return true;
	}
	
	

}
