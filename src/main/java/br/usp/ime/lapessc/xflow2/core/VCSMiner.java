/* 
 * 
 * XFlow
 * _______
 * 
 *  
 *  (C) Copyright 2010, by Universidade Federal do Pará (UFPA), Francisco Santana, Jean Costa, Pedro Treccani and Cleidson de Souza.
 * 
 *  This file is part of XFlow.
 *
 *  XFlow is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  XFlow is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XFlow.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  ==================
 *  DataExtractor.java
 *  ==================
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import br.usp.ime.lapessc.xflow2.connectivity.transformations.loc.LOCProcessor;
import br.usp.ime.lapessc.xflow2.entity.Author;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.FileArtifact;
import br.usp.ime.lapessc.xflow2.entity.Folder;
import br.usp.ime.lapessc.xflow2.entity.MiningSettings;
import br.usp.ime.lapessc.xflow2.entity.Resource;
import br.usp.ime.lapessc.xflow2.entity.VCSMiningProject;
import br.usp.ime.lapessc.xflow2.entity.Study;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.AuthorDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.FolderDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.VCSMiningProjectDAO;
import br.usp.ime.lapessc.xflow2.entity.database.EntityManagerHelper;
import br.usp.ime.lapessc.xflow2.exception.cm.CMException;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.BufferedVCSLogParser;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.CommitDTO;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.FileArtifactDTO;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.FolderArtifactDTO;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.VCSLogParserFactory;
import br.usp.ime.lapessc.xflow2.util.FileArtifactUtils;

public class VCSMiner {

	private Study study;
	private VCSMiningProject miningProject;
	
	public VCSMiner(Study study){
		this.study = study;
	}
	
	public VCSMiningProject mine(MiningSettings miningSettings) throws 
		CMException, DatabaseException{
			
		this.miningProject = createMiningProject(miningSettings);
		
		BufferedVCSLogParser bufferedLogParser = createParser(miningSettings);
				
		while (bufferedLogParser.hasNotEnded()){
			List<CommitDTO> commits = bufferedLogParser.parse();
			System.out.print("Storing commits ");
			for (CommitDTO commitDTO : commits){
				buildAndStoreCommit(miningProject, commitDTO);
				System.out.print(".");
			}
			System.out.println("done!");
		}
		
		return miningProject;
	
	}

	private BufferedVCSLogParser createParser(MiningSettings miningSettings){
		BufferedVCSLogParser bufferedLogParser = 
				new BufferedVCSLogParser(
						VCSLogParserFactory.create(miningSettings));
		
		return bufferedLogParser;
	}

	private void buildAndStoreCommit(VCSMiningProject miningProject,
			CommitDTO commitDTO) {
		
		Commit commit = new Commit();
		
		commit.setAuthor(getAuthor(commitDTO));		
		commit.setComment(commitDTO.getComment().replaceAll("�", ""));
		commit.setDate(commitDTO.getDate());
		commit.setRevision(commitDTO.getRevision());
		commit.setVcsMiningProject(miningProject);
		commit.setEntryFiles(getEntryFiles(commitDTO));
		commit.setEntryFolders(getEntryFolders(commitDTO));
		
		try{
			final EntityManager manager = EntityManagerHelper.getEntityManager();
			manager.getTransaction().begin();
			manager.persist(commit);
			manager.getTransaction().commit();
		}catch(DatabaseException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//FIXME: boolear o fixOperationType
		//fixOperationType(commit);
		
		//Fix parent folder for folders
		for(Folder folder : commit.getEntryFolders()){
			fixFolder(folder, commit);
		}
		
		//Fix parent folder for files
		setParentFolders(commit);
					
		setDeletedOnForFileArtifacts(commit);
		setDeletedOnForFolders(commit);
		
		if (miningProject.getMiningSettings().isCodeDownloadEnabled()){
			setLocMeasures(commit);
		}
	
		try{
			final EntityManager manager = EntityManagerHelper.getEntityManager();
			manager.getTransaction().begin();
			manager.flush();
			manager.getTransaction().commit();
			manager.clear();
		}catch(DatabaseException e){
			e.printStackTrace();
		}
	}
	
	private List<FileArtifact> getEntryFiles(CommitDTO dto) {
		List<FileArtifact> files = new ArrayList<FileArtifact>();
		for(FileArtifactDTO fileDTO : dto.getFileArtifacts()){
			FileArtifact file = new FileArtifact();
			
			//If the file is replaced, we treat it as if it had been modified
			if(fileDTO.getOperationType() == 'R') file.setOperationType('M');
			else file.setOperationType(fileDTO.getOperationType());
			
			file.setPath(fileDTO.getPath());
			
			String fileName = StringUtils.substringAfterLast(file.getPath(), "/");
			file.setName(fileName);
			
			String fileExtension = StringUtils.substringAfterLast(fileName, ".");
			file.setExtesion(fileExtension);
			
			file.setSourceCode(fileDTO.getSourceCode());
			file.setDiffCode(fileDTO.getDiffCode());
			
			files.add(file);
		}
		return files;
	}

	private List<Folder> getEntryFolders(CommitDTO dto) {
		
		List<Folder> folders = new ArrayList<Folder>();
		for(FolderArtifactDTO folderDTO : dto.getFolderArtifacts()){
			Folder folder = new Folder();
			folder.setPath(folderDTO.getPath());
			folder.setOperationType(folderDTO.getOperationType());

			String folderName = StringUtils.substringAfterLast(
					folder.getPath(), "/");
			folder.setName(folderName);	
			
			folders.add(folder);
		}
		return folders;
	}

	private Author getAuthor(CommitDTO dto){
		
		Author author = null;
		
		try{
			
			String authorName = dto.getAuthorName();
			if (authorName == null) authorName = "null";
			
			author = new AuthorDAO().findAuthorByName(miningProject, 
					authorName);
				
			if (author == null){
				author = new Author(authorName,dto.getDate());
				author.setVcsMiningProject(miningProject);
				new AuthorDAO().insert(author);
			}
			else{
				author.setLastContribution(dto.getDate());
			}
		}catch(DatabaseException e){
			e.printStackTrace();
		}
		
		return author;
	}

	private VCSMiningProject createMiningProject(MiningSettings miningSettings){
		
		VCSMiningProject vcsMiningProject = 
				new VCSMiningProject(study,miningSettings);
		
		try{
			
			VCSMiningProjectDAO vcsMiningProjectDAO = 
					new VCSMiningProjectDAO();
			
			vcsMiningProjectDAO.insert(vcsMiningProject);
						
		}catch(DatabaseException e){
			e.printStackTrace();
		}
		
		return vcsMiningProject;
	}
	
	private void fixFolder(Resource resource, Commit commit){
		
		try{
			String path = resource.getPath();
			final int lastSlash = path.lastIndexOf("/");
			
	    	//Fim da recursão
	    	if(lastSlash != 0){
	    		String parentFolderPath = StringUtils.substringBeforeLast(path, "/");
	    		
	    		Folder parentFolder = new FolderDAO().findFolderByPath(
	    				commit.getVcsMiningProject(), parentFolderPath);
	    		
	    		if(parentFolder != null){
	    			resource.setParentFolder(parentFolder);
	    		}
	    		else{
	    			parentFolder = new Folder();
	    			parentFolder.setPath(parentFolderPath);
	    			parentFolder.setName(StringUtils.substringAfterLast(
	    					parentFolder.getPath(), "/"));
	    			parentFolder.setOperationType('A');
	    			parentFolder.setCommit(commit);
	    			resource.setParentFolder(parentFolder);
	    			
	    			new FolderDAO().insert(parentFolder);
	    			
	    			fixFolder(parentFolder, commit);
	    		}
	    	}
		}catch(DatabaseException e){
			e.printStackTrace();
		}
	}
	
	private void fixOperationType(Commit commit){
		try{
			for(FileArtifact file : commit.getEntryFiles()){
				
				//If operationType is 'M' but the file has never been added
				//(because the analyzed period does not include the file or 
				//because the repo is inconsistent), then we change it to 'A'
				if (file.getOperationType() == 'M'){
					if (FileArtifactUtils.searchFile(
							commit.getVcsMiningProject().getId(),
							file.getPath()) == null){
						
						file.setOperationType('A');
					}
				}
				
			}
		}catch(DatabaseException e){
			e.printStackTrace();
		}
	}
	
	//seta os parents, criando os folders necessarios
	private void setParentFolders(Commit commit){
		for(FileArtifact file : commit.getEntryFiles()){
			if (file.getOperationType() == 'A') {
				fixFolder(file, commit);				
			}
		}	
	}
	
	private void setLocMeasures(Commit commit){
		try{
			for(FileArtifact file : commit.getEntryFiles()){
				//TODO: Refatorar esse método estático
				if (file.getOperationType() != 'D') {
					LOCProcessor.extractCodeInfo(file);
				}
			}
		}catch(DatabaseException e){
			e.printStackTrace();
		}
	}
	
	private void setDeletedOnForFileArtifacts(Commit commit){
				
		try{
			for(FileArtifact file : commit.getEntryFiles()){

				if (file.getOperationType() == 'D') {

					FileArtifact existingFile = FileArtifactUtils.searchFile(
							commit.getVcsMiningProject().getId(), 
							file.getPath());
					
					if(existingFile != null){
						existingFile.setDeletedOn(commit);
					}
					
				}
			}
		}catch(DatabaseException e){
			e.printStackTrace();
		}
	}
	
	private void setDeletedOnForFolders(Commit commit) {

		try{
			for(Folder folder : commit.getEntryFolders()){

				if (folder.getOperationType() == 'D') {

					Folder existingFolder = new FolderDAO().findFolderByPath(
							commit.getVcsMiningProject(), 
							folder.getPath());
					
					if(existingFolder != null){
						existingFolder.setDeletedOn(commit);
					}					
				}
			}
		}catch(DatabaseException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		HashMap<String, List<FolderArtifactDTO>> map = 
				new HashMap<String, List<FolderArtifactDTO>>();
		
		List<FolderArtifactDTO> list = map.get("path");
		if (list == null) list = new ArrayList<FolderArtifactDTO>();
		list.add(new FolderArtifactDTO());
		map.put("path", list);
		System.out.println(map);
	}
}