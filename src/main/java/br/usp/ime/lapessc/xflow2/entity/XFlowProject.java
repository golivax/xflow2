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
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name = "xflowproject")
public class XFlowProject {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "XFLOWPRJ_ID")
	private long id;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "PRJ_DATE", nullable = false)
	private Date date;
	
	@OneToOne(cascade = CascadeType.ALL)
	private SoftwareProject softwareProject;
	
	@OneToMany(cascade = CascadeType.ALL)
	private List<VCSMiningProject> vcsMiningProjects = 
		new ArrayList<VCSMiningProject>();
	
	
	public XFlowProject(String projectName){
		softwareProject = new SoftwareProject(projectName);
		date = new Date();
	}
	
	public VCSRepository getVcsRepository(){
		return softwareProject.getVcsRepository();
	}
	
	public void setVcsRepository(VCSRepository vcsRepository) {
		softwareProject.setVcsRepository(vcsRepository);
	}
	
	public void addVcsMiningProject(VCSMiningProject vcsMiningProject){
		vcsMiningProjects.add(vcsMiningProject);
	}
	
}