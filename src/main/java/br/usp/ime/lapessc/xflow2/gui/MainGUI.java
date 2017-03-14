package br.usp.ime.lapessc.xflow2.gui;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

public class MainGUI {

	private JFrame frame = new JFrame("XFlow 2");
	
	public MainGUI(){
		JFrame jFrame = buildJFrame();
		jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		jFrame.setVisible(true);
	}
	
	private JFrame buildJFrame() {
		
		//Builds the Menu and add it to the frame
		MenuBuilder menuBuilder = new MenuBuilder();
		JMenuBar menuBar = menuBuilder.build();
		frame.setJMenuBar(menuBar);

		//Other stuff
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		return frame;
	}
	
	public static void main(String[] args) {
		MainGUI mainGUI = new MainGUI();
	}
	
}