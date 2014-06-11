package br.usp.ime.lapessc.xflow2.core.processors.cochanges;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.usp.ime.lapessc.xflow2.core.processors.DependenciesIdentifier;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.Author;
import br.usp.ime.lapessc.xflow2.entity.AuthorDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.CoordinationRequirementsGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencyObject;
import br.usp.ime.lapessc.xflow2.entity.DependencySet;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.FileArtifact;
import br.usp.ime.lapessc.xflow2.entity.TaskAssignmentGraph;
import br.usp.ime.lapessc.xflow2.entity.TaskDependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.ArtifactDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.AuthorDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.FileDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.database.DatabaseManager;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.Matrix;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.MatrixFactory;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.repository.vcs.dao.CommitDAO;
import br.usp.ime.lapessc.xflow2.util.Filter;

public final class CoChangesCollector implements DependenciesIdentifier {

	private CoChangesAnalysis analysis;
	
	private Map<String, DependencyObject> dependencyObjectsCache;
	
	private Filter filter;
	
	// MATRICES FOR COORDINATION REQUIREMENTS CALCS
	private Matrix taskAssignmentMatrix;
	private Matrix taskDependencyMatrix;
	
	private int latestFileStampAssigned;
	private int latestAuthorStampAssigned;
	
	@Override
	public final void dataCollect(List<Long> revisions, Analysis analysis, 
			Filter filter) throws DatabaseException {
		
		this.analysis = (CoChangesAnalysis) analysis;
		this.filter = filter;
		initiateCache();
		
		CommitDAO commitDAO = new CommitDAO();
		DependencyDAO dependencyDAO = new DependencyDAO();
		
		System.out.println("** Starting CoChanges analysis **");
	
		for (Long revision : revisions) {
			
			final Commit commit = commitDAO.findEntryFromRevision(
					analysis.getProject(), revision);			
			
			System.out.print(
					"- Processing entry: " + commit.getRevision() + 
					" (" + revision + ")\n");
			
			System.out.print("* Collecting task dependencies...");
					
			final Set<DependencySet<FileDependencyObject, FileDependencyObject>> fileToFileDependencies = 
					gatherFileToFileDependencies(commit.getEntryFiles());
			
			if(fileToFileDependencies.size() > 0) { 
				
				final TaskDependencyGraph taskDependency = new TaskDependencyGraph(false);
				taskDependency.setAssociatedAnalysis(analysis);
				taskDependency.setAssociatedEntry(commit);
	
				//Sets on both sides (bi-directional association)
				taskDependency.setDependencies(fileToFileDependencies);
						
				dependencyDAO.insert(taskDependency);						
				System.out.print(" done!\n");
					
				System.out.print("* Collecting tasks assignments...");
				
				final TaskAssignmentGraph taskAssignment = new TaskAssignmentGraph();
				taskAssignment.setAssociatedAnalysis(this.analysis);
				taskAssignment.setAssociatedEntry(commit);
				taskAssignment.setDirectedDependency(true);
				
				final Set<DependencySet<FileDependencyObject,AuthorDependencyObject>> authorToFileDependencies = 
						gatherAuthorToFileDependencies(commit.getAuthor(), fileToFileDependencies);
				
				for (DependencySet<FileDependencyObject,AuthorDependencyObject> dependencySet : authorToFileDependencies) {
					dependencySet.setAssociatedDependency(taskAssignment);
				}
				
				taskAssignment.setDependencies(authorToFileDependencies);
				dependencyDAO.insert(taskAssignment);
				System.out.print(" done!\n");
				
			
				if(this.analysis.isCoordinationRequirementPersisted()){
			
					System.out.print("* Calculating coordination requirements...");
					
					final CoordinationRequirementsGraph coordinationRequirement = 
							new CoordinationRequirementsGraph();
					
					coordinationRequirement.setAssociatedAnalysis(
							this.analysis);
					
					coordinationRequirement.setAssociatedEntry(commit);
					coordinationRequirement.setDirectedDependency(false);
					
					final Set<DependencySet<AuthorDependencyObject, AuthorDependencyObject>> coordinationDependencies = 
							gatherCoordinationRequirements(taskDependency, taskAssignment);
					
					for (DependencySet<AuthorDependencyObject, AuthorDependencyObject> dependencySet : coordinationDependencies) {
						dependencySet.setAssociatedDependency(coordinationRequirement);
					}
					
					coordinationRequirement.setDependencies(coordinationDependencies);
					dependencyDAO.insert(coordinationRequirement);
					System.out.print(" done!\n");
				}

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
		dependencyObjectsCache = new HashMap<String, DependencyObject>();
		
		latestFileStampAssigned = new FileDependencyObjectDAO().checkHighestStamp(analysis);
		latestAuthorStampAssigned = new AuthorDependencyObjectDAO().checkHighestStamp(analysis);
		taskAssignmentMatrix = MatrixFactory.createMatrix();
		taskDependencyMatrix = MatrixFactory.createMatrix();
	}

	private Set<DependencySet<FileDependencyObject, FileDependencyObject>> gatherFileToFileDependencies(List<FileArtifact> changedFiles) throws DatabaseException {
		
		FileDependencyObjectDAO dependencyObjDAO = 
				new FileDependencyObjectDAO();
		
		Set<DependencySet<FileDependencyObject, FileDependencyObject>> setOfDependencySets = 
				new HashSet<DependencySet<FileDependencyObject, FileDependencyObject>>();
		
		List<FileDependencyObject> fileDependencyObjectList = 
				new ArrayList<FileDependencyObject>();

		//Builds the list of dependency objects
		for (FileArtifact changedFile : changedFiles) {
			if(filter.match(changedFile.getPath())){
				
				FileDependencyObject fileDependencyObject;
				
				//Added file
				if(changedFile.getOperationType() == 'A'){
					latestFileStampAssigned++;
					
					fileDependencyObject = new FileDependencyObject();
					fileDependencyObject.setAnalysis(analysis);
					fileDependencyObject.setFile(changedFile);
					fileDependencyObject.setFilePath(changedFile.getPath());
					fileDependencyObject.setAssignedStamp(latestFileStampAssigned);
					
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
							latestFileStampAssigned++;

							fileDependencyObject = new FileDependencyObject();
							fileDependencyObject.setAnalysis(analysis);
							fileDependencyObject.setFile(changedFile);
							fileDependencyObject.setFilePath(changedFile.getPath());
							fileDependencyObject.setAssignedStamp(latestFileStampAssigned);
							
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
		for (int i = 0; i < fileDependencyObjectList.size(); i++) {
			
			Map<FileDependencyObject, Integer> dependenciesMap = 
				new HashMap<FileDependencyObject, Integer>();
			
			for (int j = i; j < fileDependencyObjectList.size(); j++) {
				dependenciesMap.put(fileDependencyObjectList.get(j), 1);
			}
			
			DependencySet<FileDependencyObject, FileDependencyObject> dependencySet = 
				new DependencySet<FileDependencyObject, FileDependencyObject>();
			dependencySet.setSupplier(fileDependencyObjectList.get(i));
			dependencySet.setClientsMap(dependenciesMap);
			setOfDependencySets.add(dependencySet);
		}
		
		return setOfDependencySets;
	}
	
	private Set<DependencySet<FileDependencyObject,AuthorDependencyObject>> gatherAuthorToFileDependencies(
			Author author, Set<DependencySet<FileDependencyObject, FileDependencyObject>> fileDependencies) throws DatabaseException {
		
		final AuthorDependencyObjectDAO authorDependencyDAO = new AuthorDependencyObjectDAO();
		
		final Set<DependencySet<FileDependencyObject,AuthorDependencyObject>> dependenciesSet = 
				new HashSet<DependencySet<FileDependencyObject,AuthorDependencyObject>>();
		
		final AuthorDependencyObject dependedAuthor;
		if(dependencyObjectsCache.containsKey(author.getName())){
			dependedAuthor = (AuthorDependencyObject) dependencyObjectsCache.get(author.getName());
		} else {
			latestAuthorStampAssigned++;
			
			dependedAuthor = new AuthorDependencyObject();
			dependedAuthor.setAnalysis(analysis);
			dependedAuthor.setAssignedStamp(latestAuthorStampAssigned);
			dependedAuthor.setAuthor(author);
			
			authorDependencyDAO.insert(dependedAuthor);
			dependencyObjectsCache.put(author.getName(), dependedAuthor);
			dependencyObjectsCache.put("\\u0A"+latestAuthorStampAssigned, dependedAuthor);
		}
			
		//Builds the set of dependency objects
		final Map<FileDependencyObject, Integer> dependenciesMap = new HashMap<FileDependencyObject, Integer>();
		for (DependencySet<FileDependencyObject, FileDependencyObject> fileDependency : fileDependencies) {
			dependenciesMap.put(fileDependency.getSupplier(), 1);
		}

		DependencySet<FileDependencyObject,AuthorDependencyObject> dependencySet = 
				new DependencySet<FileDependencyObject,AuthorDependencyObject>();
		
		dependencySet.setSupplier(dependedAuthor);
		dependencySet.setClientsMap(dependenciesMap);
		
		dependenciesSet.add(dependencySet);
	    return dependenciesSet;
	}
	
	
	private Set<DependencySet<AuthorDependencyObject, AuthorDependencyObject>> gatherCoordinationRequirements(final TaskDependencyGraph taskDependency, final TaskAssignmentGraph taskAssignment) throws DatabaseException {

		final Set<DependencySet<AuthorDependencyObject, AuthorDependencyObject>> coordinationDependencies = new HashSet<DependencySet<AuthorDependencyObject, AuthorDependencyObject>>();
		
		Matrix entryTaskDependencyMatrix = analysis.processDependencyMatrix(taskDependency);
		Matrix entryTaskAssignmentMatrix = analysis.processDependencyMatrix(taskAssignment);

		taskDependencyMatrix = entryTaskDependencyMatrix.sumDifferentOrderMatrix(taskDependencyMatrix);
		taskAssignmentMatrix = entryTaskAssignmentMatrix.sumDifferentOrderMatrix(taskAssignmentMatrix);
		
		entryTaskAssignmentMatrix = null;
		entryTaskDependencyMatrix = null;
		
		Matrix coordReq = taskAssignmentMatrix.multiply(taskDependencyMatrix).multiply(taskAssignmentMatrix.getTransposeMatrix());
		for (int i = 0; i < coordReq.getRows(); i++) {
			DependencySet<AuthorDependencyObject, AuthorDependencyObject> authorDependencies = new DependencySet<AuthorDependencyObject, AuthorDependencyObject>();
			final AuthorDependencyObject dependedAuthor;
			if(dependencyObjectsCache.containsKey("\\u0A"+i)){
				dependedAuthor = (AuthorDependencyObject) dependencyObjectsCache.get("\\u0A"+i);
			} else {
				dependedAuthor = null;
			}
			
			Map<AuthorDependencyObject, Integer> dependenciesMap = new HashMap<AuthorDependencyObject, Integer>();
			for (int j = 0; j < coordReq.getColumns(); j++) {
				final AuthorDependencyObject dependentAuthor;
				
				if(dependencyObjectsCache.containsKey("\\u0A"+j)){
					dependentAuthor = (AuthorDependencyObject) dependencyObjectsCache.get("\\u0A"+j);
				} else {
					dependentAuthor = null;
				}
				
				dependenciesMap.put(dependentAuthor, coordReq.getValueAt(j, i));
			}
			authorDependencies.setClientsMap(dependenciesMap);
			authorDependencies.setSupplier(dependedAuthor);
			coordinationDependencies.add(authorDependencies);
		}
		return coordinationDependencies;
	}
}
