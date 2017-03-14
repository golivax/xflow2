package br.usp.ime.lapessc.xflow2.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuBuilder {
		
	public JMenuBar build() {
				
		JMenuBar jMenuBar = new JMenuBar();
	
		JMenu fileMenu = buildFileMenu();
		JMenu helpMenu = buildHelpMenu();

		jMenuBar.add(fileMenu);
		jMenuBar.add(helpMenu);
			
		return jMenuBar;
	}
	
	private ImageIcon getImageIcon(String iconName){
		URL url = getClass().getClassLoader().getResource(iconName);
		ImageIcon imageIcon = new ImageIcon(url);
		return imageIcon;
	}

	private JMenu buildFileMenu() {
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		JMenuItem newProjectMenuItem = new JMenuItem("New Project");		
		
		newProjectMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
	
			}
		});
		
		JMenuItem openProjectMenuItem = new JMenuItem("Open Project");
		
		openProjectMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
	
			}
		});
		
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});

		//Assembly
		fileMenu.add(newProjectMenuItem);
		fileMenu.add(openProjectMenuItem);
		fileMenu.add(exitItem);
		return fileMenu;
	}

	private JMenu buildHelpMenu() {
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);

		JMenuItem aboutItem = new JMenuItem("About");
		helpMenu.add(aboutItem);
		return helpMenu;
	}
	
}
