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
 *  ClusterCoefficient.java
 *  =======================
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.metrics.project;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.dao.metrics.ProjectMetricsDAO;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGEdge;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGGraph;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGVertex;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import edu.uci.ics.jung.graph.Graph;

public class ClusterCoefficient extends ProjectMetricModel {

	@Override
	public final void evaluate(final JUNGGraph graph, final ProjectMetricValues table) {
		if(graph == null) {
			table.setClusterCoefficient(0);
		}
		else {
			final Graph<JUNGVertex, JUNGEdge> dependencyGraph = graph.getGraph();
			final HashMap<JUNGVertex, Double> mapClusterCoef = (HashMap<JUNGVertex, Double>) edu.uci.ics.jung.algorithms.metrics.Metrics.clusteringCoefficients(dependencyGraph);

			double sumValues = 0;

			final Collection<Double>  values = mapClusterCoef.values();
			Iterator<Double> it = values.iterator();

			while (it.hasNext()) {
				sumValues += it.next();
			}

			double clusterCoefficient = sumValues/values.size();

			if (Double.valueOf(clusterCoefficient).isNaN()){
				clusterCoefficient = 0;
			}

			table.setClusterCoefficient(clusterCoefficient);
		}
	}

	@Override
	public final String getMetricName() {
		return "Cluster Coefficient";
	}

	@Override
	public final double getAverageValue(final Metrics metrics) throws DatabaseException {
		return new ProjectMetricsDAO().getClusterCoefficientAverageValue(metrics);
	}
	
	@Override
	public final double getStdDevValue(final Metrics metrics) throws DatabaseException {
		return new ProjectMetricsDAO().getClusterCoefficientDeviationValue(metrics);
	}

	@Override
	public double getMetricValue(final Metrics metrics, final Commit entry) throws DatabaseException {
		return new ProjectMetricsDAO().getClusterMetricValueByEntry(metrics, entry);
	}
	
}
