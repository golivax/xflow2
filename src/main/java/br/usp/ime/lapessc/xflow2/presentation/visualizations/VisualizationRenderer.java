package br.usp.ime.lapessc.xflow2.presentation.visualizations;

import javax.swing.JComponent;

import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public interface VisualizationRenderer<ConcreteVisualization extends Visualization> {

	public static final int LOW_QUALITY = 0;
	public static final int HIGH_QUALITY = 1;
	
	public void composeVisualization(JComponent visualizationComponent) throws DatabaseException;
	public void updateVisualizationLimits(int inferiorLimit, int superiorLimit) throws DatabaseException;
	public void setLowerQuality();
	public void setHighQuality();
	
}
