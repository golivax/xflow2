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
 *  Author.java
 *  ===========
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


@Entity(name = "author")
public class Author {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "AUTH_ID")
	private long id;
	
	@ManyToOne
	@JoinColumn(name = "AUTH_PROJECT", nullable = false)
	private VCSMiningProject vcsMiningProject;
	
	@Column(name = "AUTH_NAME", nullable = false)
	private String name;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "AUTH_STARTDATE", nullable = false)
	private Date startDate;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "AUTH_LASTCONTRIB", nullable = false)
	private Date lastContribution;
	
	@OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE)
	private List<Commit> entries = new ArrayList<Commit>();

	public Author() {
	}
	
	public Author(final String name){
		this.name = name;
	}
	
	public Author(final String name, final Date startDate){
		this.name = name;
		this.startDate = startDate;
		this.lastContribution = startDate;
	}
	

	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public Date getLastContribution() {
		return lastContribution;
	}
	
	public List<Commit> getEntries() {
		return entries;
	}
	
	public void setLastContribution(final Date date) {
		this.lastContribution = date;
	}
	
	public VCSMiningProject getVcsMiningProject() {
		return vcsMiningProject;
	}
	
	public void setVcsMiningProject(final VCSMiningProject project) {
		this.vcsMiningProject = project;
		project.addAuthor(this);
	}

	public VCSMiningProject getProject() {
		return vcsMiningProject;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		Author other = (Author) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
