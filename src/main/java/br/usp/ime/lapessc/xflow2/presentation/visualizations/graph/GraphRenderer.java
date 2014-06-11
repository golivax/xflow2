package br.usp.ime.lapessc.xflow2.presentation.visualizations.graph;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.layout.graph.FruchtermanReingoldLayout;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Tuple;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.visual.VisualItem;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.AuthorDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.CoordinationRequirementsGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraphType;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.dao.core.AuthorDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.DependencyDAO;
import br.usp.ime.lapessc.xflow2.entity.representation.Converter;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGEdge;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGGraph;
import br.usp.ime.lapessc.xflow2.entity.representation.jung.JUNGVertex;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.Matrix;
import br.usp.ime.lapessc.xflow2.entity.representation.prefuse.PrefuseGraph;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.VisualizationRenderer;
import br.usp.ime.lapessc.xflow2.repository.vcs.dao.CommitDAO;
import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;

@SuppressWarnings("unchecked")
public class GraphRenderer implements VisualizationRenderer<GraphVisualization> {

	private Metrics metricsSession;
	
	private Display display;
	private PrefuseGraph graph;
	
	private int representedDependency = DependencyGraphType.COORDINATION_REQUIREMENTS.getValue();
	private long currentRevision;
	
	@Override
	public void composeVisualization(JComponent visualizationComponent) throws DatabaseException {
		this.metricsSession = (Metrics) visualizationComponent.getClientProperty("Metrics Session");
		constructGraph();
		visualizationComponent.add(this.draw(), BorderLayout.CENTER);
	}
	
	private void constructGraph() throws DatabaseException {
		if(metricsSession.getAssociatedAnalysis().isCoordinationRequirementPersisted()){
			//FIXME
			//this.graph = Converter.convertJungToPrefuseGraph(
			//		metricsSession.getAssociatedAnalysis().processEntryDependencyGraph(
			//				new CommitDAO().findById(Commit.class, 2127L), 
			//				DependencyGraph.COORD_REQUIREMENTS));
		} else {
			
			Analysis analysis = metricsSession.getAssociatedAnalysis();
			
			//Latest commit with files
			CommitDAO commitDAO = new CommitDAO();
			Commit commit = commitDAO.getLatestCommitWithFiles(analysis);
			
			System.out.println("Commit: " + commit.getRevision());
			
			final DependencyGraph<AuthorDependencyObject, AuthorDependencyObject> dependencyDTO = 
					new CoordinationRequirementsGraph();
						
			dependencyDTO.setAssociatedAnalysis(analysis);
			dependencyDTO.setAssociatedEntry(commit);
			
			final Matrix taskAssignmentMatrix = 
					analysis.getDependencyMatrixForEntry(
							commit,DependencyGraphType.TASK_ASSIGNMENT.getValue());
			
			System.out.println(taskAssignmentMatrix.getRows() + ", " + 
					taskAssignmentMatrix.getColumns());
			
			final Matrix taskDependencyMatrix = 
					analysis.getDependencyMatrixForEntry(
							commit,DependencyGraphType.TASK_DEPENDENCY.getValue());
			
			System.out.println(taskDependencyMatrix.getRows() + ", " + 
					taskDependencyMatrix.getColumns());
			
			//Remove deps lógicas com supp = 1
			//System.out.println("Applying filter to taskDependencyMatrix");
			//taskDependencyMatrix.applyStatisticalFilters(2, 0);
			
			final Matrix matrix = taskAssignmentMatrix.multiply(
					taskDependencyMatrix).multiply(
							taskAssignmentMatrix.getTransposeMatrix());
			
			//Transforma a matriz em binária
			for (int i = 0; i < matrix.getRows(); i++){
				for (int j = 0; j < matrix.getColumns(); j++){
					if (matrix.getValueAt(i,j) > 0){
						matrix.putValueAt(1, i, j);
					}
				}
			}
			
			//Printa a matriz
			for (int i = 0; i < matrix.getRows(); i++){
				for (int j = 0; j < matrix.getColumns(); j++){
					System.out.print(matrix.getValueAt(i, j));
					System.out.print(" ");
				}
				System.out.println();
			}
			
			
			final AuthorDependencyObjectDAO fileDependencyDAO = 
					new AuthorDependencyObjectDAO();

			//Printa a matriz
			for (int i = 0; i < matrix.getRows(); i++){
				final AuthorDependencyObject dependedEntity = 
						fileDependencyDAO.findDependencyObjectByStamp(
								metricsSession.getAssociatedAnalysis(), i);
				
				System.out.println("Line " + i + ": " + 
						dependedEntity.getAuthor().getName());
			}
			
			//Calcula centralidades
			JUNGGraph graph = new JUNGGraph();
			graph = JUNGGraph.convertMatrixToJUNGGraph(matrix, dependencyDTO);
			
			EigenvectorCentrality<JUNGVertex, JUNGEdge> egvc = 
					new EigenvectorCentrality<JUNGVertex, JUNGEdge>(
							graph.getGraph());
			
			egvc.evaluate();
	
			for(JUNGVertex v : graph.getGraph().getVertices()){
				System.out.println(v.getName());
				System.out.println("Degree: " + graph.getGraph().degree(v));
				System.out.println("EGVC: " + egvc.getVertexScore(v));
			}
			
			this.graph = Converter.convertJungToPrefuseGraph(graph);
			applyLabelMask();
		}
		this.currentRevision = metricsSession.getAssociatedAnalysis().getLastEntry().getRevision();
	}

	private void applyLabelMask() {
		Map<String,String> labelMap = new HashMap<String,String>();
		labelMap.put("ddevienne","dev A");
		labelMap.put("scohen","dev B");
		labelMap.put("conor","dev C");
		labelMap.put("umagesh","dev D");
		labelMap.put("alexeys","dev E");
		labelMap.put("bruce","dev F");
		labelMap.put("sbailliez","dev G");
		labelMap.put("jhm","dev H");
		labelMap.put("kevj","dev I");
		labelMap.put("jglick","dev J");
		labelMap.put("antoine","dev K");
		labelMap.put("jkf","dev L");
		labelMap.put("stevel","dev M");
		labelMap.put("bodewig","dev N");
		labelMap.put("mbenson","dev O");
		labelMap.put("peterreilly","dev P");
		
		Iterator<Tuple> tupleIterator = this.graph.getPrefuseGraph().getNodes().tuples();
		while(tupleIterator.hasNext()){
			Tuple tuple = tupleIterator.next();
			String currentName = tuple.getString("name");
			System.out.println("Cur Name " + currentName);
			String mappedName = labelMap.get(currentName);
			System.out.println("Mapped Name " + mappedName);
			tuple.setString("name", mappedName);
		}
			
			
		
	}

	public static void main(String[] args) throws DatabaseException {
		new GraphRenderer();
	}

	public void setDisplay(Display display) {
		this.display = display;
	}

	public Display getDisplay() {
		return display;
	} 

	public JPanel draw() {

		Visualization visualization = new Visualization();
		visualization.addGraph("graph", graph.getPrefuseGraph());
		visualization.setValue("graph.edges", null, VisualItem.INTERACTIVE, Boolean.FALSE);



		/*
		 * SETUP RENDERERS.
		 * 
		 * Controls how nodes and edges will be presented
		 * (e.g. directed or undirected edges, label field and size).
		 */
		defineRenderers(visualization);

		/*
		 * SETUP COLORS.
		 * 
		 * Definitions for nodes and edges colors
		 * (e.g. node and node text color, edge color).
		 */
		defineColors(visualization);

		/*
		 * SETUP ACTIONS.
		 * 
		 * Definitions for visualization basic effects.
		 */
		createActions(visualization);

		/*
		 * SETUP LAYOUTS.
		 * 
		 */
		defineLayout(visualization);

		/*
		 * GROUPS DEFINITION
		 */
//		defineGroups(visualization, visualGraph);

		/*
		 * SETUP DISPLAY
		 */
		setupDisplay(visualization);

		visualization.run("draw");
		visualization.run("layout");

		JPanel graphPanel = new JPanel(new BorderLayout());
		graphPanel.add(display, BorderLayout.CENTER);
		return graphPanel;
	}

	private void setupDisplay(final Visualization visualization) {
		display = new Display(visualization);
		display.setSize(1920,1080);
		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new FocusControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new NeighborHighlightControl());


		//display.pan(display.getSize().getWidth(), display.getSize().getHeight());
		
		display.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				display.setHighQuality(false);
				display.getVisualization().run("draw");
				display.setHighQuality(true);
			}
		});
	}

//	private void defineGroups(final Visualization visualization, VisualGraph visualGraph) {
//        TupleSet focusGroup = visualization.getGroup(Visualization.FOCUS_ITEMS); 
//        focusGroup.addTupleSetListener(new TupleSetListener() {
//            public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem)
//            {
//                for ( int i=0; i<rem.length; ++i )
//                    ((VisualItem)rem[i]).setFixed(false);
//                for ( int i=0; i<add.length; ++i ) {
//                    ((VisualItem)add[i]).setFixed(false);
//                    ((VisualItem)add[i]).setFixed(true);
//                }
//                visualization.run("draw");
//            }
//        });
//
//
//		//	        NodeItem focus = (NodeItem)visualGraph.getNode(0);
//		//	        PrefuseLib.setX(focus, null, 400);
//		//	        PrefuseLib.setY(focus, null, 250);
//		//	        focusGroup.setTuple(focus);
//
//	}

	private void defineRenderers(Visualization visualization) {

		// Label Renderer
		LabelRenderer labelRenderer = new LabelRenderer("name");
		labelRenderer.setRoundedCorner(8, 8);
		labelRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
		labelRenderer.setHorizontalAlignment(Constants.CENTER);

		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.setBaseSize(16);
		shapeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_DRAW_AND_FILL);
		
		// Edge Renderer
		EdgeRenderer edgeRenderer = new EdgeRenderer();
		edgeRenderer.setArrowType(Constants.EDGE_ARROW_NONE);
		edgeRenderer.setEdgeType(Constants.EDGE_TYPE_CURVE);

		// Renderer Factory
		DefaultRendererFactory rendererFactory = new DefaultRendererFactory();
		rendererFactory.setDefaultRenderer(labelRenderer);
		rendererFactory.setDefaultEdgeRenderer(edgeRenderer);

		visualization.setRendererFactory(rendererFactory);
	}

	private void defineColors(Visualization visualization) {

		// Nodes Colors
		int[] palette = new int[] { ColorLib.rgb(200,200,255),
				ColorLib.rgb(255,50,50), ColorLib.rgb(255,180,180)};
		ColorAction fill = new ColorAction("graph.nodes", VisualItem.FILLCOLOR, palette[0]);
//		fill.add("_fixed", palette[1]);
//		fill.add("_highlight", palette[2]);
//		fill.add("_neighbours", palette[2]);

		// Text and edges Colors
		ColorAction textColor = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray(0));
		ColorAction edgeColor = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(100));
				
		ActionList colorActions = new ActionList();
		colorActions.add(fill);
		colorActions.add(textColor);
		colorActions.add(edgeColor);
				
		visualization.putAction("color", colorActions);
		
		//Font size
		FontAction fontAction = new FontAction("graph.nodes", FontLib.getFont("Arial", 18));
		visualization.putAction("font", fontAction);
	}

	private void defineLayout(Visualization visualization) {

		// Tree Layout
		//RadialTreeLayout treeLayout = new RadialTreeLayout("graph");
		
		// Circle Layout
		//CircleLayout circleLayout = new CircleLayout("graph");
		//circleLayout.setRadius(700);
		
		// Force Layout
		//ForceDirectedLayout forceLayout = new ForceDirectedLayout("graph");
		FruchtermanReingoldLayout frLayout = new FruchtermanReingoldLayout("graph", 1000);
		
		//ForceSimulator forceSimulator = forceLayout.getForceSimulator();
		//forceSimulator.getForces()[0].setParameter(0, -1.2f);

		ActionList layoutAction = new ActionList();
		//layoutAction.add(treeLayout);
		//layoutAction.add(frLayout);
		//layoutAction.add(circleLayout);
		//layoutAction.add(new RandomLayout("graph"));
		layoutAction.add(frLayout);
		layoutAction.add(new RepaintAction());
		visualization.putAction("layout", layoutAction);
		//visualization.alwaysRunAfter("draw", "layout");
	}

	private void createActions(Visualization visualization) {
		ActionList draw = new ActionList();
//		DataSizeAction a = new DataSizeAction("graph.edges", "weight");
//		a.setMaximumSize(150);
//		ShapeAction graphShape = new ShapeAction("graph.nodes", Constants.SHAPE_ELLIPSE);
//		a.setIs2DArea(true);
//		a.setBinCount(8);
//		a.setScale(Constants.QUANTILE_SCALE);
//		draw.add(a);
		draw.add(visualization.getAction("color"));
		draw.add(visualization.getAction("font"));
//		draw.add(a);
//		draw.add(graphShape);
		visualization.putAction("draw", draw);
	}


	public void changeGraphType(int newType) throws DatabaseException {
		representedDependency = newType;
		updateGraph(this.currentRevision);
	}
	
	public void updateGraph(long newSequence) throws DatabaseException {
		this.getDisplay().getVisualization().reset();
		this.graph = new PrefuseGraph();
		
		final Commit entry;
		if(this.metricsSession.getAssociatedAnalysis().isTemporalConsistencyForced()){
			entry = new CommitDAO().findEntryFromSequence(this.metricsSession.getAssociatedAnalysis().getProject(), newSequence);
		} else {
			entry = new CommitDAO().findEntryFromRevision(this.metricsSession.getAssociatedAnalysis().getProject(), newSequence);
		}

		if(representedDependency == DependencyGraphType.COORDINATION_REQUIREMENTS.getValue()){
			if(this.metricsSession.getAssociatedAnalysis().isCoordinationRequirementPersisted()){
				this.graph = Converter.convertJungToPrefuseGraph(metricsSession.getAssociatedAnalysis().processEntryDependencyGraph(entry, DependencyGraphType.COORDINATION_REQUIREMENTS.getValue()));
			} else {
				final DependencyGraph<AuthorDependencyObject, AuthorDependencyObject> dependencyDTO = new DependencyDAO().findDependencyByEntry(metricsSession.getAssociatedAnalysis().getId(), entry.getId(), DependencyGraphType.COORDINATION_REQUIREMENTS.getValue());
				final Matrix taskAssignmentMatrix = this.metricsSession.getAssociatedAnalysis().getDependencyMatrixForEntry(entry, DependencyGraphType.TASK_ASSIGNMENT.getValue());
				final Matrix taskDependencyMatrix = this.metricsSession.getAssociatedAnalysis().getDependencyMatrixForEntry(entry, DependencyGraphType.TASK_DEPENDENCY.getValue());
				final Matrix matrix = taskAssignmentMatrix.multiply(taskDependencyMatrix).multiply(taskAssignmentMatrix.getTransposeMatrix());

				this.graph = Converter.convertJungToPrefuseGraph(JUNGGraph.convertMatrixToJUNGGraph(matrix, dependencyDTO));
			}
		} else {
//			final Dependency<AuthorDependencyObject, AuthorDependencyObject> dependencyDTO = new CoordinationRequirements();
//			dependencyDTO.setAssociatedAnalysis(metricsSession.getAssociatedAnalysis());
//			dependencyDTO.setAssociatedEntry(metricsSession.getAssociatedAnalysis().getLastEntry());
//			final Matrix taskAssignmentMatrix = this.metricsSession.getAssociatedAnalysis().processEntryDependencyMatrix(metricsSession.getAssociatedAnalysis().getLastEntry(), Dependency.TASK_ASSIGNMENT);
//			System.out.println(taskAssignmentMatrix.getRows()+", "+taskAssignmentMatrix.getColumns());
//			final Matrix taskDependencyMatrix = this.metricsSession.getAssociatedAnalysis().processEntryDependencyMatrix(metricsSession.getAssociatedAnalysis().getLastEntry(), Dependency.TASK_DEPENDENCY);
//			System.out.println(taskDependencyMatrix.getRows()+", "+taskDependencyMatrix.getColumns());
//			final Matrix matrix = taskAssignmentMatrix.multiply(taskDependencyMatrix).multiply(taskAssignmentMatrix.getTransposeMatrix());
//			JUNGGraph graph = new JUNGGraph();
//			graph = JUNGGraph.convertMatrixToJUNGGraph(matrix, dependencyDTO);
//			this.graph = Converter.convertJungToPrefuseGrapha(graph);
			
			final DependencyGraph dependency = new DependencyDAO().findHighestDependencyByEntry(this.metricsSession.getAssociatedAnalysis().getId(), entry.getId(), representedDependency);
			JUNGGraph graph = new JUNGGraph();
			final Matrix m = this.metricsSession.getAssociatedAnalysis().getDependencyMatrixForEntry(entry, dependency.getType());
			System.out.println(m.getColumns());
			graph = JUNGGraph.convertMatrixToJUNGGraph(m, dependency);
			System.out.println(graph.getGraph());
			this.graph = Converter.convertJungToPrefuseGraph(graph);
		}
		this.getDisplay().getVisualization().addGraph("graph", this.graph.getPrefuseGraph());
		this.getDisplay().getVisualization().run("draw");
		this.getDisplay().getVisualization().run("layout");
		this.currentRevision = newSequence;
	}

	@Override
	public void setLowerQuality() {
		this.display.setHighQuality(false);
	}

	@Override
	public void setHighQuality() {
		this.display.setHighQuality(true);
	}

	@Override
	public void updateVisualizationLimits(int inferiorLimit, int superiorLimit) throws DatabaseException {
		this.updateGraph(superiorLimit);
	}
}
