package br.usp.ime.lapessc.xflow2.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity(name="dependency")
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class DependencyGraph<Client extends DependencyObject, Supplier extends DependencyObject> {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "DEPENDENCY_ID")
	private long id;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "associatedDependency", targetEntity = DependencySet.class)
	private Set<DependencySet<Client, Supplier>> dependencies;
	
	@OneToOne
	@JoinColumn(name = "ENTRY_ID", nullable = false)
	private Commit associatedEntry;
	
	@ManyToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "ANALYSIS_ID", nullable = false)
	private Analysis associatedAnalysis;
	
	@Column(name = "DIRECTED_DEPENDENCY")
	private boolean directedDependency;
	
	@Column(name = "DEPENDENCY_TYPE")
	private int type;
	
	public DependencyGraph() {
		// Empty constructor.
	}

	public DependencyGraph(boolean isDirected){
		this.directedDependency = isDirected;
	}
	
	public long getId() {
		return id;
	}
	
	public void addDependency(DependencySet<Client, Supplier> dependencySet){
		if(dependencies == null){
			this.dependencies = new HashSet<>();
		}
		
		dependencySet.setAssociatedDependency(this);
		dependencies.add(dependencySet);
	}

	public void setDependencies(Set<DependencySet<Client, Supplier>> dependencies) {
		for (DependencySet<Client, Supplier> dependencySet : dependencies) {
			dependencySet.setAssociatedDependency(this);
		}
		this.dependencies = dependencies;
	}

	public Set<DependencySet<Client, Supplier>> getDependencies() {
		return dependencies;
	}

	public Commit getAssociatedEntry() {
		return associatedEntry;
	}

	public void setAssociatedEntry(final Commit associatedEntry) {
		this.associatedEntry = associatedEntry;
	}

	public Analysis getAssociatedAnalysis() {
		return associatedAnalysis;
	}

	public void setAssociatedAnalysis(final Analysis associatedAnalysis) {
		this.associatedAnalysis = associatedAnalysis;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isDirectedDependency() {
		return directedDependency;
	}

	public void setDirectedDependency(final boolean directedDependency) {
		this.directedDependency = directedDependency;
	}
	
	public Set<RawDependency<Client,Supplier,Commit>> getRawDependencies(){
		
		Set<RawDependency<Client,Supplier,Commit>> rawDeps = 
				new HashSet<RawDependency<Client,Supplier,Commit>>();
		
		for(DependencySet<Client,Supplier> dependencySet : getDependencies()){

			Supplier supplier = dependencySet.getSupplier();
			
			for(DependencyObject depObject : dependencySet.getClients()){
				
				Client client = (Client) depObject;
				
				RawDependency<Client,Supplier,Commit> rawDep = 
						new RawDependency<Client,Supplier,Commit>(
								client, supplier, this.getAssociatedEntry());
				
				rawDeps.add(rawDep);
			}			
		}
		
		return rawDeps;
	}
	
	public Set<Supplier> getSuppliers(){
		Set<Supplier> suppliers = new HashSet<>();
		for(DependencySet<Client,Supplier> dependencySet : getDependencies()){
			suppliers.add(dependencySet.getSupplier());
		}
		return suppliers;
	}
	
	public String toString(){
		String s = new String();
		for(RawDependency<Client, Supplier, Commit> rawDep: getRawDependencies()){
			s += rawDep.getClient() + " --> " + rawDep.getSupplier() + "\n"; 
		}
		return s;
	}
}
