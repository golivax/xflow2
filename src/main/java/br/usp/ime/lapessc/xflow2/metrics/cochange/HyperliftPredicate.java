package br.usp.ime.lapessc.xflow2.metrics.cochange;

import org.apache.commons.collections4.Predicate;

import br.usp.ime.lapessc.xflow2.metrics.cochange.ChangeDependency;

public class HyperliftPredicate implements Predicate<ChangeDependency>{

	private double minHyperlift;
	
	public HyperliftPredicate(double minHyperlift){
		this.minHyperlift = minHyperlift;
	}

	public boolean evaluate(ChangeDependency changeDep) {
		return changeDep.getHyperLift() >= minHyperlift;
	}
}
