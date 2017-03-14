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
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.IRealMatrix;
import br.usp.ime.lapessc.xflow2.entity.representation.prefuse.PrefuseGraph;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import edu.uci.ics.jung.graph.AbstractTypedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public final class Converter {
	
	/**
	public static void convertDependenciesToLargeMatrix(Matrix resultMatrix, List<Long> dependencySetsIds, boolean isDependencyDirected) {
		
		try{
			DependencySetDAO dependencySetDAO = new DependencySetDAO();
			
			//100 load
			int bulkLoad = 100;
			
			for(int i = 0; (i + bulkLoad - 1) < dependencySetsIds.size(); i += bulkLoad){
				int startIndex = i;
				int endIndex = i + bulkLoad - 1;

				System.out.println("Processing dependecySets from " + startIndex + " to " + endIndex);
				
				List<DependencySet> dependencySets = dependencySetDAO.findByIds(dependencySetsIds.subList(startIndex, endIndex + 1));
				convertDependenciesToMatrix(resultMatrix, dependencySets, isDependencyDirected);
				DatabaseManager.getDatabaseSession().clear();
			}
				
			int rest = dependencySetsIds.size() % bulkLoad;
			if (rest != 0){
				
				int endIndex = dependencySetsIds.size() - 1;
				int startIndex = endIndex - rest + 1;
				
				System.out.println("Processing dependecySets from " + startIndex + " to " + endIndex);
				
				List<DependencySet> dependencySets = dependencySetDAO.findByIds(dependencySetsIds.subList(startIndex, endIndex + 1));
				convertDependenciesToMatrix(resultMatrix, dependencySets, isDependencyDirected);
				DatabaseManager.getDatabaseSession().clear();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
				
	}
	*/
		
	/*
	 * LIST<DependencyObject> TO MATRIX
	 */
	
	/**
	//Cria a matriz com base na caralhada de dependency sets
	//IMPORTANTE: Os stamps sao usados pra indexar as linhas e colunas
	public static void convertDependenciesToMatrix(Matrix resultMatrix, List<DependencySet> dependencySets, boolean isDependencyDirected) {

		
		
		if(isDependencyDirected){
			for (DependencySet dependencySet : dependencySets) {
				
				DependencyObject supplier = dependencySet.getSupplier();
				int supplierStamp = supplier.getId().intValue();
				
				if(supplierStamp > (resultMatrix.getRows() - 1)){
					resultMatrix.incrementMatrixRowsTo(supplierStamp + 1);
				}

				Set<DependencyObject> clients = dependencySet.getClients();

				for (DependencyObject client : clients) {
					
					int clientStamp = client.getId().intValue();
					
					if(clientStamp > (resultMatrix.getColumns() - 1)){
						resultMatrix.incrementMatrixColumnsTo(clientStamp + 1);
					}
					
					Integer coupling = (Integer) dependencySet.getClientsMap().get(client);
					resultMatrix.incrementValueAt(coupling, clientStamp, supplierStamp);
				}
			}
		}
		else{
			for (DependencySet dependencySet : dependencySets) {
				
				DependencyObject supplier = dependencySet.getSupplier();
				int supplierStamp = supplier.getAssignedStamp();
				
				if(supplierStamp > (resultMatrix.getRows() - 1)){
					resultMatrix.incrementMatrixRowsTo(supplierStamp + 1);
				}
				if(supplierStamp > (resultMatrix.getColumns() - 1)){
					resultMatrix.incrementMatrixColumnsTo(supplierStamp + 1);
				}
				
				resultMatrix.incrementValueAt(1, supplierStamp, supplierStamp);

				Set<DependencyObject> clients = dependencySet.getClients();
				
				for (DependencyObject client : clients) {
					
					int clientStamp = client.getAssignedStamp();
					
					if(clientStamp > (resultMatrix.getRows() - 1)){
						resultMatrix.incrementMatrixRowsTo(clientStamp + 1);
					}
					if(clientStamp > (resultMatrix.getColumns() - 1)){
						resultMatrix.incrementMatrixColumnsTo(clientStamp + 1);
					}
					
					if(clientStamp == supplierStamp){
						continue;
					}
					
					Integer coupling = (Integer) dependencySet.getClientsMap().get(client);
					
					resultMatrix.incrementValueAt(coupling, clientStamp, supplierStamp);
					resultMatrix.incrementValueAt(coupling, supplierStamp, clientStamp);
				}
			}
		}	
	}
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

				final Edge prefuseEdge = prefuseGraph.addEdge(n1, n2);
				prefuseEdge.setLong("weight", edge.getWeight());

			}
		}
		
		return prefuseGraph;
	}
}