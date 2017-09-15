package br.usp.ime.lapessc.xflow2.repository.vcs.parser;

public abstract class ArtifactDTO {

	private String path;
	private char operationType;
	
	private String copyPath;
	private Long copyRevision;
	
	private boolean propMods;
	private boolean textMods;
	private boolean mergeInfoMod;
	
	private String mergedFromPath;
	private Long mergedFromRev;
	
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

	public String getCopyPath() {
		return copyPath;
	}

	public void setCopyPath(String copyPath) {
		this.copyPath = copyPath;
	}

	public Long getCopyRevision() {
		return copyRevision;
	}

	public void setCopyRevision(Long copyRevision) {
		this.copyRevision = copyRevision;
	}

	public boolean getPropMods() {
		return propMods;
	}

	public void setPropMods(boolean propMods) {
		this.propMods = propMods;
	}

	public boolean getTextMods() {
		return textMods;
	}

	public void setTextMods(boolean textMods) {
		this.textMods = textMods;
	}

	public boolean getMergeInfoMod() {
		return mergeInfoMod;
	}

	public void setMergeInfoMod(boolean mergeInfoMod) {
		this.mergeInfoMod = mergeInfoMod;
	}

	public String getMergedFromPath() {
		return mergedFromPath;
	}

	public void setMergedFromPath(String mergedFromPath) {
		this.mergedFromPath = mergedFromPath;
	}

	public Long getMergedFromRev() {
		return mergedFromRev;
	}

	public void setMergedFromRev(Long mergedFromRev) {
		this.mergedFromRev = mergedFromRev;
	}

		
}
