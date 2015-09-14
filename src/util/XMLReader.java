package util;

import gui.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import modules.BaseModule;
import modules.BaseModule.AvailableModules;
import modules.Link;
import modules.parts.Port;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import simulator.Main;

public class XMLReader {

    /**
     * Reads an XML-format file
     *
     * @param path
     */
    public static void readFile(File xmlFile) {
        try {
            DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbF.newDocumentBuilder();
            Document doc = db.parse(xmlFile);

            doc.getDocumentElement().normalize();

            // Read the document elements into the program
            Main.sim.newSim();

            // View load
            Element view = (Element) doc.getElementsByTagName("view").item(0);
            View v = Main.ui.view;
            v.camX = Double.parseDouble(view.getAttribute("camX"));
            v.camY = Double.parseDouble(view.getAttribute("camY"));
            v.zoomI = Integer.parseInt(view.getAttribute("zoom"));
            v.zoom = 0.2 * v.zoomI;
            v.calcXForm();

            // Module load
            NodeList mods = doc.getElementsByTagName("module");
            List<Port> loadedPorts = new ArrayList<Port>();

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
                        System.err.println("Warning: Skipping unrecognized module '"+modType+"'");
                        continue;
                    }

                    BaseModule m = (BaseModule) am.getSrcModule().createNew();
                    m.ID = id;

                    // Set the dimensions
                    Element dim = (Element) module.getElementsByTagName("dim").item(0);
                    m.pos.x = Double.parseDouble(dim.getAttribute("x"));
                    m.pos.y = Double.parseDouble(dim.getAttribute("y"));
                    m.orientation = Integer.parseInt(dim.getAttribute("orient"));

                    // Set input IDs
                    NodeList inputs = module.getElementsByTagName("input");
                    for (int j = 0; j < inputs.getLength(); j++) {
                        Element inID = (Element) inputs.item(j);
                        m.inputs.get(j).ID = Integer.parseInt(inID.getAttribute("ID"));
                        loadedPorts.add(m.inputs.get(j));
                    }

                    // Set output IDs
                    NodeList outputs = module.getElementsByTagName("output");
                    for (int j = 0; j < outputs.getLength(); j++) {
                        Element outID = (Element) outputs.item(j);
                        m.outputs.get(j).ID = Integer.parseInt(outID.getAttribute("ID"));
                        loadedPorts.add(m.outputs.get(j));
                    }

                    // Additional module data (for RAM and inputs)
                    NodeList data = module.getElementsByTagName("data");
                    for (int j = 0; j < data.getLength(); j++) {
                        Element d = (Element) data.item(j);
                        m.dataIn(d);
                    }

                    // Add to the simulation
                    Main.sim.addEntity(m);
                    m.enabled = true;
                }
            }

            // Link load
            NodeList links = doc.getElementsByTagName("link");
            int badLinks = 0;

            for (int i = 0; i < links.getLength(); i++) {
                Node n = links.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element link = (Element) n;

                    int srcID = Integer.parseInt(link.getAttribute("src"));
                    int targID = Integer.parseInt(link.getAttribute("targ"));
                    
                    if (srcID == targID) {
                        System.err.println("Warning: Link's source and target are the same ("+srcID+"). Skipping link");
                        continue;
                    }

                    Port src = null, targ = null;

                    // Find the source and target
                    for (Port p : loadedPorts) {
                        if (p.ID == srcID) {
                            src = p;
                        }
                        else if (p.ID == targID) {
                            targ = p;
                        }
                    }

                    // Generate the bezier curve
                    BezierPath curve = new BezierPath();

                    NodeList points = link.getElementsByTagName("ctrlPt");
                    for (int j = 0; j < points.getLength(); j++) {
                        Element pt = (Element) points.item(j);
                        double x = Double.parseDouble(pt.getAttribute("x"));
                        double y = Double.parseDouble(pt.getAttribute("y"));
                        curve.addPt(new CtrlPt(x, y));
                    }

                    // Create the link
                    Link l = Link.createLink(src, targ, curve);

                    // Add to the simulation
                    if (l != null) {
                        Main.sim.addLink(l);
                    }
                    else {
                        badLinks++;
                    }
                }
            }
            
            // Notify user of partially corrupted file
            if (badLinks != 0) {
                JOptionPane.showMessageDialog(null, "Detected " + badLinks + " bad links in the file. These were ignored.\n"+
                                                    "A known bug in an older version of ModuleSim may have corrupted your file - "+
                                                    "there may be other incorrect or missing links.");
            }

            // Save the file path
            Main.sim.filePath = xmlFile.getPath();
            Main.ui.updateTitle();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while loading file "+xmlFile.getName()+": " + e.getMessage());
        }
    }
}
