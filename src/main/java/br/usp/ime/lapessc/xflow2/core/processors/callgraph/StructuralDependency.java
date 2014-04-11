package br.usp.ime.lapessc.xflow2.core.processors.callgraph;

import java.util.HashMap;
import java.util.Map;

public class StructuralDependency {

	private String client;
	private Map<String,Integer> suppliers;	
	
	public StructuralDependency(){
		suppliers = new HashMap<String,Integer>();
	}

	public void addDegree(String supplier, Integer degree){
		Integer currentDegree = 0;
		if (suppliers.containsKey(supplier)){
			currentDegree = suppliers.get(supplier);	
		}
		suppliers.put(supplier, currentDegree + degree);
	}
	
	public String getClient(){
		return client;
	}
	
	public void setClient(String client){
		this.client = client;
	}
	
	public Integer getDegree(String supplier){
		return suppliers.get(supplier);
	}
	
	public boolean hasSupplier(String supplier){
		return suppliers.containsKey(supplier);
	}
	
	public String toString(){
		StringBuilder s = new StringBuilder();
		for(String supplier : suppliers.keySet()){
			s.append(client + " --> " + supplier + " [" + suppliers.get(supplier) + "]" + "\n");
		}
		return s.toString();
	}
	
}
