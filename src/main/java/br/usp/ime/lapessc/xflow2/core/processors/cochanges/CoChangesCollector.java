package br.usp.ime.lapessc.xflow2.core.processors.cochanges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.usp.ime.lapessc.xflow2.core.processors.DependenciesIdentifier;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.DependencyObject;
import br.usp.ime.lapessc.xflow2.entity.DependencySet;
import br.usp.ime.lapessc.xflow2.entity.FileArtifact;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.TaskDependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.ArtifactDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyGraphDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.FileDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.database.DatabaseManager;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.repository.vcs.dao.CommitDAO;
import br.usp.ime.lapessc.xflow2.util.Filter;

public final class CoChangesCollector implements DependenciesIdentifier {

	private CoChangesAnalysis analysis;
	
	private Map<String, DependencyObject> dependencyObjectsCache;
	
	private Filter filter;
	
	@Override
	public final void identifyDependencies(List<Long> revisions, Analysis analysis, 
			Filter filter) throws DatabaseException {
		
		this.analysis = (CoChangesAnalysis) analysis;
		this.filter = filter;
		
		CommitDAO commitDAO = new CommitDAO();
		DependencyGraphDAO dependencyDAO = new DependencyGraphDAO();
		
		System.out.println("** Starting CoChanges analysis **");
	
		for (Long revision : revisions) {
			
			final Commit commit = commitDAO.findEntryFromRevision(
					analysis.getProject(), revision);			
			
			System.out.print(
					"- Processing commit: " + commit.getRevision() + 
					" (" + revision + ")\n");
			
			System.out.print("* Collecting task dependencies...");
					
			final Set<DependencySet<FileDependencyObject, FileDependencyObject>> fileToFileDependencies = 
					gatherFileToFileDependencies(commit.getEntryFiles());
			
			if(fileToFileDependencies.size() > 0) { 
				
				final TaskDependencyGraph taskDependency = new TaskDependencyGraph(false);
				taskDependency.setAssociatedAnalysis(analysis);
				taskDependency.setAssociatedEntry(commit);
				taskDependency.setDependencies(fileToFileDependencies); //Sets on both sides (bi-directional association)
						
				dependencyDAO.insert(taskDependency);						
				System.out.print(" done!\n");
			} else {
				System.out.println("\nSkipped. No dependency collected on specified entry.");
			}
			//FIXME:
			//As we don't have an application layer yet, it is necessary 
			//to frequently clear the persistence context to avoid memory issues
			DatabaseManager.getDatabaseSession().clear();
		}
	}

	private Set<DependencySet<FileDependencyObject, FileDependencyObject>> gatherFileToFileDependencies(List<FileArtifact> changedFiles) throws DatabaseException {
		
		FileDependencyObjectDAO dependencyObjDAO = 
				new FileDependencyObjectDAO();
		
		List<FileDependencyObject> fileDependencyObjectList = 
				new ArrayList<FileDependencyObject>();

		//Builds the list of dependency objects
		for (FileArtifact changedFile : changedFiles) {
			if(filter.match(changedFile.getPath())){
				
				FileDependencyObject fileDependencyObject;
				
				//Added file
				if(changedFile.getOperationType() == 'A'){
					
					fileDependencyObject = new FileDependencyObject();
					fileDependencyObject.setAnalysis(analysis);
					fileDependencyObject.setFile(changedFile);
					fileDependencyObject.setFilePath(changedFile.getPath());
					
					dependencyObjDAO.insert(fileDependencyObject);
				
				//Modified or deleted file
				} else {
					
					//Search database for matching FileDependencyObject 
					fileDependencyObject = 
						dependencyObjDAO.findLastDependencyObjectByFilePath(
								analysis, changedFile.getPath());
					
					if (fileDependencyObject == null){
						FileArtifact addedFileReference = 
							new ArtifactDAO().findAddedFileByPathUntilEntry(
									analysis.getProject(), 
									changedFile.getCommit(), 
									changedFile.getPath());
						
						if(addedFileReference != null){

							fileDependencyObject = new FileDependencyObject();
							fileDependencyObject.setAnalysis(analysis);
							fileDependencyObject.setFile(changedFile);
							fileDependencyObject.setFilePath(changedFile.getPath());
							
							dependencyObjDAO.insert(fileDependencyObject);
						}
					}
				}
				
				if(fileDependencyObject != null){
					fileDependencyObjectList.add(fileDependencyObject);
				}
			}
		}

		//Builds the set of DependencySets
		Set<DependencySet<FileDependencyObject, FileDependencyObject>> setOfDependencySets = 
				new HashSet<DependencySet<FileDependencyObject, FileDependencyObject>>();
		
		for (int i = 0; i < fileDependencyObjectList.size(); i++) {
			
			FileDependencyObject supplier = fileDependencyObjectList.get(i);
			
			//Each entry denotes a dependency from a client (key) to the 
			//supplier with a certain strength (value)
			Map<FileDependencyObject, Integer> dependenciesMap = 
				new HashMap<FileDependencyObject, Integer>();
			
			for (int j = i; j < fileDependencyObjectList.size(); j++) {
				FileDependencyObject client = fileDependencyObjectList.get(j);
				dependenciesMap.put(client, 1);
			}
			
			DependencySet<FileDependencyObject, FileDependencyObject> dependencySet = 
				new DependencySet<FileDependencyObject, FileDependencyObject>();
			
			dependencySet.setSupplier(supplier);
			dependencySet.setClientsMap(dependenciesMap);
			
			setOfDependencySets.add(dependencySet);
		}
		
		return setOfDependencySets;
	}
	
}
