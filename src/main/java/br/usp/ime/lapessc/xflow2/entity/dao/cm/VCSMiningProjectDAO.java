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
 *  ===============
 *  ProjectDAO.java
 *  ===============
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.entity.dao.cm;

import java.util.Collection;

import br.usp.ime.lapessc.xflow2.entity.VCSMiningProject;
import br.usp.ime.lapessc.xflow2.entity.dao.BaseDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;


public class VCSMiningProjectDAO extends BaseDAO<VCSMiningProject> {

	@Override
	public VCSMiningProject findById(final Class<VCSMiningProject> clazz, final long id) throws DatabaseException {
		return super.findById(clazz, id);
	}

	@Override
	public boolean insert(final VCSMiningProject project) throws DatabaseException {
		return super.insert(project);
	}

	@Override
	public boolean remove(final VCSMiningProject project) throws DatabaseException {
		return super.remove(project);
	}

	@Override
	public boolean update(final VCSMiningProject project) throws DatabaseException {
		return super.update(project);
	}

	@Override
	public Collection<VCSMiningProject> findAll(final Class<? extends VCSMiningProject> myClass) throws DatabaseException {
		return super.findAll(myClass);
	}
	
	@Override
	protected VCSMiningProject findUnique(final Class<VCSMiningProject> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findUnique(clazz, query, parameters);
	}

	@Override
	protected Collection<VCSMiningProject> findByQuery(final Class<VCSMiningProject> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findByQuery(clazz, query, parameters);
	}

	public VCSMiningProject findByName(final String projectName) throws DatabaseException {
		final String query = "SELECT p from project p where p.name = :projectName";
		final Object[] parameter = new Object[]{"projectName", projectName};
		
		return findUnique(VCSMiningProject.class, query, parameter);
	}

	public int countProjectAuthors(final long projectId) throws DatabaseException{
		final String query = "SELECT COUNT(*) from author a where a.project.id = :projectId";
		final Object[] parameter1 = new Object[]{"projectId", projectId};
		
		return getIntegerValueByQuery(query, parameter1);
	}

	public int getProjectLastDownloadedCommit(final long projectId) throws DatabaseException{
		final String query = "SELECT MAX(e.revision) from entry e where e.project.id = :id";
		final Object[] parameter1 = new Object[]{"id", projectId};
		
		return getIntegerValueByQuery(query, parameter1);
	}

	public int resumeProjectAuthorsCounter(final long projectId) throws DatabaseException{
		final String query = "SELECT MAX(a.matrixPosition) from author a where a.project.id = :id";
		final Object[] parameter1 = new Object[]{"id", projectId};
		
		return getIntegerValueByQuery(query, parameter1);
	}

	public int resumeProjectFilesCounter(final long projectId) throws DatabaseException{
		final String query = "SELECT MAX(f.matrixPosition) from file f where f.entry.project.id = :id";
		final Object[] parameter1 = new Object[]{"id", projectId};
		
		return getIntegerValueByQuery(query, parameter1);
	}
}
