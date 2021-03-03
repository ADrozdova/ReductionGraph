package com.toposat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Scanner;
import java.util.Vector;

public class ProcessorFiles {
    // parsing from .graphml to Graph
    public static void extractGraph(Element root, ComplexSimplicialAbstract complex) {
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
                    complex.m_graph.addVertex(v);
                    complex.m_verticesGraph.put(label, v);
                    ++id;
                }
                if (node.getNodeName().equals("edge")) {
                    NEdge e = new NEdge(eElement.getAttribute("id"), cnt, cnt + 1);
                    complex.m_graph.addEdge(
                            complex.m_verticesGraph.get(eElement.getAttribute("source")),
                            complex.m_verticesGraph.get(eElement.getAttribute("target")), e);
                    cnt += 2;
                }
                extractGraph(eElement, complex);
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
    public static void readResultFileSMTLIB(String filename, SolverResult m_Result) {
        m_Result.m_vecStr_TrueVariables = new Vector<>();
        m_Result.m_vecStr_FalseVariables = new Vector<>();

        try {
            File res = new File(filename);
            Scanner Reader = new Scanner(res);
            String line = Reader.nextLine();
            if (line.equals("sat")) {
//                System.out.println("Unsatisfiable formula");
                m_Result.m_SAT = true;
                m_Result.m_UNSAT = false;
            } else if (line.equals("unsat")) {
                m_Result.m_UNSAT = true;
                m_Result.m_SAT = false;
                return;
            } else {
                System.out.println("Bad format");
                return;
            }
            while (Reader.hasNextLine()) {
                line = Reader.nextLine();
                if (line.contains("define-fun")) {
                    String variable = line.replace("  (define-fun ", "");
                    variable = variable.replace(" () Bool", "");
                    line = Reader.nextLine();
                    if (line.contains("true")) {
                        m_Result.m_vecStr_TrueVariables.addElement(variable);
                    }
                    if (line.contains("false")) {
                        m_Result.m_vecStr_FalseVariables.addElement(variable);
                    }

                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // writing .cnf file in dimacs
    public static void writeDimacsCNF(NodeFormula root, String filename) {
        numberVariables nv = new numberVariables();
        nv.declareVariablesCNF(root);
        try {
            FileWriter Writer = new FileWriter(filename);
            Writer.write("p cnf " + nv.getVarcnt() + " " + nv.getClcnt() + "\n");
            treeWalkCNFdimacs(root, Writer);
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

    // ====================================================================
    // Prepare file before introducing new variables
    static void cutFileTail(String filepathSMTLIB) {
        cutFileTail(filepathSMTLIB,24);
    }

    private static void cutFileTail(String filepathSMTLIB, int sizeTail) {
        try {
            RandomAccessFile file = new RandomAccessFile(filepathSMTLIB, "rw");
            long length = file.length();
            // ending is 24 symbols:
            // (check-sat)
            // (get-model)
            file.setLength(length - sizeTail);
            file.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    // ====================================================================

    // ====================================================================
    // CNF serializer
    // recursive writer of formula from a tree(for CNF) in dimacs
    public static int treeWalkCNFdimacs(NodeFormula root, FileWriter Writer) throws IOException {
        int was_zero = 0;
        if (root == null) {
            return 1;
        }
        if (root.operation == TypeOperation.variable) {
            Writer.write(root.var + " ");
        }
        int last = treeWalkCNFdimacs(root.left, Writer);
        if ((root.operation == TypeOperation.conjunction) && (last == 0)) {
            Writer.write("0\n");
            was_zero = 1;
        }
        last = treeWalkCNFdimacs(root.right, Writer);
        if ((root.operation == TypeOperation.conjunction) && (last == 0)) {
            Writer.write("0\n");
            was_zero = 1;
        }
        if (root.operation == TypeOperation.conjunction) {
            was_zero = 1;
        }
        return was_zero;
    }
    // ====================================================================

}
