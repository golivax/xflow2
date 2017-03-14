package br.usp.ime.lapessc.xflow2.core.processors.coordreq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.entity.Author;
import br.usp.ime.lapessc.xflow2.entity.AuthorDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraphType;
import br.usp.ime.lapessc.xflow2.entity.DependencySet;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.TaskAssignmentGraph;
import br.usp.ime.lapessc.xflow2.entity.TaskDependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.dao.core.AuthorDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyGraphDAO;
import br.usp.ime.lapessc.xflow2.entity.database.DatabaseManager;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public final class CoordReqsCollector{

	private Map<Author,AuthorDependencyObject> authorCache;
	private CoordReqsAnalysis coordReqAnalysis;
	
	public void run(CoordReqsAnalysis coordReqAnalysis) throws DatabaseException {
		
		this.authorCache = new HashMap<>();
		this.coordReqAnalysis = coordReqAnalysis;
		
		DependencyGraphDAO dependencyDAO = new DependencyGraphDAO();
		
		System.out.println("** Starting Coordination Requirements Analysis **");
	
		for (Commit commit : coordReqAnalysis.getCoChangesAnalysis().getCommits()) {		
			
			System.out.print("- Processing commit: " + commit.getRevision() + "\n");

			TaskDependencyGraph taskDepGraph = 
					(TaskDependencyGraph) dependencyDAO.findDependencyByEntry(
							coordReqAnalysis.getCoChangesAnalysis().getId(), 
							commit.getId(), 
							DependencyGraphType.TASK_DEPENDENCY.getValue());

			System.out.print("* Collecting tasks assignments...");

			final TaskAssignmentGraph taskAssignment = new TaskAssignmentGraph();
			taskAssignment.setAssociatedAnalysis(coordReqAnalysis);
			taskAssignment.setAssociatedEntry(commit);
			taskAssignment.setDirectedDependency(true);

			final DependencySet<FileDependencyObject,AuthorDependencyObject> fileToAuthorDependencies = 
					gatherFileToAuthorDependencies(commit.getAuthor(), taskDepGraph);

			taskAssignment.addDependency(fileToAuthorDependencies);
			dependencyDAO.insert(taskAssignment);
			System.out.print(" done!\n");

			//FIXME:
			//As we don't have an application layer yet, it is necessary 
			//to frequently clear the persistence context to avoid memory issues
			DatabaseManager.getDatabaseSession().clear();
		}
	}

	private DependencySet<FileDependencyObject,AuthorDependencyObject> gatherFileToAuthorDependencies(
			Author author, TaskDependencyGraph taskDependencyGraph) throws DatabaseException {
		
		AuthorDependencyObjectDAO authorDependencyDAO = 
				new AuthorDependencyObjectDAO();
			
		AuthorDependencyObject dependedAuthor = authorCache.get(author);
		if (dependedAuthor == null){	
			dependedAuthor = new AuthorDependencyObject();
			dependedAuthor.setAnalysis(coordReqAnalysis);
			dependedAuthor.setAuthor(author);			
			authorDependencyDAO.insert(dependedAuthor);
			authorCache.put(author,dependedAuthor);
		}
			
		//Builds the set of dependency objects
		final Map<FileDependencyObject, Integer> dependenciesMap = new HashMap<FileDependencyObject, Integer>();
		for (FileDependencyObject fileDependencyObject : taskDependencyGraph.getSuppliers()) {
			dependenciesMap.put(fileDependencyObject, 1);
		}

		DependencySet<FileDependencyObject,AuthorDependencyObject> dependencySet = 
				new DependencySet<FileDependencyObject,AuthorDependencyObject>();
		
		dependencySet.setSupplier(dependedAuthor);
		dependencySet.setClientsMap(dependenciesMap);
		
	    return dependencySet;
	}
	
}
