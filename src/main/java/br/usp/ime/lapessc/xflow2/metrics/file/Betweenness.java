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
 *  ================
 *  Betweenness.java
 *  ================
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.metrics.file;

import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.dao.metrics.FileMetricsDAO;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGEdge;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGGraph;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGVertex;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;

public class Betweenness extends FileMetricModel {

	@Override
	public void evaluate(final JUNGGraph graph, final long fileID, final FileMetricValues table) {
		final Graph<JUNGVertex, JUNGEdge> dependencyGraph = graph.getGraph();
		final BetweennessCentrality<JUNGVertex, JUNGEdge> ranker = new BetweennessCentrality<JUNGVertex, JUNGEdge>(graph.getGraph());
		ranker.setRemoveRankScoresOnFinalize(false);
		ranker.evaluate();

		JUNGVertex vertex = null;
		if(FileMetricModel.verticesCache.containsKey(fileID)){
			vertex = FileMetricModel.verticesCache.get(fileID);
		} else {
			for (JUNGVertex graphVertex : dependencyGraph.getVertices()) {
				if(graphVertex.getId() == fileID){
					vertex = graphVertex;
					FileMetricModel.verticesCache.put(fileID, vertex);
					break;
				}
			}
		}

		double betweenness = ranker.getVertexRankScore(vertex);
		table.setBetweennessCentrality(betweenness);
		betweenness = betweenness / (((dependencyGraph.getVertexCount() - 1) * (dependencyGraph.getVertexCount() - 2))/2);
		betweenness = (Double.valueOf(betweenness).isNaN() ? 0 : betweenness);
		table.setNormalizedBetweennessCentrality(betweenness);
	}

	@Override
	public final String getMetricName() {
		return "Betweenness Centrality";
	}

	@Override
	public final double getAverageValue(final Metrics metrics) throws DatabaseException {
		return new FileMetricsDAO().getBetweennessAverageValue(metrics);
	}
	
	@Override
	public final double getStdDevValue(final Metrics metrics) throws DatabaseException {
		return new FileMetricsDAO().getBetweennessDeviationValue(metrics);
	}

	@Override
	public final double getMetricValue(final Metrics metrics, final Commit entry) throws DatabaseException {
		return new FileMetricsDAO().getBetweennessValueByEntry(metrics, entry);
	}
}
