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
 *  ===========================
 *  DevelopersPanelControl.java
 *  ===========================
 *  
 *  Original Author: Francisco Santana;
 *  Contributor(s):  -;
 *  
 */

package br.usp.ime.lapessc.xflow2.presentation.commons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import prefuse.util.ui.JToggleGroup;
import br.usp.ime.lapessc.xflow2.entity.Author;
import br.usp.ime.lapessc.xflow2.entity.Metrics;
import br.usp.ime.lapessc.xflow2.entity.dao.cm.AuthorDAO;
import br.usp.ime.lapessc.xflow2.exception.persistence.DatabaseException;
import br.usp.ime.lapessc.xflow2.presentation.commons.util.ColorPalette;
import br.usp.ime.lapessc.xflow2.presentation.visualizations.Visualization;

public class DevelopersPanelControl extends JComponent implements ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2826230211306944049L;

	private JButton selectAllButton;
	private JButton deselectAllButton;
	private JToggleGroup checkBoxList;
	private List<Author> developersList;

	private String selectedAuthorsQuery; 
	
	public DevelopersPanelControl(Metrics metricsSession) {
		
		if(metricsSession.getAssociatedAnalysis().isTemporalConsistencyForced()){
			developersList = new AuthorDAO().getAuthorsListByEntries(
					metricsSession.getAssociatedAnalysis().getProject(),
					metricsSession.getAssociatedAnalysis().getFirstEntry(), 
					metricsSession.getAssociatedAnalysis().getLastEntry());
		} else {
			developersList = metricsSession.getAssociatedAnalysis().getProject().getAuthors();
		}
		
		//Initiate color palette
		ColorPalette.initiateColors(developersList.size());
				
		//Builds the list model
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		for (Author author : developersList) {
			listModel.addElement(author.getName());
		}
		
		this.checkBoxList = new JToggleGroup(JToggleGroup.CHECKBOX, listModel);
		
		System.out.println(checkBoxList.getComponentCount());
		
		//Set the foreground color of each author
		int[] palette = ColorPalette.getAuthorsColorPalette();
		for (int i=0; i < checkBoxList.getComponentCount(); i++) {
			 Component component = checkBoxList.getComponent(i);
			 component.setForeground(new Color(palette[i]));
		}
		
		this.checkBoxList.getSelectionModel().addListSelectionListener(this);
	}

	public List<Author> getDevelopersList() {
		return developersList;
	}

	public JComponent createControlPanel() {
		setupCheckBoxList();

		selectAllButton = setupSelectAllButton();
		deselectAllButton = setupDeselectAllButton();

		JScrollPane developersPanel = new JScrollPane(checkBoxList);
		developersPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		developersPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		developersPanel.setBorder(null);

		GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(
				layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(selectAllButton)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(deselectAllButton)
						.addGap(1,1,1))
						.addComponent(developersPanel, GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
		);

		layout.setVerticalGroup(
				layout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(developersPanel, GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(selectAllButton)
								.addComponent(deselectAllButton)))
		);
		this.setBorder(BorderFactory.createTitledBorder("Developers"));
		this.add(developersPanel, BorderLayout.CENTER);
		this.setLayout(layout);
		return this;
	}


	private void setupCheckBoxList() {
		checkBoxList.setAutoscrolls(true);
		checkBoxList.setAxisType(BoxLayout.Y_AXIS);
	}


	private JButton setupSelectAllButton() {
		selectAllButton = new JButton("Select all");
		selectAllButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				checkBoxList.getSelectionModel().setSelectionInterval(0, checkBoxList.getComponentCount()-1);
				new ListSelectionEvent(new Object(), 0, checkBoxList.getModel().getSize()-1, false); 
			}
		});

		return selectAllButton;
	}


	private JButton setupDeselectAllButton() {
		deselectAllButton = new JButton("Deselect all");
		deselectAllButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				checkBoxList.getSelectionModel().clearSelection();
			}
		});
		return deselectAllButton;
	}


	public JToggleGroup getCheckBoxList() {
		return checkBoxList;
	}


	public void setCheckBoxList(JToggleGroup checkBoxList) {
		this.checkBoxList = checkBoxList;
	}

	@Override
	public void valueChanged(ListSelectionEvent paramListSelectionEvent) {
		selectedAuthorsQuery = new String();
		StringBuilder authorNames = new StringBuilder();
		for ( int i=0; i<checkBoxList.getModel().getSize(); ++i ) {
			if(checkBoxList.getSelectionModel().isSelectedIndex(i)){
				JCheckBox selectedComponent = (JCheckBox)checkBoxList.getComponent(i);
				authorNames.append(selectedComponent.getText());
				authorNames.append(" | ");
			}
		}
		selectedAuthorsQuery = new String(authorNames);

		Visualization[] visualizations = (Visualization[]) ((JComponent) this.getParent()).getClientProperty("Visualizations");
		for (Visualization visualization : visualizations) {
			try {
				visualization.updateAuthorsVisibility(selectedAuthorsQuery);
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
		}
	}
}