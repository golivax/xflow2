package br.usp.ime.lapessc.xflow2.entity;

public enum DependencyObjectType {

	FILE_DEPENDENCY(1),
	AUTHOR_DEPENDENCY(2);
	
	private int value;
	
	private DependencyObjectType(Integer value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
}
