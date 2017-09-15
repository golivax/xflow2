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
 *  ===========
 *  Folder.java
 *  ===========
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity(name = "folder")
@Table(indexes = {
		@Index(name="file_operation_index", columnList="FILE_OPERATION"),
		@Index(name="file_path_index", columnList="PATH")})
public class FolderVersion extends ArtifactVersion implements Comparable<FolderVersion>{

	private static final long serialVersionUID = 8302324702620512851L;

	public FolderVersion(){

	}

	public FolderVersion(final String name) {
		super(name);
	}

	public FolderVersion(final String name, final FolderVersion parent) {
		super(name);
		this.parentFolder = parent;
	}

	@Override
	public int compareTo(final FolderVersion compared) {
		if(this.id < compared.getId()){
			return -1;
		}
		else if(this.id == compared.getId()){
			return 0;
		}
		return 1;
	}
}
