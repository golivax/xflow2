package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import br.usp.ime.lapessc.xflow2.repository.vcs.entities.VCSType;

@Entity(name = "vcsrepository")
public class VCSRepository{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "VCS_ID")
	private long id;
	
	private VCSType type;
	
	@Column(nullable = false)
	private String URI;
	
	public VCSRepository(){
		
	}
	
	public VCSRepository(VCSType type, String URI){
		this.type = type;
		this.URI = URI;
	}

	public VCSType getType() {
		return type;
	}

	public long getId() {
		return id;
	}

	public String getURI() {
		return URI;
	}

}