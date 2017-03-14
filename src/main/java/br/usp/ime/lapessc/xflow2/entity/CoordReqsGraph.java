package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Entity;

@Entity(name="coordination_requirements")
public class CoordReqsGraph extends DependencyGraph<AuthorDependencyObject, AuthorDependencyObject> {
	
	public CoordReqsGraph() {
		this.setDirectedDependency(false);
		this.setType(DependencyGraphType.COORDINATION_REQUIREMENTS.getValue());
	}
	
}
