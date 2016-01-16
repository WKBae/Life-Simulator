package net.wkbae.lifesimulator.window;

import javax.swing.SwingUtilities;

public class SimulatorMain {
	
	public static void main(String[] args) {
		//System.setProperty("sun.java2d.opengl", "true");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SimulatorFrame();
			}
		});
	}
}
