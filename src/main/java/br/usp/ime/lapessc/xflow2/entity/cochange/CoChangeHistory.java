package br.usp.ime.lapessc.xflow2.entity.cochange;

import java.util.ArrayList;
import java.util.List;

import br.usp.ime.lapessc.xflow2.entity.Commit;

public class CoChangeHistory {
	private List<Commit> commits = new ArrayList<Commit>();
	
	public CoChangeHistory(){

	}
	
	public void addCommit(Commit commit){
		this.commits.add(commit);
	}
	
	public List<Commit> getCommits(){
		return commits;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((commits == null) ? 0 : commits.hashCode());
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
		if (commits == null) {
			if (other.commits != null)
				return false;
		} else if (!commits.equals(other.commits))
			return false;
		return true;
	}
	
	
}
