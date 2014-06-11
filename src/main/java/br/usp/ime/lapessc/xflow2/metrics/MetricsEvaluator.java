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
 *  =====================
 *  MetricEvaluation.java
 *  =====================
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.metrics;

import java.util.List;

import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraphType;
import br.usp.ime.lapessc.xflow2.entity.DependencySet;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.FileArtifact;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.ArtifactDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.metrics.EntryMetricsDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.metrics.FileMetricsDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.metrics.MetricsDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.metrics.ProjectMetricsDAO;
import br.usp.ime.lapessc.xflow2.entity.database.DatabaseManager;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGGraph;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGVertex;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.metrics.entry.AddedFiles;
import br.usp.ime.lapessc.xflow2.metrics.entry.DeletedFiles;
import br.usp.ime.lapessc.xflow2.metrics.entry.EntryLOC;
import br.usp.ime.lapessc.xflow2.metrics.entry.EntryMetricModel;
import br.usp.ime.lapessc.xflow2.metrics.entry.EntryMetricValues;
import br.usp.ime.lapessc.xflow2.metrics.entry.ModifiedFiles;
import br.usp.ime.lapessc.xflow2.metrics.file.Betweenness;
import br.usp.ime.lapessc.xflow2.metrics.file.Centrality;
import br.usp.ime.lapessc.xflow2.metrics.file.FileMetricModel;
import br.usp.ime.lapessc.xflow2.metrics.file.FileMetricValues;
import br.usp.ime.lapessc.xflow2.metrics.file.LOC;
import br.usp.ime.lapessc.xflow2.metrics.project.ClusterCoefficient;
import br.usp.ime.lapessc.xflow2.metrics.project.Density;
import br.usp.ime.lapessc.xflow2.metrics.project.ProjectMetricModel;
import br.usp.ime.lapessc.xflow2.metrics.project.ProjectMetricValues;
import br.usp.ime.lapessc.xflow2.repository.vcs.dao.CommitDAO;


public class MetricsEvaluator {

	private FileMetricModel[] fileMetrics = new FileMetricModel[]{
			new Centrality(), new Betweenness(), new LOC()
	};

	private EntryMetricModel[] entryMetrics = new EntryMetricModel[]{
			new AddedFiles(), new ModifiedFiles(), new EntryLOC(), new DeletedFiles()
	};

	private ProjectMetricModel[] projectMetrics = new ProjectMetricModel[]{
			new Density(), new ClusterCoefficient()
	};
	
	private Metrics metricsSession;
	
	private DependencyGraph initialDependency;

	public MetricsEvaluator(){
		JUNGGraph.clearVerticesCache();
	}

	public void evaluateMetrics(final Analysis analysis) throws DatabaseException {
		System.out.println("Metric evaluation started.\n");
		Metrics metrics = new Metrics();
		metrics.setAssociatedAnalysis(analysis);
		new MetricsDAO().insert(metrics);
		this.metricsSession = metrics;
		
		initiateCaches();
		
		final List<Long> revisions;
		if(analysis.isTemporalConsistencyForced()){
			//Retrieving all entries is crazy, so we just look for revision numbers
			//entries = new EntryDAO().getAllEntriesWithinEntries(analysis.getFirstEntry(), analysis.getLastEntry());
			revisions = new CommitDAO().getAllRevisionsWithinEntries(
					analysis.getFirstEntry(), 
					analysis.getLastEntry(), 
					0);
		} else {
			//Same from above
			//entries = new EntryDAO().getAllEntriesWithinRevisions(analysis.getProject(), analysis.getFirstEntry().getRevision(), analysis.getLastEntry().getRevision());
			revisions = new CommitDAO().getAllRevisionsWithinRevisions(
					analysis.getProject(), 
					analysis.getFirstEntry().getRevision(), 
					analysis.getLastEntry().getRevision(),
					0);
		}
		
		for (Long revisionNumber : revisions) {
			final Commit entry = new CommitDAO().findEntryFromRevision(analysis.getProject(), revisionNumber);
			System.out.print("Evaluating revision "+entry.getRevision()+"\n");
			DependencyGraph<FileDependencyObject, FileDependencyObject> entryDependency = new DependencyDAO().findDependencyByEntry(analysis.getId(), entry.getId(), DependencyGraphType.TASK_DEPENDENCY.getValue());
			if(entryDependency != null){
				if(analysis.checkCutoffValues(entry)){
					calculateEntryMetrics(entry);
					System.out.print("Entry metrics done!\n");
					calculateGraphMetrics(entryDependency);
					System.out.print("Graph metrics done!\n");
				} else {
					System.out.println("Revision skipped. Entry is not valid for current analysis.");
				}
			} else {
				if(analysis.checkCutoffValues(entry)){
					calculateEntryMetrics(entry);
					System.out.print("Entry metrics done!\n");
				}
				System.out.println("Graph metrics evaluation skipped. No dependencies collected for selected entry.");
			}
			//FIXME:
			//As we don't have an application layer yet, it is necessary 
			//to frequently clear the persistence context to avoid memory issues
			DatabaseManager.getDatabaseSession().clear();
		}

		clearCaches();
	}
	
	public void evaluateMetrics(final Analysis analysis, List<Commit> entries) throws DatabaseException {
		System.out.println("Metric evaluation started.\n");
		Metrics metrics = new Metrics();
		metrics.setAssociatedAnalysis(analysis);
		new MetricsDAO().insert(metrics);
		this.metricsSession = metrics;
//		this.metricsSession = new MetricsDAO().findById(Metrics.class, 4L);
		
		initiateCaches();
		
		for (Commit entry : entries) {
			System.out.print("Evaluating revision "+entry.getRevision()+"\n");
			DependencyGraph<FileDependencyObject, FileDependencyObject> entryDependency = new DependencyDAO().findDependencyByEntry(analysis.getId(), entry.getId(), DependencyGraphType.TASK_DEPENDENCY.getValue());
			if(entryDependency != null){
				if(analysis.checkCutoffValues(entry)){
					calculateEntryMetrics(entry);
					System.out.print("Entry metrics done!\n");
					calculateGraphMetrics3(entryDependency);
					System.out.print("Graph metrics done!\n");
				} else {
					System.out.println("Revision skipped. Entry is not valid for current analysis.");
				}
			} else {
				if(analysis.checkCutoffValues(entry)){
					calculateEntryMetrics(entry);
					System.out.print("Entry metrics done!\n");
				}
				System.out.println("Graph metrics evaluation skipped. No dependencies collected for selected entry.");
			}
		}

		clearCaches();
	}

	private void calculateGraphMetrics(final DependencyGraph<FileDependencyObject, FileDependencyObject> entryDependency) throws DatabaseException {
		final Commit entry = entryDependency.getAssociatedEntry();
		final JUNGGraph dependencyGraph = metricsSession.getAssociatedAnalysis().processDependencyGraph(entryDependency);
		
		for (DependencySet<FileDependencyObject, FileDependencyObject> dependencySet : entryDependency.getDependencies()) {
			FileDependencyObject fileDependency = dependencySet.getSupplier();
			
			final FileMetricValues fileMetricTable = new FileMetricValues();
			fileMetricTable.setAssociatedMetricsObject(metricsSession);
			fileMetricTable.setEntry(entry);
			fileMetricTable.setFile(fileDependency.getFile());

			calculateFileMetrics(dependencyGraph, fileDependency.getFile().getId(), fileMetricTable);

			new FileMetricsDAO().insert(fileMetricTable);
		}


		final ProjectMetricValues projectMetricTable = new ProjectMetricValues();
		projectMetricTable.setAssociatedMetricsObject(metricsSession);
		projectMetricTable.setEntry(entry);

		calculateProjectMetrics(dependencyGraph, projectMetricTable);
		new ProjectMetricsDAO().insert(projectMetricTable);
	}
	
	private void calculateGraphMetrics2(final DependencyGraph<FileDependencyObject, FileDependencyObject> entryDependency) throws DatabaseException {
		final Commit entry = entryDependency.getAssociatedEntry();
		final JUNGGraph dependencyGraph = ((CoChangesAnalysis) metricsSession.getAssociatedAnalysis()).processDependencyGraph2(entryDependency);
		
		for (JUNGVertex vertex : dependencyGraph.getGraph().getVertices()) {
			final FileArtifact matrixFile = new ArtifactDAO().findById(FileArtifact.class, vertex.getId());
			final FileMetricValues fileMetricTable = new FileMetricValues();
			fileMetricTable.setAssociatedMetricsObject(metricsSession);
			fileMetricTable.setEntry(entry);
			fileMetricTable.setFile(matrixFile);
			calculateFileMetrics(dependencyGraph, vertex.getId(), fileMetricTable);
			new FileMetricsDAO().insert(fileMetricTable);
		}
		
		final ProjectMetricValues projectMetricTable = new ProjectMetricValues();
		projectMetricTable.setAssociatedMetricsObject(metricsSession);
		projectMetricTable.setEntry(entry);

		calculateProjectMetrics(dependencyGraph, projectMetricTable);
		new ProjectMetricsDAO().insert(projectMetricTable);
	}
	
	private void calculateGraphMetrics3(final DependencyGraph<FileDependencyObject, FileDependencyObject> entryDependency) throws DatabaseException {
		if(initialDependency == null){
			initialDependency = entryDependency;
			return;
		}
		
		final Commit entry = entryDependency.getAssociatedEntry();
		final JUNGGraph dependencyGraph = ((CoChangesAnalysis) metricsSession.getAssociatedAnalysis()).processDependencyGraph3(initialDependency, entryDependency);
		
		for (JUNGVertex vertex : dependencyGraph.getGraph().getVertices()) {
			final FileArtifact matrixFile = new ArtifactDAO().findById(FileArtifact.class, vertex.getId());
			final FileMetricValues fileMetricTable = new FileMetricValues();
			fileMetricTable.setAssociatedMetricsObject(metricsSession);
			fileMetricTable.setEntry(entry);
			fileMetricTable.setFile(matrixFile);
			calculateFileMetrics(dependencyGraph, vertex.getId(), fileMetricTable);
			new FileMetricsDAO().insert(fileMetricTable);
		}
		
		final ProjectMetricValues projectMetricTable = new ProjectMetricValues();
		projectMetricTable.setAssociatedMetricsObject(metricsSession);
		projectMetricTable.setEntry(entry);

		calculateProjectMetrics(dependencyGraph, projectMetricTable);
		new ProjectMetricsDAO().insert(projectMetricTable);
	}


	private void calculateFileMetrics(final JUNGGraph dependencyGraph, final long fileID, final FileMetricValues fileMetricTable) throws DatabaseException {
		for (FileMetricModel fileMetric : fileMetrics) {
			fileMetric.evaluate(dependencyGraph, fileID, fileMetricTable);
		}
	}

	private void calculateProjectMetrics(final JUNGGraph dependencyGraph, final ProjectMetricValues projectMetricTable) {
		for (ProjectMetricModel projectMetric : projectMetrics) {
			projectMetric.evaluate(dependencyGraph, projectMetricTable);
		}
	}

	private void calculateEntryMetrics(final Commit entry) throws DatabaseException {
		final EntryMetricValues metricValues = new EntryMetricValues();
		metricValues.setAuthor(entry.getAuthor());
		metricValues.setAssociatedMetricsObject(metricsSession);
		metricValues.setEntry(entry);
		for (EntryMetricModel entryMetric : entryMetrics) {
			entryMetric.evaluate(entry, metricValues);
		}
		new EntryMetricsDAO().insert(metricValues);
	}

	public void setEntryMetrics(final EntryMetricModel[] entryMetrics) {
		this.entryMetrics = entryMetrics;
	}

	public void setFileMetrics(final FileMetricModel[] fileMetrics) {
		this.fileMetrics = fileMetrics;
	}

	public void setProjectMetrics(final ProjectMetricModel[] projectMetrics) {
		this.projectMetrics = projectMetrics;
	}

	private void clearCaches() {
		FileMetricModel.clearVerticesCache();
	}
	

	private void initiateCaches() {
		FileMetricModel.initiateCache();
	}
}
