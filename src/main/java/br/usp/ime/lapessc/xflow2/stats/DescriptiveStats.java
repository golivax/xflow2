package br.usp.ime.lapessc.xflow2.stats;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class DescriptiveStats {

	private DescriptiveStatistics stats;
	
	public DescriptiveStats(){
		stats = new DescriptiveStatistics();
	}
	
	public void addValue(double value){
		stats.addValue(value);
	}
	
	public String toString(){
		return stats.toString();
	}
	
	public double getPercentile(int n){
		return stats.getPercentile(n);
	}
	
	public QuartileAnalysis getQuartileAnalysis(){
		return new QuartileAnalysis(this.stats);
	}
	
	public void reset(){
		stats.clear();
	}
	
	public Long getN(){
		return stats.getN();
	}
	
	public Double getMin(){
		return stats.getMin();
	}
	
	public Double getMax(){
		return stats.getMax();
	}
	
	public Double getMean(){
		return stats.getMean();
	}
	
	public Double getStdDev(){
		return stats.getStandardDeviation();
	}
	
	public Double getMedian(){
		return stats.getPercentile(50);
	}
	
	public Double getSkewness(){
		return stats.getSkewness();
	}
	
	public Double getKurtosis(){
		return stats.getKurtosis();
	}	
	
	public List<Double> getValues(){
		List<Double> values = new ArrayList<>();
		for(double value : stats.getValues()){
			values.add(value);
		}
		return values;
	}
	
	public double getSum(){
		return stats.getSum();
	}
	
	public static void main(String[] args) {
		DescriptiveStats d = new DescriptiveStats();
		d.addValue(2);
		d.addValue(8);
		
		System.out.println(d);
	}
	
}
