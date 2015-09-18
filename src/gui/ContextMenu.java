package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import modules.BaseModule;
import modules.BaseModule.AvailableModules;
import modules.NRAM;
import modules.parts.Port;
import simulator.Main;
import simulator.PickableEntity;
import tools.RotateOperation;
import util.Selection;

public class ContextMenu  {


	public JPopupMenu moduleMenu;
	public JPopupMenu portMenu;
	private List<PickableEntity> entities = new ArrayList<PickableEntity>();
	private Port port;

	private JMenuItem rmLink, rotCW, rotCCW, rot180, copy, paste, delete, ramEdit;

	/**
	 * Instantiates the menu system, generating the menu items
	 */
	public ContextMenu() {
	    // Remove link
        rmLink = new JMenuItem("Remove Link");
        rmLink.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (port != null && port.link != null) {
					port.link.delete();
				}
            }
        });

        // Rotation action
        ActionListener rotate = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String cmd = event.getActionCommand();
                BaseModule.rotationDir dir;
                if (cmd.contains("right")) {
                    dir = BaseModule.rotationDir.ROT_CW;
                }
                else if (cmd.contains("left")) {
                    dir = BaseModule.rotationDir.ROT_CCW;
                }
                else if (cmd.contains("180")) {
                    dir = BaseModule.rotationDir.ROT_180;
                }
                else {
                    throw new InvalidParameterException(cmd + " is not a valid rotation command.");
                }

                Main.opStack.beginCompoundOp();
                for (PickableEntity e : entities) {
                    if (e.getClass().getGenericSuperclass() == BaseModule.class) {
                        BaseModule m = (BaseModule) e;
                        m.rotate(dir);

                        Main.opStack.pushOp(new RotateOperation(m, dir));
                    }
                }
                Main.opStack.endCompoundOp();
            }
        };

		// Rotate CW
		rotCW = new JMenuItem("Rotate right");
		rotCW.addActionListener(rotate);

		// Rotate CCW
		rotCCW = new JMenuItem("Rotate left");
		rotCCW.addActionListener(rotate);

		// Rotate 180
		rot180 = new JMenuItem("Rotate 180");
		rot180.addActionListener(rotate);

		// Copy/pasteInto
        copy = new JMenuItem("Copy");
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Main.clipboard.copy(entities);
            }
        });

        paste = new JMenuItem("Paste");
        paste.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Main.ui.view.pasteInto();
            }
        });

		// Delete
		delete = new JMenuItem("Delete");
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
                Main.selection.deleteAll();
			}
		});

		////////// Memory-specfic

		// Edit memory
		ramEdit = new JMenuItem("View/Edit Data");
		ramEdit.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent arg0) {
		        NRAM nram = (NRAM) entities.get(0);
		        MemEdit editor = Main.ui.newMemEdit();
		        editor.show(nram);
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
	    port = ViewUtil.portAt(x, y);

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

	        if (entities.size() == 1) {
    	        PickableEntity e = entities.get(0);

    	        // If it's a NRAM module
    	        if (e.getType() == PickableEntity.MODULE && ((BaseModule)e).getModType().equals(AvailableModules.RAM)) {
    	            menu.addSeparator();
    	            menu.add(ramEdit);
    	        }
	        }
	    }

	    // A menu gets shown any which way
	    menu.show(Main.ui.view, x, y);
	}

}
