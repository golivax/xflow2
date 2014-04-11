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
 *  ============
 *  ObjFile.java
 *  ============
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity(name = "file")
public class FileArtifact extends Resource implements Comparable<FileArtifact>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9048464688545200449L;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "FILE_ENTRY", nullable = false)
	private Commit commit;
	
	@Column(name = "FILE_CODE", columnDefinition="LONGTEXT")
	private String sourceCode;
	
	@Transient
	private String diffCode;
	
	@Column(name = "FILE_EXTENSION", nullable = false)
	private String extesion;
	
	@Column(name = "LOC")
	private int totalLinesOfCode;
	
	@Column(name = "ADDED_LOC")
	private int addedLinesOfCode;
	
	@Column(name = "REMOVED_LOC")
	private int removedLinesOfCode;
	
	@Column(name = "MODIFIED_LOC")
	private int modifiedLinesOfCode;
	
	
	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(final String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public String getDiffCode() {
		return diffCode;
	}

	public void setDiffCode(final String diffCode) {
		this.diffCode = diffCode;
	}

	public String getExtesion() {
		return extesion;
	}

	public void setExtesion(final String extesion) {
		this.extesion = extesion;
	}
	
	public void setCommit(final Commit commit) {
		this.commit = commit;
	}

	public Commit getCommit() {
		return commit;
	}

	public void setTotalLinesOfCode(final int linesOfCode) {
		this.totalLinesOfCode = linesOfCode;
	}

	public int getTotalLinesOfCode() {
		return totalLinesOfCode;
	}

	public int getAddedLinesOfCode() {
		return addedLinesOfCode;
	}

	public void setAddedLinesOfCode(final int addedLinesOfCode) {
		this.addedLinesOfCode = addedLinesOfCode;
	}

	public int getRemovedLinesOfCode() {
		return removedLinesOfCode;
	}

	public void setRemovedLinesOfCode(final int removedLinesOfCode) {
		this.removedLinesOfCode = removedLinesOfCode;
	}

	public int getModifiedLinesOfCode() {
		return modifiedLinesOfCode;
	}

	public void setModifiedLinesOfCode(final int modifiedLinesOfCode) {
		this.modifiedLinesOfCode = modifiedLinesOfCode;
	}

	public int compareTo(final FileArtifact compared) {
		if(this.id < compared.getId()){
			return -1;
		}
		else if(this.id == compared.getId()){
			return 0;
		}
		return 1;
	}
	
	public String toString(){
		return getPath();
	}
}
