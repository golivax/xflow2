package br.usp.ime.lapessc.xflow2.metrics.cochange;

import org.apache.commons.collections4.Predicate;

import br.usp.ime.lapessc.xflow2.metrics.cochange.ChangeDependency;

public class ConfidencePredicate implements Predicate<ChangeDependency>{

	private double minConfidence;
	
	public ConfidencePredicate(double minConfidence){
		this.minConfidence = minConfidence;
	}

	public boolean evaluate(ChangeDependency changeDep) {
		return changeDep.getConfidence() >= minConfidence;
	}
}
