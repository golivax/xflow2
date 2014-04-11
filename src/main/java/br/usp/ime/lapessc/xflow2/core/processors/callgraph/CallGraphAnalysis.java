package br.usp.ime.lapessc.xflow2.core.processors.callgraph;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import br.usp.ime.lapessc.xflow2.core.AnalysisFactory;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencySet;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencySetDAO;
import br.usp.ime.lapessc.xflow2.entity.representation.Converter;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGGraph;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.Matrix;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.MatrixFactory;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

@Entity(name = "callgraph_analysis")
@DiscriminatorValue(""+AnalysisFactory.CALLGRAPH_ANALYSIS)
public class CallGraphAnalysis extends Analysis{

	@Transient
	private Matrix matrixCache = null;
	
	@Column(name = "is_whole_system_snapshot")
	private boolean wholeSystemSnapshot;
	
	@Transient
	private DependencyGraph dependencyCache = null;
	
	@Transient
	private JUNGGraph graphCache = null;
	
	public CallGraphAnalysis(){
		this.setType(AnalysisFactory.CALLGRAPH_ANALYSIS);
		this.setWholeSystemSnapshot(false);
	}
	
	@Override
	public JUNGGraph processEntryDependencyGraph(Commit entry, int dependencyType)
			throws DatabaseException {
		final Matrix matrix;
		DependencyGraph dependency = new DependencyDAO().findDependencyByEntry(this.getId(), entry.getId(), dependencyType);
		
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
			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, dependency);
//			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, dependency, graphCache);
		}
		
		return graphCache;
	}

	private Matrix processDependencyMatrix(DependencyGraph dependency) {
		Matrix matrix = MatrixFactory.createMatrix();
		
		if(!dependency.getDependencies().isEmpty()){
			Converter.convertDependenciesToMatrix(matrix, new ArrayList<DependencySet>(dependency.getDependencies()), dependency.isDirectedDependency());
		}
		
		return matrix;
	}

	@Override
	public JUNGGraph processDependencyGraph(DependencyGraph entryDependency)
			throws DatabaseException {
		final Matrix matrix;
		DependencyGraph dependency = new DependencyDAO().findDependencyByEntry(this.getId(), entryDependency.getAssociatedEntry().getId(), entryDependency.getType());
		
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
			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, dependency);
//			graphCache = JUNGGraph.convertMatrixToJUNGGraph(processedMatrix, dependency, graphCache);
		}
		
		return graphCache;
	}

	@Override
	public Matrix getDependencyMatrixForEntry(Commit entry, int dependencyType)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkCutoffValues(Commit entry) {
		if(this.getMaxFilesPerRevision() <= 0) return true;
		if(entry.getEntryFiles().size() > this.getMaxFilesPerRevision()){
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public final Matrix processHistoricalDependencyMatrix(final DependencyGraph dependency) throws DatabaseException {
		
		if(!dependency.getDependencies().isEmpty()){
			final List<Long> dependencySetsIds = new DependencySetDAO().getAllDependenciesSetUntilDependency(dependency);
			Matrix matrix = MatrixFactory.createMatrix();
			Converter.convertDependenciesToLargeMatrix(matrix, dependencySetsIds, true);
			return matrix;
		}
		else{
			return matrixCache;
		}
	}

	public void setWholeSystemSnapshot(boolean wholeSystemSnapshot) {
		this.wholeSystemSnapshot = wholeSystemSnapshot;
	}

	public boolean isWholeSystemSnapshot() {
		return wholeSystemSnapshot;
	}

//	public void setWholeSystemSnapshot(boolean wholeSystemSnapshot) {
//		this.wholeSystemSnapshot = wholeSystemSnapshot;
//	}
//
//	public boolean isWholeSystemSnapshot() {
//		return wholeSystemSnapshot;
//	}


}
