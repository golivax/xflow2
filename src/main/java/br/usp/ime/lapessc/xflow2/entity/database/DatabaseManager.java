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
 *  ====================
 *  DatabaseManager.java
 *  ====================
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  Pedro Treccani, David Bentolila;
 *  
 */

package br.usp.ime.lapessc.xflow2.entity.database;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import br.usp.ime.lapessc.xflow2.core.processors.callgraph.CallGraphAnalysis;
import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.core.transactions.TimeWindow;
import br.usp.ime.lapessc.xflow2.entity.AccessCredentials;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.Author;
import br.usp.ime.lapessc.xflow2.entity.AuthorDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.CoordinationRequirements;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencyObject;
import br.usp.ime.lapessc.xflow2.entity.DependencySet;
import br.usp.ime.lapessc.xflow2.entity.FileArtifact;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.Folder;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.MiningSettings;
import br.usp.ime.lapessc.xflow2.entity.Resource;
import br.usp.ime.lapessc.xflow2.entity.SoftwareProject;
import br.usp.ime.lapessc.xflow2.entity.TaskAssignment;
import br.usp.ime.lapessc.xflow2.entity.TaskDependency;
import br.usp.ime.lapessc.xflow2.entity.VCSMiningProject;
import br.usp.ime.lapessc.xflow2.entity.VCSRepository;
import br.usp.ime.lapessc.xflow2.entity.XFlowProject;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.metrics.cochange.CoChange;
import br.usp.ime.lapessc.xflow2.metrics.cochange.StructuralCoupling;
import br.usp.ime.lapessc.xflow2.metrics.entry.EntryMetricValues;
import br.usp.ime.lapessc.xflow2.metrics.file.FileMetricValues;
import br.usp.ime.lapessc.xflow2.metrics.project.ProjectMetricValues;
import br.usp.ime.lapessc.xflow2.util.DatabaseConfig;
import br.usp.ime.lapessc.xflow2.util.Filter;
import br.usp.ime.lapessc.xflow2.util.XFlowConfig;


public class DatabaseManager {

	private static EntityManagerFactory emf = null;
	private static EntityManager em = null;
	
	private final Class<?>[] CLAZZS = new Class<?>[]{XFlowProject.class, SoftwareProject.class, 
			VCSRepository.class, VCSMiningProject.class, MiningSettings.class, AccessCredentials.class, 
			Filter.class, DependencyGraph.class, Author.class, Commit.class, Folder.class, FileArtifact.class, Resource.class, 
			Analysis.class, CallGraphAnalysis.class, CoChangesAnalysis.class, EntryMetricValues.class, Metrics.class,
			ProjectMetricValues.class, FileMetricValues.class, DependencyObject.class, 
			FileDependencyObject.class, AuthorDependencyObject.class, TaskDependency.class,
			TaskAssignment.class, CoordinationRequirements.class, DependencySet.class, 
			StructuralCoupling.class, CoChange.class, TimeWindow.class
	};
	
	private DatabaseManager() throws DatabaseException {

		DatabaseConfig dbConfig = XFlowConfig.getInstance().getDBConfig();
		String dialect = "br.usp.ime.lapessc.xflow2.entity.database.XFlowMySqlDialect";
			
		emf = DynamicPersistenceUnits.createEMF(CLAZZS, dialect, 
				dbConfig.getUrl(), dbConfig.getPort(), dbConfig.getDatabase(), 
				dbConfig.getUser(), dbConfig.getPassword());  
			
		em = emf.createEntityManager();
			
	}
	
	private synchronized static EntityManagerFactory getManagerFactory() 
			throws DatabaseException {
		
		if (emf == null) {
			new DatabaseManager();
		}
		return emf;
	}

	public static EntityManager getDatabaseSession() throws DatabaseException {
		if (em == null)
			em = getManagerFactory().createEntityManager();
		return em;
	}
	
}
