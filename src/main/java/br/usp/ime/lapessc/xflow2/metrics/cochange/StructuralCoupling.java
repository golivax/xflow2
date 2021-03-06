package br.usp.ime.lapessc.xflow2.metrics.cochange;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Index;

@Entity(name = "structuralcoupling")
public class StructuralCoupling {

	public StructuralCoupling(){}
	
	@Id
	@Column(name = "SC_ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "client", columnDefinition = "VARCHAR(350)")
	private String client;
	
	@Index(name = "clientStamp_index")
	private int clientStamp;
	
	@Column(name = "supplier", columnDefinition = "VARCHAR(350)")
	private String supplier;
	
	@Index(name = "supplierStamp_index")
	private int supplierStamp;
	
	private double degree;
	
	public StructuralCoupling(String client, int clientStamp, String supplier, 
			int supplierStamp, int totalCalls, int clientChanges){
		
		this.client = client;
		this.clientStamp = clientStamp;
		
		this.supplier = supplier;
		this.supplierStamp = supplierStamp;
		
		//Structural coupling calculation
		//this.degree = (double)totalCalls/clientChanges;
		this.degree = totalCalls;
	}
	
	public String getClient() {
		return client;
	}
	
	public int getClientStamp() {
		return clientStamp;
	}

	public String getSupplier() {
		return supplier;
	}
	
	public int getSupplierStamp() {
		return supplierStamp;
	}

	public double getDegree() {
		return degree;
	}
	
	public String toString(){
		String dependency = client + " -> " + supplier;
		String degree = "Average Degree: " + this.degree;
		return dependency + "\n" + degree + "\n"; 
	}
}