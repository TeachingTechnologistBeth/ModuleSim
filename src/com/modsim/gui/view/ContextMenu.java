package com.modsim.gui.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import com.modsim.operations.Ops;
import com.modsim.gui.MemEdit;
import com.modsim.modules.BaseModule;
import com.modsim.modules.BaseModule.AvailableModules;
import com.modsim.modules.LEDMatrix;
import com.modsim.modules.NRAM;
import com.modsim.modules.Register;
import com.modsim.modules.parts.Port;
import com.modsim.Main;
import com.modsim.simulator.PickableEntity;
import com.modsim.util.BinData;

public class ContextMenu  {


	public JPopupMenu moduleMenu;
	public JPopupMenu portMenu;
	private List<PickableEntity> entities = new ArrayList<PickableEntity>();
	private Port port;

	private JMenuItem rmLink, rotCW, rotCCW, rot180, copy, paste, delete,
			ramEdit, ramClear, regEdit, regClear, labelEdit, labelSize, persistanceOn, persistanceOff;

	/**
	 * Instantiates the menu system, generating the menu items
	 */
	public ContextMenu() {
	    // Remove link (this is still a special case for now)
        rmLink = new JMenuItem("Remove Link");
        rmLink.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (port != null && port.link != null) {
					port.link.delete();
					//redraw
					Main.ui.view.flagStaticRedraw();
				}
            }
        });

		// Rotation
		rotCW = new JMenuItem(Ops.rotateCW);
		rotCCW = new JMenuItem(Ops.rotateCCW);
		rot180 = new JMenuItem(Ops.rotate180);

		// Copy/paste
        copy = new JMenuItem(Ops.copy);
        paste = new JMenuItem(Ops.paste);

		// Delete
		delete = new JMenuItem(Ops.delete);

        ////////// Module-specific

        // Labelling
        labelEdit = new JMenuItem(Ops.labelEdit);

        labelSize = new JMenu("Label size");
        labelSize.add(new JMenuItem(Ops.labelSmall));
        labelSize.add(new JMenuItem(Ops.labelBig));

		////////// Memory-specfic

		// Edit memory
		ramEdit = new JMenuItem("View/Edit NRAM Data");
		ramEdit.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent arg0) {
				for (PickableEntity e : entities) {
					// If it's an NRAM module
					if (e.getType() == PickableEntity.MODULE && ((BaseModule)e).getModType().equals(AvailableModules.RAM)) {
						NRAM nram = (NRAM) e;
						MemEdit editor = Main.ui.newMemEdit();
						editor.show(nram);
					}
				}
		    }
		});

		// Clear memory
		ramClear = new JMenuItem("Clear NRAM Data");
		ramClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int res = JOptionPane.showConfirmDialog(Main.ui.frame, "Wiping NRAM data cannot be undone",
						"Are you sure?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

				for (PickableEntity entity : entities) {
					// If it's an NRAM module
					if (entity.getType() == PickableEntity.MODULE &&
							((BaseModule)entity).getModType().equals(AvailableModules.RAM)) {
						NRAM ram = (NRAM) entity;
						switch (res) {
							case JOptionPane.OK_OPTION:
								ram.clear();
								Main.sim.propagate(ram);
							default:
								break;
						}
					}
				}
			}
		});

		////////// Register-specfic

		// Edit
		regEdit = new JMenuItem("Set Register value");
		regEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BinData newVal = new BinData(0);
				if (entities.size() == 1) {
					newVal = ((Register)entities.get(0)).getStoredVal();
				}

				String valStr = JOptionPane.showInputDialog(Main.ui.frame, "Enter new Register value:", newVal);
				if (valStr != null) {
					if (valStr.length() != 4) {
                        JOptionPane.showMessageDialog(Main.ui.frame, "4 bits required", "Bad Input",
                                JOptionPane.ERROR_MESSAGE);
						return;
                    }

					boolean b0 = Integer.parseInt(valStr.substring(0, 1)) == 1;
					boolean b1 = Integer.parseInt(valStr.substring(1, 2)) == 1;
					boolean b2 = Integer.parseInt(valStr.substring(2, 3)) == 1;
					boolean b3 = Integer.parseInt(valStr.substring(3)) == 1;
					newVal.setBool(b0, b1, b2, b3);

					for (PickableEntity entity : entities) {
                        if (entity.getType() == PickableEntity.MODULE &&
                                ((BaseModule)entity).getModType().equals(AvailableModules.REGISTER)) {
                            Register reg = (Register) entity;
                            reg.setStoredVal(newVal);

                            Main.sim.propagate(reg);
                        }
                    }
				}
			}
		});

		// Clear
		regClear = new JMenuItem("Clear Register value");
		regClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (PickableEntity entity : entities) {
					if (entity.getType() == PickableEntity.MODULE &&
							((BaseModule)entity).getModType().equals(AvailableModules.REGISTER)) {
						Register reg = (Register) entity;
						reg.clear();

						Main.sim.propagate(reg);
					}
				}
			}
		});
		
		
		///////////////LED Matrix specific
		
		//Toggle Persistance
		persistanceOff = new JMenuItem("Turn off low-speed persistence");
		persistanceOff.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				for (PickableEntity entity : entities) {
					if (entity.getType() == PickableEntity.MODULE &&
							((BaseModule)entity).getModType().equals(AvailableModules.LEDMatrix)) {
						LEDMatrix ledmatrix = (LEDMatrix) entity;
						ledmatrix.turnOffPersist();						
					}
				}
			}
		});
		
		persistanceOn = new JMenuItem("Turn on low-speed persistence");
		persistanceOn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				for (PickableEntity entity : entities) {
					if (entity.getType() == PickableEntity.MODULE &&
							((BaseModule)entity).getModType().equals(AvailableModules.LEDMatrix)) {
						LEDMatrix ledmatrix = (LEDMatrix) entity;
						ledmatrix.turnOnPersist();						
					}
				}
			}
		});
		

	}

	/**
	 * Displays a context-sensitive edit menu
	 * @param modules The 'selection' to operate on
	 * @param x X-position to display the menu at
	 * @param y Y-position to display the menu at
	 */
    public void showEntityMenu(List<PickableEntity> modules, int x, int y) {
	    // Menu to display - fill it in based on context
	    JPopupMenu menu = new JPopupMenu();

	    // Check for a port at the clicked location - for link removal (hacky, yeah)
	    port = ViewUtil.screenSpace_portAt(x, y);

	    // Grab the entities - put them into the class variable
        entities = new ArrayList<PickableEntity>();
        entities.addAll(modules);

	    if (port != null) {
	        menu.add(rmLink);
	    }
	    else {
	        // Standard module options
	        menu.add(rotCW);
	        menu.add(rotCCW);
	        menu.add(rot180);
	        menu.add(copy);
	        menu.add(paste);
	        menu.add(delete);


			// Additional module options

            for (PickableEntity e : entities) {
                // If it's an NRAM module
                if (e.getType() == PickableEntity.MODULE && ((BaseModule)e).getModType().equals(AvailableModules.RAM)) {
                    menu.addSeparator();
                    menu.add(ramEdit);
                    menu.add(ramClear);
                    break;
                }
            }
            
            for (PickableEntity e : entities) {
                // If it's an LEDMatrix module
                if (e.getType() == PickableEntity.MODULE && ((BaseModule)e).getModType().equals(AvailableModules.LEDMatrix)) {
                    menu.addSeparator();
                    if(((LEDMatrix)e).isPersistEnabled()){
                    	menu.add(persistanceOff);
                    }else{
                    	menu.add(persistanceOn);
                    }
                    break;
                }
            }

            for (PickableEntity e : entities) {
                // If it's a Register module
                if (e.getType() == PickableEntity.MODULE &&
						((BaseModule)e).getModType().equals(AvailableModules.REGISTER)) {
                    menu.addSeparator();
                    menu.add(regEdit);
                    menu.add(regClear);

                    break;
                }
            }

            for (PickableEntity e : entities) {
                // If it's a module
                if (e.getType() == PickableEntity.MODULE) {
                    menu.addSeparator();
                    menu.add(labelEdit);
                    menu.add(labelSize);

                    break;
                }
            }
        }

	    // A menu gets shown any which way
	    menu.show(Main.ui.view, x, y);
	}

}
