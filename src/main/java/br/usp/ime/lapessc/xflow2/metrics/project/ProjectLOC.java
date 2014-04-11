package br.usp.ime.lapessc.xflow2.metrics.project;

import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGGraph;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class ProjectLOC extends ProjectMetricModel {

	@Override
	public double getAverageValue(Metrics metrics) throws DatabaseException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getStdDevValue(Metrics metrics) throws DatabaseException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMetricName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getMetricValue(Metrics metrics, Commit entry)
			throws DatabaseException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void evaluate(JUNGGraph dependencyGraph, ProjectMetricValues table) {
		// TODO Auto-generated method stub
		
	}

}
