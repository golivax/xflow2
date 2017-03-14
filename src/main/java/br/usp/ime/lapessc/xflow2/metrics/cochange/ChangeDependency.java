package br.usp.ime.lapessc.xflow2.metrics.cochange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.collections4.ListUtils;

import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.FileArtifact;
import net.sourceforge.jdistlib.HyperGeometric;

/**
## calculate hyperlift for existing rules.
##
## Michael Hahsler, Kurt Hornik, and Thomas Reutterer. 
## Implications of probabilistic data modeling for rule mining. 
## Report 14, Research Report Series, Department of Statistics and 
## Mathematics, Wirtschaftsuniversitaet Wien, Augasse 2-6, 1090 Wien, 
## Austria, March 2005.

## hyperlift(X => Y) = c_X,Y / Q_d[C_X,Y] 
##
## where Q_d[C_X,Y] = qhyper(d, m = c_Y, n = length(trans.) - c_Y, k = c_X)
##
## c_X,Y = count(X => Y)
## c_X = count(X)
## c_Y = count(Y)
##
## this implements only hyperlift for rules with a single item in the consequent


.hyperLift <- function(x, transactions, reuse, d = 0.99) {
  
  counts <- .getCounts(x, transactions, reuse)
  
  t <- counts$N
  c_XY <- counts$f11
  c_X <- counts$f1x
  c_Y <- counts$fx1
  t <- length(transactions)
  
  Q <- qhyper(d, m = c_Y, n = t - c_Y, k = c_X, lower.tail = TRUE)
  hyperlift <- c_XY / Q
  
  hyperlift
}


## calculate hyperconfidence for existing rules.
## (confidence level that we observe too high/low counts)
## 
## uses the model from:
## Michael Hahsler, Kurt Hornik, and Thomas Reutterer. 
## Implications of probabilistic data modeling for rule mining. 
## Report 14, Research Report Series, Department of Statistics and 
## Mathematics, Wirtschaftsuniversitaet Wien, Augasse 2-6, 1090 Wien, 
## Austria, March 2005.


.hyperConfidence <- function(x, transactions, reuse = TRUE, complements = TRUE, 
  significance = FALSE) {
  
  ## significance: return significance levels instead of
  ##   confidence levels
  
  counts <- .getCounts(x, transactions, reuse)
  
  t <- counts$N
  c_XY <- counts$f11
  c_X <- counts$f1x
  c_Y <- counts$fx1
  
  if(complements == TRUE)
    ## c_XY - 1 so we get P[C_XY < c_XY] instead of P[C_XY <= c_XY]
    res <- phyper(c_XY - 1, m=c_Y, n=t-c_Y, k=c_X, lower.tail = !significance)
  
  else
    ## substitutes; Pr[C_XY > c_XY]
    res <- phyper(c_XY, m=c_Y, n=t-c_X, k=c_X, lower.tail = significance)
  
  ## todo: check resulting NaN
  res
}
*/


@Entity(name = "changedep")
public class ChangeDependency {

	public ChangeDependency(){
		
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@OneToOne
	private CoChangesAnalysis analysis;

	private String lhs;
	private String rhs;
	
	private double support; 
	private double supportCount;
	private double confidence;
	private double lift;
	private double stdLift;
	private double hyperLift;
	private double hyperConfidence;
	private double conviction;
	
	
	@OneToMany
	@JoinColumn(name="changedep_id")
	private List<Commit> coChanges;
		
	/**
	 * Ok, the code to build ChangeDependencies is getting complicated. Should
	 * now probably create a factory for Change Dependencies, which calculates
	 * all those "interest" rules
	 * @param analysis
	 * @param totalCommits
	 * @param lhs
	 * @param rhs
	 * @param commitsWithLHS
	 * @param commitsWithRHS
	 */
	public ChangeDependency(CoChangesAnalysis analysis, int totalCommits,
			String lhs, String rhs, List<Commit> commitsWithLHS, 
			List<Commit> commitsWithRHS){

		this.analysis = analysis;
		
		this.lhs = lhs;
		this.rhs = rhs;		
		
		this.coChanges = ListUtils.intersection(commitsWithLHS, commitsWithRHS);
		
		//Check if these 3 methods are ok
		Long firstCommit = determineFirstCommit(commitsWithLHS, commitsWithRHS);
		Long lastCommit = determineLastCommit(commitsWithLHS, commitsWithRHS);
		
		List<Long> filteredCommitsWithLHS = 
				filterCommitList(commitsWithLHS, firstCommit, lastCommit);

		List<Long> filteredCommitsWithRHS = 
				filterCommitList(commitsWithRHS, firstCommit, lastCommit);
		
		double supportLHS = (double)commitsWithLHS.size() / totalCommits;
		double supportRHS = (double)commitsWithRHS.size() / totalCommits;
		
		this.supportCount = coChanges.size();
		this.support = supportCount / totalCommits;
		
		this.confidence = support/supportLHS;
		
		this.lift = support / (supportLHS * supportRHS);
		
		this.conviction = (1 - supportRHS) / (1 - confidence); 
		
		//////////////////////

		//p: probability, it must be between 0 and 1.
		//NR: the number of red balls in the urn.
		//NB: the number of black balls in the urn.
		//n: the number of balls drawn from the urn.
		//lowerTail: logical; if TRUE (default), probabilities are P[X ≤ x], otherwise, P[X > x].
		//log_p: logical; if TRUE, probabilities p are given as log(p).

		double p = 0.99;
		double NR = commitsWithRHS.size(); //m = c_Y
		double NB = totalCommits - commitsWithRHS.size(); //n = all transactions - cY
		double n = commitsWithLHS.size(); //k = c_X
		boolean lower_tail = true;
		boolean log_p = false;

		double q = HyperGeometric.quantile(p, NR, NB, n, lower_tail, log_p);
		this.hyperLift = supportCount / q; //c_XY / Q

		//////////////////////
		
		double x = supportCount - 1; //c_XY - 1
		NR =  commitsWithRHS.size(); //m = c_Y
		NB = totalCommits - commitsWithRHS.size(); //n = t - c_Y
		n = commitsWithLHS.size(); //k = c_X
		lower_tail = true;
		log_p = false;
		
		this.hyperConfidence = 
				HyperGeometric.cumulative(x, NR, NB, n, lower_tail, log_p);
		
		//////////////////////
		
		n = totalCommits;		
		double lambda = (4 * n) / (Math.pow(n+1, 2));
		double v = n;
		
		this.stdLift = (lift - lambda) / (v - lambda);
	}

	private List<Long> filterCommitList(List<Commit> commits,
			Long firstCommit, Long lastCommit) {
		
		List<Long> filteredCommitList = new ArrayList<>();
		for(Commit commit : commits){
			
			if(commit.getRevision() >= firstCommit && 
				commit.getRevision() <= lastCommit){
			
				filteredCommitList.add(commit.getRevision());
			}
		}
		
		return filteredCommitList;
	}

	private Long determineLastCommit(List<Commit> commitsWithLHS,
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
			return Math.max(lastCommitWithLHS.getRevision(),
					lastCommitWithRHS.getRevision());
		}
		else{
			//Smallest value
			return lastCommitCandidates.first();
		}		
	}

	private Long determineFirstCommit(List<Commit> commitsWithLHS,
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
	
	public String getRhs() {
		return rhs;
	}
	
	public Double getSupport() {
		return support;
	}

	public Double getSupportCount() {
		return supportCount;
	}

	public Double getConfidence() {
		return confidence;
	}
	
	public String getClient(){
		return rhs;
	}
	
	public String getSupplier(){
		return lhs;
	}
	
	public Collection<Commit> getCoChanges(){
		return coChanges;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lhs == null) ? 0 : lhs.hashCode());
		result = prime * result + ((rhs == null) ? 0 : rhs.hashCode());
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
		ChangeDependency other = (ChangeDependency) obj;
		if (lhs == null) {
			if (other.lhs != null)
				return false;
		} else if (!lhs.equals(other.lhs))
			return false;
		if (rhs == null) {
			if (other.rhs != null)
				return false;
		} else if (!rhs.equals(other.rhs))
			return false;
		return true;
	}
	
	public Double getLift() {
		return lift;
	}
	
	public String toString(){
		String dep = "Dep: " + getClient() + " -> " + getSupplier();
		//String rule = "Rule: " + lhs + " -> " + rhs;
		//String support = "Support: " + this.support;
		//String confidence = "Confidence: " + this.confidence;
		//String dep = lhs + "," + rhs + "," + support + "," + confidence + "," + lift;
		return dep; //+ "\n" + rule + "\n" + support + "\n" + confidence + "\n"; 
	}
	
	public static void main(String[] args) {
		Commit c1 = new Commit();
		c1.setRevision(1);
		
		Commit c2 = new Commit();
		c2.setRevision(2);

		Commit c3 = new Commit();
		c3.setRevision(3);
		
		Commit c4 = new Commit();
		c4.setRevision(4);
		
		Commit c5 = new Commit();
		c5.setRevision(5);
		
		Commit c6 = new Commit();
		c6.setRevision(6);
		
		Commit c7 = new Commit();
		c7.setRevision(7);
		
		List<Commit> commitsWithLHS = new ArrayList<>();
		commitsWithLHS.add(c1);
		commitsWithLHS.add(c2);
		commitsWithLHS.add(c3);
		commitsWithLHS.add(c4);
		
		List<Commit> commitsWithRHS = new ArrayList<>();
		commitsWithRHS.add(c1);
		commitsWithRHS.add(c2);
		commitsWithRHS.add(c4);
		commitsWithRHS.add(c6);
		
		ChangeDependency c = 
				new ChangeDependency(
						null, 7, "A", "0", commitsWithLHS, commitsWithRHS);
		
		System.out.println(c.getSupportCount());
		System.out.println(c.getConfidence());
		System.out.println(c.getLift());
	}
		
	public Double getStdLift(){
		return stdLift;
	}
	
	public Double getHyperLift() {
		return hyperLift;
	}
	
	public double getHyperConfidence() {
		return hyperConfidence;
	}
	
	public Double getConviction() {
		return conviction;
	}
}