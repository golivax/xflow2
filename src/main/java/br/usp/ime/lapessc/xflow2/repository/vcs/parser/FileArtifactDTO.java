package br.usp.ime.lapessc.xflow2.repository.vcs.parser;


public class FileArtifactDTO extends ArtifactDTO{

	private String sourceCode;
	private String diffCode;
	
	public String getSourceCode() {
		return sourceCode;
	}
	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}
	public String getDiffCode() {
		return diffCode;
	}
	public void setDiffCode(String diffCode) {
		this.diffCode = diffCode;
	}
	
	
}
