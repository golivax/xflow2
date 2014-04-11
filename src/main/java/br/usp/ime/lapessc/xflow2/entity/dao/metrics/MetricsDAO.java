package br.usp.ime.lapessc.xflow2.entity.dao.metrics;

import java.util.Collection;

import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.dao.BaseDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class MetricsDAO extends BaseDAO<Metrics> {

	@Override
	public boolean insert(Metrics entity) throws DatabaseException {
		return super.insert(entity);
	}

	@Override
	public boolean remove(Metrics entity) throws DatabaseException {
		return super.remove(entity);
	}

	@Override
	public boolean update(Metrics entity) throws DatabaseException {
		return super.update(entity);
	}

	@Override
	public Metrics findById(Class<Metrics> clazz, long id) throws DatabaseException {
		return super.findById(clazz, id);
	}

	@Override
	protected Collection<Metrics> findByQuery(Class<Metrics> clazz, String query, Object[]... parameters) throws DatabaseException {
		return super.findByQuery(clazz, query, parameters);
	}

}
