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
 *  =======================
 *  ProjectMetricModel.java
 *  =======================
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.metrics.project;

import java.util.ArrayList;

import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.dao.metrics.ProjectMetricsDAO;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGGraph;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.metrics.MetricModel;

public abstract class ProjectMetricModel implements MetricModel {

	public abstract void evaluate(JUNGGraph dependencyGraph, ProjectMetricValues table);
	
	public ProjectMetricValues getMetricTable(Metrics metrics, Commit entry) throws DatabaseException {
		return new ProjectMetricsDAO().findProjectMetricValuesByEntry(metrics, entry);
	}
	
	@Override
	public final ArrayList<ProjectMetricValues> getAllMetricsTables(Metrics metrics) throws DatabaseException {
		return new ProjectMetricsDAO().getProjectMetricValues(metrics);
	}
}
