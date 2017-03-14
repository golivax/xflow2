package br.usp.ime.lapessc.xflow2.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
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
	
	@Column(name = "DETAILS")
	private String details;
		
	@Embedded
	private Filter filter;
	
	@Column(name = "CODE_DOWNLOAD_ENABLED", nullable = false)
	private boolean codeDownloadEnabled;
	
	@Column(name = "TEMPORAL_CONSISTENCY_FORCED", nullable = false)
	private boolean temporalConsistencyForced = false;

	@Column(name = "FIRSTREV")
	private long firstRev;

	@Column(name = "LASTREV")
	private long lastRev;
	
	@Column(name = "PEGREV")
	private Long pegRev;

	@ElementCollection
	private List<String> paths;
	
	//For JPA use only
	public MiningSettings(){
		
	}
	
	public MiningSettings(VCSRepository vcs, 
			AccessCredentials accessCredentials, String details, long firstRev, 
			long lastRev, Filter filter, boolean codeDownloadEnabled, 
			boolean temporalConsistencyForced){
		
		this(vcs,accessCredentials,details,firstRev,lastRev,null,null,filter,
				codeDownloadEnabled,temporalConsistencyForced);
	}
	
	public MiningSettings(VCSRepository vcs, 
			AccessCredentials accessCredentials, String details, long firstRev, 
			long lastRev, List<String> paths, Filter filter,	
			boolean codeDownloadEnabled, boolean temporalConsistencyForced){
		
		this(vcs,accessCredentials,details,firstRev,lastRev,null,paths,filter,
				codeDownloadEnabled,temporalConsistencyForced);
	}
	
	public MiningSettings(VCSRepository vcs, 
			AccessCredentials accessCredentials, String details, long firstRev, 
			long lastRev, Long pegRev, List<String> paths, Filter filter,	
			boolean codeDownloadEnabled, boolean temporalConsistencyForced){
		
		this.vcs = vcs;
		this.accessCredentials = accessCredentials;
		this.details = details;
		this.firstRev = firstRev;
		this.lastRev = lastRev;
		this.pegRev = pegRev;
		this.paths = paths;
		this.filter = filter;
		this.codeDownloadEnabled  = codeDownloadEnabled;
		this.temporalConsistencyForced = temporalConsistencyForced;
	
	}
	
	public VCSRepository getVcs() {
		return vcs;
	}

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

	public Long getPegRev() {
		return pegRev;
	}

	public long getFirstRev() {
		return firstRev;
	}

	public long getLastRev() {
		return lastRev;
	}

	public List<String> getPaths() {
		return paths;
	}

}
