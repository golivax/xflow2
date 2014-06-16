package br.usp.ime.lapessc.xflow2.entity.dao.core;

import java.util.Collection;
import java.util.List;

import br.usp.ime.lapessc.xflow2.entity.AuthorDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraphType;
import br.usp.ime.lapessc.xflow2.entity.DependencyObject;
import br.usp.ime.lapessc.xflow2.entity.dao.BaseDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class DependencyDAO extends BaseDAO<DependencyGraph> {

	@Override
	public boolean insert(final DependencyGraph entity) throws DatabaseException {
		return super.insert(entity);
	}

	@Override
	public boolean remove(final DependencyGraph entity) throws DatabaseException {
		return super.remove(entity);
	}

	@Override
	public boolean update(final DependencyGraph entity) throws DatabaseException {
		return super.update(entity);
	}

	@Override
	public DependencyGraph findById(final Class<DependencyGraph> clazz, final long id) throws DatabaseException {
		return super.findById(clazz, id);
	}
	
	@Override
	public Collection<DependencyGraph> findAll(final Class<? extends DependencyGraph> myClass) throws DatabaseException {
		return super.findAll(myClass);
	}

	@Override
	protected DependencyGraph findUnique(final Class<DependencyGraph> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findUnique(clazz, query, parameters);
	}

	@Override
	protected Collection<DependencyGraph> findByQuery(final Class<DependencyGraph> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findByQuery(clazz, query, parameters);
	}
	
	//FIXME: Polymorphic query
	public Collection findAllDependenciesByAnalysis(final long analysisID, final DependencyGraphType dependencyGraphType) throws DatabaseException {
		final String query = "SELECT dep from dependency dep " +
				"join dep.associatedEntry as entry where " +
				"dep.associatedAnalysis.id = :analysisID " +
				"and dep.type = :dependencyType " +
				"order by entry.revision";
		
		final Object[] parameter1 = new Object[]{"analysisID", analysisID};
		final Object[] parameter2 = new Object[]{"dependencyType", dependencyGraphType.getValue()};
		
		return findByQuery(DependencyGraph.class, query, parameter1, parameter2);
	}
	
	public DependencyGraph findDependencyByEntry(final long analysisID, final long entryID, final int dependencyType) throws DatabaseException {
		final String query = "SELECT dep from dependency dep where dep.associatedAnalysis.id = :analysisID and dep.associatedEntry.id = :entryID and dep.type = :dependencyType";
		final Object[] parameter1 = new Object[]{"analysisID", analysisID};
		final Object[] parameter2 = new Object[]{"entryID", entryID};
		final Object[] parameter3 = new Object[]{"dependencyType", dependencyType};
		
		return findUnique(DependencyGraph.class, query, parameter1, parameter2, parameter3);
	}
	
	public DependencyGraph findHighestDependencyByEntry(final long analysisID, final long entryID, final int dependencyType) throws DatabaseException {
		final String entryQuery = "select MAX(d.associatedEntry.id) from dependency d where d.associatedAnalysis.id = :analysisID and d.type = :dependencyType and d.associatedEntry.id <= :entryID";
		
		final Object[] parameter1 = new Object[]{"analysisID", analysisID};
		final Object[] parameter2 = new Object[]{"entryID", entryID};
		final Object[] parameter3 = new Object[]{"dependencyType", dependencyType};
		
		Long foundEntry = getLongValueByQuery(entryQuery, parameter1, parameter2, parameter3);
	
		final String subquery = "SELECT dep from dependency dep where dep.associatedAnalysis.id = :analysisID and dep.type = :dependencyType and dep.associatedEntry.id = "+foundEntry;
		return findUnique(DependencyGraph.class, subquery, parameter1, parameter3);
	}
	
	public List<DependencyGraph> findAllDependenciesUntilIt(final DependencyGraph dependency) throws DatabaseException {
		final String query = "SELECT dep from dependency dep where dep.associatedAnalysis = :analysis and dep.id <= :dependencyID and dep.type = :dependencyType";
		final Object[] parameter1 = new Object[]{"analysis", dependency.getAssociatedAnalysis()};
		final Object[] parameter2 = new Object[]{"dependencyID", dependency.getId()};
		final Object[] parameter3 = new Object[]{"dependencyType", dependency.getType()};
		
		return (List<DependencyGraph>) findByQuery(DependencyGraph.class, query, parameter1, parameter2, parameter3);
	}

	public List<DependencyGraph> findDependenciesBetweenDependencies(DependencyGraph initialEntryDependency, DependencyGraph finalEntryDependency) throws DatabaseException {
		final String query = "SELECT dep from dependency dep where dep.associatedAnalysis = :analysis and dep.type = :dependencyType and dep.associatedEntry.id between :initialEntry and :finalEntry";
		final Object[] parameter1 = new Object[]{"analysis", initialEntryDependency.getAssociatedAnalysis()};
		final Object[] parameter2 = new Object[]{"dependencyType", initialEntryDependency.getType()};
		final Object[] parameter3 = new Object[]{"initialEntry", initialEntryDependency.getAssociatedEntry().getId()};
		final Object[] parameter4 = new Object[]{"finalEntry", finalEntryDependency.getAssociatedEntry().getId()};
		
		return (List<DependencyGraph>) findByQuery(DependencyGraph.class, query, parameter1, parameter2, parameter3, parameter4);
	}


}
