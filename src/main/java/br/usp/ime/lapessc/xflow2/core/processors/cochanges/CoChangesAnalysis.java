package br.usp.ime.lapessc.xflow2.core.processors.cochanges;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import br.usp.ime.lapessc.xflow2.core.AnalysisFactory;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.CoChangeHistory;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraphType;
import br.usp.ime.lapessc.xflow2.entity.DependencySet;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.RawDependency;
import br.usp.ime.lapessc.xflow2.entity.TaskDependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencySetDAO;
import br.usp.ime.lapessc.xflow2.entity.representation.Converter;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGGraph;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.Matrix;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.MatrixFactory;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

@Entity(name = "cochanges_analysis")
public final class CoChangesAnalysis extends Analysis {

	@Transient
	private Matrix matrixCache = null;
	
	@Transient
	private DependencyGraph dependencyCache = null;
	
	@Transient
	private JUNGGraph graphCache = null;
	
	public CoChangesAnalysis(){
		this.setType(AnalysisFactory.COCHANGES_ANALYSIS);
	}
	
	//Co-Change graph
	public UndirectedGraph<FileDependencyObject, CoChangeHistory> getCoChangeGraph()
			throws DatabaseException {
		
		int edgeCount = 0;
		
		DependencyDAO dependencyDAO = new DependencyDAO();
		
		UndirectedSparseGraph<FileDependencyObject, CoChangeHistory> coChangeGraph = 
				new UndirectedSparseGraph<FileDependencyObject, CoChangeHistory>();
		
		List<DependencyGraph> dependencyGraphs = 
				dependencyDAO.findAllDependenciesByAnalysis(
						this.getId(), DependencyGraphType.TASK_DEPENDENCY.getValue());
		
		for(DependencyGraph dependencyGraph : dependencyGraphs){
			
			TaskDependencyGraph taskDependency = (TaskDependencyGraph) dependencyGraph;
			for(RawDependency<FileDependencyObject,FileDependencyObject,Commit> rawDep : taskDependency.getRawDependencies()){
				
				CoChangeHistory coChangeHistory = coChangeGraph.findEdge(rawDep.getClient(),rawDep.getSupplier());
				if (coChangeHistory == null){
					edgeCount++;
					coChangeHistory = new CoChangeHistory(edgeCount);
					coChangeHistory.addCommit(rawDep.getLabel());
					coChangeGraph.addEdge(coChangeHistory, rawDep.getClient(), rawDep.getSupplier());
				}
				else{
					coChangeHistory.addCommit(rawDep.getLabel());
				}
			}
		}
		
		return coChangeGraph;
	}
	
	@Override
	public boolean checkCutoffValues(Commit entry) {
		if(this.getMaxFilesPerRevision() <= 0) return true;
		if(entry.getEntryFiles().size() > this.getMaxFilesPerRevision()){
			return false;
		}
		return true;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Matrix getDependencyMatrixForEntry(Commit entry, int dependencyType) throws DatabaseException {
		final Matrix matrix;
		DependencyGraph dependency = 
				new DependencyDAO().findDependencyByEntry(
						this.getId(), entry.getId(), dependencyType);
		
		if(dependency == null){
			if(dependencyCache != null){
				return matrixCache;
			}
			else{
				return null;
			}
		}
		
//		if(matrixCache == null){
			matrix = processHistoricalDependencyMatrix(dependency);
//			matrixCache = matrix;
//			dependencyCache = dependency;
//		}
//		else{
//			Matrix processedMatrix = processDependencyMatrix(dependency);
//			matrix = processedMatrix.sumDifferentOrderMatrix(matrixCache);
//			matrixCache = matrix;
//			dependencyCache = dependency;
//		}
		
		return matrix;
	}

	
	@Override
	@SuppressWarnings("rawtypes")
	public final JUNGGraph processEntryDependencyGraph(
			final Commit entry, final int dependencyType) 
					throws DatabaseException {
		
		final Matrix matrix;
		DependencyGraph dependency = new DependencyDAO().findDependencyByEntry(
				this.getId(), entry.getId(), dependencyType);
		
		if(dependency == null){
			if(dependencyCache != null){
				return graphCache;
			}
			else{
				return null;
			}
		}
		
		if(matrixCache == null){
			matrix = processHistoricalDependencyMatrix(dependency);
			matrixCache = matrix;
			dependencyCache = dependency;
			graphCache = JUNGGraph.convertMatrixToJUNGGraph(matrix, dependency);
		}
		else{
			Matrix processedMatrix = processDependencyMatrix(dependency);
//			matrix = processedMatrix.sumDifferentOrderMatrix(matrixCache);
//			matrixCache = matrix;
//			dependencyCache = dependency;
			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, dependency, graphCache);
		}
		
		return graphCache;
	}
	
	@SuppressWarnings("rawtypes")
	public final JUNGGraph processDependencyGraph(final DependencyGraph entryDependency) throws DatabaseException {
		final Matrix matrix;
		
		if(entryDependency == null){
			if(dependencyCache != null){
				return graphCache;
			}
			else{
				return null;
			}
		}
		
		if(matrixCache == null){
			matrix = processHistoricalDependencyMatrix(entryDependency);
			matrixCache = matrix;
			dependencyCache = entryDependency;
			graphCache = JUNGGraph.convertMatrixToJUNGGraph(matrix, entryDependency);
		}
		else{
			Matrix processedMatrix = processDependencyMatrix(entryDependency);
//			matrix = processedMatrix.sumDifferentOrderMatrix(matrixCache);
//			matrixCache = matrix;
//			dependencyCache = dependency;
			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, entryDependency, graphCache);
		}
		
		return graphCache;
	}
	
	@SuppressWarnings("rawtypes")
	public final JUNGGraph processDependencyGraph2(final DependencyGraph entryDependency) throws DatabaseException {
		final Matrix matrix;
		
		if(entryDependency == null){
			if(dependencyCache != null){
				return graphCache;
			}
			else{
				return null;
			}
		}
		
		if(matrixCache == null){
			matrix = processHistoricalDependencyMatrix(entryDependency);
			matrixCache = matrix;
			dependencyCache = entryDependency;
			graphCache = JUNGGraph.convertMatrixToJUNGGraph(matrix, entryDependency);
		}
		else{
			Matrix processedMatrix = processDependencyMatrix(entryDependency);
//			matrix = processedMatrix.sumDifferentOrderMatrix(matrixCache);
//			matrixCache = matrix;
//			dependencyCache = dependency;
			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, entryDependency, graphCache);
		}
		
		return graphCache;
	}
	
	@SuppressWarnings("rawtypes")
	public final JUNGGraph processDependencyGraph3(final DependencyGraph initialEntryDependency, final DependencyGraph finalEntryDependency) throws DatabaseException {
		Matrix matrix = MatrixFactory.createMatrix();
		
		if(finalEntryDependency == null){
			if(dependencyCache != null){
				return graphCache;
			}
			else{
				return null;
			}
		}
		
		if(matrixCache == null){
			List<Long> dependencySetsIds = new DependencySetDAO().getAllDependenciesSetBetweenDependencies(initialEntryDependency, finalEntryDependency);
			matrix = processHistoricalDependencyMatrix2(finalEntryDependency, dependencySetsIds);
			matrixCache = matrix;
			dependencyCache = finalEntryDependency;
			graphCache = JUNGGraph.convertMatrixToJUNGGraph(matrix, finalEntryDependency);
		}
		else{
			Matrix processedMatrix = processDependencyMatrix(finalEntryDependency);
//			matrix = processedMatrix.sumDifferentOrderMatrix(matrixCache);
//			matrixCache = matrix;
//			dependencyCache = dependency;
			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, finalEntryDependency, graphCache);
		}
		
//		if(matrixCache == null){
//			List<Dependency> dependencies = new DependencyDAO().findDependenciesBetweenDependencies(initialEntryDependency, finalEntryDependency);
//			for (Dependency dependency : dependencies) {
//				matrix = matrix.sumDifferentOrderMatrix(processDependencyMatrix(dependency));
//			}
//			matrixCache = matrix;	
//			dependencyCache = finalEntryDependency;
//			graphCache = JUNGGraph.convertMatrixToJUNGGraph(matrix, finalEntryDependency);
//		}
//		else{
//			Matrix processedMatrix = processDependencyMatrix(finalEntryDependency);
////			matrix = processedMatrix.sumDifferentOrderMatrix(matrixCache);
////			matrixCache = matrix;
////			dependencyCache = dependency;
//			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, finalEntryDependency, graphCache);
//			System.out.println("?");
//		}
		
		return graphCache;
	}
	
	@SuppressWarnings("rawtypes")
	public final Matrix processHistoricalDependencyMatrix(final DependencyGraph dependencyGraph) throws DatabaseException {
		if(!dependencyGraph.getDependencies().isEmpty()){
			final List<Long> dependencySetsIds = new DependencySetDAO().getAllDependenciesSetUntilDependency(dependencyGraph);
			Matrix matrix = MatrixFactory.createMatrix();
			Converter.convertDependenciesToLargeMatrix(matrix, dependencySetsIds, dependencyGraph.isDirectedDependency());
			return matrix;
		}
		else{
			return matrixCache;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public final Matrix processHistoricalDependencyMatrix(final DependencyGraph initialDependency, final DependencyGraph finalDependency) throws DatabaseException {
		List<Long> dependencies = new DependencySetDAO().getAllDependenciesSetBetweenDependencies(initialDependency, finalDependency);
		Matrix matrix = MatrixFactory.createMatrix();
		Converter.convertDependenciesToLargeMatrix(matrix, dependencies, initialDependency.isDirectedDependency());
		return matrix;
	}
	
	@SuppressWarnings("rawtypes")
	private final Matrix processHistoricalDependencyMatrix2(DependencyGraph dependency, List<Long> dependencySetsIds) throws DatabaseException {
		Matrix matrix = MatrixFactory.createMatrix();
		Converter.convertDependenciesToLargeMatrix(matrix, dependencySetsIds, dependency.isDirectedDependency());
		return matrix;
	}

	@SuppressWarnings("rawtypes")
	public final Matrix processDependencyMatrix(final DependencyGraph dependency) throws DatabaseException {
		Matrix matrix = MatrixFactory.createMatrix();
		
		if(!dependency.getDependencies().isEmpty()){
			Converter.convertDependenciesToMatrix(matrix, new ArrayList<DependencySet>(dependency.getDependencies()), dependency.isDirectedDependency());
		}
		return matrix;
	}
}
