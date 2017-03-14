package br.usp.ime.lapessc.xflow2.entity.dao.cm;

import java.util.Collection;

import br.usp.ime.lapessc.xflow2.entity.Study;
import br.usp.ime.lapessc.xflow2.entity.dao.BaseDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class StudyDAO extends BaseDAO<Study>{

	@Override
	public Study findById(final Class<Study> clazz, final long id) throws DatabaseException {
		return super.findById(clazz, id);
	}

	@Override
	public boolean insert(final Study study) throws DatabaseException {
		return super.insert(study);
	}

	@Override
	public boolean remove(final Study study) throws DatabaseException {
		return super.remove(study);
	}

	@Override
	public boolean update(final Study study) throws DatabaseException {
		return super.update(study);
	}

	@Override
	public Collection<Study> findAll(final Class<? extends Study> myClass) throws DatabaseException {
		return super.findAll(myClass);
	}
	
	@Override
	protected Study findUnique(final Class<Study> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findUnique(clazz, query, parameters);
	}

	@Override
	protected Collection<Study> findByQuery(final Class<Study> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findByQuery(clazz, query, parameters);
	}

}
