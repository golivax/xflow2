package br.usp.ime.lapessc.xflow2.core.transactions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.time.DateUtils;

import br.usp.ime.lapessc.xflow2.entity.Author;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.FileArtifact;
import br.usp.ime.lapessc.xflow2.entity.VCSMiningProject;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.VCSMiningProjectDAO;
import br.usp.ime.lapessc.xflow2.entity.database.EntityManagerHelper;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.repository.vcs.dao.CommitDAO;

public class SlidingTimeWindowProcessor {

	private static VCSMiningProject timeWindowedProject;
	
	public static void process(VCSMiningProject project, int seconds) throws DatabaseException{
		
		CommitDAO entryDAO = new CommitDAO();
		
		//timeWindowedProject = setupNewProject(project);	


		List<Author> authorList = project.getAuthors();
		int numAuthors = authorList.size();
		int currentAuthor = 0;
		
		for(Author author : authorList){
			currentAuthor++;
			//System.out.print("(" + currentAuthor + "/" + numAuthors + ") ");
			//System.out.println("Processing revisions from " + author.getName());
			
			List<Commit> entryList = entryDAO.getNonBlankEntriesByAuthorSortedByDate(author);
			int numEntries = entryList.size();
			int currentEntry = 0;
			
			if (!entryList.isEmpty()){
				
				List<Commit> windowEntries = new ArrayList<Commit>();
				//List<ObjFile> windowFiles = new ArrayList<ObjFile>();
				
				//Inserts a dummy entry at the end of the list
				Commit dummyEntry = new Commit();
				entryList.add(dummyEntry);
				
				ListIterator<Commit> entryIterator = entryList.listIterator();
				Commit previousEntry = entryIterator.next();
				
				currentEntry++;
				//System.out.print("(" + currentEntry + "/" + numEntries + ") ");
				//System.out.println("Processing revision " + previousEntry.getRevision());
				
				while (entryIterator.hasNext()){
				
					windowEntries.add(previousEntry);
					Commit entry = entryIterator.next();

					//System.out.println("Comparing with " + entry.getRevision());
					
					if(entriesOnSameWindow(entry, previousEntry, seconds)){
						//System.out.println("Revisions on same window");						
					}					
					
					//New transaction
					else{
					
						System.out.println(windowEntries.size());
						
						/**						
						//System.out.println("Revisions not on same window.");
						Commit window = TimeWindowFactory.createWindow(
								timeWindowedProject, author, windowEntries);
						
						//FIXME: If the window has no files, then it should be 
						//discarded
						saveWindow(window);
						
						*/
						
						//Starting a new window
						windowEntries.clear();
					}
					
					previousEntry = entry;
					
					currentEntry++;
					
					if(currentEntry <= numEntries){
						//System.out.print("(" + currentEntry + "/" + numEntries + ") ");
						//System.out.println("Processing revision " + previousEntry.getRevision());
					}
				}
			}
			
			//FIXME:
			//As we don't have an application layer yet, it is necessary 
			//to frequently clear the persistence context to avoid memory issues
			EntityManagerHelper.getEntityManager().clear();			
		}
	
	}

	private static VCSMiningProject setupNewProject(VCSMiningProject project) {
		VCSMiningProject timeWindowedProject = new VCSMiningProject(null, project.getMiningSettings());
		
		try{
			List<Author> listAuthors = new ArrayList<Author>();
			for (Author author : project.getAuthors()){
				Author newAuthor = new Author(author.getName(),author.getStartDate());
				newAuthor.setVcsMiningProject(timeWindowedProject);
				listAuthors.add(newAuthor);
			}
			
			timeWindowedProject.setAuthors(listAuthors);
			
			//Authors are also saved by cascade
			VCSMiningProjectDAO projectDAO = new VCSMiningProjectDAO();
			projectDAO.insert(timeWindowedProject);
			
			//FIXME:
			//As we don't have an application layer yet, it is necessary 
			//to frequently clear the persistence context to avoid memory issues
			EntityManagerHelper.getEntityManager().clear();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return timeWindowedProject;
	}

	private static boolean entriesOnSameWindow(Commit entry, 
			Commit previousEntry, int windowSize) {
		
		//Same comments
		boolean sameComments = 
			previousEntry.getComment().equals(entry.getComment()); 

		//Inside time window
		Date limit = DateUtils.addSeconds(previousEntry.getDate(), windowSize);
		boolean insideTimeWindow = 
			entry.getDate() != null && !entry.getDate().after(limit);
			
		//Entries on same window
		boolean entriesOnSameWindow = sameComments && insideTimeWindow;
		return entriesOnSameWindow;
	}

	private static void saveWindow(Commit window){
		
		CommitDAO entryDAO = new CommitDAO();
		try{
			
			//Detaching all objFiles (to force insertion)
			for(FileArtifact file : window.getEntryFiles()){
				EntityManagerHelper.getEntityManager().detach(file);
				file.setId(0);
			}
			
			//Insert entry and associated files
			entryDAO.insert(window);
			
			//Detach the window (and associated files) from persistence context
			EntityManagerHelper.getEntityManager().detach(window);
			
		}catch(Exception e){
			e.printStackTrace();
		}		
	}

}