package br.usp.ime.lapessc.xflow2.repository.vcs.parser;

import java.util.List;

import br.usp.ime.lapessc.xflow2.exception.cm.CMException;

public class BufferedVCSLogParser{

	private int BUFFER_SIZE = 5000;
	private int INVOCATIONS_COUNT = 0;
	private long REMAINING_COMMITS;
	
	private VCSLogParser vcsLogParser;
	
	public BufferedVCSLogParser(VCSLogParser vcsLogParser){
		
		this.vcsLogParser = vcsLogParser;	
		
		this.REMAINING_COMMITS = 
				vcsLogParser.getSettings().getLastRev() - 
				vcsLogParser.getSettings().getFirstRev() + 1;
	}
	
	public BufferedVCSLogParser(VCSLogParser vcsLogParser, int buffersize){
		
		this(vcsLogParser);
		this.BUFFER_SIZE = buffersize; 
	}
	
	//Move this to data extractor
	public List<CommitDTO> parse() throws CMException {
		
		long startCommit = vcsLogParser.getSettings().getFirstRev();
		long endCommit = vcsLogParser.getSettings().getLastRev();
		
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
