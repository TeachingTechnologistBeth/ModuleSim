package simulator;

import gui.GUI;
import gui.Ticker;

import javax.swing.*;

/**
 * Just does initialisation for the program
 * @author aw12700
 *
 */
public class Main {

	public static GUI ui = null;
	public static Sim sim = null;

	/**
	 * Program starting point
	 * @param args System argument
	 */
	public static void main(String[] args) {
		// Set up GUI thread
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// (In the new thread)
				ui = new GUI();
				ui.generateUI();
				ui.showUI(true);

				// Start render ticking
				Thread t = new Thread(new Ticker());
				t.start();

				// Start sim ticking - sim is initialized below *before* this is called
				sim.start();
			}
		});

		// Set up simulator
		sim = new Sim();
	}

}
