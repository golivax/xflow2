package br.usp.ime.lapessc.xflow2.entity;

public enum AnalysisType {
	
	COCHANGES_ANALYSIS(1),
	CALLGRAPH_ANALYSIS(2);

	private int value;
	
	private AnalysisType(Integer value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
		
}
