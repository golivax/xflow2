package br.usp.ime.lapessc.xflow2.entity;

import java.util.ArrayList;
import java.util.List;

public class CoChangeHistory {

	private int id;
	private List<Commit> commits = new ArrayList<Commit>();
	
	public CoChangeHistory(int id){
		this.id = id;
	}
	
	public void addCommit(Commit commit){
		this.commits.add(commit);
	}
	
	public List<Commit> getCommits(){
		return commits;
	}
	
	public Integer getId(){
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CoChangeHistory other = (CoChangeHistory) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
