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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.fs.FSPathChange;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNPropertyData;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import br.usp.ime.lapessc.xflow2.entity.AccessCredentials;
import br.usp.ime.lapessc.xflow2.entity.MiningSettings;
import br.usp.ime.lapessc.xflow2.exception.cm.CMException;
import br.usp.ime.lapessc.xflow2.exception.cm.svn.SVNProtocolNotSupportedException;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.ArtifactDTO;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.CommitDTO;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.FileArtifactDTO;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.FolderArtifactDTO;
import br.usp.ime.lapessc.xflow2.repository.vcs.parser.VCSLogParser;

public class SVNLogParser implements VCSLogParser {
		
	private static final Logger logger = LogManager.getLogger();
	
	private String relativeURL;	
	private MiningSettings miningSettings;
	
	private SVNRepository svn;
	private SVNLogParserUtils parserUtils;
	
	public SVNLogParser(MiningSettings miningSettings) 
			throws SVNProtocolNotSupportedException{
		 
		this.miningSettings = miningSettings;
		
		//FIXME: SVN should be closed when this parser is done (create method releaseResources or smth like that)
		this.svn = SVNFactory.create(miningSettings.getVcs().getURI(), miningSettings.getAccessCredentials()); 
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
				
				SVNClientManager svnClientManager = getSVNClientManager();
				SVNLogClient logClient = svnClientManager.getLogClient();
				
			    SVNURL url = SVNURL.parseURIEncoded(miningSettings.getVcs().getURI());
			    
				String[] paths = miningSettings.getPaths().toArray(new String[0]);
				
				SVNRevision pegRev = miningSettings.getPegRev() != null ?
						SVNRevision.create(miningSettings.getPegRev()) :
						SVNRevision.UNDEFINED;
				
				//Defaults to peg if SVNRevision.UNDEFINED is provided
	            SVNRevision startRevision = SVNRevision.create(startCommit);
	            
	            //Defaults to 0 if SVNRevision.UNDEFINED is provided
	            SVNRevision endRevision = SVNRevision.create(endCommit);
	            
	            boolean stopOnCopy = false;
	            boolean includePaths = true;
	            boolean includeMergedRevisions = false;
	            	            
	            long limit = -1;  // no limit
			    
	            String[] revProperties = null;
	            
			    logClient.doLog(
			    		url, paths, pegRev, startRevision, endRevision, 
			    		stopOnCopy, includePaths, includeMergedRevisions, limit,
			    		revProperties, logEntryHandler);
			    
			    svnClientManager.dispose();
				
			}

		}catch(SVNException e){
			logger.error("Problem during the invocation of 'svn log' via SVNKit");
			logger.error("The stack trace is as follows", e);
		}
		
		return logEntryHandler.getCommits();
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
			
			logger.info("Parsing commit {} from SVN",logEntry.getRevision());

			CommitDTO commit = new CommitDTO();
			processRevision(logEntry, commit);
			processRelativeURL(logEntry, commit);				
			
			//Parsing the Artifacts	
			for (SVNLogEntryPath logEntryPath : logEntry.getChangedPaths().values()) {
			
				FSPathChange fsPathChange = (FSPathChange) logEntryPath;				
				SVNNodeKind nodeKind = fsPathChange.getKind();
				
				//Sometimes SVNKit is not able to retrieve the node kind, so we must ask it explicitly
				if (nodeKind.equals(SVNNodeKind.UNKNOWN)){
					nodeKind = svn.checkPath(fsPathChange.getPath(), logEntry.getRevision());
				}
										
				if (nodeKind.equals(SVNNodeKind.DIR)) {
					processFolder(fsPathChange, commit);
				}
				else if (nodeKind.equals(SVNNodeKind.FILE)){
					processFile(fsPathChange, commit);
				}
			}

			//If the commit has no artifacts, then something weird happened and we discard it
			if(commit.getFileArtifacts().isEmpty() && commit.getFolderArtifacts().isEmpty()){
				
				logger.error("Revision {} is empty", commit.getRevision());
			}
			//Otherwise, we parse the rest of the data
			else{
				
				processAuthor(logEntry, commit);
				processDate(logEntry, commit);
				processComment(logEntry, commit);
				
				commits.add(commit);
			}
        }	

		private void processRevision(SVNLogEntry logEntry, CommitDTO commit) {
			commit.setRevision(logEntry.getRevision());
		}
		
		private void processFile(FSPathChange fsPathChange, CommitDTO commit) throws SVNException {		
			
			if (miningSettings.getFilter().match(fsPathChange.getPath())){
				
				FileArtifactDTO fileArtifactDTO = miningSettings.isCodeDownloadEnabled() ? 
						getFileWithCode(fsPathChange, commit) : getFileWithoutCode(fsPathChange, commit);
				
				commit.addFileArtifact(fileArtifactDTO);
			}
		}
		
		private FileArtifactDTO getFileWithCode(FSPathChange fsPathChange, CommitDTO commit) throws SVNException{
			
			FileArtifactDTO file = getFileWithoutCode(fsPathChange, commit);
			
			try{
			
				//If the file has not been deleted, then we grab its source code
				if (file.getOperationType() != 'D'){
					
					String sourceCode = parserUtils.getFileOnRepository(fsPathChange.getPath(), commit.getRevision());
					file.setSourceCode(sourceCode);	
				}
						
				//if the file has been modified, then we also grab its diff code
				if (file.getOperationType() == 'M'){
					
					String diffCode = parserUtils.doDiff(
							miningSettings.getVcs().getURI(), fsPathChange.getPath(), 
							commit.getRevision() - 1, commit.getRevision(), false);
					
					file.setDiffCode(diffCode);	
				}
				
			}catch(SVNException e){
				logger.error("Error while fetching the contents and properties for file {} at revision {}", 
						fsPathChange.getPath(), commit.getRevision());
				
				logger.error("The stack trace is as follows", e);
			}
			
			return file;
		}
		
		private FileArtifactDTO getFileWithoutCode(FSPathChange fsPathChange, CommitDTO commit) throws SVNException{
			
			FileArtifactDTO file = new FileArtifactDTO();
			processArtifactInfo(fsPathChange, file, commit);
			
			return file;
		}
		
		private void processFolder(FSPathChange fsPathChange, CommitDTO commit) throws SVNException{
			
			if (miningSettings.getFilter().match(fsPathChange.getPath())){
								
				FolderArtifactDTO folder = new FolderArtifactDTO();
				processArtifactInfo(fsPathChange, folder, commit);				
				commit.addFolderArtifact(folder);
			}
		}
		
		private void processArtifactInfo(FSPathChange fsPathChange, ArtifactDTO artifact, CommitDTO commit) throws SVNException {
			
			artifact.setPath(fsPathChange.getPath());
			artifact.setOperationType(fsPathChange.getType());
			
			if(fsPathChange.getCopyPath() != null) {
				
				logger.info("Copy path found for folder {} at {}.", 
						fsPathChange.getPath(), commit.getRevision());
				
				logger.info("Copy path is {} and copy revision is {}", 
						fsPathChange.getCopyPath(), fsPathChange.getCopyRevision());
				
				artifact.setCopyPath(fsPathChange.getCopyPath());
				artifact.setCopyRevision(fsPathChange.getCopyRevision());
			}
			
			artifact.setPropMods(fsPathChange.arePropertiesModified());
			artifact.setTextMods(fsPathChange.isTextModified());
			artifact.setMergeInfoMod(fsPathChange.getMergeInfoModified());
			
			if(fsPathChange.getMergeInfoModified()) {
				logger.info("Merge info changed for {}", fsPathChange.getPath());
				
				SVNClientManager svnClientManager = getSVNClientManager();				
				SVNWCClient workingCopyClient = svnClientManager.getWCClient();
				
				String repositoryRoot = svn.getRepositoryRoot(true).toString();
				String filePath = fsPathChange.getPath();
				String fullPath = repositoryRoot + filePath;
				
				SVNPropertyData svnMergeInfoProp = workingCopyClient.doGetProperty(
							SVNURL.parseURIEncoded(fullPath), "svn:mergeinfo", 
							SVNRevision.create(commit.getRevision()), SVNRevision.create(commit.getRevision()));
				
				if(svnMergeInfoProp == null) {
					logger.warn("Looks like merge info did not actually change. Bug in SVNKit?");
					artifact.setMergeInfoMod(false);
				}
				else {
					String mergeInfoValue = svnMergeInfoProp.getValue().getString();
					
					String mergedFromPath = StringUtils.substringBefore(mergeInfoValue, ":");
					artifact.setMergedFromPath(mergedFromPath);
					
					//MergedFromRevs can be empty, a revision, or a list of revision separated by comma. So this code
					//deals with all these situations
					String mergedFromRevs = StringUtils.substringAfter(mergeInfoValue, ":");				
					String mergedFromRev;
					if(mergedFromRevs.contains(",")) {
						mergedFromRev = StringUtils.substringAfterLast(mergedFromRevs, ",");					
					}
					else {
						mergedFromRev = mergedFromRevs;
					}
					
					if(NumberUtils.isCreatable(mergedFromRev)) {
						artifact.setMergedFromRev(Long.parseLong(mergedFromRev));
					}
				}
				
				//Closing the client
				svnClientManager.dispose();				
			}
		}

		private void processAuthor(SVNLogEntry logEntry, CommitDTO commit) {
			commit.setAuthorName(logEntry.getAuthor());
		}			

		private void processDate(SVNLogEntry logEntry, CommitDTO commit) {
			commit.setDate(logEntry.getDate());
		}
		
		private void processComment(SVNLogEntry logEntry, CommitDTO commit) {
			if (logEntry.getMessage() == null) commit.setComment("");
			else commit.setComment(logEntry.getMessage());
		}
		
		private void processRelativeURL(SVNLogEntry logEntry, CommitDTO commit) throws SVNException{
			
			boolean changedRelativeURL = false;
			
			String slashRelativeURL = "/" + relativeURL;
			for(SVNLogEntryPath svnLogEntryPath : logEntry.getChangedPaths().values()) {
				
				if(relativeURL == null || !svnLogEntryPath.getPath().startsWith(slashRelativeURL)){
					
					changedRelativeURL = true;
					break;
				}
			}
			
			if(changedRelativeURL) {
				SVNClientManager svnClientManager = getSVNClientManager();
				SVNWCClient workingCopyClient = svnClientManager.getWCClient();
				
				SVNInfo svnInfo = workingCopyClient.doInfo(
						SVNURL.parseURIEncoded(miningSettings.getVcs().getURI()), 
						SVNRevision.UNDEFINED, SVNRevision.create(logEntry.getRevision()));		
				
				relativeURL = svnInfo.getPath();
				
				svnClientManager.dispose();
			}
			
			commit.setRelativeURL(relativeURL);			
		}

		public List<CommitDTO> getCommits(){
			return commits;
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

	private SVNClientManager getSVNClientManager() {
		
		AccessCredentials accessCredentials = miningSettings.getAccessCredentials();
		
		ISVNAuthenticationManager authManager = 
				SVNWCUtil.createDefaultAuthenticationManager(accessCredentials.getUsername(), 
						accessCredentials.getPassword().toCharArray());
		
		return SVNClientManager.newInstance(null, authManager);
	}
}