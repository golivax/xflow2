package br.usp.ime.lapessc.xflow2.core;

import java.util.Date;

import br.usp.ime.lapessc.xflow2.core.processors.callgraph.CallGraphAnalysis;
import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.SoftwareProject;
import br.usp.ime.lapessc.xflow2.entity.VCSMiningProject;
import br.usp.ime.lapessc.xflow2.entity.dao.core.AnalysisDAO;
import br.usp.ime.lapessc.xflow2.exception.core.analysis.AnalysisRangeException;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.repository.vcs.dao.CommitDAO;

public abstract class AnalysisFactory {

	//FIXME: Criar enums e matar isto
	public static final int COCHANGES_ANALYSIS = 1;
	public static final int CALLGRAPH_ANALYSIS = 2;
	
	public static Analysis createCoChangesAnalysis(VCSMiningProject project, 
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
		
		final Commit initialEntry;
		final Commit finalEntry;
		final CommitDAO entryDAO = new CommitDAO();
		
		if(analysis.isTemporalConsistencyForced()){
			initialEntry = entryDAO.findEntryFromSequence(project, startRevision);
			finalEntry = entryDAO.findEntryFromSequence(project, endRevision);
		}
		else{
			initialEntry = entryDAO.findEntryFromRevision(project, startRevision);
			finalEntry = entryDAO.findEntryFromRevision(project, endRevision);
		}
		
		if(isIntervalValid(initialEntry, finalEntry)){
			analysis.setInterval(initialEntry, finalEntry);
		}
	}
	
	private static final boolean isIntervalValid(final Commit initialEntry, final Commit finalEntry) throws DatabaseException, AnalysisRangeException {
		int entries = new CommitDAO().countEntriesByEntriesLimit(initialEntry, finalEntry);
		if(entries <= 0) throw new AnalysisRangeException();
		else return true;
	}
	
}