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
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = "study")
public class Study {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "STUDY_ID")
	private long id;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "STUDY_DATE", nullable = false)
	private Date date;
	
	private String name;
	
	@OneToMany(mappedBy = "study", cascade = CascadeType.REMOVE)
	private List<VCSMiningProject> vcsMiningProjects = 
		new ArrayList<VCSMiningProject>();

	//For JPA use only
	public Study(){}
	
	public Study(String name){
		this.name = name;
		this.date = new Date();
	}

	public void addVcsMiningProject(VCSMiningProject vcsMiningProject){
		vcsMiningProjects.add(vcsMiningProject);
	}
	
	public String getName() {
		return name;
	}

	public List<VCSMiningProject> getVcsMiningProjects() {
		return vcsMiningProjects;
	}
	
	public VCSMiningProject getVCSMiningProject(long id){
		for(VCSMiningProject vcsMiningProject : vcsMiningProjects){
			if(vcsMiningProject.getId() == id){
				return vcsMiningProject;
			}
		}
		
		return null;
	}
}