package br.usp.ime.lapessc.xflow2.metrics.cochange;

import org.apache.commons.collections4.Predicate;

import br.usp.ime.lapessc.xflow2.metrics.cochange.ChangeDependency;

public abstract class ConfidenceIntervalPredicate implements Predicate<ChangeDependency>{

	private double startInclusive;
	private double finishExclusive;
	
	public ConfidenceIntervalPredicate(double startInclusive, double finishExclusive){
		this.startInclusive = startInclusive;
		this.finishExclusive = finishExclusive;
	}

	public boolean evaluate(ChangeDependency changeDep) {
		return changeDep.getConfidence() >= startInclusive && 
				changeDep.getConfidence() < finishExclusive;
	}
}
