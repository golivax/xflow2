package br.usp.ime.lapessc.xflow2.entity;

public enum DependencyGraphType {

	TASK_DEPENDENCY(0),
	TASK_ASSIGNMENT(1),
	COORDINATION_REQUIREMENTS(2);
	
	private int value;
	
	private DependencyGraphType(Integer value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
	
	
}
