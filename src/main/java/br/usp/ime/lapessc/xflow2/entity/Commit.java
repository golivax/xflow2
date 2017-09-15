/* 
 * 
 * XFlow
 * _______
 * 
 *  
 *  (C) Copyright 2010, by Universidade Federal do Pará (UFPA), Francisco Santana, Jean Costa, Pedro Treccani and Cleidson de Souza.
 * 
 *  This file is part of XFlow.
 *
 *  XFlow is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  XFlow is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XFlow.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  ==========
 *  Entry.java
 *  ==========
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Table(indexes = {@Index(name="entry_rev_index", columnList="ENTRY_REV")})
@Entity(name = "entry")
public class Commit implements Comparable<Commit>{

	@Id
	@Column(name = "ENTRY_ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name = "ENTRY_REV", nullable = false)
	private Long revision;
	
	@ManyToOne
	@JoinColumn(name = "ENTRY_PROJECT", nullable = false)
	private VCSMiningProject vcsMiningProject;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ENTRY_DATE", nullable = false)
	private Date date;
	
	@ManyToOne
	@JoinColumn(name = "ENTRY_AUTHOR", nullable = false)
	private Author author;
	
	@Column(name = "ENTRY_COMMENT", columnDefinition="MEDIUMTEXT", nullable = false)
	private String comment;
	
	@Column(name = "ENTRY_RELATIVEURL")
	private String relativeURL;

	@OneToMany(mappedBy = "commit", cascade = CascadeType.ALL)
	private List<FileVersion> entryFiles = new ArrayList<>();
	
	//Esse cascade salva as folders puras (isto é, que estão no log)
	@OneToMany(mappedBy = "commit", cascade = CascadeType.ALL)
	private List<FolderVersion> entryFolders = new ArrayList<>();
	
	public Commit() {
		
	}
	
	public Commit(final long revision, final Date date, final String comment, final Author author){
		this.revision = revision;
		this.date = date;
		this.author = author;
		this.comment = comment;
		this.entryFiles = new ArrayList<FileVersion>();
	}

	
	public long getId() {
		return id;
	}
	
	public void setId(long id){
		this.id = id;
	}

	public Long getRevision() {
		return revision;
	}
	
	public void setRevision(final long revision) {
		this.revision = revision;
	}

	public Date getDate() {
		return date;
	}
	
	public void setDate(final Date date) {
		this.date = date;
	}
	
	public Author getAuthor() {
		return author;
	}
	
	public void setAuthor(final Author author) {
		this.author = author;
	}
	
	public String getComment() {
		return comment;
	}
	
	public VCSMiningProject getVcsMiningProject() {
		return vcsMiningProject;
	}
	
	public void setVcsMiningProject(final VCSMiningProject project) {
		this.vcsMiningProject = project;
		project.addCommit(this);
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}
	
	public List<FileVersion> getEntryFiles() {
		return entryFiles;
	}
	
	public void setEntryFiles(final List<FileVersion> modifiedFiles) {
		this.entryFiles = modifiedFiles;
		
		for(FileVersion entryFile : entryFiles){
			entryFile.setCommit(this);
		}
	}
	
	public List<FolderVersion> getEntryFolders() {
		return entryFolders;
	}
	
	public void setEntryFolders(final List<FolderVersion> entryFolders) {
		this.entryFolders = entryFolders;
		
		for(FolderVersion entryFolder: entryFolders){
			entryFolder.setCommit(this);
		}
	}

	public String getListOfEntryFiles(){
		final String listOfFiles = new String();
		for (FileVersion file : entryFiles) {
			listOfFiles.concat(file.getPath()+"\n");
		}
		
		return listOfFiles;
	}
	
	public void addFile(FileVersion file){
		file.setCommit(this);
		entryFiles.add(file);
	}
	
	public void addFiles(List<FileVersion> files){
		for (FileVersion file : files){
			file.setCommit(this);
		}
		entryFiles.addAll(files);
	}
	
	public void addFolder(FolderVersion folder){
		folder.setCommit(this);
		entryFolders.add(folder);
	}
	
	public void addFolders(List<FolderVersion> folders){
		for(FolderVersion folder : folders){
			folder.setCommit(this);
		}
		entryFolders.addAll(folders);
	}
	
	public String getRelativeURL() {
		return relativeURL;
	}

	public void setRelativeURL(String relativeURL) {
		this.relativeURL = relativeURL;
	}

	public String toString(){
		return "Commit " + revision;
	}

	@Override
	public int compareTo(Commit otherCommit) {
		return this.getRevision().compareTo(otherCommit.getRevision());
	}
	
	public List<ArtifactVersion> getArtifacts(){
		List<ArtifactVersion> artifacts = new ArrayList<>();
		artifacts.addAll(this.getEntryFiles());
		artifacts.addAll(this.getEntryFolders());
		return artifacts;
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((revision == null) ? 0 : revision.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Commit other = (Commit) obj;
		if (revision == null) {
			if (other.revision != null)
				return false;
		} else if (!revision.equals(other.revision))
			return false;
		return true;
	}

}
