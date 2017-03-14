package br.usp.ime.lapessc.xflow2.metrics.cochange;

import org.apache.commons.collections4.Predicate;

import br.usp.ime.lapessc.xflow2.metrics.cochange.ChangeDependency;

public class ConvictionPredicate implements Predicate<ChangeDependency>{

	private double minConviction;
	
	public ConvictionPredicate(double minConviction){
		this.minConviction = minConviction;
	}

	public boolean evaluate(ChangeDependency changeDep) {
		return changeDep.getConviction() >= minConviction;
	}
}
