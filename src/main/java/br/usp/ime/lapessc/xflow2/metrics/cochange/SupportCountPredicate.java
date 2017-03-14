package br.usp.ime.lapessc.xflow2.metrics.cochange;

import org.apache.commons.collections4.Predicate;

import br.usp.ime.lapessc.xflow2.metrics.cochange.ChangeDependency;

public class SupportCountPredicate implements Predicate<ChangeDependency>{

	private double minSupport;
	
	public SupportCountPredicate(double minSupport){
		this.minSupport = minSupport;
	}

	public boolean evaluate(ChangeDependency changeDep) {
		return changeDep.getSupportCount() >= minSupport;
	}
}
