package com.modsim.util;

import com.modsim.gui.view.View;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.modsim.modules.BaseModule;
import com.modsim.modules.Link;
import com.modsim.modules.ports.BidirPort;
import com.modsim.modules.parts.Port;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.modsim.Main;

public class XMLWriter {

    /**
     * Generates unique IDs for entities in the simulation
     */
    private static void genIDs() {
        synchronized (Main.sim) {
            int id = 0;

            for (BaseModule m : Main.sim.getModules()) {
                m.ID = id++;

                for (Port p : m.ports) {
                    p.ID = id++;
                }
            }
        }
    }

    /**
     * Write an XML format file
     *
     * @param path
     */
    public static void writeFile(File xmlFile) {
        try {
            Document doc = constructXMLDocument(Main.sim.getModules(), Main.sim.getLinks());

            // Saving operation
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource src = new DOMSource(doc);
            StreamResult r = new StreamResult(xmlFile);

            t.transform(src, r);
            System.out.println("Saved simulation to " + xmlFile.getAbsolutePath());

            Main.sim.filePath = xmlFile.getPath();
            Main.ui.updateTitle();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String writeString(List<BaseModule> modules, List<Link> links) {
        try {
            Document doc = constructXMLDocument(modules, links);

            // Saving operation
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource src = new DOMSource(doc);

            StringWriter writer = new StringWriter();
            t.transform(src, new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\r", "");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Construct an XML document for the current file.
     * 
     * @return The XML document.
     */
    private static Document constructXMLDocument( List<BaseModule> modules
                                                , List<Link> links)
        throws ParserConfigurationException {
        DocumentBuilderFactory dbF = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbF.newDocumentBuilder();

        Document doc = db.newDocument();

        // Build the document... add elements, attributes to represent the entities
        Element rootElem = doc.createElement("ModuleSim");
        doc.appendChild(rootElem);

        // Store the view information
        Element view = doc.createElement("view");
        View v = Main.ui.view;
        view.setAttribute("camX", "" + v.camX);
        view.setAttribute("camY", "" + v.camY);
        view.setAttribute("zoom", "" + v.zoomI);
        rootElem.appendChild(view);

        synchronized (Main.sim) {
            // Generate IDs for storage
            genIDs();

            // Store the modules
            Element mods = doc.createElement("ModuleSim");
            rootElem.appendChild(mods);

            for (BaseModule m : modules) {
                Element modElem = doc.createElement("module");
                modElem.setAttribute("ID", "" + m.getID());
                modElem.setAttribute("type", m.getModType().name());

                // Dimensions
                Element dim = doc.createElement("dim");
                dim.setAttribute("x", "" + m.pos.x);
                dim.setAttribute("y", "" + m.pos.y);
                dim.setAttribute("orient", "" + m.orientation);
                modElem.appendChild(dim);

                // HAX: See XMLReader for an explanation of what's going on here
                ArrayList<Port> moduleInputs = new ArrayList<>();
                moduleInputs.addAll(m.inputs);
                ArrayList<Port> moduleOutputs = new ArrayList<>();
                moduleOutputs.addAll(m.outputs);

                for (BidirPort p : m.bidirs) {
                    if (p.side == 1) {
                        moduleInputs.add(p);
                    }
                    else {
                        moduleOutputs.add(p);
                    }
                }

                // Inputs (i.e. ports on the input side)
                Element inputs = doc.createElement("inputs");

                for (Port inputEdgePort : moduleInputs) {
                    Element e = doc.createElement("input");
                    e.setAttribute("ID", "" + inputEdgePort.getID());
                    inputs.appendChild(e);
                }

                modElem.appendChild(inputs);

                // Outputs (i.e. ports on the output side)
                Element outputs = doc.createElement("outputs");

                for (Port outputEdgePort : moduleOutputs) {
                    Element e = doc.createElement("output");
                    e.setAttribute("ID", "" + outputEdgePort.getID());
                    outputs.appendChild(e);
                }

                modElem.appendChild(outputs);

                // Data - stored only if the module's dataOut override indicates a modification has been made
                Element data = doc.createElement("data");
                HashMap<String, String> dataMap = m.dataOut();
                if (dataMap != null) {
                    for (String key : dataMap.keySet()) {
                        data.setAttribute(key, dataMap.get(key));
                    }
                    modElem.appendChild(data);
                }

                mods.appendChild(modElem);
            }

            // Store the links
            Element linksElem = doc.createElement("links");
            rootElem.appendChild(linksElem);

            for (Link l : links) {
                Element lElem = doc.createElement("link");
                lElem.setAttribute("src", "" + l.src.ID);
                lElem.setAttribute("targ", "" + l.targ.ID);
                lElem.setAttribute("type", l.path.XMLTagName());

                // Curve points
                for (CtrlPt c : l.path.ctrlPts) {
                    Element point = doc.createElement("ctrlPt");
                    point.setAttribute("x", "" + c.pos.x);
                    point.setAttribute("y", "" + c.pos.y);
                    lElem.appendChild(point);
                }

                linksElem.appendChild(lElem);
            }
        }
        return doc;
    }
}
