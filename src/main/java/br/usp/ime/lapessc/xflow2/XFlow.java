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
 *  =============
 *  Launcher.java
 *  =============
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2;
import java.util.Date;

import javax.persistence.EntityManager;

import br.usp.ime.lapessc.xflow2.core.AnalysisFactory;
import br.usp.ime.lapessc.xflow2.core.DataProcessor;
import br.usp.ime.lapessc.xflow2.core.VCSMiner;
import br.usp.ime.lapessc.xflow2.core.transactions.SlidingTimeWindowProcessor;
import br.usp.ime.lapessc.xflow2.entity.AccessCredentials;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.MiningSettings;
import br.usp.ime.lapessc.xflow2.entity.VCSMiningProject;
import br.usp.ime.lapessc.xflow2.entity.VCSRepository;
import br.usp.ime.lapessc.xflow2.entity.XFlowProject;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.VCSMiningProjectDAO;
import br.usp.ime.lapessc.xflow2.entity.database.DatabaseManager;
import br.usp.ime.lapessc.xflow2.exception.cm.CMException;
import br.usp.ime.lapessc.xflow2.exception.core.analysis.AnalysisRangeException;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.metrics.MetricsEvaluator;
import br.usp.ime.lapessc.xflow2.metrics.entry.EntryMetricModel;
import br.usp.ime.lapessc.xflow2.metrics.file.FileMetricModel;
import br.usp.ime.lapessc.xflow2.metrics.project.ProjectMetricModel;
import br.usp.ime.lapessc.xflow2.repository.vcs.entities.VCSType;
import br.usp.ime.lapessc.xflow2.util.Filter;
import br.usp.ime.lapessc.xflow2.util.XFlowConfig;
import br.usp.ime.lapessc.xflow2.util.io.Kbd;


public class XFlow {
		
	/**
	
	public void downloadProjectData(SoftwareProject project, long firstRevision, long lastRevision, Filter filter) throws CMException, DatabaseException {
		DataExtractor dataExtractor = new DataExtractor();
		dataExtractor.extractData(project, firstRevision, lastRevision, filter);
	}
	
	public void resumeProjectDownload(SoftwareProject project, int newFinalCommit, Filter filter) throws CMException, DatabaseException{
		int lastDownloadedCommit = (int) project.getLastRevision();
		DataExtractor extractor = new DataExtractor();
		extractor.extractData(project, lastDownloadedCommit+1, newFinalCommit, filter);
	}

	public void deleteProject(SoftwareProject project){
		try {
			new ProjectDAO().remove(project);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
	*/
	
	public void processProject(Analysis analysis, Filter filter) throws DatabaseException {
		DataProcessor.processEntries(analysis, filter);
	}	
	
	public void resumeProjectProcessing(Analysis analysis, long endRevision, Filter filter, String details) throws DatabaseException {
		DataProcessor.resumeProcess(analysis, endRevision, filter, details);
	}
	
	public void resumeProjectProcessing(Analysis analysis, Commit finalEntry, Filter filter, String details) throws DatabaseException {
		DataProcessor.resumeProcess(analysis, finalEntry, filter, details);
	}

	public void evaluateMetrics(Analysis analysis, ProjectMetricModel[] selectedProjectMetrics, EntryMetricModel[] selectedEntryMetrics, FileMetricModel[] selectedFileMetrics) throws DatabaseException {
		MetricsEvaluator dataEvaluator = new MetricsEvaluator();
		
		if (selectedProjectMetrics != null){
			dataEvaluator.setProjectMetrics(selectedProjectMetrics);
		}
		
		if (selectedEntryMetrics != null){
			dataEvaluator.setEntryMetrics(selectedEntryMetrics);
		}
		
		if (selectedFileMetrics != null){
			dataEvaluator.setFileMetrics(selectedFileMetrics);
		}
		
		dataEvaluator.evaluateMetrics(analysis);
	}

	public static void main(String[] args) {
		
		System.out.println(new Date(System.currentTimeMillis()));
		
		/**
		try{
			//CallGraphAnalysis callGraphAnalysis = (CallGraphAnalysis)new AnalysisDAO().findById(Analysis.class, 171L);
			//StructuralCouplingCalculator couplingCalculator = new StructuralCouplingCalculator();
			//couplingCalculator.calculate(callGraphAnalysis);
			
			CoChangesAnalysis coChangesAnalysis = 
				(CoChangesAnalysis)new AnalysisDAO().findById(
				Analysis.class, 1L);
			
			CoChangeCalculator coChangeCalculator = new CoChangeCalculator();
			coChangeCalculator.calculate(coChangesAnalysis);
		}catch(Exception e){
			e.printStackTrace();
		}
		*/
		
		/**
		try{
			Project project = new ProjectDAO().findById(Project.class, 1L);
			FragilityCalculator fragilityCalculator = new FragilityCalculator();
			fragilityCalculator.calculate(project, 5);
		}catch(Exception e){
			e.printStackTrace();
		}
		*/
		
		/**
		Project p = null;
		Launcher a = new Launcher();
		
		try {
			p = new ProjectDAO().findById(Project.class, 1L);
			a.resumeProjectDownload(p, 500000, new Filter(".*?"));
		} catch (DatabaseException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	
		
		try {

			XFlowProject xFlowProject = createXFlowProject();
			
			String miningDescription = Kbd.readString(
					"Enter mining description: ");
			
			Long firstRev = Kbd.readLong("Enter initial revision number: ");
			Long lastRev = Kbd.readLong("Enter last revision number: ");
			
			String filterRegex = Kbd.readString(
					"Enter file path filter (regex): ");
			boolean codeDownloadEnabled = Kbd.readBoolean(
					"Would you like it to download source code (y/n): ");
			boolean temporalConsistencyForced = Kbd.readBoolean(
					"Would you like to force temporal consistency (y/n): ");
			
			AccessCredentials accessCredentials = new AccessCredentials(
					XFlowConfig.getInstance().getVCSConfig().getUser(),
					XFlowConfig.getInstance().getVCSConfig().getPassword());
			
			MiningSettings settings = new MiningSettings(
					xFlowProject.getVcsRepository(), accessCredentials, 
					miningDescription, new Filter(filterRegex), 
					codeDownloadEnabled, temporalConsistencyForced); 
			
			VCSMiner vcsMiner = new VCSMiner();
			
			VCSMiningProject miningProject = vcsMiner.mine(settings, 
					firstRev, lastRev);

			xFlowProject.addVcsMiningProject(miningProject);
			
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (CMException e){
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
	
		/**		
		try{
			VCSMiningProject miningProject = new VCSMiningProjectDAO().findById(
					VCSMiningProject.class, 1L);
			
			//CoChange
			Analysis analysis = AnalysisFactory.createCoChangesAnalysis(
					miningProject, "CoChanges for the Ant project", false, 
					275817, 486465, 0, 0, 5, false);
			
			DataProcessor.processEntries(analysis, new Filter(".*?"));
			
		}catch(Exception e){
			e.printStackTrace();
		}
		*/
		
		/**
		try {
			VCSMiningProject miningProject = new VCSMiningProjectDAO().findById(
					VCSMiningProject.class, 1L);

			SlidingTimeWindowProcessor.process(miningProject, 200);
			
			//a.resumeProjectDownload(p, 7185, new Filter(".*?/trunk/.*?(\\.(java|html|h|c|cpp))"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		*/
		
		/**
		//XXX: CÓDIGO PARA PROCESSAMENTO DO PROJETO
		try {
			
			VCSMiningProject miningProject = new VCSMiningProjectDAO().findById(
					VCSMiningProject.class, 1L);
			
			//CoChange
			Analysis analysis = AnalysisFactory.createCoChangesAnalysis(
					miningProject, "CoChanges for the Ant project", false, 
					275814, 486471, 0, 0, 9, false);
			
			//CallGraph
			//Analysis analysis = AnalysisFactory.createCallGraphAnalysis(p, 
			//"CallGraph - All ASF (*.java)", false, 141, 1120394, 13);
			
			DataProcessor.processEntries(analysis, new Filter(".*?"));
			
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (AnalysisRangeException e) {
			e.printStackTrace();
		}
		*/
				
		/**
		FileMetricModel[] fileMetrics = new FileMetricModel[]{
	            new Centrality(), new Betweenness()
		};

		
		ProjectMetricModel[] projectMetrics = new ProjectMetricModel[]{
	            new Density(), new ClusterCoefficient()
		};
		
		
		EntryMetricModel[] entryMetrics = new EntryMetricModel[]{
	            new AddedFiles(), new ModifiedFiles(), new DeletedFiles(), 
	            new EntryLOC()
		};

		//XXX: CÓDIGO PARA CALCULDO DAS MÉTRICAS
		try {
			Launcher a = new Launcher();
			a.evaluateMetrics(new AnalysisDAO().findById(
			Analysis.class, 1L), null, entryMetrics, null);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		System.out.println(new Date(System.currentTimeMillis()));
	}

	private static XFlowProject createXFlowProject() {
		
		String xFlowProjectName = 
				Kbd.readString("Enter XFlow project name: ");
				
		XFlowProject xFlowProject = 
				new XFlowProject(xFlowProjectName);
		
		String vcsURI = XFlowConfig.getInstance().getVCSConfig().getUrl();
		VCSRepository vcsRepository = new VCSRepository(VCSType.Subversion, 
				vcsURI);
		
		xFlowProject.setVcsRepository(vcsRepository);
		
		try{
			
			final EntityManager manager = DatabaseManager.getDatabaseSession();
			manager.getTransaction().begin();
			manager.persist(xFlowProject);
			manager.getTransaction().commit();
			
		}catch(DatabaseException e){
			e.printStackTrace();
		}
		
		return xFlowProject;
	}
}