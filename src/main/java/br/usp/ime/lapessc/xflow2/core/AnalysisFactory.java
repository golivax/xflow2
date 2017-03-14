package br.usp.ime.lapessc.xflow2.core;

import java.util.Date;
import java.util.List;

import br.usp.ime.lapessc.xflow2.core.processors.callgraph.CallGraphAnalysis;
import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.core.processors.coordreq.CoordReqsAnalysis;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.VCSMiningProject;
import br.usp.ime.lapessc.xflow2.entity.dao.core.AnalysisDAO;
import br.usp.ime.lapessc.xflow2.exception.core.analysis.AnalysisRangeException;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.repository.vcs.dao.CommitDAO;

public abstract class AnalysisFactory {
	
	public static CoordReqsAnalysis createCoordReqAnalysis(
			CoChangesAnalysis coChangesAnalysis) 
					throws DatabaseException, AnalysisRangeException {
		
		CoordReqsAnalysis coordReqAnalysis = 
				new CoordReqsAnalysis(coChangesAnalysis);

		//Sets up analysis attributes
		
		coordReqAnalysis.setProject(coChangesAnalysis.getProject());
		coordReqAnalysis.setDetails("Coordination Requirements");
		coordReqAnalysis.setDate(new Date());
		
		coordReqAnalysis.setTemporalConsistencyForced(
				coChangesAnalysis.isTemporalConsistencyForced());
		
		coordReqAnalysis.setInterval(
				coChangesAnalysis.getFirstEntry(), 
				coChangesAnalysis.getLastEntry());
		
		coordReqAnalysis.setMaxFilesPerRevision(
				coChangesAnalysis.getMaxFilesPerRevision());
		
		new AnalysisDAO().insert(coordReqAnalysis);
		
		return coordReqAnalysis;
	}
	
	public static CoChangesAnalysis createCoChangesAnalysis(VCSMiningProject project, 
			String details, final int supportValue, final int confidenceValue, 
			final int maxFilesPerRevision, 
			final boolean persistCoordinationRequirements) 
			throws DatabaseException, AnalysisRangeException {
		
		List<Commit> commits = project.getCommits();
		long startRevision = commits.get(0).getRevision();
		long endRevision = commits.get(commits.size() - 1).getRevision();		
		
		return createCoChangesAnalysis(project, 
				details, false, startRevision, endRevision, 
				supportValue, confidenceValue, 
				maxFilesPerRevision, persistCoordinationRequirements);
	}
	
	public static CoChangesAnalysis createCoChangesAnalysis(VCSMiningProject project, 
			String details, boolean temporalConsistencyForced, 
			final long startRevision, final long endRevision, 
			final int supportValue, final int confidenceValue, 
			final int maxFilesPerRevision, 
			final boolean persistCoordinationRequirements) 
			throws DatabaseException, AnalysisRangeException {
		
		CoChangesAnalysis analysis = new CoChangesAnalysis();
		
		//Sets up analysis attributes
		setupAnalysis(analysis, project, details, temporalConsistencyForced,
				startRevision, endRevision);
		
		//Sets up specific Co-Changes Attributes
		analysis.setConfidenceValue(confidenceValue);
		analysis.setSupportValue(supportValue);
		analysis.setMaxFilesPerRevision(maxFilesPerRevision);
		analysis.setCoordinationRequirementPersisted(persistCoordinationRequirements);
		
		new AnalysisDAO().insert(analysis);
		
		return analysis;
	}
	
	public static Analysis createCallGraphAnalysis(VCSMiningProject project, 
			String details, boolean temporalConsistencyForced, 
			final long startRevision, final long endRevision, 
			final int maxFilesPerRevision) throws DatabaseException, 
			AnalysisRangeException {
		
		CallGraphAnalysis analysis = new CallGraphAnalysis();
		
		//Sets up analysis attributes
		setupAnalysis(analysis, project, details, temporalConsistencyForced,
				startRevision, endRevision);
		
		//Sets up specific Call-Graph Attributes
		analysis.setMaxFilesPerRevision(maxFilesPerRevision);
		
		new AnalysisDAO().insert(analysis);
		
		return analysis;
	}

	private static void setupAnalysis(Analysis analysis, 
			VCSMiningProject project, String details, boolean temporalConsistencyForced, 
			final long startRevision, final long endRevision) throws DatabaseException, 
			AnalysisRangeException {
	
		//Analysis attributes
		analysis.setProject(project);
		analysis.setDetails(details);
		analysis.setDate(new Date());
		analysis.setTemporalConsistencyForced(temporalConsistencyForced);
		
		final Commit initialCommit;
		final Commit finalCommit;
		final CommitDAO commitDAO = new CommitDAO();
		
		if(analysis.isTemporalConsistencyForced()){
			initialCommit = commitDAO.findEntryFromSequence(project, startRevision);
			finalCommit = commitDAO.findEntryFromSequence(project, endRevision);
		}
		else{
			initialCommit = commitDAO.findEntryFromRevision(project, startRevision);
			finalCommit = commitDAO.findEntryFromRevision(project, endRevision);
		}
		
		if(isIntervalValid(initialCommit, finalCommit)){
			analysis.setInterval(initialCommit, finalCommit);
		}
	}
	
	private static final boolean isIntervalValid(final Commit initialEntry, final Commit finalEntry) throws DatabaseException, AnalysisRangeException {
		int entries = new CommitDAO().countEntriesByEntriesLimit(initialEntry, finalEntry);
		if(entries <= 0) throw new AnalysisRangeException();
		else return true;
	}
	
}