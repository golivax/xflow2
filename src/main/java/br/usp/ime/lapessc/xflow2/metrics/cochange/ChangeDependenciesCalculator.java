package br.usp.ime.lapessc.xflow2.metrics.cochange;

import java.util.Set;

import javax.persistence.EntityManager;

import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.entity.database.DatabaseManager;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class ChangeDependenciesCalculator {

	public Set<ChangeDependency> calculate(CoChangesAnalysis analysis) throws DatabaseException{
				
		Set<ChangeDependency> changeDeps = analysis.getChangeDependencies();
		System.out.println("Change deps list size: " + changeDeps.size());
		
		//Persists Co-Changes
		/**
		for(ChangeDependency changeDep: changeDeps){
			insert(changeDep);
			DatabaseManager.getDatabaseSession().clear();
		}
		*/
		
		return changeDeps;
	}

	private void insert(ChangeDependency coChange){
		try {
			EntityManager manager = DatabaseManager.getDatabaseSession();
			manager.getTransaction().begin();
			manager.persist(coChange);
			manager.getTransaction().commit();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}