package com.modsim.operations;

import com.modsim.res.ResourceLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Created by Ali on 24/09/2015.
 * Defines the application's implementation of AbstractAction, unifying UI enable/disable state and shortcut keys
 */
public class DesignAction extends AbstractAction {

    Consumer<ActionEvent> onAction;

    // Various convenience constructors

    public DesignAction(Consumer<ActionEvent> action, String name) {
        onAction = action;
        putValue(NAME, name);

        // Attempts to find the corresponding icon in the res folder
        URL imgURL = ResourceLoader.class.getResource(name + ".png");
        URL imgURL_16 = ResourceLoader.class.getResource(name + "_16.png");

        if (imgURL != null) {
            putValue(SMALL_ICON, new ImageIcon(imgURL, null));
        }
        if (imgURL_16 != null) {
            putValue(SMALL_ICON, new ImageIcon(imgURL_16, null));
        }
    }

    public DesignAction(Consumer<ActionEvent> action, String name, String tooltip) {
        this(action, name);
        putValue(SHORT_DESCRIPTION, tooltip);
    }

    public DesignAction(Consumer<ActionEvent> action, String name, String tooltip, int mnemonic) {
        this(action, name, tooltip);
        putValue(MNEMONIC_KEY, mnemonic);
    }

    public DesignAction(Consumer<ActionEvent> action, String name, String tooltip, KeyStroke shortcut) {
        this(action, name, tooltip);
        putValue(ACCELERATOR_KEY, shortcut);
    }

    public DesignAction(Consumer<ActionEvent> action, String name, String tooltip, KeyStroke shortcut, int mnemonic) {
        this(action, name, tooltip, shortcut);
        putValue(MNEMONIC_KEY, mnemonic);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        onAction.accept(e);
    }
}
