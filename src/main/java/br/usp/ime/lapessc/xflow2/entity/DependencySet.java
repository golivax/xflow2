package br.usp.ime.lapessc.xflow2.entity;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;

@Entity(name = "dependency_set")
public class DependencySet<Client extends DependencyObject, Dependent extends DependencyObject> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "DEPENDENCYSET_ID")
	public long id;
	
	@ManyToOne(cascade = {CascadeType.MERGE,CascadeType.REMOVE}, targetEntity = DependencyObject.class)
	@JoinColumn(name = "DEPENDED_OBJECT_ID")
	private Client dependedObject;

	@ManyToOne(cascade = CascadeType.ALL, targetEntity = DependencyGraph.class)
	@JoinColumn(name = "DEPENDENCY_ID")
	private DependencyGraph<Client, Dependent> associatedDependency;
	
	//See http://download.oracle.com/javaee/6/api/javax/persistence/MapKeyJoinColumn.html
	@ElementCollection(targetClass = Integer.class)
	@CollectionTable(name = "DEPENDENT_OBJECT_DEPENDENCIES", 
	    joinColumns = @JoinColumn(name = "DEPENDENCY_SET_ID"))
	@MapKeyJoinColumn(name="dependency_object", 
			referencedColumnName = "DEPENDENCY_OBJECT_ID")
	@Column(name = "DEPENDENCY_DEGREE")
	private Map<DependencyObject, Integer> dependenciesMap;

	public Client getDependedObject() {
		return dependedObject;
	}

	public void setDependedObject(Client dependedObject) {
		this.dependedObject = dependedObject;
	}

	public void setAssociatedDependency(DependencyGraph<Client, Dependent> associatedDependency) {
		this.associatedDependency = associatedDependency;
	}

	public DependencyGraph<Client, Dependent> getAssociatedDependency() {
		return associatedDependency;
	}

	public Map<? extends DependencyObject, Integer> getDependenciesMap() {
		return dependenciesMap;
	}

	public void setDependenciesMap(Map<Dependent, Integer> dependenciesMap) {
		this.dependenciesMap = (Map<DependencyObject, Integer>) dependenciesMap;
	}

	public long getId() {
		return id;
	} 
	
}
