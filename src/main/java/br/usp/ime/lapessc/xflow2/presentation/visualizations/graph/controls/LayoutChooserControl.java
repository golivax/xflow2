package br.usp.ime.lapessc.xflow2.presentation.visualizations.graph.controls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.VisualizationControl;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.graph.GraphRenderer;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.graph.GraphVisualization;

public class LayoutChooserControl implements VisualizationControl<GraphVisualization>, ActionListener {

	private JComboBox layoutChooserComboBox;
	private GraphVisualization visualizationControlled;
	
	@Override
	public void buildControlGUI(JComponent visualizationComponent) {
		this.visualizationControlled = (GraphVisualization) ((JComponent) visualizationComponent.getParent()).getClientProperty("Visualization Instance");
		JPanel metricPickerPanel = new JPanel();
		
		layoutChooserComboBox = createChooserComboBox();
		layoutChooserComboBox.addActionListener(this);

		JLabel layoutChooserLabel = new JLabel("Choose layout:");
		metricPickerPanel.add(layoutChooserLabel);
		metricPickerPanel.add(layoutChooserComboBox);
		metricPickerPanel.setOpaque(false);
		
		visualizationComponent.add(metricPickerPanel);
	}

	private JComboBox createChooserComboBox() {
		final String[] layoutPossibilites = new String[]{"Radial", "Fruchterman-Reingold", "Circle", "Force"};
		JComboBox layoutChooserComboBox = new JComboBox(layoutPossibilites);
		return layoutChooserComboBox;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(((String) layoutChooserComboBox.getSelectedItem()).equals("Radial")){
			
		} else if (((String) layoutChooserComboBox.getSelectedItem()).equals("Fruchterman-Reingold")){
			
		} else if (((String) layoutChooserComboBox.getSelectedItem()).equals("Circle")){
			
		} else if (((String) layoutChooserComboBox.getSelectedItem()).equals("Force")){
			
		}
//		try {
//			((GraphRenderer) visualizationControlled.getRenderers()[0]).changeGraphType(selectedRepresentation);
//		} catch (DatabaseException e) {
//			e.printStackTrace();
//		}
//		Visualizer.getScatterPlotView().getScatterPlotRenderer().updateYAxis((String) metricPicker.getSelectedItem());
//		Visualizer.getScatterPlotView().getScatterPlotRenderer().getDisplay().getVisualization().run("update");
	}


}
