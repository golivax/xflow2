package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Entity;

@Entity(name="task_assignment")
public class TaskAssignment extends DependencyGraph<AuthorDependencyObject, FileDependencyObject>  {

	public TaskAssignment() {
		// Empty constructor.
		this.setDirectedDependency(true);
		this.setType(TASK_ASSIGNMENT);
	}
	
}
