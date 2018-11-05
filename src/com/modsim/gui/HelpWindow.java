package com.modsim.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class HelpWindow  extends JFrame {

	public HelpWindow(){
		super("Help");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
        JLabel text = new JLabel();
        //static help text
        text.setText("<html><h2>ModuleSim Help</h2><h3>General Operations</h3>You can zoom in and out by scrolling or pinching on a multi-touch touch pad. You can pan around using the right mouse key.<br><br>Dragging the left mouse key enables you to select all modules in a region. Standard operations like saving to and loading from file, undo and redo and copy and paste are available through menu bar and keyboard shortcuts.<br><br>Note to Mac users: right click is <i>probably</i> two finger tap.<h3>Adding Modules</h3>To use a module, left click on it (in the left sidebar) and then left click to drop in place. If you selected one in error press escape (ESC).<br><br>To delete a module select it with a left click and then either press the delete key or right click and select 'delete'.<br><br>Right clicking on modules brings up a context menu, where you can do things like label and rotate the modules.<h3>Adding Wires</h3>To connect two modules together, left click on the two sockets (indicated on the modules as coloured circles), one after the other. The wire will be drawn between the two sockets. If you start drawing a wire by accident press the ESC key.<br><br>You can change the shape of the wire by adding waypoints. This is done by left clicking as many times as you want before selecting the destination socket.<br><br>You can add waypoints afterwards by clicking on the wire and then again on the wire in the place where you want the waypoint. You can then drag this point around.<br><br>The software will naturally draw a straight line between the points. To create a curve, hold SHIFT when you click on the module to start the wire.<br><br>To delete a wire right click on one of the ends and select delete.<br><br>To delete a waypoint right click it and select delete.<h3>Specific Modules</h3>The switches can be used to produce constants. Their value can be set by clicking on each of the four switches to toggle the bit value.<br><br>The clock module is what controls the timing of your circuits. Once it has been connected up you can put it in to reset mode by holding down the button on the module with the mouse. To progress the clock signal you can use the control panel underneath the menu bar. You can step the clock one step a time or press play and pause to run it automatically, adjusting the speed with the slider.<br><br>The RAM module write jumper is implemented as a switch. This needs to be switched on for the circuit to write data to it.<br><br>You can also load data in to the ram from a file. Right click on the NRAM and select 'View/Edit Data'. Then File->Load. The file must be a text file containing each byte written in hex (capital letters) delimited by spaces.</html>");
        text.setPreferredSize(new Dimension(600, 1000));
        text.setBorder(BorderFactory.createEmptyBorder(50,50,50,50));
        JScrollPane scrollPane = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED	);
        scrollPane.setPreferredSize(new Dimension(621, 800));
        add(scrollPane);
        pack();
        //place in center of screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = this.getSize().width;
        int h = this.getSize().height;
        int x = (dim.width-w)/2;
        int y = (dim.height-h)/2;
        setLocation(x, y);
    }
}
