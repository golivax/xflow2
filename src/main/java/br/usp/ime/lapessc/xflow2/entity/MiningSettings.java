package br.usp.ime.lapessc.xflow2.entity;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import br.usp.ime.lapessc.xflow2.util.Filter;

@Entity(name = "vcs_settings")
public class MiningSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "VCS_ANALYSIS_ID")
	private long id;
	
	@ManyToOne
	private VCSRepository vcs;
	
	@Embedded
	private AccessCredentials accessCredentials;
	
	public MiningSettings(){
		
	}
	
	public MiningSettings(VCSRepository vcs, 
			AccessCredentials accessCredentials, String details, Filter filter,	
			boolean codeDownloadEnabled, boolean temporalConsistencyForced){
		
		this.vcs = vcs;
		this.accessCredentials = accessCredentials;
		this.details = details;
		this.filter = filter;
		this.codeDownloadEnabled  = codeDownloadEnabled;
		this.temporalConsistencyForced = temporalConsistencyForced;
	
	}
	
	public VCSRepository getVcs() {
		return vcs;
	}

	@Column(name = "DETAILS")
	private String details;
		
	@Embedded
	private Filter filter;
	
	@Column(name = "CODE_DOWNLOAD_ENABLED", nullable = false)
	private boolean codeDownloadEnabled;
	
	@Column(name = "TEMPORAL_CONSISTENCY_FORCED", nullable = false)
	private boolean temporalConsistencyForced = false;

	public long getId() {
		return id;
	}

	public AccessCredentials getAccessCredentials() {
		return accessCredentials;
	}

	public String getDetails() {
		return details;
	}

	public Filter getFilter(){
		return filter;
	}
	
	public boolean isCodeDownloadEnabled() {
		return codeDownloadEnabled;
	}

	public boolean isTemporalConsistencyForced() {
		return temporalConsistencyForced;
	}

	
}
