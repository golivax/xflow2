package br.usp.ime.lapessc.xflow2.presentation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;

import br.usp.ime.lapessc.xflow2.core.processors.cochanges.CoChangesAnalysis;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.dao.core.AnalysisDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.metrics.MetricsDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.metrics.MetricModel;
import br.usp.ime.lapessc.xflow2.metrics.entry.AddedFiles;
import br.usp.ime.lapessc.xflow2.metrics.entry.DeletedFiles;
import br.usp.ime.lapessc.xflow2.metrics.entry.EntryLOC;
import br.usp.ime.lapessc.xflow2.metrics.entry.EntryMetricModel;
import br.usp.ime.lapessc.xflow2.metrics.entry.ModifiedFiles;
import br.usp.ime.lapessc.xflow2.metrics.file.Betweenness;
import br.usp.ime.lapessc.xflow2.metrics.file.Centrality;
import br.usp.ime.lapessc.xflow2.metrics.file.FileMetricModel;
import br.usp.ime.lapessc.xflow2.metrics.project.ClusterCoefficient;
import br.usp.ime.lapessc.xflow2.metrics.project.Density;
import br.usp.ime.lapessc.xflow2.metrics.project.ProjectMetricModel;
import br.usp.ime.lapessc.xflow2.presentation.view.ProjectViewer;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.Visualization;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.activity.ActivityVisualization;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.graph.GraphVisualization;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.line.LineVisualization;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.scatterplot.ScatterplotVisualization;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.treemap.TreemapVisualization;


public class Visualizer {

	public static final int LINE_VISUALIZATION = 0;
	public static final int GRAPH_VISUALIZATION = 1;
	public static final int SCATTERPLOT_VISUALIZATION = 2;
	public static final int TREEMAP_VISUALIZATION = 3;
	public static final int ACTIVITY_VISUALIZATION = 4;
	
	private static ProjectViewer projectsViewer;
	
	public Visualizer(){
		//Empty constructor.
	}
	
	public Visualizer(int viewType){
		this.projectsViewer = ProjectViewer.createInstance(viewType);
	}
	
	public static ProjectViewer getProjectsViewer() {
		return projectsViewer;
	}

	public static void setProjectsViewer(ProjectViewer projectsViewer) {
		Visualizer.projectsViewer = projectsViewer;
	}

	
	public JComponent composeVisualizations(boolean[] selectedVisualizations, MetricModel[] selectedMetrics, Metrics ... metricsSession) throws DatabaseException{
		Collection<Visualization> visualizationsRequired = new Vector<Visualization>();
		for (Metrics metrics : metricsSession) {
			Collection<Visualization> visualizations = identifySelectedVisualizations(selectedVisualizations, metrics);
			visualizationsRequired.addAll(visualizations);
		}
		
		Collection<ProjectMetricModel> projectMetrics = new Vector<ProjectMetricModel>();
		Collection<EntryMetricModel> entryMetrics = new Vector<EntryMetricModel>();
		Collection<FileMetricModel> fileMetrics = new Vector<FileMetricModel>();
		for (MetricModel metric : selectedMetrics) {
			if (metric instanceof ProjectMetricModel) {
				projectMetrics.add((ProjectMetricModel) metric);
			} else if (metric instanceof EntryMetricModel) {
				entryMetrics.add((EntryMetricModel) metric);
			} else if (metric instanceof FileMetricModel) {
				fileMetrics.add((FileMetricModel) metric);
			}
		}
		projectsViewer.setVisualizations(visualizationsRequired);
		projectsViewer.setMetrics(Arrays.asList(metricsSession));
		ProjectViewer.setEntryMetrics(entryMetrics.toArray(new EntryMetricModel[]{}));
		ProjectViewer.setFileMetrics(fileMetrics.toArray(new FileMetricModel[]{}));
		ProjectViewer.setProjectMetrics(projectMetrics.toArray(new ProjectMetricModel[]{}));
		
		return projectsViewer.displayVisualizations();
	}

	private Collection<Visualization> identifySelectedVisualizations(boolean[] selectedVisualizations, Metrics metricsSession) {
		Collection<Visualization> validVisualizations = new Vector<Visualization>();
		if(selectedVisualizations[LINE_VISUALIZATION]){
			validVisualizations.add(new LineVisualization(metricsSession));
		}
		if(selectedVisualizations[GRAPH_VISUALIZATION]){
			validVisualizations.add(new GraphVisualization(metricsSession));
		}
		if(selectedVisualizations[SCATTERPLOT_VISUALIZATION]){
			validVisualizations.add(new ScatterplotVisualization(metricsSession));
		}
		if(selectedVisualizations[TREEMAP_VISUALIZATION]){
			validVisualizations.add(new TreemapVisualization(metricsSession));
		}
		if(selectedVisualizations[ACTIVITY_VISUALIZATION]){
			validVisualizations.add(new ActivityVisualization(metricsSession));
		}
		
		return validVisualizations;
	}
	
	public static void main(String[] args) throws DatabaseException {
//		try {
//	        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
//	    } catch (Exception e) {
//	           e.printStackTrace();
//	    }
	    
		Visualizer vis = new Visualizer(ProjectViewer.SINGLE_PROJECT_VIEW);
        boolean[] visualizations = new boolean[]{false, true, false, false, false};
        
        /**
        MetricModel[] metrics = new MetricModel[]{
        		new Density(), new ClusterCoefficient(), new AddedFiles(), new ModifiedFiles(), new DeletedFiles(), new EntryLOC(),
        		new Centrality()
        		, new Betweenness()
        };
        */
        
        /**
        Metrics[] metricsSession = new Metrics[]{
        		new MetricsDAO().findById(Metrics.class, 1L), 
        		new MetricsDAO().findById(Metrics.class, 2L), 
        		new MetricsDAO().findById(Metrics.class, 4L)};
        */
        
        MetricModel[] metricModel = new MetricModel[0];
        
        
    	AnalysisDAO analysisDAO = new AnalysisDAO();
		Analysis analysis = analysisDAO.findById(Analysis.class, 1L);
        
		Metrics metrics = new Metrics();
		metrics.setAssociatedAnalysis(analysis);
		
        Metrics[] metricsSession = {metrics};
        
//        Metrics[] metricsSession = new Metrics[]{new MetricsDAO().findById(Metrics.class, 2L), new MetricsDAO().findById(Metrics.class, 4L)};
//        Metrics[] metricsSession = new Metrics[]{new MetricsDAO().findById(Metrics.class, 4L), new MetricsDAO().findById(Metrics.class, 4L), new MetricsDAO().findById(Metrics.class, 4L)};
//        Metrics[] metricsSession = new Metrics[]{new MetricsDAO().findById(Metrics.class, 4L)};
//      Metrics[] metricsSession = new Metrics[]{new MetricsDAO().findById(Metrics.class, 10L)};
        
        
//        Metrics metric = new Metrics();
//        metric.setAssociatedAnalysis(new AnalysisDAO().findById(Analysis.class, 1L));
//        System.out.println(metric.getAssociatedAnalysis().getFirstEntry());
//        System.out.println(metric.getAssociatedAnalysis().getLastEntry());
////        Entry entry = new EntryDAO().findById(Entry.class, 83399);
////        metric.getAssociatedAnalysis().setLastEntry(entry);
//        Metrics[] metricsSession = new Metrics[1];
//        metricsSession[0] = metric;
        
		JComponent component = vis.composeVisualizations(
				visualizations, metricModel, metricsSession);
		
        JFrame frame = new JFrame();
        frame.setExtendedState(frame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.add(component);
        //frame.pack();
		frame.setVisible(true);
	}
}
