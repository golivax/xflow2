package br.usp.ime.lapessc.xflow2.repository.vcs.parser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommitDTO {

	private long revision;	
	private Date date;
	private String authorName;
	private String comment;
	
	private List<FileArtifactDTO> fileArtifacts = 
			new ArrayList<FileArtifactDTO>();
	
	private List<FolderArtifactDTO> folderArtifacts = 
			new ArrayList<FolderArtifactDTO>();
	
	public long getRevision() {
		return revision;
	}
	
	public void setRevision(long revision) {
		this.revision = revision;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getAuthorName() {
		return authorName;
	}
	
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public void addFileArtifact(FileArtifactDTO fileArtifactDTO){
		this.fileArtifacts.add(fileArtifactDTO);
	}
	
	public void addFolderArtifact(FolderArtifactDTO folderArtifactDTO){
		this.folderArtifacts.add(folderArtifactDTO);
	}

	public List<FileArtifactDTO> getFileArtifacts() {
		return fileArtifacts;
	}

	public void setFileArtifacts(List<FileArtifactDTO> fileArtifacts) {
		this.fileArtifacts = fileArtifacts;
	}

	public List<FolderArtifactDTO> getFolderArtifacts() {
		return folderArtifacts;
	}

	public void setFolderArtifacts(List<FolderArtifactDTO> folderArtifacts) {
		this.folderArtifacts = folderArtifacts;
	}
}
