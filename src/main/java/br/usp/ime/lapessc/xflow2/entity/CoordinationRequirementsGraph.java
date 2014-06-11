package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Entity;

@Entity(name="coordination_requirements")
public class CoordinationRequirementsGraph extends DependencyGraph<AuthorDependencyObject, AuthorDependencyObject> {
	
	public CoordinationRequirementsGraph() {
		this.setDirectedDependency(false);
		this.setType(DependencyGraphType.COORDINATION_REQUIREMENTS.getValue());
	}
	
}
