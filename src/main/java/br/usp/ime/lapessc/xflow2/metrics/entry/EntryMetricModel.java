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
 *  =====================
 *  EntryMetricModel.java
 *  =====================
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.metrics.entry;

import java.util.ArrayList;

import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.dao.metrics.EntryMetricsDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.metrics.MetricModel;

public abstract class EntryMetricModel implements MetricModel {

	abstract public void evaluate(Commit entry, EntryMetricValues table) throws DatabaseException;
	
	public final EntryMetricValues getMetricTable(final Metrics metrics, final Commit entry) throws DatabaseException {
		return new EntryMetricsDAO().findEntryMetricValuesByEntry(metrics, entry);
	}

	@Override
	public final ArrayList<EntryMetricValues> getAllMetricsTables(final Metrics metrics) throws DatabaseException {
		return new EntryMetricsDAO().getEntryMetricValues(metrics);
	}
}
