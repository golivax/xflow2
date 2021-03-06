package br.usp.ime.lapessc.xflow2.presentation.visualizations.line.controls;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

import prefuse.util.ui.JRangeSlider;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.VisualizationControl;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.line.LineRenderer;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.line.LineVisualization;

public class ZoomControl implements VisualizationControl<LineVisualization>, MouseMotionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2790925597150859161L;

	protected static final int ORDERED_BY_REVISION = 0;
	protected static final int ORDERED_BY_SEQUENCE = 1;
	
	private int currentOrder = ORDERED_BY_REVISION;
	
	private JRangeSlider zoomSlider;
	private LineVisualization visualizationControlled;
	
	@Override
	public void buildControlGUI(JComponent visualizationComponent) {
		visualizationControlled = (LineVisualization) ((JComponent) visualizationComponent.getParent()).getClientProperty("Visualization Instance");
		Metrics metricsSession = (Metrics) ((JComponent) visualizationComponent.getParent()).getClientProperty("Metrics Session");
		
		zoomSlider = new JRangeSlider(
				metricsSession.getAssociatedAnalysis().getFirstEntry().getRevision().intValue(), 
				metricsSession.getAssociatedAnalysis().getLastEntry().getRevision().intValue(), 
				metricsSession.getAssociatedAnalysis().getFirstEntry().getRevision().intValue(), 
				metricsSession.getAssociatedAnalysis().getLastEntry().getRevision().intValue(), 
				JRangeSlider.HORIZONTAL, JRangeSlider.LEFTRIGHT_TOPBOTTOM);
		zoomSlider.setThumbColor(null);
		setupZoomSliderMouseEvent();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(zoomSlider);
		panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,0,0,0));
		visualizationComponent.add(panel);
	}

	private void setupZoomSliderMouseEvent() {
		zoomSlider.addMouseMotionListener(this);
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		((LineRenderer) visualizationControlled.getRenderers()[0]).getChart().getXYPlot().getDomainAxis().setRange(zoomSlider.getLowValue(), zoomSlider.getHighValue());
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

	public JRangeSlider getZoomSlider() {
		return zoomSlider;
	}
}
