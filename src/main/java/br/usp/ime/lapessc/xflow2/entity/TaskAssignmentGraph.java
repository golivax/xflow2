package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Entity;

@Entity(name="task_assignment")
public class TaskAssignmentGraph extends DependencyGraph<FileDependencyObject,AuthorDependencyObject>  {

	public TaskAssignmentGraph() {
		this.setDirectedDependency(true);
		this.setType(DependencyGraphType.TASK_ASSIGNMENT.getValue());
	}
	
}
