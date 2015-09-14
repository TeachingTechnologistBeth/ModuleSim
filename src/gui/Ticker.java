package gui;

import simulator.Main;

public class Ticker implements Runnable {

	public void run() {
		// Updates the View @ ~30fps
		while(true) {
			Main.ui.view.repaint();
			
			try {Thread.sleep(24);}
			catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

}
