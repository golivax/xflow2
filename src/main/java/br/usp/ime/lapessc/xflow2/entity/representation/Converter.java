package br.usp.ime.lapessc.xflow2.entity.representation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import prefuse.data.Edge;
import prefuse.data.Node;
import br.usp.ime.lapessc.xflow2.entity.DependencyObject;
import br.usp.ime.lapessc.xflow2.entity.DependencySet;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencySetDAO;
import br.usp.ime.lapessc.xflow2.entity.database.DatabaseManager;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGEdge;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGGraph;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGVertex;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.Matrix;
import br.usp.ime.lapessc.xflow2.entity.representation.prefuse.PrefuseGraph;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import edu.uci.ics.jung.graph.AbstractTypedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public final class Converter {
	
	public static void convertDependenciesToLargeMatrix(Matrix resultMatrix, List<Long> dependencySetsIds, boolean isDependencyDirected) {
		
		try{
			DependencySetDAO dependencySetDAO = new DependencySetDAO();
			
			//100 load
			int bulkLoad = 1;
			
			for(int i = 0; (i + bulkLoad -1) < dependencySetsIds.size(); i += bulkLoad){
				long startID = dependencySetsIds.get(i);
				long endID =  dependencySetsIds.get(i + bulkLoad - 1);
				System.out.println("Processing dependecySets from " + startID + " to " + endID);
				List<DependencySet> dependencySets = dependencySetDAO.findByIds(startID, endID);
				convertDependenciesToMatrix(resultMatrix, dependencySets, isDependencyDirected);
				DatabaseManager.getDatabaseSession().clear();
			}
				
			int rest = dependencySetsIds.size() % bulkLoad;
			if (rest != 0){
				long endID = dependencySetsIds.get(dependencySetsIds.size()-1);
				long startID = endID - rest + 1;
				System.out.println("Processing dependecySets from " + startID + " to " + endID);
				List<DependencySet> dependencySets = dependencySetDAO.findByIds(startID, endID);
				convertDependenciesToMatrix(resultMatrix, dependencySets, isDependencyDirected);
				DatabaseManager.getDatabaseSession().clear();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
				
	}

		
	/*
	 * LIST<DependencyObject> TO MATRIX
	 */
	public static void convertDependenciesToMatrix(Matrix resultMatrix, List<DependencySet> dependencySets, boolean isDependencyDirected) {

		if(isDependencyDirected){
			for (DependencySet dependencySet : dependencySets) {
				
				if(dependencySet.getDependedObject().getAssignedStamp() > (resultMatrix.getRows()-1)){
					resultMatrix.incrementMatrixRowsTo(dependencySet.getDependedObject().getAssignedStamp()+1);
				}

				Set<DependencyObject> dependentObjects = dependencySet.getDependenciesMap().keySet();

				for (DependencyObject dependentObject : dependentObjects) {
					if(dependentObject.getAssignedStamp() > (resultMatrix.getColumns()-1)){
						resultMatrix.incrementMatrixColumnsTo(dependentObject.getAssignedStamp()+1);
					}
					
					resultMatrix.incrementValueAt((Integer) dependencySet.getDependenciesMap().get(dependentObject), dependencySet.getDependedObject().getAssignedStamp(), dependentObject.getAssignedStamp());
				}
			}
		}
		else{
			for (DependencySet dependencySet : dependencySets) {
				
				if(dependencySet.getDependedObject().getAssignedStamp() > (resultMatrix.getRows()-1)){
					resultMatrix.incrementMatrixRowsTo(dependencySet.getDependedObject().getAssignedStamp()+1);
				}
				if(dependencySet.getDependedObject().getAssignedStamp() > (resultMatrix.getColumns()-1)){
					resultMatrix.incrementMatrixColumnsTo(dependencySet.getDependedObject().getAssignedStamp()+1);
				}
				resultMatrix.incrementValueAt(1, dependencySet.getDependedObject().getAssignedStamp(), dependencySet.getDependedObject().getAssignedStamp());

				Set<DependencyObject> dependentObjects = dependencySet.getDependenciesMap().keySet();
				
				for (DependencyObject dependentObject : dependentObjects) {
					if(dependentObject.getAssignedStamp() > (resultMatrix.getRows()-1)){
						resultMatrix.incrementMatrixRowsTo(dependentObject.getAssignedStamp()+1);
					}
					if(dependentObject.getAssignedStamp() > (resultMatrix.getColumns()-1)){
						resultMatrix.incrementMatrixColumnsTo(dependentObject.getAssignedStamp()+1);
					}
					
					if(dependencySet.getDependedObject().getAssignedStamp() == dependentObject.getAssignedStamp()){
						continue;
					}
					resultMatrix.incrementValueAt((Integer) dependencySet.getDependenciesMap().get(dependentObject), dependencySet.getDependedObject().getAssignedStamp(), dependentObject.getAssignedStamp());
					resultMatrix.incrementValueAt((Integer) dependencySet.getDependenciesMap().get(dependentObject), dependentObject.getAssignedStamp(), dependencySet.getDependedObject().getAssignedStamp());
				}
			}
		}	
	}

	/*
	 * Dependency TO Matrix
	 */
	
	
	
	/*
	 * LIST<DependencyObject> TO JUNGGraph
	 */
	
	
	/*
	 * LIST<Dependency> TO JUNGGraph
	 */
	
	public static final JUNGGraph convertDependenciesToJUNGGraph(final List<? extends DependencyObject> dependencies, final boolean isDirected) throws DatabaseException {

		final AbstractTypedGraph<JUNGVertex,JUNGEdge> graph;

		if(isDirected){
			graph = new DirectedSparseGraph();
		}
		else {
			graph = new UndirectedSparseGraph();
		}
		
		final JUNGGraph jungGraph = new JUNGGraph();
		jungGraph.setGraph(graph);
		return jungGraph;
	}
	
	
	/*
	 * JUNGGraph TO PrefuseGraph
	 */
	
	public static void convertJungToPrefuseGraph(final JUNGGraph convertee, PrefuseGraph converted){
		converted = Converter.convertJungToPrefuseGraph(convertee);
	}
	
	public static PrefuseGraph convertJungToPrefuseGraph(JUNGGraph convertee){
		
		final PrefuseGraph prefuseGraph = new PrefuseGraph();
		final HashMap<Long,Node> nodeList = new HashMap<Long,Node>();

		for (JUNGEdge edge : convertee.getGraph().getEdges()) {

			final Collection<JUNGVertex> vertexes = convertee.getGraph().getIncidentVertices(edge);
			for (Iterator<JUNGVertex> iterator = vertexes.iterator(); iterator.hasNext();) {
				final JUNGVertex v1 = (JUNGVertex) iterator.next();
				final JUNGVertex v2 = (JUNGVertex) iterator.next();

				Node n1 = nodeList.get(v1.getId());
				Node n2 = nodeList.get(v2.getId());

				if(n1 == null){
					if(v1.getName() == null){
						n1 = prefuseGraph.createNode(v1.getId(), "null");
					}
					else{
						n1 = prefuseGraph.createNode(v1.getId(), v1.getName());
					}
					nodeList.put(v1.getId(), n1);
				}
				if(n2 == null){
					if(v2.getName() == null){
						n2 = prefuseGraph.createNode(v2.getId(), "null");
					}
					else{
						n2 = prefuseGraph.createNode(v2.getId(), v2.getName());
					}
					nodeList.put(v2.getId(), n2);
				}

				final Edge prefuseEdge = prefuseGraph.getPrefuseGraph().addEdge(n1, n2);
				prefuseEdge.setLong("weight", edge.getWeight());

			}
		}
		
		return prefuseGraph;
	}
}