package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity(name = "branch")
public class Branch {

	@Id
	@Column(name = "BRANCH_ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name = "RELATIVE_NAME")
	private String relativeName;
	
	@ManyToOne(optional = false)
	private Commit createdOn;
	
	@ManyToOne(optional = true)
	private Commit deletedOn;

	public Branch(){
		
	}
	
	public boolean isRoot() {
		return this.getRelativeName().equals("[root]");
	}

	public String getRelativeName() {
		return relativeName;
	}

	public void setRelativeName(String relativeName) {
		this.relativeName = relativeName;
	}
	
	public String getFullName() {
		String relativeURL = this.getCreatedOn().getRelativeURL();
		String fullName = relativeURL + "/" + relativeName;
		return fullName;
	}
	
	public Commit getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Commit createdOn) {
		this.createdOn = createdOn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdOn == null) ? 0 : createdOn.hashCode());
		result = prime * result + ((relativeName == null) ? 0 : relativeName.hashCode());
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
		Branch other = (Branch) obj;
		if (createdOn == null) {
			if (other.createdOn != null)
				return false;
		} else if (!createdOn.equals(other.createdOn))
			return false;
		if (relativeName == null) {
			if (other.relativeName != null)
				return false;
		} else if (!relativeName.equals(other.relativeName))
			return false;
		return true;
	}

	public Commit getDeletedOn() {
		return deletedOn;
	}

	public void setDeletedOn(Commit deletedOn) {
		this.deletedOn = deletedOn;
	}
	
	public boolean wasDeleted() {
		return deletedOn != null;
	}
	
	public String toString() {
		return this.getRelativeName();
	}
	
}
