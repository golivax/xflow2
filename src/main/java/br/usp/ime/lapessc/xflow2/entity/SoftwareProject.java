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
 *  Project.java
 *  ============
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity(name = "swproject")
public class SoftwareProject implements Serializable{

	private static final long serialVersionUID = 7907150525824812352L;

	//TODO: Add URL and license fields
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PRJ_ID")
	private long id;

	@Column(name = "PRJ_NAME", nullable = false)
	private String name;
	
	@ManyToOne(cascade = CascadeType.ALL)
	private VCSRepository vcsRepository;
	
	public SoftwareProject(String name){
		this.name = name;
	}
			
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
    
	public void setName(final String name) {
    	this.name = name;
    }
	
	public String getName() {
		return name;
	}
	
	public VCSRepository getVcsRepository() {
		return vcsRepository;
	}

	public void setVcsRepository(VCSRepository vcsRepository) {
		this.vcsRepository = vcsRepository;
	}

}
