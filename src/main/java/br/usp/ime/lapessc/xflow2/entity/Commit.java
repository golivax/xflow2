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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

@Entity(name = "entry")
public class Commit{

	@Id
	@Column(name = "ENTRY_ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Index(name = "entry_rev_index")
	@Column(name = "ENTRY_REV", nullable = false)
	private long revision;
	
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
	
	@OneToMany(mappedBy = "commit", cascade = CascadeType.ALL)
	private List<FileArtifact> entryFiles = new ArrayList<FileArtifact>();
	
	//Esse cascade salva as folders puras (isto é, que estão no log)
	@OneToMany(mappedBy = "commit", cascade = CascadeType.ALL)
	private List<Folder> entryFolders = new ArrayList<Folder>();
	
	public Commit() {
		
	}
	
	public Commit(final long revision, final Date date, final String comment, final Author author){
		this.revision = revision;
		this.date = date;
		this.author = author;
		this.comment = comment;
		this.entryFiles = new ArrayList<FileArtifact>();
	}

	
	public long getId() {
		return id;
	}
	
	public void setId(long id){
		this.id = id;
	}

	public long getRevision() {
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
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}
	
	public List<FileArtifact> getEntryFiles() {
		return entryFiles;
	}
	
	public void setEntryFiles(final List<FileArtifact> modifiedFiles) {
		this.entryFiles = modifiedFiles;
		
		for(FileArtifact entryFile : entryFiles){
			entryFile.setCommit(this);
		}
	}
	
	public List<Folder> getEntryFolders() {
		return entryFolders;
	}
	
	public void setEntryFolders(final List<Folder> entryFolders) {
		this.entryFolders = entryFolders;
		
		for(Folder entryFolder: entryFolders){
			entryFolder.setCommit(this);
		}
	}

	public String getListOfEntryFiles(){
		final String listOfFiles = new String();
		for (FileArtifact file : entryFiles) {
			listOfFiles.concat(file.getPath()+"\n");
		}
		
		return listOfFiles;
	}
	
	public void addFile(FileArtifact file){
		file.setCommit(this);
		entryFiles.add(file);
	}
	
	public void addFiles(List<FileArtifact> files){
		for (FileArtifact file : files){
			file.setCommit(this);
		}
		entryFiles.addAll(files);
	}
	
	public void addFolder(Folder folder){
		folder.setCommit(this);
		entryFolders.add(folder);
	}
	
	public void addFolders(List<Folder> folders){
		for(Folder folder : folders){
			folder.setCommit(this);
		}
		entryFolders.addAll(folders);
	}
	

	public String toString(){
		return "Commit " + revision;
	}

}
