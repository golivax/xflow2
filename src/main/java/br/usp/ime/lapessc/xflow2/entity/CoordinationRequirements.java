package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Entity;

@Entity(name="coordination_requirements")
public class CoordinationRequirements extends DependencyGraph<AuthorDependencyObject, AuthorDependencyObject> {
	
	public CoordinationRequirements() {
		//Empty constructor.
		this.setDirectedDependency(false);
		this.setType(COORD_REQUIREMENTS);
	}
	
}
