/* 
 * 
 * XFlow
 * _______
 * 
 *  
 *  (C) Copyright 2010, by Universidade Federal do Pará (UFPA), Francisco Santana, Jean Costa, Pedro Treccani and Cleidson de Souza.
 * 
 *  This file is part of XFlow.
 *
 *  XFlow is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  XFlow is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XFlow.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  ==============
 *  AuthorDAO.java
 *  ==============
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.entity.dao.cm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import br.usp.ime.lapessc.xflow2.entity.Author;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.VCSMiningProject;
import br.usp.ime.lapessc.xflow2.entity.dao.BaseDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;


public class AuthorDAO extends BaseDAO<Author>{

	@Override
	public final Author findById(final Class<Author> clazz, final long id) throws DatabaseException {
		return super.findById(clazz, id);
	}

	@Override
	public final boolean insert(final Author entity) throws DatabaseException {
		return super.insert(entity);
	}

	@Override
	public final boolean remove(final Author entity) throws DatabaseException {
		return super.remove(entity);
	}

	@Override
	public final boolean update(final Author entity) throws DatabaseException {
		return super.update(entity);
	}
	
	@Override
	protected final Author findUnique(final Class<Author> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findUnique(clazz, query, parameters);
	}
	
	@Override
	protected final Collection<Author> findByQuery(final Class<Author> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findByQuery(clazz, query, parameters);
	}

	@Override
	public final Collection<Author> findAll(final Class<? extends Author> myClass) throws DatabaseException {
		return super.findAll(myClass);
	}
	
	public final List<Author> getProjectAuthors(final long projectId) throws DatabaseException{
		final String query = "SELECT a from author a where a.vcsMiningProject.id = :projectId order by a.id";
		final Object[] parameter1 = new Object[]{"projectId", projectId};
		
		return (List<Author>) findByQuery(Author.class, query, parameter1);
	}
	
	public List<Author> getProjectAuthorsUntilEntry(final long projectID, final long entryID, boolean temporalConsistencyForced) throws DatabaseException {
		final List<Author> authorsList;
		if(temporalConsistencyForced){
			final String query = "SELECT a from author a where a.vcsMiningProject.id = :projectId";
			final Object[] parameter1 = new Object[]{"projectId", projectID};
			
			authorsList = (List<Author>) findByQuery(Author.class, query, parameter1);
		} else {
			final String query = "SELECT a from author a where a.vcsMiningProject.id = :projectId";
			final Object[] parameter1 = new Object[]{"projectId", projectID};
			
			authorsList = (List<Author>) findByQuery(Author.class, query, parameter1);			
		}
		
		return authorsList;
	}

	public final Author findAuthorByName(final VCSMiningProject vcsMiningProject, final String name) throws DatabaseException {
		final String query = "SELECT a FROM author a WHERE a.name = :name and a.vcsMiningProject.id = :vcsMiningProject";
		final Object[] parameter1 = new Object[]{"name", name};
		final Object[] parameter2 = new Object[]{"vcsMiningProject", vcsMiningProject.getId()};
		
		return findUnique(Author.class, query, parameter1, parameter2);
	}
	
	public final Author findAuthorFromMatrixPosition(final VCSMiningProject vcsMiningProject, final int matrixPosition) throws DatabaseException {
		final String query = "select a from author a where a.matrixPosition = :position and a.vcsMiningProject = :vcsMiningProject";
		final Object[] parameter1 = new Object[]{"position", matrixPosition};
		final Object[] parameter2 = new Object[]{"vcsMiningProject", vcsMiningProject};

		return findUnique(Author.class, query, parameter1, parameter2);
	}
	
	public final ArrayList<Long> getAuthorChangedFilesIds(final Author author, final long revision) throws DatabaseException {
		final String query = "SELECT DISTINCT f.id from file f where f.entry.revision <= :revision and f.entry.author = :author and (f.deletedOn >= :revision or f.deletedOn = null)";
		final Object[] parameter1 = new Object[]{"author", author};
		final Object[] parameter2 = new Object[]{"revision", revision};
		
		return (ArrayList<Long>) findObjectsByQuery(query, parameter1, parameter2);
	}
	
	public final List<String> getAuthorChangedFilesPath(final Author author, final Commit entry) throws DatabaseException {
		final String query = "SELECT f.path from file f where f.commit.id <= :entryID and f.commit.author.id = :authorID";
		final Object[] parameter1 = new Object[]{"entryID", entry.getId()};
		final Object[] parameter2 = new Object[]{"authorID", author.getId()};
		
		
		return (List<String>) findObjectsByQuery(query, parameter1, parameter2);
	}

	public final long getLatestAuthorSequenceNumber(final Author author) throws DatabaseException {
		final String query = "SELECT COUNT(*) from entry e where e.author = :author";
		final Object[] parameter1 = new Object[]{"author", author};
		
		return getLongValueByQuery(query, parameter1);
	}

	public final long countAuthorsRevisionsOnMonth(final Author author, final int year, final int month) throws DatabaseException {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DATE, calendar.getMinimum(Calendar.DATE));
		final Date lowestDate = calendar.getTime();
		calendar.set(Calendar.DATE, calendar.getMaximum(Calendar.DATE));
		final Date highestDate = calendar.getTime();

		final String query = "SELECT COUNT(*) from entry e where e.author = :author and e.date between :lowestDate and :highestDate";
		final Object[] parameter1 = {"author", author};
		final Object[] parameter2 = {"lowestDate", lowestDate};
		final Object[] parameter3 = {"highestDate", highestDate};
		return getLongValueByQuery(query, parameter1, parameter2, parameter3);
	}
	
	//FIXME: Query the db instead
	public List<Author> getAuthorsListByEntries(VCSMiningProject vcsMiningProject, Commit initialEntry, Commit finalEntry) {
		final List<Author> authorsList = new ArrayList<Author>();
		if(vcsMiningProject.getMiningSettings().isTemporalConsistencyForced()){
			for (Author author : vcsMiningProject.getAuthors()) {
				if((author.getEntries().get(0).getId() >= initialEntry.getId()) && (author.getEntries().get(0).getId() <= finalEntry.getId())){
					authorsList.add(author);
				}
			}
		} else {
			for (Author author : vcsMiningProject.getAuthors()) {
				if((author.getEntries().get(0).getRevision() >= initialEntry.getRevision()) && (author.getEntries().get(0).getRevision() <= finalEntry.getRevision())){
					authorsList.add(author);
				}
			}
		}
		
		return authorsList;
	}

}
