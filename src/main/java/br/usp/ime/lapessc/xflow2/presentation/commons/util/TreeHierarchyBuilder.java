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
 *  =========================
 *  TreeHierarchyBuilder.java
 *  =========================
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.presentation.commons.util;

import java.util.ArrayList;

import prefuse.data.Node;
import prefuse.data.Tree;
import br.usp.ime.lapessc.xflow2.entity.Analysis;
import br.usp.ime.lapessc.xflow2.entity.Commit;
import br.usp.ime.lapessc.xflow2.entity.FolderVersion;
import br.usp.ime.lapessc.xflow2.entity.FileVersion;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.FolderDAO;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.ArtifactDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;

public class TreeHierarchyBuilder {
	
	private static Analysis analysis;
	private static int fileNumbers = 0;
	
	public static Tree createTreeMapGraph(final Analysis analysis, Commit entry) throws DatabaseException{
		TreeHierarchyBuilder.analysis = analysis;
		final ArrayList<FolderVersion> folders;
		
		if(analysis.isTemporalConsistencyForced()){
			folders = new FolderDAO().findRootFoldersUntilSequence(analysis.getProject(), entry.getId());
		} else {
			folders = new FolderDAO().findRootFoldersUntilRevision(analysis.getProject(), entry.getRevision());
		}
		
		Tree tree = new Tree();
	    tree.addColumn("name", String.class);
	    tree.addColumn("type", String.class);
	    tree.addColumn("id", long.class);
		
		Node root = tree.addRoot();
		for (FolderVersion folder : folders) {
			Node leaf = tree.addChild(root);
			leaf.set("name", folder.getName());
			leaf.set("type", "folder");
			leaf.set("id", folder.getId());
			final ArrayList<FolderVersion> subfolders;
			final ArrayList<FileVersion> files;
			if(analysis.isTemporalConsistencyForced()){
				//FIXME
				subfolders = new FolderDAO().findSubFoldersUntilSequence(folder.getId(), 1541);
				files = new ArtifactDAO().getFilesFromFolderUntilSequence(folder, 1541);
			} else {
				subfolders = new FolderDAO().findSubFoldersUntilRevision(folder.getId(), entry.getRevision());
				files = new ArtifactDAO().getFilesFromFolderUntilRevision(folder.getId(), entry.getRevision());
			}
			
			extractLeafs(tree, leaf, subfolders, entry);
			extractFiles(tree, leaf, files);
		}
		
		System.out.println(fileNumbers);
		
		return tree;
	}
	
	private static void extractLeafs(Tree tree, Node parent, ArrayList<FolderVersion> folders, Commit entry) throws DatabaseException {
		for (FolderVersion folder : folders) {
			Node leaf = tree.addChild(parent);
			leaf.set("name", folder.getName());
			leaf.set("type", "folder");
			leaf.set("id", folder.getId());
			
			final ArrayList<FolderVersion> subfolders;
			final ArrayList<FileVersion> files;
			
			if(analysis.isTemporalConsistencyForced()){
				//FIXME
				subfolders = new FolderDAO().findSubFoldersUntilSequence(folder.getId(), 1541);
				files = new ArtifactDAO().getFilesFromFolderUntilSequence(folder, 1541);
			} else {
				subfolders = new FolderDAO().findSubFoldersUntilRevision(folder.getId(), entry.getRevision());
				files = new ArtifactDAO().getFilesFromFolderUntilRevision(folder.getId(), entry.getRevision());
			}
			
			if(subfolders.size() > 0){
				extractLeafs(tree, leaf, subfolders, entry);
			}
			
			extractFiles(tree, leaf, files);
		}
	}
	
	private static void extractFiles(Tree tree, Node parent, ArrayList<FileVersion> files){
		if(files != null){
			for (FileVersion file : files) {
				Node fileLeaf = tree.addChild(parent);
				fileLeaf.set("name", file.getPath());
				fileLeaf.set("type", "file");
				fileLeaf.set("id", file.getId());
				fileNumbers++;
			}
		}
	}

}
