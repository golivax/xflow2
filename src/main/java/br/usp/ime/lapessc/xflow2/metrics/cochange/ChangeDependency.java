package br.usp.ime.lapessc.xflow2.metrics.cochange;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.collections15.CollectionUtils;
import org.hibernate.annotations.Index;

import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.FileArtifact;

@Entity(name = "cochange")
public class ChangeDependency {

	@Id
	@Column(name = "CC_ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "LHS", columnDefinition = "VARCHAR(350)")
	private String lhs;
	
	@Index(name = "lhsStamp_index")
	private int lhsStamp;
	
	@Column(name = "RHS", columnDefinition = "VARCHAR(350)")
	private String rhs;
	
	@Index(name = "rhsStamp_index")
	private int rhsStamp;
	
	private int support;
	private double confidence;
	
	private long firstCommit;
	private long lastCommit;
		
	public ChangeDependency(String lhs, int lhsStamp, String rhs, int rhsStamp, 
			List<Commit> commitsWithLHS, List<Commit> commitsWithRHS){

		this.lhs = lhs;
		this.lhsStamp = lhsStamp;
		
		this.rhs = rhs;
		this.rhsStamp = rhsStamp;
		
		this.firstCommit = determineFirstCommit(commitsWithLHS, commitsWithRHS);
		this.lastCommit = determineLastCommit(commitsWithLHS, commitsWithRHS);
		
		List<Long> filteredCommitsWithLHS = 
				filterCommitList(commitsWithLHS, firstCommit, lastCommit);
		
		List<Long> filteredCommitsWithRHS = 
				filterCommitList(commitsWithRHS, firstCommit, lastCommit);
		
		this.support= CollectionUtils.intersection(
				filteredCommitsWithLHS, filteredCommitsWithRHS).size();
		
		this.confidence = (double)support/filteredCommitsWithLHS.size();
	}

	private List<Long> filterCommitList(List<Commit> commits,
			long firstCommit, long lastCommit) {
		
		List<Long> filteredCommitList = new ArrayList<>();
		for(Commit commit : commits){
			
			if(commit.getRevision() >= firstCommit && 
				commit.getRevision() <= lastCommit){
			
				filteredCommitList.add(commit.getRevision());
			}
		}
		
		return filteredCommitList;
	}

	private long determineLastCommit(List<Commit> commitsWithLHS,
			List<Commit> commitsWithRHS) {
		
		SortedSet<Long> lastCommitCandidates = new TreeSet<>();
				
		Commit lastCommitWithLHS = commitsWithLHS.get(commitsWithLHS.size()-1);
				 
		for (FileArtifact fileArtifact : lastCommitWithLHS.getEntryFiles()){
			if (fileArtifact.getPath().equals(lhs)){
				if (fileArtifact.getOperationType() == 'D'){
					lastCommitCandidates.add(lastCommitWithLHS.getRevision());
				}
			}
		}
		
		Commit lastCommitWithRHS = commitsWithRHS.get(commitsWithRHS.size()-1);
		
		for (FileArtifact fileArtifact : lastCommitWithRHS.getEntryFiles()){
			if (fileArtifact.getPath().equals(rhs)){
				if (fileArtifact.getOperationType() == 'D'){
					lastCommitCandidates.add(lastCommitWithRHS.getRevision());
				}
			}
		}
		
		if (lastCommitCandidates.isEmpty()){
			//To represent infinite
			return Long.MAX_VALUE;
		}
		else{
			//Smallest value
			return lastCommitCandidates.first();
		}		
	}

	private long determineFirstCommit(List<Commit> commitsWithLHS,
			List<Commit> commitsWithRHS) {
		
		return Math.max(commitsWithLHS.get(0).getRevision(), 
				commitsWithRHS.get(0).getRevision());
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getLhs() {
		return lhs;
	}
	
	public int getLhsStamp() {
		return lhsStamp;
	}

	public String getRhs() {
		return rhs;
	}
	
	public int getRhsStamp() {
		return rhsStamp;
	}

	public int getSupport() {
		return support;
	}

	public double getConfidence() {
		return confidence;
	}
	
	public String toString(){
		String rule = lhs + " -> " + rhs;
		String support = "Support: " + this.support;
		String confidence = "Confidence: " + this.confidence;
		return rule + "\n" + support + "\n" + confidence + "\n"; 
	}
}