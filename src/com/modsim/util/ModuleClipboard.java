package com.modsim.util;

import com.modsim.modules.BaseModule;
import com.modsim.modules.Link;
import com.modsim.modules.parts.Port;
import com.modsim.Main;
import com.modsim.simulator.PickableEntity;
import com.modsim.operations.CreateOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Ali on 05/09/2015.
 */
public final class ModuleClipboard implements ClipboardOwner {

    /**
     * Whether the clipboard has any items on it
     * @return True if the clipboard is isEmpty
     */
    public boolean isEmpty() {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable contents = clipboard.getContents(null);
        final boolean hasTransferableText = (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        final boolean hasTransferableFiles = (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        return !(hasTransferableText || hasTransferableFiles);
    }

    /**
     * Copies the given entities to the clipboard in their current state. Only links and control points BETWEEN copied
     * com.modsim.modules are stored. Caller should ensure no modifications are made to the original entities while this method
     * is executing.
     * @param entities The entities to copy
     */
    public void copy(final List<PickableEntity> entities) {
        // Pick out the com.modsim.modules from the generic entities list
        final List<BaseModule> copiedRefs = new ArrayList<BaseModule>();
        for (final PickableEntity e : entities) {
            if (e.getType() == PickableEntity.MODULE) {
                copiedRefs.add((BaseModule) e);
            }
        }

        // Copy across to clipboard storage
        doCopy(copiedRefs);
    }

    /**
     * Copies the given entities to the clipboard in their current state. Only links
     * and control points BETWEEN copied com.modsim.modules are stored. Caller
     * should ensure no modifications are made to the original entities while this
     * method is executing.
     * 
     * @param selection Selection containing the entities to copy
     */
    public void copy(final Selection selection) {
        // Pick out the com.modsim.modules from the generic entities list
        final List<BaseModule> copiedRefs = new ArrayList<BaseModule>();
        for (final PickableEntity e : selection.internalSelection) {
            if (e.getType() == PickableEntity.MODULE) {
                copiedRefs.add((BaseModule) e);
            }
        }

        // Copy across to clipboard storage
        doCopy(copiedRefs);
    }

    /**
     * Copies the stored com.modsim.modules into the simulation, generating the
     * corresponding creation operations, and returns all created entities as a
     * list.
     * 
     * @return The newly created entities
     * @throws Exception
     */
    public List<PickableEntity> paste() throws Exception {
        
        String xmlStr = getClipboardContents();
        if (xmlStr != null) {
            ResultData result = XMLReader.readString(xmlStr);

            final List<BaseModule> modules = result.modules;
            final List<Link> links = result.links;

            final List<PickableEntity> output = new ArrayList<>(modules);

            // Add to the simulation
            for (final BaseModule m : modules) {
                Main.opStack.pushOp(new CreateOperation(m));
            }
            for (final Link l : links) {
                Main.opStack.pushOp(new CreateOperation(l));

                // Need to return control points as well
                output.addAll(l.path.ctrlPts);
            }

            return output;
        }

        throw new Exception("Clipboard contents empty or not valid.");
    }

    public List<Link> getAllLinks(final List<BaseModule> modules) {
        List<Link> result = new ArrayList<Link>();

        for (int i = 0; i < modules.size(); i++) {
            final BaseModule m = modules.get(i);

            for (int j = 0; j < m.ports.size(); j++) {
                final Port port = m.ports.get(j);
                
                // Check it's a link between two copied entities
                if (port.link != null && port == port.link.src && modules.contains(port.link.src.owner)
                        && modules.contains(port.link.targ.owner)) {

                    result.add(port.link);
                }
            }
        }

        return result;
    }

    /**
     * Internal method: copies com.modsim.modules with their complete properties and
     * shared links/control points
     * 
     * @param src         List of com.modsim.modules to copy
     * @param destModules (out) list of copied com.modsim.modules
     * @param destLinks   (out) list of copied links
     */
    protected void doCopy(final List<BaseModule> src) {
        setClipboardContents(XMLWriter.writeString(src, getAllLinks(src)));
    }

    /**
     * Empty implementation of the ClipboardOwner interface.
     */
    @Override
    public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
        // do nothing
    }

    /**
     * Place a String on the clipboard, and make this class the owner of the
     * Clipboard's contents.
     */
    public void setClipboardContents(final String string) {
        final StringSelection stringSelection = new StringSelection(string);
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    /**
     * Get the String residing on the clipboard.
     *
     * @return any text found on the Clipboard; if none found, returns an empty
     *         String.
     */
    public String getClipboardContents() {
        String result = null;
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable contents = clipboard.getContents(null);
        final boolean hasTransferableText = (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        final boolean hasTransferableFiles = (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        
        if (hasTransferableFiles) {
            try {
                List<File> files = (List<File>) contents.getTransferData(DataFlavor.javaFileListFlavor);
                if (files.size() == 1) {
                    File file = files.get(0);
                    result = new String(Files.readAllBytes(file.toPath()));
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                System.out.println(ex);
                ex.printStackTrace();
            }
        }
        else if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException ex) {
                System.out.println(ex);
                ex.printStackTrace();
            }
        }

        return result;
    }
}
