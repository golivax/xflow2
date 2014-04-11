package br.usp.ime.lapessc.xflow2.repository.vcs.parser.svn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

public class SVNLogParserUtils {

	private SVNRepository svn;
	
	public SVNLogParserUtils(SVNRepository svn){
		this.svn = svn;
	}
	
	public List<SVNLogEntry> getOrderedLogEntries(SVNRepository svn, 
			long startRevision,	long endRevision) throws SVNException{
	
		List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
		
		List<SVNLogEntry> chronologicalOrderedEntries = 
				(List<SVNLogEntry>) svn.log(new String[]{""}, 
				null, 0, -1, true, true);
			
		Collections.sort(chronologicalOrderedEntries, 
				SVNLogEntryDateComparator.getInstance());
	
		int startIndex = 0;
		while(chronologicalOrderedEntries.get(startIndex).getRevision() 
				!= startRevision){
			
			startIndex++;
		}
		
		int i = startIndex;
		while(chronologicalOrderedEntries.get(i).getRevision() 
				!= endRevision){
		
			logEntries.add(chronologicalOrderedEntries.get(i));
			i++;
		}
		
		return logEntries;
	}

	public String getFileOnRepository(final String filePath, 
			final long revision) throws SVNException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		svn.getFile(filePath, revision, null, baos);
		return baos.toString();
	}
	
	public String doDiff(final String URI, final String filePath, 
			final long revision1, final long revision2, 
			final boolean recursive) throws SVNException{
		
		String completeFilePath = getCompleteFilePath(URI, filePath);
		
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		
		SVNDiffClient diffClient = new SVNDiffClient(
				svn.getAuthenticationManager(), null);
		
		diffClient.doDiff(SVNURL.parseURIEncoded(completeFilePath), 
				SVNRevision.create(revision1), 
				SVNURL.parseURIEncoded(completeFilePath), 
				SVNRevision.create(revision2), 
				SVNDepth.EMPTY, true, result);
		
		return result.toString();
	}
	
	private String getCompleteFilePath(String URI, String filePath){
		
		String lcs = longestCommonSubstring(URI, filePath);
		
		String repoPath = StringUtils.remove(URI, lcs);
		String completeFilePath = repoPath.concat(filePath);
		return completeFilePath;
	}
	
	private static String longestCommonSubstring(String s1, String s2){
		int start = 0;
	    int max = 0;
	    for (int i = 0; i < s1.length(); i++){
	        for (int j = 0; j < s2.length(); j++){
	            int x = 0;
	            while (s1.charAt(i + x) == s2.charAt(j + x)){
	                x++;
	                if (((i + x) >= s1.length()) || ((j + x) >= s2.length())){ 
	                	break;
	                }
	            }
	            if (x > max){
	                max = x;
	                start = i;
	            }
	         }
	    }
	    return s1.substring(start, (start + max));
	}
}