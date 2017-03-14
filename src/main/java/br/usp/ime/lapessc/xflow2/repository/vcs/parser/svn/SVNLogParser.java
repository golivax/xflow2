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

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

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
		this.parserUtils = new SVNLogParserUtils(svn);
	}

	public List<CommitDTO> parse() throws CMException{
		return parse(miningSettings.getFirstRev(), miningSettings.getLastRev());		
	}
	
	@Override
	public List<CommitDTO> parse(long startCommit, long endCommit)
			throws CMException {
		

		SVNLogEntryHandler logEntryHandler = new SVNLogEntryHandler();
		
		try{	
		
			if(miningSettings.isTemporalConsistencyForced()){
				
				List<SVNLogEntry> logEntries = parserUtils.getOrderedLogEntries(
						svn, miningSettings.getFirstRev(), 
						miningSettings.getLastRev());
				
				logEntryHandler.handleLogEntries(logEntries);
			}
			else {
				
				//FIXME: Should use AccessCredentials 
				
				SVNClientManager manager = SVNClientManager.newInstance(); 
				SVNLogClient logClient = manager.getLogClient();
				
			    SVNURL url = SVNURL.parseURIEncoded(
			    		miningSettings.getVcs().getURI());
			    
				String[] paths = miningSettings.getPaths().toArray(
						new String[0]);
				
				SVNRevision pegRev = miningSettings.getPegRev() != null ?
						SVNRevision.create(miningSettings.getPegRev()) :
						SVNRevision.UNDEFINED;
				
				//Defaults to peg if SVNRevision.UNDEFINED is provided
	            SVNRevision startRevision = SVNRevision.create(startCommit);
	            
	            //Defaults to 0 if SVNRevision.UNDEFINED is provided
	            SVNRevision endRevision = SVNRevision.create(endCommit);
	            
	            boolean stopOnCopy = false;
	            boolean includePaths = true;
	            boolean includeMergeInfo = false;
	            	            
	            long limit = -1;  // no limit
			    
	            String[] revProperties = null;
	            
			    logClient.doLog(
			    		url, paths, pegRev, startRevision, endRevision, 
			    		stopOnCopy, includePaths, includeMergeInfo, limit,
			    		revProperties, logEntryHandler);
				
			}

		}catch(SVNException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return logEntryHandler.getCommits();
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
					
			//if the file has been modified, then we grab its diff code
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

	@Override
	public MiningSettings getSettings() {
		return miningSettings;
	}

	
	class SVNLogEntryHandler implements ISVNLogEntryHandler{

		private List<CommitDTO> commits = new ArrayList<>();
		
		public void handleLogEntries(List<SVNLogEntry> svnLogEntries) throws SVNException{
			for(SVNLogEntry svnLogEntry : svnLogEntries){
				handleLogEntry(svnLogEntry);
			}
		}
		
		@Override
		public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
						
			//TODO: Extract relative-url (maybe it's in the logEntry.getRevisionProperties())
			
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
					nodeKind = svn.checkPath(
							entryPath.getPath(), logEntry.getRevision());
				}
									
				if (nodeKind.equals(SVNNodeKind.DIR)) {
					setFolder(entryPath, commit);
				}
				else if (nodeKind.equals(SVNNodeKind.FILE)){
					setFile(entryPath, commit); 
				}
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
		
		public List<CommitDTO> getCommits(){
			return commits;
		}
		
	}

}