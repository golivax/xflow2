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

import br.usp.ime.lapessc.xflow2.entity.VCSRepository;
import br.usp.ime.lapessc.xflow2.entity.dao.BaseDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;


public class VCSRepositoryDAO extends BaseDAO<VCSRepository> {

	@Override
	public VCSRepository findById(final Class<VCSRepository> clazz, final long id) throws DatabaseException {
		return super.findById(clazz, id);
	}

	@Override
	public boolean insert(final VCSRepository project) throws DatabaseException {
		return super.insert(project);
	}

	@Override
	public boolean remove(final VCSRepository project) throws DatabaseException {
		return super.remove(project);
	}

	@Override
	public boolean update(final VCSRepository project) throws DatabaseException {
		return super.update(project);
	}

	@Override
	public Collection<VCSRepository> findAll(final Class<? extends VCSRepository> myClass) throws DatabaseException {
		return super.findAll(myClass);
	}
	
	@Override
	protected VCSRepository findUnique(final Class<VCSRepository> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findUnique(clazz, query, parameters);
	}

	@Override
	protected Collection<VCSRepository> findByQuery(final Class<VCSRepository> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findByQuery(clazz, query, parameters);
	}
}
