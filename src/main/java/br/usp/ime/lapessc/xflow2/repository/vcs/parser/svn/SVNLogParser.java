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
 *  ==============
 *  SVNAccess.java
 *  ==============
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  Jean Costa, Pedro Treccani;
 *  
 */

package br.usp.ime.lapessc.xflow2.repository.vcs.parser.svn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.SVNRepository;

import br.usp.ime.lapessc.xflow2.entity.MiningSettings;
import br.usp.ime.lapessc.xflow2.exception.cm.CMException;
import br.usp.ime.lapessc.xflow2.exception.cm.svn.SVNProtocolNotSupportedException;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.CommitDTO;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.FileArtifactDTO;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.FolderArtifactDTO;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.VCSLogParser;

public class SVNLogParser implements VCSLogParser {
		
	private MiningSettings miningSettings;
	
	private SVNRepository svn;
	private SVNLogParserUtils parserUtils;
	
	public SVNLogParser(MiningSettings miningSettings) 
			throws SVNProtocolNotSupportedException{
		
		this.miningSettings = miningSettings;		
		
		this.svn = SVNFactory.create(miningSettings.getVcs().getURI(), 
				miningSettings.getAccessCredentials().getUsername(), 
				miningSettings.getAccessCredentials().getPassword());
		
		this.parserUtils = new SVNLogParserUtils(svn);
	}

	public List<CommitDTO> parse(long startCommit, long endCommit) {
			
		List<CommitDTO> commits = new ArrayList<CommitDTO>();
		
		try{	
			List<SVNLogEntry> logEntries;
			
			if(miningSettings.isTemporalConsistencyForced()){
				logEntries = parserUtils.getOrderedLogEntries(
						svn, startCommit, endCommit);
			}
			else {
				logEntries = (List<SVNLogEntry>) svn.log(new String[]{""}, 
					null, startCommit, endCommit, true, true);
			}
	
			for (SVNLogEntry logEntry : logEntries) {
				
				System.out.print("Parsing commit " + 
						logEntry.getRevision() + "\n");
	
				CommitDTO commit = new CommitDTO();
				setRevision(logEntry, commit);
				
				//Parsing the Artifacts	
				for (SVNLogEntryPath entryPath : 
						logEntry.getChangedPaths().values()) {

					SVNNodeKind nodeKind = entryPath.getKind();
					
					//Sometimes SVNKit is not able to retrieve the node kind, so
					//we must ask it explicitly
					if (nodeKind.equals(SVNNodeKind.UNKNOWN)){
						nodeKind = svn.checkPath(entryPath.getPath(), logEntry.getRevision());
					}
										
					if (nodeKind.equals(SVNNodeKind.DIR)) setFolder(entryPath, commit);
					else if (nodeKind.equals(SVNNodeKind.FILE)) setFile(entryPath, commit); 
				}

				//If the commit has no artifacts, then we discard it
				if(commit.getFileArtifacts().isEmpty() && 
				   commit.getFolderArtifacts().isEmpty()){
					
					System.out.println("Commit does not match data filter");
					System.out.println("Ignoring this commit");
				}
				//Otherwise, we parse the rest of the data
				else{
					setAuthor(logEntry, commit);
					setDate(logEntry, commit);
					setComment(logEntry, commit);
					
					commits.add(commit);
				}
			}
		}catch(SVNException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			svn.closeSession();	
		}
		
		return commits;
	}

	private void setRevision(SVNLogEntry logEntry, CommitDTO commit) {
		commit.setRevision(logEntry.getRevision());
	}

	private void setComment(SVNLogEntry logEntry, CommitDTO commit) {
		if (logEntry.getMessage() == null) commit.setComment(" ");
		else commit.setComment(logEntry.getMessage());
	}

	private void setDate(SVNLogEntry logEntry, CommitDTO commit) {
		commit.setDate(logEntry.getDate());
	}

	private void setAuthor(SVNLogEntry logEntry, CommitDTO commit) {
		commit.setAuthorName(logEntry.getAuthor());
	}
		
	private void setFile(SVNLogEntryPath entryPath, CommitDTO commit) {		
		if (miningSettings.getFilter().match(entryPath.getPath())){
			
			if (miningSettings.isCodeDownloadEnabled()){ 
				commit.addFileArtifact(getFileWithCode(entryPath, commit));
			}
			else{
				commit.addFileArtifact(getFileWithoutCode(entryPath, commit));
			}
		}
	}
	
	private FileArtifactDTO getFileWithCode(SVNLogEntryPath entryPath, CommitDTO commit){
		
		FileArtifactDTO file = getFileWithoutCode(entryPath, commit);
		
		try{
		
			//If the file has not been deleted, then we grab its source code
			if (file.getOperationType() != 'D'){
				file.setSourceCode(parserUtils.getFileOnRepository(
					entryPath.getPath(), commit.getRevision()));	
			}
					
			//if the file has been modified, then we also grab its diff code
			if (file.getOperationType() == 'M'){
				file.setDiffCode(parserUtils.doDiff(
						miningSettings.getVcs().getURI(), entryPath.getPath(), 
						commit.getRevision() - 1, commit.getRevision(), false));	
			}
			
			
		}catch(SVNException e){
			System.out.println(e.getCause());
			System.err.println("error while fetching the file contents and properties: " + e.getMessage());
			System.err.println("error on " + entryPath.getPath() + " revision " + commit.getRevision() + " ;");	
		}
		
		return file;
	}
	
	private FileArtifactDTO getFileWithoutCode(SVNLogEntryPath entryPath, CommitDTO commit){
		
		FileArtifactDTO file = new FileArtifactDTO();
		file.setPath(entryPath.getPath());
		file.setOperationType(entryPath.getType());
		
		return file;
	}
	
	private void setFolder(SVNLogEntryPath entryPath, CommitDTO commit) {
		
		if (miningSettings.getFilter().match(entryPath.getPath())){
			FolderArtifactDTO folder = new FolderArtifactDTO();
			folder.setPath(entryPath.getPath());
			folder.setOperationType(entryPath.getType());
				
			commit.addFolderArtifact(folder);
		}
	}
	
	/*public boolean checkForDateInconsistencies(VCSMiningProject project) 
			throws DatabaseException, SVNException, 
			SVNProtocolNotSupportedException{
		
		boolean inconsistency = false;
		
		final CommitDTO commit = 
				new CommitDAO().findEntryFromLastRevision(project);
		
		final Collection<SVNLogEntry> logEntries = 
				svn.log(new String[]{""}, null, 
				commit.getRevision() + 1, -1, true, true);
		
		for (SVNLogEntry svnLogEntry : logEntries) {
			if(svnLogEntry.getDate().compareTo(commit.getDate()) < 0){
				inconsistency = true;
				break;
			}
		}
		
		return inconsistency;
	}*/

	
	@Override
	public List<CommitDTO> parse(Date startDate, Date endDate)
			throws CMException {
		// TODO Auto-generated method stub
		return null;
	}


}