package br.usp.ime.lapessc.xflow2.stats;


import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class QuartileAnalysis {

	private Double q1;
	private Double q2;
	private Double q3;
	private Double iqr;
	
	public QuartileAnalysis(DescriptiveStatistics stats){
		//Calculates the quartiles
		this.q1 = stats.getPercentile(25);
		this.q2 = stats.getPercentile(50);
		this.q3 = stats.getPercentile(75);
		this.iqr = q3 - q1;
	}	
	
	public Double getQ1() {
		return q1;
	}

	public Double getQ2() {
		return q2;
	}

	public Double getQ3() {
		return q3;
	}

	public Double getIqr() {
		return iqr;
	}

	public Double getLowerWhisker(){
		return q1 - (1.5 * iqr);
	}
	
	public Double getUpperWhisker(){
		return q3 + (1.5 * iqr);
		
	}
	
	public Double getExtremeLowerWhisker(){
		return q1 - (3 * iqr);
	}

	public Double getExtremeUpperWhisker(){
		return q3 + (3 * iqr);
	}
	
	public String toString(){
		//Calculates the quartiles
	
		String s = new String();
		s+="q1: " + getQ1() + "\n";
		s+="q3: " + getQ3() + "\n";
		s+="iqr: " + getIqr() + "\n";
		
		s+="lowerWhisker: " + getLowerWhisker() + "\n";
		s+="upperWhisker: " + getUpperWhisker() + "\n";
		s+="extremeLowerWhisker: " + getExtremeLowerWhisker() + "\n";
		s+="extremeUpperWhisker: " + getExtremeUpperWhisker() + "\n";
		
		return s;
	}
}
