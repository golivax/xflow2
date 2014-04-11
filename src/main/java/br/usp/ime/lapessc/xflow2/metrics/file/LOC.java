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
 *  ========
 *  LOC.java
 *  ========
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.metrics.file;

import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.FileArtifact;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.ArtifactDAO;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGGraph;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;


public final class LOC extends FileMetricModel {

	@Override
	public final void evaluate(final JUNGGraph dependencyGraph, final long fileID, final FileMetricValues table) throws DatabaseException {
		table.setLOC(new ArtifactDAO().findById(FileArtifact.class, fileID).getTotalLinesOfCode());
	}

	@Override
	public final String getMetricName() {
		return "Lines of Code";
	}

	@Override
	public final double getAverageValue(final Metrics metrics) {
		return 0;
	}
	
	@Override
	public final double getStdDevValue(final Metrics metrics) {
		return 0;
	}

	@Override
	public final double getMetricValue(final Metrics metrics, final Commit entry) throws DatabaseException {
		// TODO ??? o que fazer?? Maior? média?
		return 0;
	}
	
}
