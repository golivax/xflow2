package br.usp.ime.lapessc.xflow2.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity(name = "vcs_mining_prj")
public class VCSMiningProject {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PRJ_ID")
	private long id;
	
	@ManyToOne(cascade = CascadeType.ALL)
	private MiningSettings miningSettings;
	
	@OneToMany(mappedBy = "vcsMiningProject", cascade = CascadeType.ALL)
	@OrderBy("revision")
	private List<Commit> commits = new ArrayList<Commit>();
	
	@OneToMany(mappedBy = "vcsMiningProject", cascade = CascadeType.ALL)
	private List<Author> authors = new ArrayList<Author>();
	
	@OneToMany(mappedBy = "project")
	private List<Analysis> analyses = new ArrayList<Analysis>();
	
	@ManyToOne
	private Study study;
	
	//For JPA use only
	public VCSMiningProject(){}
	
	public MiningSettings getMiningSettings() {
		return miningSettings;
	}
	
	public VCSMiningProject(Study study, MiningSettings miningSettings){
		this.study = study;
		study.addVcsMiningProject(this);
		
		this.miningSettings = miningSettings;
	}
	
	public List<Commit> getCommits() {
		return commits;
	}

	public void addCommit(Commit commit){
		this.commits.add(commit);
	}
	
	public void addCommits(Collection<Commit> commits){
		this.commits.addAll(commits);
	}
	
	public void setCommits(List<Commit> commits){
		this.commits = commits;
	}
	
	public List<Author> getAuthors() {
		return authors;
	}
	
	public void addAuthor(Author author){
		this.authors.add(author);
	}
	
	public void addAuthors(Collection<Author> authors){
		this.authors.addAll(authors);
	}
	
	public void setAuthors(List<Author> authors){
		this.authors = authors;
	}

	public List<Analysis> getAnalyses() {
		return analyses;
	}

	public long getId() {
		return id;
	}

	public List<Commit> getCommitChunk(int chunkIndex, int totalChunks) {
		
		//Floor
		int listSize = commits.size()/totalChunks;
		int fromIndex = chunkIndex * listSize;
		int toIndex = fromIndex + listSize;
		
		//Last Chunk
		if(chunkIndex == (totalChunks-1)){
			toIndex = commits.size();
		}
		
		return commits.subList(fromIndex, toIndex);
	}
	
	/**
	
	public Collection<String> getAuthorsStringList() {
		final ArrayList<String> authorsList = new ArrayList<String>();
		for (Author author : this.getAuthors()) {
			System.out.println(author.getName());
			authorsList.add(author.getName());	
		}
		
		return authorsList;
	}
	*/

}