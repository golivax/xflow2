package br.usp.ime.lapessc.xflow2.presentation.visualizations;

import javax.swing.JComponent;

public interface VisualizationControl<ConcreteVisualization extends Visualization> {

	public void buildControlGUI(JComponent visualizationComponent);
	
}
