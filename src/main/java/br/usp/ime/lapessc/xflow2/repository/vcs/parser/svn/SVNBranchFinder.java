package br.usp.ime.lapessc.xflow2.repository.vcs.parser.svn;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import br.usp.ime.lapessc.xflow2.entity.AccessCredentials;
import br.usp.ime.lapessc.xflow2.entity.ArtifactVersion;
import br.usp.ime.lapessc.xflow2.entity.Branch;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.FolderVersion;
import br.usp.ime.lapessc.xflow2.entity.MiningSettings;
import br.usp.ime.lapessc.xflow2.entity.VCSMiningProject;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.VCSMiningProjectDAO;
import br.usp.ime.lapessc.xflow2.entity.database.EntityManagerHelper;
import br.usp.ime.lapessc.xflow2.util.io.Kbd;

public class SVNBranchFinder {

	private static final Logger logger = LogManager.getLogger();
	
	public SVNBranchFinder(){
		
	}
	
	public void run(String tmpDir, Long xflowVCSMiningProjectID){
		try{
			
			EntityManager entityManager = EntityManagerHelper.getEntityManager();
			entityManager.setFlushMode(FlushModeType.COMMIT);
						
			Map<String,Branch> branches = new TreeMap<>();
			Map<String,Branch> elementaryBranches = new TreeMap<>();
			
			VCSMiningProject vcsMiningProject = 
					new VCSMiningProjectDAO().findById(VCSMiningProject.class, xflowVCSMiningProjectID);
			 
			MiningSettings miningSettings = vcsMiningProject.getMiningSettings();
			
			int numCommits = vcsMiningProject.getCommits().size();
			for(int i = 0; i < numCommits; i++){
				
				Commit commit = vcsMiningProject.getCommits().get(i);
				logger.info("Processing commit {}/{}: {}", (i+1), numCommits, commit.getRevision());
				
				boolean commitIsDone = isCommitDone(commit);
				if(commitIsDone) {
					
					//Load map
					for(ArtifactVersion artifact : commit.getArtifacts()) {
						if(!branches.containsKey(artifact.getBranch().getRelativeName())){
							branches.put(artifact.getBranch().getRelativeName(), artifact.getBranch());
						}	
					}
					
					//Clean map
					cleanUpKeys(branches,elementaryBranches);
					
					//Go to next commit
					continue;
				}
				
				//We only open a transaction if commit is not done! :-)
				entityManager.getTransaction().begin();
				
				List<ArtifactVersion> artifacts = commit.getArtifacts();
				Collections.sort(artifacts, new Comparator<ArtifactVersion>() {

					@Override
					public int compare(ArtifactVersion a1, ArtifactVersion a2) {
						return a1.getRelativePath().compareTo(a2.getRelativePath());
					}
					
				});
				
				for(ArtifactVersion artifact : artifacts){
					
					if(artifact.getBranch() == null){
					
						//Try names already seen
						String branchName = searchForBranchesInMap(artifact,branches);
						
						//If a name was found
						if(branchName != null) {
							//Is artifact actually a more specific branch compared to branchName found?
							if(isBranchNameOutdated(branchName,artifact)) {
								branchName = null;
							}
						}
						
						//If it is an elementary branch
						if(branchName == null) {
							
							String relativePath = null;
							if(artifact instanceof FolderVersion) {
								relativePath = artifact.getRelativePath();
							}
							else {
								relativePath = StringUtils.substringBeforeLast(artifact.getRelativePath(), "/");
							}
							
							Branch branch = elementaryBranches.get(relativePath);
							if(branch != null) {
								//Temporarily add elementary branch to map, just so the association can be done
								//(it will be cleaned up right after the association)
								branchName = branch.getRelativeName();
								branches.put(branchName, branch);
							}							
						}
						
						//Not luck, let's try to use heuristics
						if(branchName == null){
							
							branchName = findBranchNameUsingHeuristics(artifact,commit);
							
							//Did not find branch names using heuristics
							if(branchName == null){
								
								logger.info("No luck!");
								
								boolean showAssist = Kbd.readBoolean("Need list of folders to double check branch? ");
								
								if(showAssist) {
									showAssistInfo(tmpDir, commit, miningSettings);
								}
								
								branchName = Kbd.readString("Please enter branch: ");
							}							
						}
												
						if(!branches.containsKey(branchName)){
							
							logger.info("*****************************");
							logger.info("Found a new branch!");
							logger.info("Branches found so far are: {}", branches.keySet());
							logger.info("--> The new branch is: {}", branchName);
							logger.info("Artifact relative path is: {}", artifact.getRelativePath());
							
							boolean showAssist = Kbd.readBoolean("Need list of folders to double check branch? ");
							
							if(showAssist) {
								showAssistInfo(tmpDir, commit, miningSettings);	
							}							
								
							String response = Kbd.readString("Now press ENTER to confirm the branch name found. "
									+ "If it is not correct, please type the correct branch name: ");
							
							if(!response.isEmpty()){
								branchName = response;
							}
							
							if(!branches.containsKey(branchName)){
								Branch branch = new Branch();
								branch.setRelativeName(branchName);
								branch.setCreatedOn(commit);
								
								//Add to map
								branches.put(branchName,branch);																
								
								//Persists branch
								entityManager.persist(branch);
							}						
						}	
						
						Branch branch = branches.get(branchName);
						artifact.setBranch(branch);
						
						//Cleaning up branches that would likely be a prefix of future branches
						cleanUpKeys(branches,elementaryBranches);
					}
					else if(!branches.containsKey(artifact.getBranch().getRelativeName())){
						branches.put(artifact.getBranch().getRelativeName(), artifact.getBranch());
					}
				}
				
				//Refreshing "deleted on"
				for(ArtifactVersion artifact : artifacts) {
					
					List<String> branchesToRemove = new ArrayList<>();
					if(artifact instanceof FolderVersion) {
						
						//If folder was deleted
						if(artifact.getOperationType() == 'D') {
							
							String folderPath = artifact.getRelativePath();
							
							List<String> allBranches = new ArrayList<>();
							allBranches.addAll(branches.keySet());
							allBranches.addAll(elementaryBranches.keySet());
							for(String branch : allBranches) {
								
								//Folder path is a prefix of a branch
								if(branch.startsWith(folderPath) || branch.equals(folderPath)) {
									branchesToRemove.add(branch);
								}
							}
							
						}						
					}
					
					for(String branchToRemove : branchesToRemove) {
						
						logger.info("Removing branch {}", branchToRemove);
						
						Branch branch = branches.get(branchToRemove);
						if(branch == null) branch = elementaryBranches.get(branchToRemove);
						
						branch.setDeletedOn(commit);
						branches.remove(branchToRemove);
						elementaryBranches.remove(branchToRemove);
					}
				}
				
				//Commiting the transaction
				entityManager.getTransaction().commit();
				
				//Hack to free up resources. It clears the persistence context every 500 commits
				if(i % 500 == 0) {
					entityManager.clear();
					vcsMiningProject = new VCSMiningProjectDAO().findById(VCSMiningProject.class, xflowVCSMiningProjectID);
				}
			}
			
			logger.info("Found branches were: ");
			for(String branch : branches.keySet()){
				logger.info(branch);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e);
		}
	}

	private boolean isBranchNameOutdated(String branchName, ArtifactVersion artifact) {
		
		if(artifact instanceof FolderVersion) {
			String afterBranchName = StringUtils.substringAfter(artifact.getRelativePath(), branchName);
			if(afterBranchName.contains("/branches/") || afterBranchName.endsWith("/branches") ||
				afterBranchName.contains("/tags/") || afterBranchName.endsWith("/tags") || 
				afterBranchName.contains("/trunk/") || afterBranchName.endsWith("/trunk")) {
				
				return true;
			}
		}
		
		return false;
	}

	private void cleanUpKeys(Map<String, Branch> branches, Map<String, Branch> elementaryBranches) {
		List<String> keys = new ArrayList<>(branches.keySet());
		for(String key : keys) {
			if(key.equals("branches") || key.endsWith("/branches") ||
				key.equals("tags") || key.endsWith("/tags")) {
				
				elementaryBranches.put(key, branches.get(key));
				branches.remove(key);				
			}
		}
	}

	/**
	 * Checks if branches are set for all commit's artifacts
	 * @param commit
	 * @return
	 */
	private boolean isCommitDone(Commit commit) {
		boolean commitIsDone = true;
		for(ArtifactVersion resource : commit.getArtifacts()){
			if(resource.getBranch() == null) {
				commitIsDone = false;
				break;
			}
		}
		return commitIsDone;
	}

	private String searchForBranchesInMap(ArtifactVersion f, Map<String, Branch> branches) throws IOException, SVNException{
				
		String path = f.getRelativePath();
		
		List<String> branchNames = new ArrayList<>(branches.keySet());
		Collections.sort(branchNames);
		Collections.reverse(branchNames);
		
		for(String branchName : branchNames){
			if(path.startsWith(branchName + "/") || path.equals(branchName)) {
				return branchName;
			}
		}
		
		return null;
	}

	private void showAssistInfo(String temp, Commit commit, MiningSettings miningSettings) throws IOException, SVNException {
		
		logger.info("Producing repository list for branch check up");
		
		List<String> dirs = new ArrayList<>();
		
		SVNURL url = SVNURL.parseURIEncoded(miningSettings.getVcs().getURI());
		SVNClientManager svnClientManager = getSVNClientManager(miningSettings);
		svnClientManager.getLogClient().doList(url, null, SVNRevision.create(commit.getRevision()), 
				false, SVNDepth.INFINITY, SVNDirEntry.DIRENT_ALL, new ISVNDirEntryHandler() {
			
			@Override
			public void handleDirEntry(SVNDirEntry dirEntry) throws SVNException {
				if(dirEntry.getKind().equals(SVNNodeKind.DIR)){
					dirs.add(dirEntry.getRelativePath());
				}
			}
		});		
		
		String dirsAsString = StringUtils.join(dirs,"\n");
				
		FileUtils.writeStringToFile(
				new File(temp + "/files.txt"), 
				dirsAsString,
				Charset.forName("UTF-8"));
		
		logger.info("Done");
	}
	
	
	private String findBranchNameUsingHeuristics(ArtifactVersion resource, Commit commit) throws Exception{
		String relativePath = resource.getRelativePath();
		if(relativePath.isEmpty()) {
			return "[root]";
		}
		
		if(relativePath.equals("trunk") || relativePath.startsWith("trunk/")) {
			return "trunk";
		}
		
		if(relativePath.contains("/trunk/")){
			String beforeTrunk = StringUtils.substringBefore(relativePath, "/trunk/");
			return beforeTrunk + "/trunk";
		}
		
		if(relativePath.endsWith("/trunk")) {
			return relativePath;
		}
		
		if(relativePath.equals("branches")) {
			return "branches";
		}
	
		if(relativePath.startsWith("branches/")) {
			String branchesOnwards = StringUtils.substringAfter(relativePath, "branches/");
			String branchName = StringUtils.substringBefore(branchesOnwards, "/");
			return "branches/" + branchName;	
		}
		
		if(relativePath.contains("/branches/")){
			String beforeBranches = StringUtils.substringBefore(relativePath, "/branches/");
			String branchesOnwards = StringUtils.substringAfter(relativePath, "/branches/");
			String branchName = StringUtils.substringBefore(branchesOnwards, "/");
			return beforeBranches + "/branches/" + branchName;			
		}
		
		if(relativePath.endsWith("/branches")) {
			return relativePath;
		}
		
		if(relativePath.equals("tags")) {
			return "tags";
		}
		
		if(relativePath.startsWith("tags/")) {
			String branchesOnwards = StringUtils.substringAfter(relativePath, "tags/");
			String branchName = StringUtils.substringBefore(branchesOnwards, "/");
			return "tags/" + branchName;	
		}
		
		if(relativePath.contains("/tags/")){
			String beforeTags = StringUtils.substringBefore(relativePath, "/tags/");
			String tagsOnwards = StringUtils.substringAfter(relativePath, "/tags/");
			String tagName = StringUtils.substringBefore(tagsOnwards, "/");
			return beforeTags + "/tags/" + tagName;			
		}
		
		if(relativePath.endsWith("/tags")) {
			return relativePath;
		}
		
		if(relativePath.equals("sandbox") || relativePath.startsWith("sandbox/")) {
			return "sandbox";
		}
		
		if(relativePath.contains("/sandbox/")){
			String beforeSandbox = StringUtils.substringBefore(relativePath, "/sandbox/"); 
			String sandboxOnwards = StringUtils.substringAfter(relativePath, "/sandbox/");
			String branchName = StringUtils.substringBefore(sandboxOnwards, "/");
			return beforeSandbox + "/sandbox/" + branchName;			
		}
		
		if(relativePath.startsWith("/src/main/java") || 
				relativePath.startsWith("/src/test/java") ||
				relativePath.startsWith("/src/java") || 
				relativePath.startsWith("/java/src") ||
				relativePath.startsWith("/java/test") ||
				relativePath.startsWith("/test/java")){
			
			return "[root]";
		}
		
		logger.info("Could not find branch for " + relativePath);
		return null;
	}
	
	private SVNClientManager getSVNClientManager(MiningSettings miningSettings) {
		
		AccessCredentials accessCredentials = miningSettings.getAccessCredentials();
		
		ISVNAuthenticationManager authManager = 
				SVNWCUtil.createDefaultAuthenticationManager(accessCredentials.getUsername(), 
						accessCredentials.getPassword().toCharArray());
		
		return SVNClientManager.newInstance(null, authManager);
	}

	public static void main(String[] args) {
		String tempFolder = "/home/local/SAIL/goliva/tmp/xflow";
		Long xflowVCSMiningProjectID = 17102L;
		
		new SVNBranchFinder().run(tempFolder, xflowVCSMiningProjectID);	
	}
	
}
