package com.toposat;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


public class ReductionGraph {
    // parsing from .graphml to Graph
    public static void getGraph(Element root, LinkedHashMap<String, NVertex> nodes, Graph<NVertex, NEdge> graph) {
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
                getGraph(eElement, nodes, graph);
            }
        }
    }

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

    // writing .cnf file in dimacs

    public static void writeDimacsCNF(NodeFormula root, String filename) {
        numberVariables nv = new numberVariables();
        nv.declareVariablesCNF(root);
        try {
            File formulaFile = new File(filename);
            formulaFile.createNewFile();
            FileWriter Writer = new FileWriter(filename);
            Writer.write("p cnf " + nv.getVarcnt() + " " + nv.getClcnt() + "\n");
            treeWalkCNFdimacs(root, Writer);
            Writer.close();
        } catch (IOException e) {
            System.out.println("An error occured.");
            e.printStackTrace();
        }
    }

    // adds clauses to formula tree

    public static void traverseGraphNodes(Visitor myVisitor) {
        Set<NVertex> vertices = m_graph.vertexSet();
        for (NVertex vertex : vertices) {
            NodeFormula placeCurrent = TseytinTransformation.findPlace(m_root);
            myVisitor.visitNode(placeCurrent, vertex, m_graph);
        }
    }

    public static void traverseGraphEdges(Visitor myvisitor) {
        Set<NEdge> edges = m_graph.edgeSet();
        for (NEdge edge : edges) {
            NodeFormula placeCurrent = TseytinTransformation.findPlace(m_root);
            myvisitor.visitEdge(placeCurrent, edge, m_graph);
        }
    }

    public static void traverseGraphNonEdges(Visitor myVisitor) {
        Set<NVertex> vertices = m_graph.vertexSet();
        for (NVertex first : vertices) {
            for (NVertex second : vertices) {
                if (first.getId() < second.getId()) {
                    if (m_graph.getAllEdges(first, second).isEmpty()) {
                        NodeFormula placeCurrent = TseytinTransformation.findPlace(m_root);
                        myVisitor.visitNonEdge(placeCurrent, first, second, m_graph);
                    }
                }
            }
        }
    }

    public static void traverseGraph(Visitor myVisitor) {
        NodeFormula placeCurrent = TseytinTransformation.findPlace(m_root);
        myVisitor.visitGraph(placeCurrent, m_graph);
    }

    // reads result file with SAT solver ansver and gets true variables
    public static Vector<Integer> getAnsDimacs(String filename) {
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

    public static Vector<String> getAnsSMT(String filename) {
        Vector<String> true_var = new Vector<>();
        try {
            File res = new File(filename);
            Scanner Reader = new Scanner(res);
            String line = Reader.nextLine();
            if (line.equals("unsat")) {
                System.out.println("Unsatisfiable formula");
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

    static public Element getDocRoot(String filename) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(filename));
        return document.getDocumentElement();
    }

    public static void solveSMTZ3(String path, String resultFile, String questionFile) throws IOException, InterruptedException {
        File result = new File(resultFile);
        result.createNewFile();
        ProcessBuilder b = new ProcessBuilder(path, "-smt2", questionFile);
        b.redirectOutput(result);
        Process p = b.start();
        p.waitFor();
    }

    public static void solveCNFPainless(String path, String resultFile, String questionFile) throws IOException, InterruptedException {
        File result = new File(resultFile);
        result.createNewFile();
        ProcessBuilder b = new ProcessBuilder(path, questionFile);
        b.redirectOutput(result);
        Process p = b.start();
        p.waitFor();
    }

    ///home/nastya/Documents/hse19-20/pReductionGraph/graph_examples/graph2.graphml

    // -e solver -p solverPath graphFile
    static String m_strUsage = "Usage: java -jar ReductionGraph.jar [OPTION]... [FILE]..." +
            "Options:\n" + "\t-e [SOLVER]\twhere SOLVER = painless|z3\n" +
            "\t-p [PATH TO SOLVER]\t\t\t--path-to-engine [PATH TO SOLVER]\n";
    static String m_solver = "default";
    static String m_solverPath = "";
    static String m_fileGraphInput = "";

    static private NodeFormula m_root;
    public NodeFormula getRoot() {
        return m_root;
    }
    public void setRoot(NodeFormula newRoot) { m_root = newRoot; }

    static private Graph<NVertex, NEdge> m_graph;
    public Graph<NVertex, NEdge> getGraph() {
        return m_graph;
    }
    public void setGraph(Graph<NVertex, NEdge> newGraph) {
        m_graph = newGraph;
    }

    static private Visitor m_visitor;
    public Visitor getVisitor() {
        return m_visitor;
    }
    public void setVisitor(Visitor newVisitor) {
        m_visitor = newVisitor;
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, InterruptedException {
        if (args.length == 0) {
            System.out.println(m_strUsage);
            return;
        }
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equals("-e")) {
                if (i + 1 == args.length) {
                    System.out.println("RG: option requires an argument -- '-e'");
                    System.out.println(m_strUsage);
                    return;
                }

                m_solver = args[i + 1].toLowerCase();
                ++i; // pass argument

                if (!(m_solver.equals("painless") || m_solver.equals("z3"))) {
                    System.out.println("Solver " + m_solver + " is not supported.");
                    System.out.println(m_strUsage);
                    return;
                }
                continue;
            }

            if (args[i].equals("-p")) {
                if (i + 1 == args.length) {
                    System.out.println("RG: option requires an argument -- '-p'");
                    System.out.println(m_strUsage);
                    return;
                }
                m_solverPath = args[i + 1];
                ++i; // pass argument
                continue;
            }

            if (i != args.length - 1)
            {
                System.out.println("Too many arguments.");
                System.out.println(m_strUsage);
                return;
            }
            m_fileGraphInput = args[i];

        }

        if (m_fileGraphInput.equals("")) {
            System.out.println("RG: path to input file is not defined.");
            System.out.println(m_strUsage);
            return;
        }

        if (m_solverPath.equals("")) {
            System.out.println("RG: path to solver not defined.");
            System.out.println(m_strUsage);
            return;
        }

        if (m_solver.equals("default")) {
            System.out.println("Warning: 'painless' is selected as a solver by default.");
        }

        m_graph = new DefaultUndirectedGraph<>(NEdge.class);
        LinkedHashMap<String, NVertex> nodes = new LinkedHashMap<>();

        getGraph(getDocRoot(m_fileGraphInput), nodes, m_graph);

        m_root = new NodeFormula();
        m_root.operation = TypeOperation.conjunction;


        CSVisitor myVisitor = new CSVisitor();
        myVisitor.cliqueSize = 3;

        traverseGraphNodes(myVisitor);
        traverseGraphEdges(myVisitor);
        traverseGraphNonEdges(myVisitor);
        traverseGraph(myVisitor);

        if (m_solver.equals("painless")) {
            String questionFile = "newFormulaCliqueCnf.cnf";
            writeDimacsCNF(m_root, questionFile);
            String resultFile = "resClique.sat";
            solveCNFPainless(m_solverPath, resultFile, questionFile);
            Vector<Integer> trueVar = getAnsDimacs(resultFile);

            if (!trueVar.isEmpty()) {
                for (int v : trueVar) {
                    System.out.println(v);
                }
            }
        }
        if (m_solver.equals("z3")) {
            String questionFile = "newFormulaChrSmt.cnf";
            TseytinTransformation.writeSmtCNF(m_root, questionFile);
            String path = args[1];
            String resultFile = "resChr.sat";
            solveSMTZ3(path, resultFile, questionFile);
            Vector<String> trueVar = getAnsSMT(resultFile);
            System.out.println("True variables:");
            for(String v : trueVar) {
                String a = v.split("_")[0];
                System.out.println(a);
                if (nodes.containsKey(a)) {
                    System.out.println(v + " " + nodes.get(a).getName());
                }
            }
        }
    }
}

