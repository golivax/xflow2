package br.usp.ime.lapessc.xflow2.entity.dao.core;

import java.util.Collection;
import java.util.List;

import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencyObject;
import br.usp.ime.lapessc.xflow2.entity.dao.BaseDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public abstract class DependencyObjectDAO<Subtype extends DependencyObject> extends BaseDAO<Subtype> {


	@Override
	protected boolean insert(final Subtype entity) throws DatabaseException {
		return super.insert(entity);
	}

	@Override
	protected boolean remove(final Subtype entity) throws DatabaseException {
		return super.remove(entity);
	}

	@Override
	protected boolean update(final Subtype entity) throws DatabaseException {
		return super.update(entity);
	}

	@Override
	protected Subtype findById(final Class<Subtype> clazz, long id) throws DatabaseException {
		return super.findById(clazz, id);
	}

	@Override
	protected Subtype findUnique(final Class<Subtype> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findUnique(clazz, query, parameters);
	}

	@Override
	protected Collection<Subtype> findByQuery(final Class<Subtype> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findByQuery(clazz, query, parameters);
	}

	@Override
	protected Collection<Subtype> findAll(final Class<? extends Subtype> myClass) throws DatabaseException {
		return super.findAll(myClass);
	}

	@Override
	protected int getIntegerValueByQuery(final String query, final Object[]... parameters) throws DatabaseException {
		return super.getIntegerValueByQuery(query, parameters);
	}

	public abstract List<Subtype> findDependencyObjsByDependency(DependencyGraph dependency) throws DatabaseException;
	
	public abstract List<Subtype> findAllDependencyObjsUntilDependency(DependencyGraph dependency) throws DatabaseException;
	
	public abstract Subtype findDependencyObjectByStamp(Analysis analysis, int stamp) throws DatabaseException;
}
