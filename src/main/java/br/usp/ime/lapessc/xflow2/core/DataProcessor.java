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
 *  ==================
 *  DataProcessor.java
 *  ==================
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.core;

import java.util.List;

import br.usp.ime.lapessc.xflow2.core.processors.DependenciesIdentifier;
import br.usp.ime.lapessc.xflow2.core.processors.callgraph.CallGraphCollector;
import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesCollector;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.AnalysisType;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.dao.core.AnalysisDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.repository.vcs.dao.CommitDAO;
import br.usp.ime.lapessc.xflow2.util.Filter;

public final class DataProcessor {

	public static final void processCommits(final Analysis analysis, 
			final Filter filter) throws DatabaseException{
		
		CommitDAO entryDAO = new CommitDAO();
		
		//TODO: Replace conditional with Polymorphism
		DependenciesIdentifier depIdentifier = null;
		
		if (analysis.getType() == AnalysisType.COCHANGES_ANALYSIS.getValue()){
			depIdentifier = new CoChangesCollector();
		}
		else if(analysis.getType() == AnalysisType.CALLGRAPH_ANALYSIS.getValue()){
			depIdentifier = new CallGraphCollector();
		}
		
		final List<Long> revisions;

		if(analysis.isTemporalConsistencyForced()){
			//Retrieving all entries is crazy, so we just look for revision numbers
			revisions = entryDAO.getAllRevisionsWithinEntries(
					analysis.getFirstEntry(), 
					analysis.getLastEntry(), 
					analysis.getMaxFilesPerRevision());
		}
		else{
			//Same from above
			revisions = entryDAO.getAllRevisionsWithinRevisions(
					analysis.getProject(), 
					analysis.getFirstEntry().getRevision(), 
					analysis.getLastEntry().getRevision(),
					analysis.getMaxFilesPerRevision());
		}
		 
		depIdentifier.identifyDependencies(revisions, analysis, filter);
	}
	
	public static final void resumeProcess(final Analysis analysis, final long finalRevision, final Filter filter, final String details) throws DatabaseException{
		
		CommitDAO entryDAO = new CommitDAO();
		DependenciesIdentifier context = new CoChangesCollector();

		final List<Long> revisions;
		
		if(analysis.isTemporalConsistencyForced()){
			long previousLastEntrySequence = entryDAO.findEntrySequence(analysis.getProject(), analysis.getLastEntry());
			Commit initialEntry = entryDAO.findEntryFromSequence(analysis.getProject(), (previousLastEntrySequence+1));
			Commit finalEntry = entryDAO.findEntryFromSequence(analysis.getProject(), finalRevision);
			revisions = entryDAO.getAllRevisionsWithinEntries(initialEntry, finalEntry, analysis.getMaxFilesPerRevision());
			analysis.setLastEntry(finalEntry);
		}
		else{
			revisions = entryDAO.getAllRevisionsWithinRevisions(analysis.getProject(), analysis.getLastEntry().getRevision()+1, finalRevision, analysis.getMaxFilesPerRevision());
			Commit finalEntry = entryDAO.findEntryFromRevision(analysis.getProject(), finalRevision);
			analysis.setLastEntry(finalEntry);
		}
		 
		context.identifyDependencies(revisions, analysis, filter);
		
		analysis.setDetails(details);		
		new AnalysisDAO().update(analysis);
	}
	
	public static final void resumeProcess(final Analysis analysis, final Commit finalEntry, final Filter filter, final String details) throws DatabaseException{
		
		CommitDAO entryDAO = new CommitDAO();
		DependenciesIdentifier context = new CoChangesCollector();
		
		long previousLastEntrySequence = entryDAO.findEntrySequence(analysis.getProject(), analysis.getLastEntry());
		Commit initialEntry = entryDAO.findEntryFromSequence(analysis.getProject(), (previousLastEntrySequence+1));
		List<Long> revisions = entryDAO.getAllRevisionsWithinEntries(initialEntry, finalEntry, analysis.getMaxFilesPerRevision());
		
		context.identifyDependencies(revisions, analysis, filter);
		
		Long lastRevision = revisions.get(revisions.size()-1);
		Commit lastEntry = entryDAO.findEntryFromRevision(analysis.getProject(), lastRevision);
		analysis.setLastEntry(lastEntry);
		
		if(!details.equals(analysis.getDetails())){
			analysis.setDetails(details);
		}
		
		new AnalysisDAO().update(analysis);
	}

}
