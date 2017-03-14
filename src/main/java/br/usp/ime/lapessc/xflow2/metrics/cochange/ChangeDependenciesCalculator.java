package br.usp.ime.lapessc.xflow2.metrics.cochange;

import java.util.Set;

import javax.persistence.EntityManager;

import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.dao.core.AnalysisDAO;
import br.usp.ime.lapessc.xflow2.entity.database.DatabaseManager;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class ChangeDependenciesCalculator {

	public Set<ChangeDependency> calculate(CoChangesAnalysis analysis) throws DatabaseException{
				
		Set<ChangeDependency> changeDeps = analysis.getChangeDependencies();
		System.out.println("Change deps list size: " + changeDeps.size());
		
		EntityManager manager = DatabaseManager.getDatabaseSession();
		manager.getTransaction().begin();
		
		//Persists Co-Changes
		for(ChangeDependency changeDep: changeDeps){
			manager.persist(changeDep);
		}
		
		manager.getTransaction().commit();
		DatabaseManager.getDatabaseSession().clear();
		return changeDeps;
	}

	
	public static void main(String[] args) {
		
		try{
			CoChangesAnalysis coChangesAnalysis = 
					(CoChangesAnalysis) new AnalysisDAO().findById(
					Analysis.class,8L);
			
			ChangeDependenciesCalculator changeDependenciesCalc = 
					new ChangeDependenciesCalculator();
			
			changeDependenciesCalc.calculate(coChangesAnalysis);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}