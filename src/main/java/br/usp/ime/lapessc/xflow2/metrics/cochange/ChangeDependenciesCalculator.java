package br.usp.ime.lapessc.xflow2.metrics.cochange;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.entity.CoChangeHistory;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.database.DatabaseManager;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class ChangeDependenciesCalculator {

	public List<ChangeDependency> calculate(CoChangesAnalysis analysis) throws DatabaseException{
				
		UndirectedGraph<FileDependencyObject, CoChangeHistory> coChangeGraph = 
				analysis.getCoChangeGraph();
		
		List<ChangeDependency> changeDeps = getChangeDependencies(coChangeGraph);
		System.out.println("Change deps list size: " + changeDeps.size());
		
		//Persists Co-Changes
		for(ChangeDependency changeDep: changeDeps){
			insert(changeDep);
			DatabaseManager.getDatabaseSession().clear();
		}
		
		return changeDeps;
	}

	private List<ChangeDependency> getChangeDependencies(
			UndirectedGraph<FileDependencyObject, CoChangeHistory> coChangeGraph) {
		
		List<ChangeDependency> changeDeps = new ArrayList<ChangeDependency>();
		for(CoChangeHistory coChangeHistory : coChangeGraph.getEdges()){
			Pair<FileDependencyObject> pair = coChangeGraph.getEndpoints(coChangeHistory);
			if(pair.getFirst() != pair.getSecond()){
				
				//Change Dep: first --> second
				
				ChangeDependency firstToSecondDep = new ChangeDependency(
						pair.getFirst().getFilePath(), 
						pair.getFirst().getAssignedStamp(),
						pair.getSecond().getFilePath(),
						pair.getSecond().getAssignedStamp(),
						coChangeGraph.findEdge(pair.getFirst(), pair.getFirst()).getCommits(), 
						coChangeGraph.findEdge(pair.getSecond(), pair.getSecond()).getCommits());
				
				//Change Dep: second --> first
				
				ChangeDependency secondToFirstDep = new ChangeDependency(
						pair.getSecond().getFilePath(),
						pair.getSecond().getAssignedStamp(),
						pair.getFirst().getFilePath(), 
						pair.getFirst().getAssignedStamp(),
						coChangeGraph.findEdge(pair.getSecond(), pair.getSecond()).getCommits(),
						coChangeGraph.findEdge(pair.getFirst(), pair.getFirst()).getCommits());
						
				
				changeDeps.add(firstToSecondDep);
				changeDeps.add(secondToFirstDep);
			}
		}
		return changeDeps;
	}

	private void insert(ChangeDependency coChange){
		try {
			EntityManager manager = DatabaseManager.getDatabaseSession();
			manager.getTransaction().begin();
			manager.persist(coChange);
			manager.getTransaction().commit();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}