package br.usp.ime.lapessc.xflow2.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import br.usp.ime.lapessc.xflow2.core.processors.coordreq.CoordReqsAnalysis;
import br.usp.ime.lapessc.xflow2.entity.dao.core.AuthorDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyGraphDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.FileDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.database.DatabaseManager;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.IRealMatrix;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.sparse.ApacheSparseMatrixWrapper;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class CoordReqsMatrixFactory {

	public CoordinationRequirementsMatrix build(
			CoordReqsAnalysis coordReqAnalysis) throws DatabaseException{

		//1) Retrieve all authorDepObjOs in coord-req analysis and map them to rows
		Map<Author, Integer> authorToRowNumber = new HashMap<>();
		AuthorDependencyObjectDAO authorDepObjDAO = new AuthorDependencyObjectDAO();
		List<AuthorDependencyObject> authDepObjs = authorDepObjDAO.findAllByAnalysis(coordReqAnalysis);

		for(int i = 0; i < authDepObjs.size(); i++){
			authorToRowNumber.put(authDepObjs.get(i).getAuthor(), i);
		}

		int numWorkers = authDepObjs.size();

		//2) Retrieve all fileDepObj in co-changes analysis and map them to cols 
		Map<Long, Integer> fileDepObjIdToColNumber = new HashMap<>();
		FileDependencyObjectDAO fileDepObjDAO = new FileDependencyObjectDAO();
		List<FileDependencyObject> fileDepObjs = fileDepObjDAO.findAllByAnalysis(coordReqAnalysis.getCoChangesAnalysis());

		for(int i = 0; i < fileDepObjs.size(); i++){
			fileDepObjIdToColNumber.put(fileDepObjs.get(i).getId(), i);
		}

		int numTasks = fileDepObjs.size();		

		//FIXME:
		//As we don't have an application layer yet, it is necessary 
		//to frequently clear the persistence context to avoid memory issues
		DatabaseManager.getDatabaseSession().clear();

		IRealMatrix ta = new ApacheSparseMatrixWrapper(numWorkers, numTasks);

		DependencyGraphDAO dependencyDAO = new DependencyGraphDAO();

		Collection<TaskAssignmentGraph> taskAssignmentGraphs = 
				dependencyDAO.findAllDependenciesByAnalysisAndType(
						coordReqAnalysis.getId(), DependencyGraphType.TASK_ASSIGNMENT);

		for(TaskAssignmentGraph taskAssignmentGraph : taskAssignmentGraphs){
			for(RawDependency<FileDependencyObject, AuthorDependencyObject, Commit> rawDep : taskAssignmentGraph.getRawDependencies()){

				Integer rowIndex = authorToRowNumber.get(rawDep.getSupplier().getAuthor());
				Integer colIndex = fileDepObjIdToColNumber.get(rawDep.getClient().getId());

				ta.incrementValueAt(1, rowIndex, colIndex);
			}
		}

		IRealMatrix td = new ApacheSparseMatrixWrapper(numTasks, numTasks);

		Collection<TaskDependencyGraph> taskDependencyGraphs = 
				dependencyDAO.findAllDependenciesByAnalysisAndType(
						coordReqAnalysis.getCoChangesAnalysis().getId(), 
						DependencyGraphType.TASK_DEPENDENCY);

		for(TaskDependencyGraph taskDependencyGraph : taskDependencyGraphs){
			for(RawDependency<FileDependencyObject, FileDependencyObject, Commit> rawDep : taskDependencyGraph.getRawDependencies()){

				Integer rowIndex = fileDepObjIdToColNumber.get(rawDep.getClient().getId());
				Integer colIndex = fileDepObjIdToColNumber.get(rawDep.getSupplier().getId());

				if(rowIndex == colIndex) continue;
				
				td.incrementValueAt(1, rowIndex, colIndex);
				td.incrementValueAt(1, colIndex, rowIndex);				
			}
		}

		IRealMatrix coordReqsMatrix = ta.multiply(td).multiply(ta.getTransposeMatrix());

		Map<Integer,Author> rowToAuthorMap = 
				MapUtils.invertMap(authorToRowNumber);
		
		CoordinationRequirementsMatrix coordReqMatrix = 
				new CoordinationRequirementsMatrix(
						coordReqsMatrix,rowToAuthorMap);
		
		return coordReqMatrix;
	}
	
}
