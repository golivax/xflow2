package br.usp.ime.lapessc.xflow2.entity;

import java.util.Map;
import java.util.Set;

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
public class DependencySet<Client extends DependencyObject, Supplier extends DependencyObject> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "DEPENDENCYSET_ID")
	private long id;
	
	@ManyToOne(cascade = {CascadeType.MERGE,CascadeType.REMOVE}, targetEntity = DependencyObject.class)
	@JoinColumn(name = "DEPENDED_OBJECT_ID")
	private Supplier supplier;

	@ManyToOne(cascade = CascadeType.ALL, targetEntity = DependencyGraph.class)
	@JoinColumn(name = "DEPENDENCY_ID")
	private DependencyGraph<Client, Supplier> associatedDependency;
	
	//See http://download.oracle.com/javaee/6/api/javax/persistence/MapKeyJoinColumn.html
	@ElementCollection(targetClass = Integer.class)
	@CollectionTable(name = "dependent_object_dependencies", 
	    joinColumns = @JoinColumn(name = "DEPENDENCY_SET_ID"))
	@MapKeyJoinColumn(name="dependency_object", 
			referencedColumnName = "DEPENDENCY_OBJECT_ID")
	@Column(name = "DEPENDENCY_DEGREE")
	private Map<DependencyObject, Integer> clientsMap;

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public void setAssociatedDependency(DependencyGraph<Client, Supplier> associatedDependency) {
		this.associatedDependency = associatedDependency;
	}

	public DependencyGraph<Client, Supplier> getAssociatedDependency() {
		return associatedDependency;
	}

	public Set<DependencyObject> getClients(){
		return clientsMap.keySet();
	}
	
	public Map<? extends DependencyObject, Integer> getClientsMap() {
		return clientsMap;
	}

	public void setClientsMap(Map<Client, Integer> clientsMap) {
		this.clientsMap = (Map<DependencyObject, Integer>) clientsMap;
	}

	public long getId() {
		return id;
	} 	
}