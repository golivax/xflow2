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
 *  ==============
 *  JUNGGraph.java
 *  ==============
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.entity.representation.jung;

import java.util.HashMap;

import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.AuthorDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraph;
import br.usp.ime.lapessc.xflow2.entity.DependencyGraphType;
import br.usp.ime.lapessc.xflow2.entity.FileDependencyObject;
import br.usp.ime.lapessc.xflow2.entity.dao.core.AuthorDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.core.FileDependencyObjectDAO;
import br.usp.ime.lapessc.xflow2.entity.representation.matrix.IRealMatrix;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import edu.uci.ics.jung.graph.AbstractTypedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class JUNGGraph {

	private AbstractTypedGraph<JUNGVertex, JUNGEdge> graph;
	
	private static HashMap<Long, JUNGVertex> verticesCache;
	private static HashMap<Integer, FileDependencyObject> stampsCache;
	
	public JUNGGraph(){
		this.graph = new UndirectedSparseGraph<JUNGVertex, JUNGEdge>();
	}
	
	public AbstractTypedGraph<JUNGVertex, JUNGEdge> getGraph() {
		return this.graph;
	}
	
	public static HashMap<Long, JUNGVertex> getVerticesCache() {
		return verticesCache;
	}

	public void setGraph(final AbstractTypedGraph<JUNGVertex, JUNGEdge> graph) {
		this.graph = graph;
	}
	
	public static JUNGGraph convertMatrixToJUNGGraph(final IRealMatrix matrix, final DependencyGraph dependency) throws DatabaseException{
		
		final AbstractTypedGraph<JUNGVertex,JUNGEdge> graph;
		
		if(dependency.isDirectedDependency()){
			if(dependency.getType() == DependencyGraphType.COORDINATION_REQUIREMENTS.getValue()){
				graph = transformCoordinationRequirementMatrixToDirectedGraph(matrix, dependency.getAssociatedAnalysis());
			}
			else if(dependency.getType() == DependencyGraphType.TASK_ASSIGNMENT.getValue()){
				graph = transformTaskAssignmentMatrixToUndirectedGraph(matrix, dependency.getAssociatedAnalysis());
			}
			else {
				graph = transformTaskDependencyToDirectedGraph(matrix, dependency.getAssociatedAnalysis());
			}
		}
		else{
			if(dependency.getType() == DependencyGraphType.COORDINATION_REQUIREMENTS.getValue()){
				graph = transformCoordinationRequirementMatrixToUndirectedGraph(matrix, dependency.getAssociatedAnalysis());
			}
			else if(dependency.getType() == DependencyGraphType.TASK_ASSIGNMENT.getValue()){
				graph = transformTaskAssignmentMatrixToUndirectedGraph(matrix, dependency.getAssociatedAnalysis());
			}
			else {
				graph = transformTaskDependencyToUndirectedGraph(matrix, dependency.getAssociatedAnalysis());
			}
		}
		
		final JUNGGraph jungGraph = new JUNGGraph();
		jungGraph.setGraph(graph);
		return jungGraph;
	}
	
	public static JUNGGraph convertMatrixToJUNGGraph(final IRealMatrix matrix, final DependencyGraph dependency, final JUNGGraph graph) throws DatabaseException{
		
		if(dependency.isDirectedDependency()){
		}
		else{
			if(dependency.getType() == DependencyGraphType.COORDINATION_REQUIREMENTS.getValue()){
			}
			else if(dependency.getType() == DependencyGraphType.TASK_ASSIGNMENT.getValue()){
			}
			else {
				transformTaskDependencyToUndirectedGraph(matrix, dependency.getAssociatedAnalysis(), graph);
			}
		}
		
		return graph;
	}
	
	private static AbstractTypedGraph<JUNGVertex, JUNGEdge> transformCoordinationRequirementMatrixToDirectedGraph(IRealMatrix matrix, Analysis associatedAnalysis) throws DatabaseException {
		return JUNGGraph.transformCoordinationRequirementMatrixToUndirectedGraph(matrix, associatedAnalysis);
	}

	private static AbstractTypedGraph<JUNGVertex, JUNGEdge> transformTaskAssignmentMatrixToDirectedGraph(IRealMatrix matrix, Analysis associatedAnalysis) {
		return null;
	}

	private static AbstractTypedGraph<JUNGVertex, JUNGEdge> transformTaskDependencyToDirectedGraph(IRealMatrix matrix, Analysis associatedAnalysis) {
		return null;
	}

	private final static AbstractTypedGraph<JUNGVertex, JUNGEdge> transformCoordinationRequirementMatrixToUndirectedGraph(final IRealMatrix matrix, final Analysis associatedAnalysis) throws DatabaseException {
		final AuthorDependencyObjectDAO fileDependencyDAO = new AuthorDependencyObjectDAO();
		final UndirectedSparseGraph<JUNGVertex, JUNGEdge> graph = new UndirectedSparseGraph<JUNGVertex, JUNGEdge>();
		
		if(verticesCache == null){
			verticesCache = new HashMap<Long, JUNGVertex>();
		}
		
		for (int i = 0; i < matrix.getRows(); i++) {
			final AuthorDependencyObject dependedEntity = fileDependencyDAO.findDependencyObjectByStamp(associatedAnalysis, i);
			
			final JUNGVertex vertex1;
			if(verticesCache.containsKey(dependedEntity.getAuthor().getId())){
				vertex1 = verticesCache.get(dependedEntity.getAuthor().getId());
			} else {
				vertex1 = new JUNGVertex();
				vertex1.setId(dependedEntity.getAuthor().getId());
				vertex1.setName(dependedEntity.getDependencyObjectName());
				verticesCache.put(dependedEntity.getAuthor().getId(), vertex1);
				graph.addVertex(vertex1);
			}
			for (int j = i+1; j < matrix.getColumns(); j++) {
				final int edgeWeight = matrix.getValueAt(i,j).intValue();
				if(edgeWeight > 0){
					final AuthorDependencyObject dependentEntity = fileDependencyDAO.findDependencyObjectByStamp(associatedAnalysis, j);
					final JUNGEdge edge = new JUNGEdge(edgeWeight);
					final JUNGVertex vertex2;
					if(verticesCache.containsKey(dependentEntity.getAuthor().getId())){
						vertex2 = verticesCache.get(dependentEntity.getAuthor().getId());
					} else {
						vertex2 = new JUNGVertex();
						vertex2.setId(dependentEntity.getAuthor().getId());
						vertex2.setName(dependentEntity.getDependencyObjectName());
						verticesCache.put(dependentEntity.getAuthor().getId(), vertex2);
						graph.addVertex(vertex2);
					}
					graph.addEdge(edge, vertex1, vertex2);
				}
			}
		}
		return graph;
	}

	private static AbstractTypedGraph<JUNGVertex, JUNGEdge> transformTaskAssignmentMatrixToUndirectedGraph(final IRealMatrix matrix, final Analysis associatedAnalysis) throws DatabaseException {
		
		final AuthorDependencyObjectDAO authorDependencyDAO = new AuthorDependencyObjectDAO();
		final FileDependencyObjectDAO fileDependencyDAO = new FileDependencyObjectDAO();
		final UndirectedSparseGraph<JUNGVertex, JUNGEdge> graph = new UndirectedSparseGraph<JUNGVertex, JUNGEdge>();
		
		if(verticesCache == null){
			verticesCache = new HashMap<Long, JUNGVertex>();
		}
		
		for (int i = 0; i < matrix.getRows(); i++) {
			final AuthorDependencyObject dependedAuthor = authorDependencyDAO.findDependencyObjectByStamp(associatedAnalysis, i);
			final JUNGVertex vertex1 = new JUNGVertex();
			vertex1.setId(dependedAuthor.getId());
			vertex1.setName(dependedAuthor.getDependencyObjectName());
			graph.addVertex(vertex1);
			for (int j = 0; j < matrix.getColumns(); j++) {
				final int edgeWeight = matrix.getValueAt(i, j).intValue();
				if(edgeWeight > 0){
					final FileDependencyObject dependentFile = fileDependencyDAO.findDependencyObjectByStamp(associatedAnalysis, j); 
					final JUNGEdge edge = new JUNGEdge(edgeWeight);
					final JUNGVertex vertex2 = new JUNGVertex();
					vertex2.setId(dependentFile.getId());
					vertex2.setName(dependentFile.getDependencyObjectName());
					graph.addVertex(vertex2);
					graph.addEdge(edge, vertex1, vertex2);
				}
			}
		}
		return graph;
	}

	private static AbstractTypedGraph<JUNGVertex, JUNGEdge> transformTaskDependencyToUndirectedGraph(final IRealMatrix matrix, final Analysis associatedAnalysis) throws DatabaseException {
		
		final FileDependencyObjectDAO fileDependencyDAO = new FileDependencyObjectDAO();
		final UndirectedSparseGraph<JUNGVertex, JUNGEdge> graph = new UndirectedSparseGraph<JUNGVertex, JUNGEdge>();
		
		if(verticesCache == null){
			verticesCache = new HashMap<Long, JUNGVertex>();
		}
		
		for (int i = 0; i < matrix.getRows(); i++) {
			final FileDependencyObject dependedFile = fileDependencyDAO.findDependencyObjectByStamp(associatedAnalysis, i);
			final JUNGVertex vertex1;
			if(verticesCache.containsKey(dependedFile.getFile().getId())){
				vertex1 = verticesCache.get(dependedFile.getFile().getId());
			} else {
				vertex1 = new JUNGVertex();
				vertex1.setId(dependedFile.getFile().getId());
				vertex1.setName(dependedFile.getDependencyObjectName());
				verticesCache.put(dependedFile.getFile().getId(), vertex1);
				graph.addVertex(vertex1);
			}
			for (int j = i+1; j < matrix.getColumns(); j++) {
				final int edgeWeight = matrix.getValueAt(i,j).intValue();
				if(edgeWeight > 0){
					final FileDependencyObject dependentFile = fileDependencyDAO.findDependencyObjectByStamp(associatedAnalysis, j);
					final JUNGEdge edge = new JUNGEdge(edgeWeight);
					final JUNGVertex vertex2;
					if(verticesCache.containsKey(dependentFile.getFile().getId())){
						vertex2 = verticesCache.get(dependentFile.getFile().getId());
					} else {
						vertex2 = new JUNGVertex();
						vertex2.setId(dependentFile.getFile().getId());
						vertex2.setName(dependentFile.getDependencyObjectName());
						verticesCache.put(dependentFile.getFile().getId(), vertex2);
						graph.addVertex(vertex2);
					}
					graph.addEdge(edge, vertex1, vertex2);
				}
			}
		}
		return graph;
	}
	
	private static void transformTaskDependencyToUndirectedGraph(final IRealMatrix matrix, final Analysis associatedAnalysis, JUNGGraph graph) throws DatabaseException {
		
		final FileDependencyObjectDAO fileDependencyDAO = new FileDependencyObjectDAO();
		final UndirectedSparseGraph<JUNGVertex, JUNGEdge> dependencyGraph = (UndirectedSparseGraph<JUNGVertex, JUNGEdge>) graph.getGraph();
		
		for (int i = 0; i < matrix.getRows(); i++) {
			final FileDependencyObject dependedFile;
			if(stampsCache.containsKey(i)){
				dependedFile = stampsCache.get(i);
			} else {
				dependedFile = fileDependencyDAO.findDependencyObjectByStamp(associatedAnalysis, i);
				stampsCache.put(i, dependedFile);
			}

			if(dependedFile != null){
				final JUNGVertex vertex1;
				if(verticesCache.containsKey(dependedFile.getFile().getId())){
					vertex1 = verticesCache.get(dependedFile.getFile().getId());
				} else {
					vertex1 = new JUNGVertex();
					vertex1.setId(dependedFile.getFile().getId());
					vertex1.setName(dependedFile.getDependencyObjectName());
					verticesCache.put(dependedFile.getFile().getId(), vertex1);
					dependencyGraph.addVertex(vertex1);
				}
				for (int j = i+1; j < matrix.getColumns(); j++) {
					final int edgeWeight = matrix.getValueAt(i,j).intValue();
					if(edgeWeight > 0){
						final FileDependencyObject dependentFile = fileDependencyDAO.findDependencyObjectByStamp(associatedAnalysis, j);
						final JUNGEdge edge = new JUNGEdge(edgeWeight);
						final JUNGVertex vertex2;
						if(verticesCache.containsKey(dependentFile.getFile().getId())){
							vertex2 = verticesCache.get(dependentFile.getFile().getId());
						} else {
							vertex2 = new JUNGVertex();
							vertex2.setId(dependentFile.getFile().getId());
							vertex2.setName(dependentFile.getDependencyObjectName());
							verticesCache.put(dependentFile.getFile().getId(), vertex2);
							dependencyGraph.addVertex(vertex2);
						}
						dependencyGraph.addEdge(edge, vertex1, vertex2);
					}
				}
			}
		}

		graph.setGraph(dependencyGraph);
	}

	public static void clearVerticesCache() {
		verticesCache = new HashMap<Long, JUNGVertex>();
		stampsCache = new HashMap<Integer, FileDependencyObject>();
	}	

}
