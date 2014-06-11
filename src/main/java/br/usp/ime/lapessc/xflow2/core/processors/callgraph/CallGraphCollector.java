package br.usp.ime.lapessc.xflow2.core.processors.callgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.usp.ime.lapessc.xflow2.core.processors.DependenciesIdentifier;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.AuthorDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.DependencyObject;
import br.usp.ime.lapessc.xflow2.entity.DependencySet;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.FileArtifact;
import br.usp.ime.lapessc.xflow2.entity.TaskDependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.ArtifactDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.AuthorDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.FileDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.database.DatabaseManager;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.repository.vcs.dao.CommitDAO;
import br.usp.ime.lapessc.xflow2.util.Filter;

public class CallGraphCollector implements DependenciesIdentifier {

	private CallGraphAnalysis analysis;	
	private Filter filter;
	private int latestFileStampAssigned;
	private int latestAuthorStampAssigned;
	
	private Map<String, AuthorDependencyObject> authorsStampsMap;
	
	@Override
	public final void dataCollect(List<Long> revisions, Analysis analysis, Filter filter) throws DatabaseException {
		this.analysis = (CallGraphAnalysis) analysis;
		this.filter = filter;
		initiateCache();
		
		ArtifactDAO artifactDAO = new ArtifactDAO();
		CommitDAO entryDAO = new CommitDAO();
		DependencyDAO dependencyDAO = new DependencyDAO();
		
		System.out.println("** Starting Structural analysis **");
		
		for (Long revision : revisions) {
		
			final Commit entry = entryDAO.findEntryFromRevision(analysis.getProject(), revision);

			System.out.print("- Processing entry: "+entry.getRevision()+" ("+revision+")\n");
			System.out.print("* Collecting file dependencies...");
			
			final List<FileArtifact> studiedFiles;
			if(this.analysis.isWholeSystemSnapshot()){
				if(this.analysis.isTemporalConsistencyForced()){
					studiedFiles = artifactDAO.getAllAddedFilesUntilEntry(entry.getVcsMiningProject(), entry);
				} else {
					studiedFiles = artifactDAO.getAllAddedFilesUntilRevision(entry.getVcsMiningProject(), entry.getRevision());
				}
			} else {
				studiedFiles = entry.getEntryFiles();
			}
			
			final Set<DependencySet<FileDependencyObject, FileDependencyObject>> structuralDependencies;
			structuralDependencies = gatherStructuralDependenciesOld(studiedFiles);
			
			/**
			if(this.analysis.isWholeSystemSnapshot()){
				structuralDependencies = gatherStructuralDependencies(studiedFiles, entry, true);
			} else {
				structuralDependencies = gatherStructuralDependencies(studiedFiles, entry);
			}
			
			System.out.println("* Collecting author dependencies...");
			
			final Set<DependencySet<AuthorDependencyObject, FileDependencyObject>> taskAssignmentDependencies;
			if(this.analysis.isWholeSystemSnapshot()){
				taskAssignmentDependencies = gatherWholeTaskAssignmentDependencies(entry);
			} else {
				taskAssignmentDependencies = gatherTaskAssignmentDependencies(entry.getAuthor(), structuralDependencies);
			}
			*/
			
			if(structuralDependencies.size() > 0) { 
				final TaskDependencyGraph taskDependency = new TaskDependencyGraph(true);
				taskDependency.setAssociatedAnalysis(analysis);
				taskDependency.setAssociatedEntry(entry);
				
				/**
				final TaskAssignment taskAssignment = new TaskAssignment();
				taskAssignment.setAssociatedAnalysis(analysis);
				taskAssignment.setAssociatedEntry(entry);
				*/
	
				//Sets on both sides (bi-directional association)
				taskDependency.setDependencies(structuralDependencies);
				//taskAssignment.setDependencies(taskAssignmentDependencies);
								
				dependencyDAO.insert(taskDependency);
				//dependencyDAO.insert(taskAssignment);
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

	private void initiateCache() throws DatabaseException {
		latestFileStampAssigned = new FileDependencyObjectDAO().checkHighestStamp(analysis);
		latestAuthorStampAssigned = new AuthorDependencyObjectDAO().checkHighestStamp(analysis);
		
		authorsStampsMap = new HashMap<String, AuthorDependencyObject>();
	}
	
	private Set<DependencySet<FileDependencyObject, FileDependencyObject>> gatherStructuralDependenciesOld(List<FileArtifact> changedFiles) throws DatabaseException {
		FileDependencyObjectDAO dependencyObjDAO = new FileDependencyObjectDAO();
	
		//Builds the list of dependency objects
		List<FileDependencyObject> dependencyObjectList = new ArrayList<FileDependencyObject>();
		
		for (FileArtifact changedFile : changedFiles) {
			if(filter.match(changedFile.getPath())){
				FileDependencyObject dependencyObject;
				
				//Added file
				if(changedFile.getOperationType() == 'A'){
					latestFileStampAssigned++;
					
					dependencyObject = new FileDependencyObject();
					dependencyObject.setAnalysis(analysis);
					dependencyObject.setFile(changedFile);
					dependencyObject.setFilePath(changedFile.getPath());
					dependencyObject.setAssignedStamp(latestFileStampAssigned);
					
					dependencyObjDAO.insert(dependencyObject);
				
				//Modified or deleted file
				} else {
					
					//Search database for matching FileDependencyObject 
					dependencyObject = 
						dependencyObjDAO.findLastDependencyObjectByFilePath(analysis, changedFile.getPath());
					
					if (dependencyObject == null){
						FileArtifact addedFileReference = 
							new ArtifactDAO().findAddedFileByPathUntilEntry(
									analysis.getProject(), 
									changedFile.getCommit(), 
									changedFile.getPath());
						
						if(addedFileReference != null){
							latestFileStampAssigned++;

							dependencyObject = new FileDependencyObject();
							dependencyObject.setAnalysis(analysis);
							dependencyObject.setFile(changedFile);
							dependencyObject.setFilePath(changedFile.getPath());
							dependencyObject.setAssignedStamp(latestFileStampAssigned);
							
							dependencyObjDAO.insert(dependencyObject);
						}
					}
					else{
						dependencyObject.setFile(changedFile);
					}
				}
				
				if(dependencyObject != null){
					dependencyObjectList.add(dependencyObject);
				}
			}
		}
	
		//Calculates dependencies (files from this revision)
		List<StructuralDependency> dependencies = 
				StructuralCouplingIdentifier.calcStructuralCoupling(dependencyObjectList);

		//Builds the set of dependencySets
		Set<DependencySet<FileDependencyObject, FileDependencyObject>> setOfDependencySets = 
				StructuralCouplingUtils.createDependencySets(dependencyObjectList, dependencies);
		
		/**
		//Debug 1
		System.out.println("Debug 1");
		for(DependencySet<FileDependencyObject, FileDependencyObject> dependencySet : setOfDependencySets){
			System.out.println("Supplier: " + dependencySet.getDependedObject().getFilePath());
			for(DependencyObject dependencyObject : dependencySet.getDependenciesMap().keySet()){
				FileDependencyObject client = (FileDependencyObject) dependencyObject;
				System.out.println("Client: " + client.getFilePath());
				System.out.println("Degree: " + dependencySet.getDependenciesMap().get(dependencyObject));
			}
		}
		*/
		
		//Fills new dependency maps with updated coupling values
		for (int i = 0; i < dependencyObjectList.size(); i++) {
			FileDependencyObject client = dependencyObjectList.get(i);
			List<FileDependencyObject> oldSuppliers = dependencyObjDAO.findSuppliers(client,dependencyObjectList);
			
			if(!oldSuppliers.isEmpty()){
				//Should have only a single dependency
				dependencies = 
						StructuralCouplingIdentifier.calcStructuralCoupling(
								client, oldSuppliers);
		
				//Adds the dependencySets from oldSuppliers to the set of dependency sets
				setOfDependencySets.addAll(
						StructuralCouplingUtils.createDependencySetsForOldSuppliers(
								client, oldSuppliers, dependencies));
			}
		}
		
		/**
		//Debug 2
		System.out.println("Debug 2");
		for(DependencySet<FileDependencyObject, FileDependencyObject> dependencySet : setOfDependencySets){
			System.out.println("Supplier: " + dependencySet.getDependedObject().getFilePath());
			for(DependencyObject dependencyObject : dependencySet.getDependenciesMap().keySet()){
				FileDependencyObject client = (FileDependencyObject) dependencyObject;
				System.out.println("Client: " + client.getFilePath());
				System.out.println("Degree: " + dependencySet.getDependenciesMap().get(dependencyObject));
			}
		}
		*/
		
		return setOfDependencySets;
	}
	
}