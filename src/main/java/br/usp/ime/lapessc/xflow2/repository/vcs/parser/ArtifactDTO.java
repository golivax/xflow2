package br.usp.ime.lapessc.xflow2.repository.vcs.parser;

public abstract class ArtifactDTO {

	private String path;
	private char operationType;
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public char getOperationType() {
		return operationType;
	}
	public void setOperationType(char operationType) {
		this.operationType = operationType;
	}
	
}
