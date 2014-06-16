package br.usp.ime.lapessc.xflow2.entity.cochange;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class CoChangeGraphEdge {

	private int id;
	private CoChangeHistory coChangeHistory;
	private UndirectedGraph<CoChangeGraphVertex, CoChangeGraphEdge> jungGraph;
	
	public CoChangeGraphEdge(int id, CoChangeHistory coChangeHistory, UndirectedGraph<CoChangeGraphVertex, CoChangeGraphEdge> jungGraph){
		this.id = id;
		this.coChangeHistory = coChangeHistory;
		this.jungGraph = jungGraph;
	}

	public int getId() {
		return id;
	}

	public CoChangeHistory getCoChangeHistory() {
		return coChangeHistory;
	}
	
	public Pair<CoChangeGraphVertex> getEndpoints(){
		return jungGraph.getEndpoints(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		CoChangeGraphEdge other = (CoChangeGraphEdge) obj;
		if (id != other.id)
			return false;
		return true;
	}

	
	
}
