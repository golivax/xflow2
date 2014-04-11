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
 *  ObjFileDAO.java
 *  ===============
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.entity.dao.cm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import br.usp.ime.lapessc.xflow2.entity.Author;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.FileArtifact;
import br.usp.ime.lapessc.xflow2.entity.Folder;
import br.usp.ime.lapessc.xflow2.entity.VCSMiningProject;
import br.usp.ime.lapessc.xflow2.entity.dao.BaseDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.repository.vcs.dao.CommitDAO;


public class ArtifactDAO extends BaseDAO<FileArtifact> {

	@Override
	public FileArtifact findById(final Class<FileArtifact> clazz, final long id) throws DatabaseException {
		return super.findById(clazz, id);
	}

	@Override
	public boolean insert(final FileArtifact file) throws DatabaseException {
		return super.insert(file);
	}

	@Override
	public boolean remove(final FileArtifact file) throws DatabaseException {
		return super.remove(file);
	}

	@Override
	public boolean update(final FileArtifact file) throws DatabaseException {
		return super.update(file);
	}
	
	@Override
	protected FileArtifact findUnique(final Class<FileArtifact> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findUnique(clazz, query, parameters);
	}

	@Override
	protected Collection<FileArtifact> findByQuery(final Class<FileArtifact> clazz, final String query, final Object[]... parameters) throws DatabaseException {
		return super.findByQuery(clazz, query, parameters);
	}

	@Override
	public Collection<FileArtifact> findAll(final Class<? extends FileArtifact> myClass) throws DatabaseException {
		return super.findAll(myClass);
	}

	public FileArtifact findFileByPathUntilRevision(final VCSMiningProject project, final long revision, final String filePath) throws DatabaseException {
		final String query = "SELECT f from file f where f.id = (select max(f.id) from file f where f.path = :filePath and f.entry.project = :project and f.entry.revision <= :revision)";
		final Object[] parameter1 = new Object[]{"filePath", filePath};
		final Object[] parameter2 = new Object[]{"project", project};
		final Object[] parameter3 = new Object[]{"revision", revision};
		
		return findUnique(FileArtifact.class, query, parameter1, parameter2, parameter3);
	}
	
	public FileArtifact findFileByPathUntilDate(final VCSMiningProject project, final Date date, final String filePath) throws DatabaseException {
		final String query = "SELECT f from file f where f.id = (select max(f.id) from file f where f.path = :filePath and f.entry.project = :project and f.entry.date <= :date)";
		final Object[] parameter1 = new Object[]{"filePath", filePath};
		final Object[] parameter2 = new Object[]{"project", project};
		final Object[] parameter3 = new Object[]{"date", date};
		
		return findUnique(FileArtifact.class, query, parameter1, parameter2, parameter3);
	}
	
	public FileArtifact findFileByPathUntilEntry(final VCSMiningProject project, final Commit entry, final String filePath) throws DatabaseException {
		final String query = "SELECT f from file f where f.id = (select max(f.id) from file f where f.path = :filePath and f.entry.project = :project and f.entry.id <= :entryID)";
		final Object[] parameter1 = new Object[]{"filePath", filePath};
		final Object[] parameter2 = new Object[]{"project", project};
		final Object[] parameter3 = new Object[]{"entryID", entry.getId()};
		
		return findUnique(FileArtifact.class, query, parameter1, parameter2, parameter3);
	}
	
	public FileArtifact findAddedFileByPathUntilEntry(final VCSMiningProject project, final Commit entry, final String filePath) throws DatabaseException {
		final String query = "SELECT f FROM file f WHERE f.id = " +
				"(SELECT MAX(f.id) FROM file f " +
				"JOIN f.commit AS entry " +
				"WHERE f.path = :filePath " +
				"AND entry.vcsMiningProject.id = :project " +
				"AND entry.id <= :entryID " +
				"AND f.operationType = 'A')";
		
		final Object[] parameter1 = new Object[]{"filePath", filePath};
		final Object[] parameter2 = new Object[]{"project", project.getId()};
		final Object[] parameter3 = new Object[]{"entryID", entry.getId()};
		
		return findUnique(FileArtifact.class, query, parameter1, parameter2, parameter3);
	}
	
	public FileArtifact findAddedFileByPathUntilRevision(final VCSMiningProject project, final long revision, final String filePath) throws DatabaseException {
		
		final String query = "SELECT MAX(file) FROM file AS file " +
				"JOIN file.commit as commit " +
				"WHERE file.operationType = 'A' " +
				"AND file.path = :path " +
				"AND commit.revision <= :revision " +
				"AND commit.vcsMiningProject.id = :project";
		
		final Object[] parameter1 = new Object[]{"path", filePath};
		final Object[] parameter2 = new Object[]{"project", project.getId()};
		final Object[] parameter3 = new Object[]{"revision", revision};
		
		return findUnique(FileArtifact.class, query, parameter1, parameter2, parameter3);
	}
	
	public List<FileArtifact> getAllAddedFilesUntilRevision(final VCSMiningProject project, final long revision) throws DatabaseException {
		final String query = "SELECT f FROM file f " +
			"JOIN f.entry AS entry " +
			"WHERE f.operationType = 'A' " +
			"AND entry.project.id = :projectID " +
			"AND entry.revision <= :revision " +
			"AND (f.deletedOn is null OR f.deletedOn.revision > :revision)";

		final Object[] parameter1 = new Object[]{"projectID", project.getId()};
		final Object[] parameter2 = new Object[]{"revision", revision};

		return (ArrayList<FileArtifact>) findByQuery(FileArtifact.class, query, parameter1, parameter2);
	}
	
	public List<FileArtifact> getAllAddedFilesUntilEntry(final VCSMiningProject project, final Commit entry) throws DatabaseException {
		final String query = "SELECT f FROM file f " +
		"JOIN f.entry AS entry " +
		"WHERE f.operationType = 'A' " +
		"AND entry.project.id = :projectID " +
		"AND entry.id <= :entryID " +
		"AND (f.deletedOn is null OR f.deletedOn.id > :entryID)";

		final Object[] parameter1 = new Object[]{"projectID", project.getId()};
		final Object[] parameter2 = new Object[]{"entryID", entry.getId()};
		
		return (ArrayList<FileArtifact>) findByQuery(FileArtifact.class, query, parameter1, parameter2);
	}
	
	public int getPreviousLoC(final VCSMiningProject project, final long fileId, 
			final String filePath) throws DatabaseException{
		
		final String query = "SELECT f.totalLinesOfCode FROM file f WHERE f.id = " +
				"(SELECT MAX(f.id) FROM file f " +
				"JOIN f.commit AS commit " +
				"WHERE f.path = :filePath " +
				"AND f.id <> :fileID " +
				"AND commit.vcsMiningProject.id = :project)";

		final Object[] parameter1 = new Object[]{"filePath", filePath};
		final Object[] parameter2 = new Object[]{"project", project.getId()};
		final Object[] parameter3 = new Object[]{"fileID", fileId};
		
		return getIntegerValueByQuery(query,parameter1,parameter2,parameter3);
	}
	
	public int getFileLOCUntilDate(final VCSMiningProject project, final Date date, final String filePath) throws DatabaseException{
		final String query = "SELECT f from file f where f.id = (select max(f.id) from file f where f.path = :filePath and f.entry.project = :project and f.entry.date <= :date)";
		final Object[] parameter1 = new Object[]{"filePath", filePath};
		final Object[] parameter2 = new Object[]{"project", project};
		final Object[] parameter3 = new Object[]{"date", date};
		
		return findUnique(FileArtifact.class, query, parameter1, parameter2, parameter3).getTotalLinesOfCode();
	}
	
	public FileArtifact findFileByPath(final long projectID, final String path) throws DatabaseException {
		//This query is more efficient, since it does not require a
		//table join on project
		final String query = "SELECT MAX(file) FROM file AS file " +
				"JOIN file.commit as commit " +
				"WHERE file.operationType = 'A' " +
				"AND file.path = :path " +
				"AND commit.vcsMiningProject.id = :project";
		final Object[] parameter1 = new Object[]{"path", path};
		final Object[] parameter2 = new Object[]{"project", projectID};
		return findUnique(FileArtifact.class, query, parameter1, parameter2);
	}
	
	public ArrayList<FileArtifact> getFilesFromEntryID(final long entryID) throws DatabaseException {
		final String query = "select distinct f from file f where f.entry.id = :entryID";
		final Object[] parameter1 = new Object[]{"entryID", entryID};
		
		return (ArrayList<FileArtifact>) findByQuery(FileArtifact.class, query, parameter1);
	}
	
	public ArrayList<FileArtifact> getFilesFromRevision(final long revision) throws DatabaseException {
		final String query = "select distinct f from file f where f.entry.revision = :revision";
		final Object[] parameter1 = new Object[]{"revision", revision};
		
		return (ArrayList<FileArtifact>) findByQuery(FileArtifact.class, query, parameter1);
	}
	
	public ArrayList<FileArtifact> getFilesFromFolderUntilRevision(final long id, final long revision) throws DatabaseException {
		final String query = "select f from file f where f.parentFolder.id = :parentId and f.operationType = 'A'";
		final Object[] parameter1 = new Object[]{"parentId", id};
		
		return (ArrayList<FileArtifact>) findByQuery(FileArtifact.class, query, parameter1);
	}

	public ArrayList<FileArtifact> getFilesFromFolderUntilSequence(Folder folder, long sequence) throws DatabaseException {
		final Commit entry = new CommitDAO().findEntryFromSequence(folder.getCommit().getVcsMiningProject(), sequence);
		final String query = "select f from file f where f.parentFolder.id = :parentID and f.operationType = 'A' and f.entry.id <= :entryID and (f.deletedOn is not null OR f.deletedOn.id <= :entryID)";
		final Object[] parameter1 = new Object[]{"parentID", folder.getId()};
		final Object[] parameter2 = new Object[]{"entryID", entry.getId()};
		
		return (ArrayList<FileArtifact>) findByQuery(FileArtifact.class, query, parameter1, parameter2);
	}
	
	public int getLoCByFile(final FileArtifact file) throws DatabaseException{
		final String query = "SELECT MAX(f.id) from file f where f = :file";
		final Object[] parameter1 = new Object[]{"file", file};
		
		return findUnique(FileArtifact.class, query, parameter1).getTotalLinesOfCode();
	}

	public ArrayList<FileArtifact> getFilesFromSequenceNumber(final VCSMiningProject project, final long sequenceNumber) throws DatabaseException {
		final String query = "select f from file f where f.entry.project = :project and f.entry.sequenceNumber = :sequenceNumber";
		final Object[] parameter1 = new Object[]{"project", project};
		final Object[] parameter2 = new Object[]{"sequenceNumber", sequenceNumber};
		
		return (ArrayList<FileArtifact>) findByQuery(FileArtifact.class, query, parameter1, parameter2);
	}
	
	public List<String> getFilesPathFromSequenceNumberOrderedByRevision(final VCSMiningProject project, final long sequenceNumber) throws DatabaseException {
		final String query = "select DISTINCT f.path from file f where f.entry.project = :project and f.entry = (select entry from entry e where e = (select count(*)))";
		final Object[] parameter1 = new Object[]{"project", project};
		final Object[] parameter2 = new Object[]{"sequenceNumber", sequenceNumber};
		
		return (List<String>) findObjectsByQuery(query, parameter1, parameter2);
	}
	
	public List<String> getFilesPathFromSequenceNumberOrderedBySequence(final VCSMiningProject project, final long sequenceNumber) throws DatabaseException {
		final String query = "select DISTINCT f.path from file f where f.entry.project = :project and f.entry = (select entry from entry e where )";
		final Object[] parameter1 = new Object[]{"project", project};
		final Object[] parameter2 = new Object[]{"sequenceNumber", sequenceNumber};
		
		return (List<String>) findObjectsByQuery(query, parameter1, parameter2);
	}
	
	public ArrayList<FileArtifact> getEntryChangedFiles(final long entryID) throws DatabaseException {
		final String query = "select f from file f where f.entry.id = :entryID";
		final Object[] parameter1 = new Object[]{"entryID", entryID};
		
		return (ArrayList<FileArtifact>) findByQuery(FileArtifact.class, query, parameter1);
	}

	public List<String> getFilePathsFromEntry(final Commit entry) throws DatabaseException{
		final String query = "select f.path from file f where f.commit.id = :entryID";
		final Object[] parameter1 = new Object[]{"entryID", entry.getId()};
		
		return (List<String>) findObjectsByQuery(query, parameter1);
	}

	public List<FileArtifact> getAllAddedFilesUntilEntryByAuthor(VCSMiningProject project, Commit entry, Author author) throws DatabaseException {

		final String query = "SELECT f FROM file f " +
		"JOIN f.entry AS entry " +
		"WHERE f.operationType = 'A' " +
		"AND entry.project.id = :projectID " +
		"AND entry.id <= :entryID " +
		"AND entry.author.id = :authorID " +
		"AND (f.deletedOn is null OR f.deletedOn.id > :entryID)";

		final Object[] parameter1 = new Object[]{"projectID", project.getId()};
		final Object[] parameter2 = new Object[]{"entryID", entry.getId()};
		final Object[] parameter3 = new Object[]{"authorID", author.getId()};

		return (ArrayList<FileArtifact>) findByQuery(FileArtifact.class, query, parameter1, parameter2, parameter3);
	}

	public List<FileArtifact> getAllAddedFilesUntilRevisionByAuthor(VCSMiningProject project, long revision, Author author) throws DatabaseException {
		final String query = "SELECT f FROM file f " +
		"JOIN f.entry AS entry " +
		"WHERE f.operationType = 'A' " +
		"AND entry.project.id = :projectID " +
		"AND entry.revision <= :revision " +
		"AND entry.author.id = :authorID " +
		"AND (f.deletedOn is null OR f.deletedOn.revision > :revision)";

		final Object[] parameter1 = new Object[]{"projectID", project.getId()};
		final Object[] parameter2 = new Object[]{"revision", revision};
		final Object[] parameter3 = new Object[]{"authorID", author.getId()};

		return (ArrayList<FileArtifact>) findByQuery(FileArtifact.class, query, parameter1, parameter2, parameter3);
	}

}
