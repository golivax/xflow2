package br.usp.ime.lapessc.xflow2.repository.vcs.parser;

import java.util.List;

import br.usp.ime.lapessc.xflow2.exception.cm.CMException;

public class BufferedVCSLogParser{

	private int BUFFER_SIZE = 1000;
	private int INVOCATIONS_COUNT = 0;
	private long REMAINING_COMMITS;
	
	private VCSLogParser vcsLogParser;
	private long startCommit;
	private long endCommit;
	
	public BufferedVCSLogParser(VCSLogParser vcsLogParser, long startCommit, 
			long endCommit){
		
		this.vcsLogParser = vcsLogParser;
		this.startCommit = startCommit;
		this.endCommit= endCommit;
		
		this.REMAINING_COMMITS = endCommit - startCommit + 1;
	}
	
	public BufferedVCSLogParser(VCSLogParser vcsLogParser, long startCommit, 
			long endCommit, int buffersize){
		
		this(vcsLogParser, startCommit, endCommit);
		this.BUFFER_SIZE = buffersize; 
	}
	
	//Move this to data extractor
	public List<CommitDTO> parse() throws CMException {
		
		long i = startCommit + (INVOCATIONS_COUNT * BUFFER_SIZE);
		long j = i + BUFFER_SIZE - 1;
		if (j > endCommit) j = endCommit;
		
		List<CommitDTO> commits = vcsLogParser.parse(i, j);
		
		INVOCATIONS_COUNT++;
		REMAINING_COMMITS -= (j - i + 1);
		
		return commits;
	}
	
	public boolean hasNotEnded(){
		return REMAINING_COMMITS > 0;
	}

}
