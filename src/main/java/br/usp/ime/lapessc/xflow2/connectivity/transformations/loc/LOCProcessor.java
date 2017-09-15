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
 *  =================
 *  LOCProcessor.java
 *  =================
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.connectivity.transformations.loc;

import br.usp.ime.lapessc.xflow2.entity.FileVersion;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.ArtifactDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.repository.vcs.entities.VCSType;


public final class LOCProcessor {

	private static LOCCounter locCounter;
	private static DiffHandler diffHandler;
	
	public final static void extractCodeInfo(FileVersion file) throws DatabaseException {
		locCounter = LOCProcessor.identifyProgrammingLanguage(file.getExtesion());

		switch (file.getOperationType()) {
		case 'A':
			countFileLOC(file);
			break;

		case 'M':
			diffHandler = LOCProcessor.createDiffHandler(
					file.getCommit().getVcsMiningProject().getMiningSettings().getVcs().getType());
			countFileModifications(file);
			break;
		}
	}

	private final static void countFileLOC(FileVersion file) {
		if(locCounter != null){
			final int totalLines = locCounter.countFileLOC(file.getSourceCode());
			file.setTotalLinesOfCode(totalLines);
			file.setAddedLinesOfCode(totalLines);
			file.setRemovedLinesOfCode(0);
			file.setModifiedLinesOfCode(0);
		}
		else{
			file.setTotalLinesOfCode(-1);
			file.setAddedLinesOfCode(-1);
			file.setRemovedLinesOfCode(-1);
			file.setModifiedLinesOfCode(-1);
		}
	}

	private final static void countFileModifications(FileVersion file) throws DatabaseException {
		if(locCounter != null){
			final int totalLines = locCounter.countFileLOC(file.getSourceCode());
			final int pastFileLOC = new ArtifactDAO().getPreviousLoC(
					file.getCommit().getVcsMiningProject(), file.getId(), file.getPath());

			diffHandler.gatherFileChanges(file);

			final int detectedLines = pastFileLOC + diffHandler.getAddedLines() - diffHandler.getDeletedLines();

			if(detectedLines != totalLines){
				if(detectedLines > totalLines){
					diffHandler.setDeletedLines(diffHandler.getDeletedLines() + (detectedLines - totalLines));
				}
				else{
					diffHandler.setAddedLines(diffHandler.getAddedLines() + (totalLines - detectedLines));
				}
			}
			
			file.setAddedLinesOfCode(diffHandler.getAddedLines());
			file.setRemovedLinesOfCode(diffHandler.getDeletedLines());
			file.setModifiedLinesOfCode(diffHandler.getModifiedLines());
			file.setTotalLinesOfCode(totalLines);
		}
		
		else{
			file.setAddedLinesOfCode(-1);
			file.setRemovedLinesOfCode(-1);
			file.setModifiedLinesOfCode(-1);
			file.setTotalLinesOfCode(-1);
		}
	}
	
	
	private final static LOCCounter identifyProgrammingLanguage(String extension) {

		if((extension.equalsIgnoreCase("c")) 
				|| (extension.equalsIgnoreCase("cpp"))
				|| (extension.equalsIgnoreCase("h"))
				|| (extension.equalsIgnoreCase("java"))){
			return new CBasedLinesCounter();
		}
		
		return null;
	}
	
	
	private final static DiffHandler createDiffHandler(VCSType vcsType) {
		switch (vcsType) {
			case Subversion:
				return new SVNDiffHandler(); 
	
			default:
				return null;
			}	
	}

	public final static LOCCounter getLocCounter() {
		return locCounter;
	}
}