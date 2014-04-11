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
 *  FolderDAO.java
 *  ==============
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.entity.dao.cm;

import java.util.ArrayList;
import java.util.Collection;

import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.Folder;
import br.usp.ime.lapessc.xflow2.entity.SoftwareProject;
import br.usp.ime.lapessc.xflow2.entity.VCSMiningProject;
import br.usp.ime.lapessc.xflow2.entity.dao.BaseDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.repository.vcs.dao.CommitDAO;


public class FolderDAO extends BaseDAO<Folder> {

	@Override
	public Folder findById(final Class<Folder> clazz, final long id) throws DatabaseException {
		return super.findById(clazz, id);
	}

	@Override
	public boolean insert(final Folder folder) throws DatabaseException {
		return super.insert(folder);
	}

	@Override
	public boolean remove(final Folder folder) throws DatabaseException {
		return super.remove(folder);
	}

	@Override
	public boolean update(final Folder folder) throws DatabaseException {
		return super.update(folder);
	}

	@Override
	protected Folder findUnique(final Class<Folder> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findUnique(clazz, query, parameters);
	}

	@Override
	protected Collection<Folder> findByQuery(final Class<Folder> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findByQuery(clazz, query, parameters);
	}

	@Override
	public Collection<Folder> findAll(final Class<? extends Folder> myClass) throws DatabaseException {
		return super.findAll(myClass);
	}

	public Folder findFolderByPath(final VCSMiningProject project, final String path) throws DatabaseException {
			
		final String query = "SELECT MAX(folder) FROM folder AS folder " +
				"JOIN folder.commit as commit " +
				"WHERE folder.path = :path " +
				"AND commit.vcsMiningProject.id = :project";
		
		final Object[] parameter1 = new Object[]{"path", path};
		final Object[] parameter2 = new Object[]{"project", project.getId()};
		return findUnique(Folder.class, query, parameter1, parameter2);
		
	}
	
	public ArrayList<Folder> findRootFoldersUntilRevision(final VCSMiningProject project, final long revision) throws DatabaseException {
		final String query = "select f from folder f where f.parentFolder = null and f.commit.vcsMiningProject = :project and f.commit.revision <= :revision";
		final Object[] parameter1 = new Object[]{"project", project};
		final Object[] parameter2 = new Object[]{"revision", revision};
		
		return (ArrayList<Folder>) findByQuery(Folder.class, query, parameter1, parameter2);
	}
	
	public ArrayList<Folder> findRootFoldersUntilSequence(VCSMiningProject project, long sequence) throws DatabaseException {
		final Commit entry = new CommitDAO().findEntryFromSequence(project, sequence); 
		final String query = "select f from folder f where f.parentFolder = null and f.entry.project = :project and f.entry.id <= :entryID";
		final Object[] parameter1 = new Object[]{"project", project};
		final Object[] parameter2 = new Object[]{"entryID", entry.getId()};
		
		return (ArrayList<Folder>) findByQuery(Folder.class, query, parameter1, parameter2);
	}
	
	public ArrayList<Folder> findSubFoldersUntilRevision(final long id, final long revision) throws DatabaseException {
		final String query = "select f from folder f where f.parentFolder.id = :parentId and f.commit.revision < :revision";
		final Object[] parameter1 = new Object[]{"parentId", id};
		final Object[] parameter2 = new Object[]{"revision", revision};
		
		return (ArrayList<Folder>) findByQuery(Folder.class, query, parameter1, parameter2);
	}
	
	public ArrayList<Folder> findSubFoldersUntilSequence(final long folderID, final long sequence) throws DatabaseException {
		final Folder folder = findById(Folder.class, folderID);
		final Commit entry = new CommitDAO().findEntryFromSequence(folder.getCommit().getVcsMiningProject(), sequence);
		final String query = "select f from folder f where f.parentFolder.id = :parentID and f.entry.project.id = :projectID and f.entry.id <= :entryID and f.fullPath like '%trunk%'";

		final Object[] parameter1 = new Object[]{"parentID", folderID};
		final Object[] parameter2 = new Object[]{"projectID", folder.getCommit().getVcsMiningProject().getId()};
		final Object[] parameter3 = new Object[]{"entryID", entry.getId()};
		
		return (ArrayList<Folder>) findByQuery(Folder.class, query, parameter1, parameter2, parameter3);
	}

}
