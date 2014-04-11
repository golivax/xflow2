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

@Entity(name = "project")
public class VCSMiningProject {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PRJ_ID")
	private long id;
	
	@ManyToOne(cascade = CascadeType.ALL)
	private MiningSettings miningSettings;
	
	@OneToMany(mappedBy = "vcsMiningProject", cascade = CascadeType.ALL)
	private List<Commit> commits = new ArrayList<Commit>();
	
	@OneToMany(mappedBy = "vcsMiningProject", cascade = CascadeType.ALL)
	private List<Author> authors = new ArrayList<Author>();
	
	public VCSMiningProject(){
		
	}
	
	public MiningSettings getMiningSettings() {
		return miningSettings;
	}
	
	public VCSMiningProject(MiningSettings miningSettings){
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

	public long getId() {
		return id;
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