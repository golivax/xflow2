package br.usp.ime.lapessc.xflow2.core.transactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import br.usp.ime.lapessc.xflow2.entity.Author;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.FileVersion;
import br.usp.ime.lapessc.xflow2.entity.VCSMiningProject;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.ArtifactDAO;
import br.usp.ime.lapessc.xflow2.entity.database.EntityManagerHelper;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class TimeWindowFactory {

	public static Commit createWindow(VCSMiningProject timeWindowedProject, Author author, List<Commit> windowEntries) throws DatabaseException {
		
		//Sliding time window
		TimeWindow window = new TimeWindow();
		
		window.setAuthor(author);
		window.setVcsMiningProject(timeWindowedProject);
		
		Commit firstEntry = windowEntries.get(0);
		Commit lastEntry = windowEntries.get(windowEntries.size() - 1);
		
		window.setComment(lastEntry.getComment());
		window.setDate(lastEntry.getDate());
		window.setRevision(lastEntry.getRevision());
		
		//Window length (minutes)
		double lengthInMillis = lastEntry.getDate().getTime() - 
				firstEntry.getDate().getTime();
		double lengthInMinutes =  lengthInMillis / 1000 / 60;
		window.setLengthInMinutes(lengthInMinutes);
		
		//Adds all files to the list of rawFiles 
		//Adds revision numbers to the window
		List<FileVersion> rawFiles = new ArrayList<FileVersion>();
		for (Commit entry : windowEntries){
			rawFiles.addAll(entry.getEntryFiles());
			window.addEntryNumber(entry);
			
			EntityManagerHelper.getEntityManager().detach(entry);
		}
		
		//Sort by filepath
		Collections.sort(rawFiles, new FileComparator());
		
		//Inserts a dummy file at the end of the list
		FileVersion dummyFile = new FileVersion();
		rawFiles.add(dummyFile);
		
		ListIterator<FileVersion> fileIterator = rawFiles.listIterator();
		FileVersion previousFile = fileIterator.next();
		FileVersion lastVersion = null;
		
		//The list of windowFiles files
		List<FileVersion> windowFiles =	new ArrayList<FileVersion>();		
		
		while(fileIterator.hasNext()){
			FileVersion file = fileIterator.next();
			
			//If same paths, then file is a new "version" of previousFile
			if(previousFile.getPath().equals(file.getPath())){
				lastVersion = file;
			}
			else{
				//At least two "versions" of the same file
				if(lastVersion != null){
					FileVersion consolidatedFile = 
						handleFiles(previousFile,lastVersion);
					
					if(consolidatedFile != null){
						windowFiles.add(consolidatedFile);
						consolidatedFile.setCommit(window);	
					}
					
					lastVersion = null;
				}
				//Just another file
				else{
					windowFiles.add(previousFile);
					previousFile.setCommit(window);
				}
				
				previousFile = file;
			}
		}
		
		window.setEntryFiles(windowFiles);
		return window;
	}
	
	private static FileVersion handleFiles(FileVersion firstFile, FileVersion lastFile) throws DatabaseException {

		/**
		The following rules apply to create a consolidated Entry:
		[i]   (D, D) = D
		[ii]  (D, A) = M
		[iii] (D, M) = M 
		[iv]  (A, A) = A 
		[v]   (A, M) = A
		[vi]  (A, D) = D 
		[vii] (M, M) = M
		[viii](M, A) = M
		[ix]  (M, D) = D 
		*/
		
		if (firstFile.getOperationType() == 'D'){
			//[i]
			if(lastFile.getOperationType() == 'D'){
				//Updating the deletedOn property to refer to the last entry
				FileVersion referredAddedFile = new ArtifactDAO().
						findAddedFileByPathUntilRevision(
								firstFile.getCommit().getVcsMiningProject(), 
								firstFile.getCommit().getRevision(), 
								firstFile.getPath());
				
				if (referredAddedFile != null){
					referredAddedFile.setDeletedOn(lastFile.getCommit());
					new ArtifactDAO().update(referredAddedFile);	
				}
			}
			//[ii]
			if(lastFile.getOperationType() == 'A'){
				lastFile.setOperationType('M');
			}
			
			//[iii] --> Retorna o último arquivo
			
			//TODO: Check loc stuff
			return lastFile;
		}
		
		
		if (firstFile.getOperationType() == 'A'){
			//[vi] We treat the file as if it had never existed
			if(lastFile.getOperationType() == 'D'){
				return null;
			} else {
				//[iv], [v] --> Retorna o último arquivo e, no caso de (A,M), seta como adicionado 
				lastFile.setOperationType('A');
				
				//TODO: Check loc stuff
				return lastFile;
			}
			
			
		}
		
		if (firstFile.getOperationType() == 'M'){
			//[vix]
			if(lastFile.getOperationType() == 'D'){
				//Updating the deletedOn property to refer to the last entry
				FileVersion referredAddedFile = new ArtifactDAO().
						findAddedFileByPathUntilRevision(
								firstFile.getCommit().getVcsMiningProject(), 
								firstFile.getCommit().getRevision(), 
								firstFile.getPath());
				
				referredAddedFile.setDeletedOn(lastFile.getCommit());
				new ArtifactDAO().update(referredAddedFile);
				
			} else {
				//[vii],[viii] Retorna o ultimo arquivo e, no caso de (M,A), seta como modificado
				lastFile.setOperationType('M');
			}
			//TODO: Check loc stuff
			return lastFile;
		}
		
		//Odd situation not foreseen
		return null;
	}
}


class FileComparator implements Comparator<FileVersion>{
	@Override
	public int compare(FileVersion file, FileVersion otherFile) {
		return file.getPath().compareTo(otherFile.getPath());
	}
}
