package br.usp.ime.lapessc.xflow2.entity;

public class RawDependency<Client,Supplier,Label> {

	
	private Client client;
	private Supplier supplier;
	private Label label;
	
	public RawDependency(Client client, Supplier supplier, Label label){
		this.client = client;
		this.supplier = supplier;
		this.label = label;
	}

	public Client getClient() {
		return client;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public Label getLabel() {
		return label;
	}
	
	
	
	
}
