package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name="dependency_object")
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class DependencyObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "DEPENDENCY_OBJECT_ID")
	private long id;
	
	@Column(name = "DEPENDENCY_STAMP", nullable = false)
	private int assignedStamp;
	
	@ManyToOne
	@JoinColumn(name = "ANALYSIS_ID", nullable = false)
	private Analysis analysis;
	
	@Column(name = "OBJECT_TYPE", nullable = false)
	final private int objectType;
	
	
	public DependencyObject(final int objectType) {
		this.objectType = objectType;
	}
	
	public abstract String getDependencyObjectName();
	

	public long getId() {
		return id;
	}

	public int getAssignedStamp() {
		return assignedStamp;
	}

	public void setAssignedStamp(final int assignedStamp) {
		this.assignedStamp = assignedStamp;
	}

	public Analysis getAnalysis() {
		return analysis;
	}

	public void setAnalysis(final Analysis analysis) {
		this.analysis = analysis;
	}

	public int getObjectType() {
		return objectType;
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
		DependencyObject other = (DependencyObject) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
	
}
