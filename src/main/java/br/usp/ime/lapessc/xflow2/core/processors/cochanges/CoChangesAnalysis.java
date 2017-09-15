package br.usp.ime.lapessc.xflow2.core.processors.cochanges;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.PredicateUtils;

import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.AnalysisType;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraphType;
import br.usp.ime.lapessc.xflow2.entity.FileVersion;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.TaskDependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.cochange.CoChangeGraph;
import br.usp.ime.lapessc.xflow2.entity.cochange.CoChangeGraphEdge;
import br.usp.ime.lapessc.xflow2.entity.cochange.CoChangeGraphVertex;
import br.usp.ime.lapessc.xflow2.entity.dao.core.AnalysisDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyGraphDAO;
import br.usp.ime.lapessc.xflow2.entity.database.EntityManagerHelper;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.metrics.cochange.ChangeDependency;
import br.usp.ime.lapessc.xflow2.metrics.cochange.ConfidencePredicate;
import br.usp.ime.lapessc.xflow2.metrics.cochange.SupportCountPredicate;
import edu.uci.ics.jung.graph.util.Pair;

@Entity(name = "cochanges_analysis")
public class CoChangesAnalysis extends Analysis {
	
	public CoChangesAnalysis(){
		this.setType(AnalysisType.COCHANGES_ANALYSIS.getValue());
	}
	
	public Set<ChangeDependency> getChangeDependencies(
			int minSup, double minConf) throws DatabaseException{
		
		Set<ChangeDependency> changeDependencies = getChangeDependencies();
		System.out.println("All change deps: " + changeDependencies.size());
				
		System.out.println("Chosen support threshold: " + minSup);
		System.out.println("Chosen confidence threshold: " + minConf);
		
		CollectionUtils.filter(changeDependencies, 
				PredicateUtils.andPredicate(
						new SupportCountPredicate(minSup), 
						new ConfidencePredicate(minConf)));

		System.out.println(changeDependencies);
		System.out.println("Relevant change deps: " + changeDependencies.size()); 
				
		return changeDependencies;
	}
	
	public Set<ChangeDependency> getRelevantChangeDependencies() throws DatabaseException{
		
		Set<ChangeDependency> changeDependencies = getChangeDependencies();
		System.out.println("All change deps: " + changeDependencies.size());
		
		//First, we pick those dependencies whose value of conviction is 
		//at least 3.0 (but not infinite)
		double minConviction = 3.0;
		double minSupportCount = Integer.MAX_VALUE;
		
		Set<ChangeDependency> filteredChangeDeps = new HashSet<>();
		for(ChangeDependency changeDep : changeDependencies){
			if(changeDep.getConviction() >= minConviction && 
				!changeDep.getConviction().isInfinite()){
				
				filteredChangeDeps.add(changeDep);
				if(changeDep.getSupportCount() < minSupportCount){
					minSupportCount = changeDep.getSupportCount();
				}
			}
		}
		
		//Then, we capture only those dependencies with infinite conviction
		//and support count > minSupportCount
		for(ChangeDependency changeDep : changeDependencies){
			if(changeDep.getConviction().isInfinite() && 
				changeDep.getSupportCount() >= minSupportCount){
				
				filteredChangeDeps.add(changeDep);
			}
		}
		
		System.out.println("Relevant change deps: " + filteredChangeDeps.size()); 
				
		return filteredChangeDeps;
	}
	
	public Set<ChangeDependency> getChangeDependencies() throws DatabaseException{
		
		AnalysisDAO analysisDAO = new AnalysisDAO();
		int numberOfCommits = analysisDAO.getCommits(this).size();
		
		CoChangeGraph coChangeGraph = getCoChangeGraph();
		
		//System.out.println("Vertices: " + coChangeGraph.getVertexCount());
		//System.out.println("Edges: " + coChangeGraph.getEdgeCount());
		
		Set<ChangeDependency> changeDeps = new HashSet<ChangeDependency>();
		for(CoChangeGraphEdge coChangeGraphEdge : coChangeGraph.getEdges()){
			
			Pair<CoChangeGraphVertex> pair = coChangeGraphEdge.getEndpoints();
			
			if(!pair.getFirst().equals(pair.getSecond())){
				
				//Ignore change dependency if changed only once and 
				//either file was deleted
				boolean eitherOneDeletedAndSingleCoChange = false;
				if(coChangeGraphEdge.getCoChangeHistory().getCommits().size() == 1){
					
					Commit commit = 
							coChangeGraphEdge.getCoChangeHistory().getCommits().iterator().next();
					
					FileDependencyObject f1 = pair.getFirst().getFileDependencyObject();
					FileDependencyObject f2 = pair.getSecond().getFileDependencyObject();
					
					for(FileVersion fileArtifact : commit.getEntryFiles()){
						
						if(fileArtifact.getPath().equals(f1.getFilePath()) || 
							fileArtifact.getPath().equals(f2.getFilePath())){
							
							if(fileArtifact.getOperationType() == 'D'){
								eitherOneDeletedAndSingleCoChange = true;
							}
						}
					}
				}
				
				if(!eitherOneDeletedAndSingleCoChange){
				
					//TODO: Replace lhs and rhs with FileDependencyObject
					
					//Assoc rule: first --> second				
					ChangeDependency firstToSecondDep = new ChangeDependency(this,
							numberOfCommits,
							pair.getFirst().getFileDependencyObject().getFilePath(), 
							pair.getSecond().getFileDependencyObject().getFilePath(),
							coChangeGraph.findEdge(pair.getFirst(), pair.getFirst()).getCoChangeHistory().getCommits(), 
							coChangeGraph.findEdge(pair.getSecond(), pair.getSecond()).getCoChangeHistory().getCommits());
					
					//Assoc rule: second --> first				
					ChangeDependency secondToFirstDep = new ChangeDependency(this,
							numberOfCommits,
							pair.getSecond().getFileDependencyObject().getFilePath(),
							pair.getFirst().getFileDependencyObject().getFilePath(), 
							coChangeGraph.findEdge(pair.getSecond(), pair.getSecond()).getCoChangeHistory().getCommits(),
							coChangeGraph.findEdge(pair.getFirst(), pair.getFirst()).getCoChangeHistory().getCommits());
									
					changeDeps.add(firstToSecondDep);
					changeDeps.add(secondToFirstDep);
				}
			}
		}
		

		EntityManagerHelper.getEntityManager().clear();
		return changeDeps;
	}
	
	public CoChangeGraph getCoChangeGraph() throws DatabaseException {
		
		DependencyGraphDAO dependencyDAO = new DependencyGraphDAO();
		
		Collection<TaskDependencyGraph> taskDependencyGraphs = 
				dependencyDAO.findAllDependenciesByAnalysisAndType(
						this.getId(), DependencyGraphType.TASK_DEPENDENCY);
		
		CoChangeGraph coChangeGraph = new CoChangeGraph(taskDependencyGraphs);		
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
	
}
