package br.usp.ime.lapessc.xflow2.repository.vcs.parser.svn;

import java.util.Comparator;

import org.tmatesoft.svn.core.SVNLogEntry;

public class SVNLogEntryDateComparator implements Comparator<SVNLogEntry> {

	private static SVNLogEntryDateComparator instance = 
			new SVNLogEntryDateComparator();
	
	private SVNLogEntryDateComparator() {
		
	}
	
	public static SVNLogEntryDateComparator getInstance(){
		return instance;
	}
	
	public int compare(SVNLogEntry entry, SVNLogEntry otherEntry) {
		return entry.getDate().compareTo(otherEntry.getDate());
	}

}
