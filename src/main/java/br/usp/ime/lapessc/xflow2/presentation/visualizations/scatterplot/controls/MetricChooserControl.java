package br.usp.ime.lapessc.xflow2.presentation.visualizations.scatterplot.controls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import br.usp.ime.lapessc.xflow2.presentation.view.ProjectViewer;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.VisualizationControl;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.scatterplot.ScatterplotRenderer;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.scatterplot.ScatterplotVisualization;

public class MetricChooserControl implements VisualizationControl<ScatterplotVisualization>, ActionListener {

	private JComboBox metricChooser;
	private ScatterplotVisualization visualizationControlled;
	
	@Override
	public void buildControlGUI(JComponent visualizationComponent) {
		this.visualizationControlled = (ScatterplotVisualization) ((JComponent) visualizationComponent.getParent()).getClientProperty("Visualization Instance");
		JPanel metricPickerPanel = new JPanel();
		
		metricChooser = createMetricsComboBox();
		metricChooser.addActionListener(this);

		JLabel chooseMetricLabel = new JLabel("Selected metric (Y Axis):");
		metricPickerPanel.add(chooseMetricLabel);
		metricPickerPanel.add(metricChooser);
		metricPickerPanel.setOpaque(false);
		
		visualizationComponent.add(metricPickerPanel);
	}

	private JComboBox createMetricsComboBox() {
		List<String> metricsNames = ProjectViewer.getAvailableProjectMetricsNames();
		metricsNames.addAll(ProjectViewer.getAvailableEntryMetricsNames());
		final String[] metricNamesVariations = new String[]{"Higher ", "Average ", "Max "};
		for (String string : ProjectViewer.getAvailableFileMetricsNames()) {
			for (int i = 0; i < metricNamesVariations.length; i++) {
				metricsNames.add(metricNamesVariations[i]+string);
			}
		}
		
		JComboBox metricPickerComboBox = new JComboBox((Vector<String>) metricsNames);
		return metricPickerComboBox;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		((ScatterplotRenderer) visualizationControlled.getRenderers()[0]).updateYAxis((String) metricChooser.getSelectedItem());
//		Visualizer.getScatterPlotView().getScatterPlotRenderer().updateYAxis((String) metricPicker.getSelectedItem());
//		Visualizer.getScatterPlotView().getScatterPlotRenderer().getDisplay().getVisualization().run("update");
	}

}
