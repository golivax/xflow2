package br.usp.ime.lapessc.xflow2.entity.dao.metrics;

import java.util.List;

import br.usp.ime.lapessc.xflow2.entity.Author;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.dao.BaseDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.metrics.MetricValuesTable;

public abstract class MetricModelDAO<MetricModelTable extends MetricValuesTable> extends BaseDAO<MetricModelTable>{

	abstract public List<MetricModelTable> getAllMetricsTable(Metrics metrics) throws DatabaseException;
	abstract public List<MetricModelTable> getMetricsTableByAuthor(Metrics metrics, Author author) throws DatabaseException;
	abstract public List<MetricModelTable> getMetricsTableFromAuthorByEntries(Metrics metrics, Author author, Commit initialEntry, Commit finalEntry) throws DatabaseException;
	
}
