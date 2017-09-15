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
 *  =============
 *  Resource.java
 *  =============
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.StringUtils;

@MappedSuperclass
public abstract class ArtifactVersion implements Serializable {
	
	private static final long serialVersionUID = 1569099270212350012L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "RESOURCE_ID")
	protected long id;
	
	@ManyToOne(optional = false)
	private Commit commit;
	
	@ManyToOne(optional = true)
	@JoinColumn(name = "PARENT_FOLDER")
	protected FolderVersion parentFolder;
	
	@Column(name = "RESOURCE_NAME", nullable = false)
	private String name;
	
	@OneToOne
	@JoinColumn(name = "DELETED_ON")
	private Commit deletedOn = null;
	
	@Column(name = "FILE_OPERATION", nullable = false)
	private char operationType;
	
	@Column(name = "PATH", nullable = false, 
	columnDefinition = "VARCHAR(255)")
	private String path;
	
	@Column(name = "COPY_PATH", nullable = true, 
			columnDefinition = "VARCHAR(255)")
	private String copyPath;
	
	@Column(name = "COPY_REVISION", nullable = true)
	private Long copyRevision;
	
	@Column(name = "PROP_MODS")
	private boolean propMods;
	
	@Column(name = "TEXT_MODS")
	private boolean textMods;
	
	@Column(name = "MERGEINFO_MODS")
	private boolean mergeInfoMod;
	
	@Column(name = "MERGED_FROM_PATH", nullable = true, 
			columnDefinition = "VARCHAR(255)")
	private String mergedFromPath;
	
	@Column(name = "MERGED_FROM_REV", nullable = true)
	private Long mergedFromRev;
	
	@ManyToOne(optional = true)
	private Branch branch;
	
	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public ArtifactVersion() {
	}
	
	public ArtifactVersion(final int id) {
		this.id = id;
	}
	
	public ArtifactVersion(final String name) {
		this.name = name;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id){
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
	
	public FolderVersion getParentFolder() {
		return parentFolder;
	}

	public void setParentFolder(final FolderVersion parent) {
		this.parentFolder = parent;
	}

	public Commit getDeletedOn() {
		return deletedOn;
	}

	public void setDeletedOn(final Commit deletedOn) {
		this.deletedOn = deletedOn;
	}
	
	public void setOperationType(final char operationType) {
		this.operationType = operationType;
	}

	public char getOperationType() {
		return operationType;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}
	
	public String getRelativePath() {
		String relativeURL = this.getCommit().getRelativeURL();
		String relativePath = StringUtils.substringAfter(this.getPath(), relativeURL + "/");
		return relativePath;
	}

	public boolean isPropMods() {
		return propMods;
	}

	public void setPropMods(boolean propMods) {
		this.propMods = propMods;
	}

	public boolean isTextMods() {
		return textMods;
	}

	public void setTextMods(boolean textMods) {
		this.textMods = textMods;
	}

	public boolean isMergeInfoMod() {
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

	public Commit getCommit() {
		return commit;
	}

	public void setCommit(Commit commit) {
		this.commit = commit;
	}
	
	public String toString(){
		return getPath();
	}

}