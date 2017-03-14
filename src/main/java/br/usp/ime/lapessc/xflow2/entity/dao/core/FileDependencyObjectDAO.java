package br.usp.ime.lapessc.xflow2.entity.dao.core;

import java.util.Collection;
import java.util.List;

import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraphType;
import br.usp.ime.lapessc.xflow2.entity.DependencyObject;
import br.usp.ime.lapessc.xflow2.entity.DependencyObjectType;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class FileDependencyObjectDAO extends DependencyObjectDAO<FileDependencyObject> {

	@Override
	public boolean insert(final FileDependencyObject entity) throws DatabaseException {
		return super.insert(entity);
	}

	@Override
	public boolean remove(final FileDependencyObject entity) throws DatabaseException {
		return super.remove(entity);
	}

	@Override
	public boolean update(final FileDependencyObject entity) throws DatabaseException {
		return super.update(entity);
	}

	@Override
	public FileDependencyObject findById(final Class<FileDependencyObject> clazz, final long id) throws DatabaseException {
		return super.findById(clazz, id);
	}

	@Override
	protected FileDependencyObject findUnique(final Class<FileDependencyObject> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findUnique(clazz, query, parameters);
	}

	@Override
	protected Collection<FileDependencyObject> findByQuery(final Class<FileDependencyObject> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findByQuery(clazz, query, parameters);
	}

	@Override
	public Collection<FileDependencyObject> findAll(final Class<? extends FileDependencyObject> myClass) throws DatabaseException {
		return super.findAll(myClass);
	}

	@Override
	protected int getIntegerValueByQuery(final String query, final Object[]... parameters) throws DatabaseException {
		return super.getIntegerValueByQuery(query, parameters);
	}

	@Override
	public List<FileDependencyObject> findAllDependencyObjsUntilDependency(final DependencyGraph dependency) throws DatabaseException {
		if(dependency.isDirectedDependency()){
			final String query = "SELECT dep from file_dependency dep, dependency d where dep.analysis = :analysis and d.id <= :dependencyID and d.type = :type and d in elements(dep.dependencies)";
			final Object[] parameter1 = new Object[]{"analysis", dependency.getAssociatedAnalysis()};
			final Object[] parameter2 = new Object[]{"dependencyID", dependency.getId()};
			final Object[] parameter3 = new Object[]{"type", dependency.getType()};
			
			return (List<FileDependencyObject>) findByQuery(FileDependencyObject.class, query, parameter1, parameter2, parameter3);			
		}
		else{
			final String query = "SELECT dep from file_dependency dep, dependency d where dep.analysis = :analysis and d.id <= :dependencyID and d.type = :type and d in elements(dep.dependencies)";
			final Object[] parameter1 = new Object[]{"analysis", dependency.getAssociatedAnalysis()};
			final Object[] parameter2 = new Object[]{"dependencyID", dependency.getId()};
			final Object[] parameter3 = new Object[]{"type", dependency.getType()};
			
			return (List<FileDependencyObject>) findByQuery(FileDependencyObject.class, query, parameter1, parameter2, parameter3);	
		}
	}

	@Override
	public List<FileDependencyObject> findDependencyObjsByDependency(final DependencyGraph dependency) throws DatabaseException {
		if(dependency.isDirectedDependency()){
			final String query = "SELECT dep from file_dependency dep, dependency d where dep.analysis = :analysis and d.id = :dependencyID and d in elements(dep.dependencies)";
			final Object[] parameter1 = new Object[]{"analysis", dependency.getAssociatedAnalysis()};
			final Object[] parameter2 = new Object[]{"dependencyID", dependency.getId()};
			
			return (List<FileDependencyObject>) findByQuery(FileDependencyObject.class, query, parameter1, parameter2);			
		}
		else{
			final String query = "SELECT dep from file_dependency dep, dependency d where dep.analysis = :analysis and d.id = :dependencyID and d in elements(dep.dependencies)";
			final Object[] parameter1 = new Object[]{"analysis", dependency.getAssociatedAnalysis()};
			final Object[] parameter2 = new Object[]{"dependencyID", dependency.getId()};
			
			return (List<FileDependencyObject>) findByQuery(FileDependencyObject.class, query, parameter1, parameter2);	
		}
	}

	//FIXME: ajeitar akee!
	@Override
	public FileDependencyObject findDependencyObjectByStamp(final Analysis analysis, final int stamp) throws DatabaseException {
		final String query = "SELECT dep from file_dependency dep where dep.analysis = :analysis and dep.assignedStamp = :stamp order by dep.id";
		final Object[] parameter1 = new Object[]{"analysis", analysis};
		final Object[] parameter2 = new Object[]{"stamp", stamp};
		
		final List<FileDependencyObject> dependencyObjects = (List<FileDependencyObject>) findByQuery(FileDependencyObject.class, query, parameter1, parameter2);
		
		if(dependencyObjects.size() == 0){
			return null;
		}
		return dependencyObjects.get(0);
	}
	

	public List<String> getFilePathsOrderedByStamp(final Analysis analysis) throws DatabaseException {
		final String query = "SELECT dep.filePath from file_dependency dep " +
				"where dep.analysis = :analysis order by dep.assignedStamp";

		final Object[] parameter1 = new Object[]{"analysis", analysis};
		
		final List<String> dependencyObjects = 
			(List<String>) findObjectsByQuery(query, parameter1);
		
		return dependencyObjects;
	}

	public FileDependencyObject findLastDependencyObjectByFilePath (Analysis analysis, String path) throws DatabaseException {
		final String query = "SELECT max(dep) from file_dependency dep where " +
				"dep.analysis = :analysis and dep.filePath = :path";
		final Object[] parameter1 = new Object[]{"analysis", analysis};
		final Object[] parameter2 = new Object[]{"path", path};
		
		return findUnique(FileDependencyObject.class, query, parameter1, parameter2);
	}

	public List<FileDependencyObject> findSuppliers(FileDependencyObject client, 
			List<FileDependencyObject> excludedSuppliers) throws DatabaseException {
		
		String query = "SELECT DISTINCT dset.dependedObject FROM dependency_set dset " +
				"JOIN dset.dependenciesMap AS map " +
				"JOIN dset.associatedDependency AS assocDep " +
				"WHERE index(map) = :client " +
				"AND dset.dependedObject NOT IN :excludedSuppliers " +
				"AND assocDep.associatedAnalysis.id = :associatedAnalysisID " +
				"AND assocDep.type = :dependencyType";
		
		final Object[] parameter1 = new Object[]{"client", client};
		final Object[] parameter2 = new Object[]{"excludedSuppliers", excludedSuppliers};
		final Object[] parameter3 = new Object[]{"associatedAnalysisID", client.getAnalysis().getId()};
		final Object[] parameter4 = new Object[]{"dependencyType", DependencyGraphType.TASK_DEPENDENCY.getValue()};
		
		return (List<FileDependencyObject>) findByQuery(
				FileDependencyObject.class, query, parameter1, parameter2, 
				parameter3, parameter4);
	}
	
	public List<FileDependencyObject> findAllByAnalysis(Analysis analysis) throws DatabaseException{
		final String query = "SELECT file_dep_obj from file_dependency as file_dep_obj where "
				+ "file_dep_obj.analysis.id = :analysisID";
		
		final Object[] parameter1 = new Object[]{"analysisID", analysis.getId()};
		
		final List<FileDependencyObject> dependencyObjects = 
				(List<FileDependencyObject>) findByQuery(
						FileDependencyObject.class, query, parameter1);
		
		return dependencyObjects;		
	}
}