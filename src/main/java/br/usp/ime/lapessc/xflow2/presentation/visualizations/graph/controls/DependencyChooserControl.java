package br.usp.ime.lapessc.xflow2.presentation.visualizations.graph.controls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import br.usp.ime.lapessc.xflow2.entity.DependencyGraphType;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.VisualizationControl;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.graph.GraphRenderer;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.graph.GraphVisualization;

public class DependencyChooserControl implements VisualizationControl<GraphVisualization>, ActionListener {

	private JComboBox dependencyChooserComboBox;
	private GraphVisualization visualizationControlled;
	
	@Override
	public void buildControlGUI(JComponent visualizationComponent) {
		this.visualizationControlled = (GraphVisualization) ((JComponent) visualizationComponent.getParent()).getClientProperty("Visualization Instance");
		JPanel metricPickerPanel = new JPanel();
		
		dependencyChooserComboBox = createChooserComboBox();
		dependencyChooserComboBox.addActionListener(this);

		JLabel chooseRepresentationLabel = new JLabel("Displaying:");
		metricPickerPanel.add(chooseRepresentationLabel);
		metricPickerPanel.add(dependencyChooserComboBox);
		metricPickerPanel.setOpaque(false);
		
		visualizationComponent.add(metricPickerPanel);
	}

	private JComboBox createChooserComboBox() {
		final String[] representationPossibilites = new String[]{"Coordination Requirements", "Task Assignment", "Task Dependency"};
		JComboBox metricPickerComboBox = new JComboBox(representationPossibilites);
		return metricPickerComboBox;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		final int selectedRepresentation;
		if(((String) dependencyChooserComboBox.getSelectedItem()).equals("Coordination Requirements")){
			selectedRepresentation = DependencyGraphType.COORDINATION_REQUIREMENTS.getValue();
		} else if (((String) dependencyChooserComboBox.getSelectedItem()).equals("Task Assignment")){
			selectedRepresentation = DependencyGraphType.TASK_ASSIGNMENT.getValue();
		} else {
			selectedRepresentation = DependencyGraphType.TASK_DEPENDENCY.getValue();
		}
		try {
			((GraphRenderer) visualizationControlled.getRenderers()[0]).changeGraphType(selectedRepresentation);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
//		Visualizer.getScatterPlotView().getScatterPlotRenderer().updateYAxis((String) metricPicker.getSelectedItem());
//		Visualizer.getScatterPlotView().getScatterPlotRenderer().getDisplay().getVisualization().run("update");
	}

}
