package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Entity;

@Entity(name="task_dependency")
public class TaskDependency extends DependencyGraph<FileDependencyObject, FileDependencyObject>  {
	
	public TaskDependency(){};
	
	public TaskDependency(boolean isDirected){
		super(isDirected);
		this.setType(TASK_DEPENDENCY);
	}
	
}
