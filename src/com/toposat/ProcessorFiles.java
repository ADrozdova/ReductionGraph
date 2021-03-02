package com.toposat;

import org.jgrapht.Graph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Vector;

public class ProcessorFiles {
    // parsing from .graphml to Graph
    public static void extractGraph(Element root, LinkedHashMap<String, NVertex> nodes, Graph<NVertex, NEdge> graph) {
        NodeList cList = root.getChildNodes();
        int cnt = 1;
        int id = 1;
        for (int i = 0; i < cList.getLength(); ++i) {
            org.w3c.dom.Node node = cList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                if (node.getNodeName().equals("node")) {
                    String label = eElement.getAttribute("id");
                    NVertex v;
                    if (eElement.getElementsByTagName("y:NodeLabel").getLength() == 0) {
                        v = new NVertex(label, id, "");
                    } else {
                        v = new NVertex(label, id, eElement.getElementsByTagName("y:NodeLabel").item(0).getTextContent());
                    }
                    graph.addVertex(v);
                    nodes.put(label, v);
                    ++id;
                }
                if (node.getNodeName().equals("edge")) {
                    NEdge e = new NEdge(eElement.getAttribute("id"), cnt, cnt + 1);
                    graph.addEdge(nodes.get(eElement.getAttribute("source")), nodes.get(eElement.getAttribute("target")), e);
                    cnt += 2;
                }
                extractGraph(eElement, nodes, graph);
            }
        }
    }

    // reads result file with SAT solver ansver and gets true variables
    public static Vector<Integer> readResultFileDimacs(String filename) {
        Vector<Integer> true_var = new Vector<>();
        try {
            File res = new File(filename);
            Scanner Reader = new Scanner(res);
            while (Reader.hasNextLine()) {
                String line = Reader.nextLine();
                String[] arr = line.split(" ");
                if (arr[0].equals("s")) {
                    if (arr[1].equals("UNSATISFIABLE")) {
                        System.out.println("Unsatisfiable formula");
                        return true_var;
                    }
                }
                if (arr[0].equals("v")) {
                    for (int i = 1; i < arr.length; ++i) {
                        int elem = Integer.parseInt(arr[i]);
                        if (elem > 0) {
                            true_var.addElement(elem);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return true_var;
    }

    // parse result file smt - getting true variables
    public static Vector<String> readResultFileSMTLIB(String filename) {
        Vector<String> true_var = new Vector<>();
        try {
            File res = new File(filename);
            Scanner Reader = new Scanner(res);
            String line = Reader.nextLine();
            if (line.equals("unsat")) {
//                System.out.println("Unsatisfiable formula");
                return true_var;
            }
            if (!line.equals("sat")) {
                System.out.println("Bad format");
                return true_var;
            }
            while (Reader.hasNextLine()) {
                line = Reader.nextLine();
                if (line.contains("define-fun")) {
                    String variable = line.replace("  (define-fun ", "");
                    variable = variable.replace(" () Bool", "");
                    line = Reader.nextLine();
                    if (line.contains("true")) {
                        true_var.addElement(variable);
                    }

                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return true_var;
    }

    // writing .cnf file in dimacs
    public static void writeDimacsCNF(NodeFormula root, String filename) {
        numberVariables nv = new numberVariables();
        nv.declareVariablesCNF(root);
        try {
            File formulaFile = new File(filename);
            formulaFile.createNewFile();
            FileWriter Writer = new FileWriter(filename);
            Writer.write("p cnf " + nv.getVarcnt() + " " + nv.getClcnt() + "\n");
            ReductionGraph.treeWalkCNFdimacs(root, Writer);
            Writer.close();
        } catch (IOException e) {
            System.out.println("An error occured.");
            e.printStackTrace();
        }
    }

    static public Element readXMLFile(String filename) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(filename));
        return document.getDocumentElement();
    }

    // Read SMTLIB file and extract false variables
    public static Vector<String> extractFalseVarsFromFileSMT(String filename) {
        Vector<String> false_var = new Vector<>();
        try {
            File res = new File(filename);
            Scanner Reader = new Scanner(res);
            String line = Reader.nextLine();
            if (line.equals("unsat")) {
//                System.out.println("Unsatisfiable formula");
                return false_var;
            }
            if (!line.equals("sat")) {
                System.out.println("Bad format");
                return false_var;
            }
            while (Reader.hasNextLine()) {
                line = Reader.nextLine();
                if (line.contains("define-fun")) {
                    String variable = line.replace("  (define-fun ", "");
                    variable = variable.replace(" () Bool", "");
                    line = Reader.nextLine();
                    if (line.contains("false")) {
                        false_var.addElement(variable);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return false_var;
    }
}
