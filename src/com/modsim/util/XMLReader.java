package com.modsim.util;

import com.modsim.gui.view.View;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.modsim.modules.BaseModule;
import com.modsim.modules.BaseModule.AvailableModules;
import com.modsim.modules.Link;
import com.modsim.modules.ports.BidirPort;
import com.modsim.modules.parts.Port;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import com.modsim.Main;

final class ResultData {
    public List<BaseModule> modules;
    public List<Link> links;
    public int badLinks;

    public double camX;
    public double camY;
    public int zoom;

    public ResultData() {
        modules = new ArrayList<BaseModule>();
        links = new ArrayList<Link>();
        badLinks = 0;
    }
}

public class XMLReader {
    /**
     * Reads an XML-format file
     *
     * @param path
     */
    public static ResultData readFile(File xmlFile) {
        ResultData result = new ResultData();

        Main.sim.beginDeferPropagations();

        // Read the document elements into the program
        Main.sim.newSim();

        try {
            DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbF.newDocumentBuilder();
            Document doc = db.parse(xmlFile);

            result = readXML(doc);

            View v = Main.ui.view;
            v.init_camX = v.camX = result.camX;
            v.init_camY = v.camY = result.camY;
            v.init_zoomI = v.zoomI = result.zoom;
            v.zoom = View.ZOOM_MULTIPLIER * v.zoomI;
            v.calcXForm();

            // Notify user of partially corrupted file
            if (result.badLinks != 0) {
                JOptionPane.showMessageDialog(null,
                        "Detected " + result.badLinks + " bad links in the file. These were ignored.\n"
                                + "A known bug in an older version of ModuleSim may have corrupted your file - "
                                + "there may be other incorrect or missing links.");
            }

            // Save the file path
            Main.sim.filePath = xmlFile.getPath();
            Main.ui.updateTitle();
        } catch (

        Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error while loading XML file " + xmlFile.getPath() + " : " + e.getMessage());
        }

        Main.sim.endDeferPropagations();
        Main.ui.view.flagStaticRedraw();

        return result;
    }

    public static ResultData readString(String xmlStr) {
        ResultData result = new ResultData();

        Main.sim.beginDeferPropagations();

        try {
            DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbF.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xmlStr)));

            result = readXML(doc);

            // Notify user of partially corrupted file
            if (result.badLinks != 0) {
                JOptionPane.showMessageDialog(null,
                        "Detected " + result.badLinks + " bad links in the file. These were ignored.\n"
                                + "A known bug in an older version of ModuleSim may have corrupted your file - "
                                + "there may be other incorrect or missing links.");
            }
        } catch (

        Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error while loading XML data: " + e.getMessage());
        }

        Main.sim.endDeferPropagations();
        Main.ui.view.flagStaticRedraw();

        return result;
    }

    /**
     * Finds the maximum ID number for entities in the simulation.
     */
    private static int findMaxID(List<BaseModule> modules) {
        synchronized (Main.sim) {
            int id = 0;

            for (BaseModule m : modules) {
                if (m.ID > id) {
                    id = m.ID;
                }

                for (Port p : m.ports) {
                    if (p.ID > id) {
                        id = p.ID;
                    }
                }
            }

            return id;
        }
    }

    private static ResultData readXML(Document doc) {
        ResultData result = new ResultData();

        try {
            doc.getDocumentElement().normalize();

            // Identity remapping
            Map<Integer, Integer> idRemap = new HashMap<Integer, Integer>();
            int remapAtId = findMaxID(Main.sim.getModules()) + 1;

            // View load
            Element view = (Element) doc.getElementsByTagName("view").item(0);
            result.camX = Double.parseDouble(view.getAttribute("camX"));
            result.camY = Double.parseDouble(view.getAttribute("camY"));
            result.zoom = Integer.parseInt(view.getAttribute("zoom"));

            // Module load
            NodeList mods = doc.getElementsByTagName("module");
            List<Port> loadedPorts = new ArrayList<>();

            for (int i = 0; i < mods.getLength(); i++) {
                Node n = mods.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element module = (Element) n;

                    int id = Integer.parseInt(module.getAttribute("ID"));
                    String modType = module.getAttribute("type");

                    AvailableModules am;
                    try {
                        am = AvailableModules.valueOf(modType);
                    } catch (IllegalArgumentException iae) {
                        System.err.println("Warning: Skipping unrecognized module '" + modType + "'");
                        continue;
                    }

                    BaseModule m = (BaseModule) am.getSrcModule().createNew();
                    m.ID = remapAtId++;
                    idRemap.put(id, m.ID);

                    // Set the dimensions
                    Element dim = (Element) module.getElementsByTagName("dim").item(0);
                    m.pos.x = Double.parseDouble(dim.getAttribute("x"));
                    m.pos.y = Double.parseDouble(dim.getAttribute("y"));
                    m.orientation = Integer.parseInt(dim.getAttribute("orient"));

                    // Set the label
                    // Note: This is left for legacy file formats.
                    // Newer versions store label information in the data tags.
                    Element label = (Element) module.getElementsByTagName("label").item(0);
                    if (label != null) {
                        m.label = label.getTextContent();
                        m.labelSize = Integer.parseInt(label.getAttribute("size"));
                    }

                    // HAX: backwards-compatibility is fun for the whole family!
                    // Previous versions of the program made no real distinction between normal
                    // ports and the
                    // split-merge's bidirectional ports - they were stored in the input/output
                    // lists based on
                    // which side they were supposed to appear on. Now we have to deal with that by
                    // picking out
                    // the bidirectional ports and appending them to the input and output lists.
                    ArrayList<Port> moduleInputs = new ArrayList<>();
                    moduleInputs.addAll(m.inputs);
                    ArrayList<Port> moduleOutputs = new ArrayList<>();
                    moduleOutputs.addAll(m.outputs);

                    for (BidirPort p : m.bidirs) {
                        if (p.side == 1) {
                            moduleInputs.add(p);
                        } else {
                            moduleOutputs.add(p);
                        }
                    }

                    // Set input IDs
                    NodeList inputs = module.getElementsByTagName("input");
                    for (int j = 0; j < inputs.getLength(); j++) {
                        Element inIDElem = (Element) inputs.item(j);
                        int inID = Integer.parseInt(inIDElem.getAttribute("ID"));
                        int inIDRemapped;
                        if (!idRemap.containsKey(inID)) {
                            inIDRemapped = remapAtId++;
                            idRemap.put(inID, inIDRemapped);
                        } else {
                            inIDRemapped = idRemap.get(inID);
                        }
                        moduleInputs.get(j).ID = inIDRemapped;
                        loadedPorts.add(moduleInputs.get(j));
                    }

                    // Set output IDs
                    NodeList outputs = module.getElementsByTagName("output");
                    for (int j = 0; j < outputs.getLength(); j++) {
                        Element outIDElem = (Element) outputs.item(j);
                        int outID = Integer.parseInt(outIDElem.getAttribute("ID"));
                        int outIDRemapped;
                        if (!idRemap.containsKey(outID)) {
                            outIDRemapped = remapAtId++;
                            idRemap.put(outID, outIDRemapped);
                        } else {
                            outIDRemapped = idRemap.get(outID);
                        }
                        moduleOutputs.get(j).ID = outIDRemapped;
                        loadedPorts.add(moduleOutputs.get(j));
                    }

                    // Additional module data (for NRAM and inputs)
                    NodeList data = module.getElementsByTagName("data");
                    // - Parse data map
                    HashMap<String, String> dataMap = new HashMap<>();
                    for (int j = 0; j < data.getLength(); j++) {
                        NamedNodeMap nodeMap = data.item(j).getAttributes();
                        for (int k = 0; k < nodeMap.getLength(); k++) {
                            Node item = nodeMap.item(k);
                            dataMap.put(item.getNodeName(), item.getNodeValue());
                        }
                    }
                    // - Load data map
                    m.dataIn(dataMap);

                    // Update module
                    m.propagate();

                    // Add to the simulation
                    Main.sim.addEntity(m);
                    m.enabled = true;

                    result.modules.add(m);
                }
            }

            // Link load
            NodeList links = doc.getElementsByTagName("link");

            String BezierPathTagName = new BezierPath().XMLTagName();
            String StraightPathTagName = new StraightPath().XMLTagName();

            for (int i = 0; i < links.getLength(); i++) {
                Node n = links.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element link = (Element) n;

                    int srcID = Integer.parseInt(link.getAttribute("src"));
                    int targID = Integer.parseInt(link.getAttribute("targ"));
                    String tagName = link.getAttribute("type");

                    if (srcID == targID) {
                        System.err.println(
                                "Warning: Link's source and target are the same (" + srcID + "). Skipping link");
                        continue;
                    } else if (!idRemap.containsKey(srcID) || !idRemap.containsKey(targID)) {
                        continue;
                        // Link may simply have been to a module that wasn't copied
                    }

                    srcID = idRemap.get(srcID);
                    targID = idRemap.get(targID);

                    Port src = null, targ = null;

                    // Find the source and target
                    for (Port p : loadedPorts) {
                        if (p.ID == srcID) {
                            src = p;
                        } else if (p.ID == targID) {
                            targ = p;
                        }
                    }

                    // Generate the bezier path
                    Path curve;
                    if (tagName.equals(BezierPathTagName)) {
                        curve = new BezierPath();
                    } else if (tagName.equals(StraightPathTagName)) {
                        curve = new StraightPath();
                    } else {
                        curve = new BezierPath();
                    }

                    NodeList points = link.getElementsByTagName("ctrlPt");
                    for (int j = 0; j < points.getLength(); j++) {
                        Element pt = (Element) points.item(j);
                        double x = Double.parseDouble(pt.getAttribute("x"));
                        double y = Double.parseDouble(pt.getAttribute("y"));
                        curve.addPt(new CtrlPt(x, y));
                    }

                    // Create the link
                    Link l = Link.createLink(src, targ, curve);
                    result.links.add(l);

                    // Add to the simulation
                    if (l != null) {
                        Main.sim.addLink(l);
                        Main.sim.propagate(l.targ.owner);
                    } else {
                        result.badLinks++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while loading XML data: " + e.getMessage());
        }

        return result;
    }
}
