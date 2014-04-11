package br.usp.ime.lapessc.xflow2.entity.dao.core;

import java.util.List;

import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencySet;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.dao.BaseDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class DependencySetDAO extends BaseDAO<DependencySet> {


	@Override
	public boolean insert(DependencySet entity) throws DatabaseException {
		return super.insert(entity);
	}

	@Override
	public boolean remove(DependencySet entity) throws DatabaseException {
		return super.remove(entity);
	}

	@Override
	public boolean update(DependencySet entity) throws DatabaseException {
		return super.update(entity);
	}

	@Override
	public DependencySet findById(Class<DependencySet> clazz, long id) throws DatabaseException {
		return super.findById(clazz, id);
	}
	
	@Override
	protected DependencySet findUnique(final Class<DependencySet> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findUnique(clazz, query, parameters);
	}

	public List<Long> getAllDependenciesSetUntilDependency(DependencyGraph dependency) throws DatabaseException {
		final String query = "SELECT dependencySet.id FROM dependency_set dependencySet WHERE " +
				"dependencySet.associatedDependency.id <= :associatedDependencyID AND " +
				"dependencySet.associatedDependency.associatedAnalysis.id = :associatedAnalysisID " +
				"AND dependencySet.associatedDependency.type = :dependencyType ORDER BY " +
				"dependencySet.id";
		final Object[] parameter1 = new Object[]{"associatedDependencyID", dependency.getId()};
		final Object[] parameter2 = new Object[]{"associatedAnalysisID", dependency.getAssociatedAnalysis().getId()};
		final Object[] parameter3 = new Object[]{"dependencyType", dependency.getType()};
		
		return (List<Long>) findObjectsByQuery(query, parameter1, parameter2, parameter3);
	}
	
	public List<DependencySet> getAllDependenciesSetByDependency(DependencyGraph dependency) throws DatabaseException {
		final String query = "SELECT dependencySet FROM dependency_set dependencySet WHERE dependencySet.associatedDependency.id = :associatedDependencyID AND dependencySet.associatedDependency.associatedAnalysis.id = :associatedAnalysisID";
		final Object[] parameter1 = new Object[]{"associatedDependencyID", dependency.getId()};
		final Object[] parameter2 = new Object[]{"associatedAnalysisID", dependency.getAssociatedAnalysis().getId()};
		
		return (List<DependencySet>) findByQuery(DependencySet.class, query, parameter1, parameter2);
	}

	public List<Long> getAllDependenciesSetBetweenDependencies(DependencyGraph initialEntryDependency, DependencyGraph finalEntryDependency) throws DatabaseException {
		final String query = "SELECT dependencySet.id FROM dependency_set dependencySet WHERE dependencySet.associatedDependency.associatedAnalysis.id = :associatedAnalysisID AND dependencySet.associatedDependency.type = :dependencyType AND dependencySet.associatedDependency.associatedEntry.id between :initialEntry and :finalEntry";
		final Object[] parameter1 = new Object[]{"associatedAnalysisID", initialEntryDependency.getAssociatedAnalysis().getId()};
		final Object[] parameter2 = new Object[]{"dependencyType", initialEntryDependency.getType()};
		final Object[] parameter3 = new Object[]{"initialEntry", initialEntryDependency.getAssociatedEntry().getId()};
		final Object[] parameter4 = new Object[]{"finalEntry", finalEntryDependency.getAssociatedEntry().getId()};
		
		
		return (List<Long>) findObjectsByQuery(query, parameter1, parameter2, parameter3, parameter4);
	}

	public boolean isSupplier(Analysis analysis, FileDependencyObject client, FileDependencyObject supplier) throws DatabaseException {
		
		boolean isSupplier = false;
		
		String query = "SELECT max(dset) FROM dependency_set dset " +
				"JOIN dset.dependenciesMap AS map " +
				"JOIN dset.associatedDependency AS assocDep " +
				"JOIN dset.dependedObject AS supplier " + 
				"WHERE index(map) = :client " +			
				"AND supplier = :supplier " +
				"AND assocDep.associatedAnalysis.id = :associatedAnalysisID " +
				"AND assocDep.type = :dependencyType";
				
		final Object[] parameter1 = new Object[]{"client", client};
		final Object[] parameter2 = new Object[]{"supplier", supplier};
		final Object[] parameter3 = new Object[]{"associatedAnalysisID", analysis.getId()};
		final Object[] parameter4 = new Object[]{"dependencyType", DependencyGraph.TASK_DEPENDENCY};
		
		//TODO: DO IT IN HQL
		DependencySetDAO dependencySetDAO = new DependencySetDAO();
		DependencySet dependencySet = dependencySetDAO.findUnique(DependencySet.class, query, parameter1, parameter2, parameter3, parameter4);
		if (dependencySet != null){
			Integer degree = (Integer)dependencySet.getDependenciesMap().get(client);
			if (degree == 1){
				isSupplier = true;
			}
		}
		
		return isSupplier;
	}

	public List<DependencySet> findByIds(long startID, long endID) throws DatabaseException{
		final String query = "SELECT dependencySet FROM dependency_set dependencySet WHERE dependencySet.id between :startID and :endID";
		final Object[] parameter1 = new Object[]{"startID", startID};
		final Object[] parameter2 = new Object[]{"endID", endID};
		
		return (List<DependencySet>) findByQuery(DependencySet.class, query, parameter1, parameter2);		
	}
	
}
