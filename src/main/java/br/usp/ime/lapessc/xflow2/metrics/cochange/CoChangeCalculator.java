package br.usp.ime.lapessc.xflow2.metrics.cochange;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.FileDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.database.DatabaseManager;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.Matrix;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class CoChangeCalculator {

	public List<CoChange> calculate(CoChangesAnalysis analysis) throws DatabaseException{
		FileDependencyObjectDAO fileDependencyDAO = 
			new FileDependencyObjectDAO();

		//Obtains the last dependency from the analysis
		DependencyGraph<FileDependencyObject,FileDependencyObject> lastDependency = 
			getLastDependency(analysis);
		
		//Builds the Co-Change Matrix
		Matrix matrix = analysis.processHistoricalDependencyMatrix(lastDependency);
		
		List<CoChange> coChangeList = getCoChangeList(analysis,
				fileDependencyDAO, matrix);
		
		System.out.println("CoChanges list size: " + coChangeList.size());
		
		//Persists Co-Changes
		for(CoChange coChange : coChangeList){
			insert(coChange);
			DatabaseManager.getDatabaseSession().clear();
		}
		
		return coChangeList;
	}

	private DependencyGraph<FileDependencyObject,FileDependencyObject> getLastDependency(Analysis analysis) throws DatabaseException{
		DependencyDAO dependencyDAO = new DependencyDAO();
		
		DependencyGraph<FileDependencyObject, FileDependencyObject> lastDependency = 
			dependencyDAO.findHighestDependencyByEntry(
				analysis.getId(), 
				analysis.getLastEntry().getId(), 
				DependencyGraph.TASK_DEPENDENCY);
		
		return lastDependency; 
	}

	private List<CoChange> getCoChangeList(CoChangesAnalysis analysis,
			FileDependencyObjectDAO fileDependencyDAO, Matrix matrix)
			throws DatabaseException {
		
		//Retrieves all file paths
		List<String> filePathList = 
			fileDependencyDAO.getFilePathsOrderedByStamp(analysis);
			
		//Builds the list of Co-Changes
		List<CoChange> coChangeList = new ArrayList<CoChange>();
		
		for(long[] coordinate : matrix.availableCoordinates()){
			
			long i = coordinate[0];
			long j = coordinate[1];
			
			//Coordinates on the right side of matrix diagonal			
			if(j > i){
				String a = filePathList.get((int)i);
				String b = filePathList.get((int)j);
				
				int support = matrix.getValueAt(i,j);
				int aChanges = matrix.getValueAt(i, i);
				int bChanges = matrix.getValueAt(j, j);
				
				coChangeList.add(new CoChange(a, (int)i, b, (int)j, support, aChanges));
				coChangeList.add(new CoChange(b, (int)j, a, (int)i, support, bChanges));
			}
		}
		
		return coChangeList;
	}
	
	private void insert(CoChange coChange){
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