package br.usp.ime.lapessc.xflow2.metrics.cochange;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import br.usp.ime.lapessc.xflow2.core.processors.callgraph.CallGraphAnalysis;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraphType;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyGraphDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.FileDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.database.EntityManagerHelper;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.IRealMatrix;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class StructuralCouplingCalculator {

	public List<StructuralCoupling> calculate(CallGraphAnalysis analysis) throws DatabaseException{
		FileDependencyObjectDAO fileDependencyDAO = 
			new FileDependencyObjectDAO();

		//Obtains the last dependency from the analysis
		DependencyGraph<FileDependencyObject,FileDependencyObject> lastDependency = 
			getLastDependency(analysis);
		
		//Builds the Co-Change Matrix
		//FIXME:
		//Matrix matrix = analysis.processHistoricalDependencyMatrix(lastDependency);
		IRealMatrix matrix = null;
				
		List<StructuralCoupling> couplingsList = 
			getStructuralCouplinsList(analysis,fileDependencyDAO, matrix);
		
		System.out.println("Couplings list: " + couplingsList.size());
		
		//Persists Couplings
		for(StructuralCoupling coupling : couplingsList){
			insert(coupling);
			EntityManagerHelper.getEntityManager().clear();
		}
		
		return couplingsList;
	}

	private DependencyGraph<FileDependencyObject,FileDependencyObject> getLastDependency(Analysis analysis) throws DatabaseException{
		DependencyGraphDAO dependencyDAO = new DependencyGraphDAO();
		
		DependencyGraph<FileDependencyObject, FileDependencyObject> lastDependency = 
			dependencyDAO.findHighestDependencyByEntry(
				analysis.getId(), 
				analysis.getLastEntry().getId(), 
				DependencyGraphType.TASK_DEPENDENCY.getValue());
		
		return lastDependency; 
	}

	private List<StructuralCoupling> getStructuralCouplinsList(CallGraphAnalysis analysis,
			FileDependencyObjectDAO fileDependencyDAO, IRealMatrix matrix)
			throws DatabaseException {
		
		/**
		
		//Retrieves all file paths
		List<String> filePathList = 
			fileDependencyDAO.getFilePathsOrderedByStamp(analysis);
			
		//Builds the couplingsList
		List<StructuralCoupling> couplingsList = 
				new ArrayList<StructuralCoupling>();
		
		for(long[] coordinate : matrix.availableCoordinates()){
			
			Long i = coordinate[0];
			Long j = coordinate[1];
			
			if (i != j){
				String supplier = filePathList.get(i.intValue());
				int clientCalls = matrix.getValueAt(i,j);
				
				if(clientCalls > 0){
					String client = filePathList.get(j.intValue());
					int clientChanges = matrix.getValueAt(j,j);
					
					couplingsList.add(new StructuralCoupling(client, 
							j.intValue(), supplier, i.intValue(), 
							clientCalls, clientChanges));
				}
			}
		}
		*/
		
		return null;
	}
	
	private void insert(StructuralCoupling coupling){
		try {
			EntityManager manager = EntityManagerHelper.getEntityManager();
			manager.getTransaction().begin();
			manager.persist(coupling);
			manager.getTransaction().commit();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}