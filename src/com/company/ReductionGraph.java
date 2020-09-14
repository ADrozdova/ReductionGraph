package com.company;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import static com.company.TseytinTransformation.*;


public class ReductionGraph {
    // parsing from .graphml to Graph
    static void getGraph(Element root, LinkedHashMap<String, NVertex> nodes, Graph<NVertex, NEdge> graph){
        NodeList cList = root.getChildNodes();
        int cnt = 1;
        int id = 1;
        for(int i = 0; i < cList.getLength(); ++i) {
            org.w3c.dom.Node node = cList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                if(node.getNodeName().equals("node")){
                    String label = eElement.getAttribute("id");
                    NVertex v;
                    if(eElement.getElementsByTagName("y:NodeLabel").getLength() == 0){
                        v = new NVertex(label, id, "");
                    } else {
                        v = new NVertex(label, id, eElement.getElementsByTagName("y:NodeLabel").item(0).getTextContent());
                    }
                    graph.addVertex(v);
                    nodes.put(label, v);
                    ++id;
                }
                if(node.getNodeName().equals("edge")){
                    NEdge e = new NEdge(eElement.getAttribute("id"), cnt, cnt+1);
                    graph.addEdge(nodes.get(eElement.getAttribute("source")), nodes.get(eElement.getAttribute("target")), e);
                    cnt += 2;
                }
                getGraph(eElement, nodes, graph);
            }
        }
    }
    // recursive writer of formula from a tree(for CNF) in dimacs
    static int treeWalkCNFdimacs(NodeFormula root, FileWriter Writer) throws IOException {
        int was_zero = 0;
        if(root == null){
            return 1;
        }
        if(root.operation == TypeOperation.variable){
            Writer.write(root.var + " ");
        }
        int last = treeWalkCNFdimacs(root.left, Writer);
        if((root.operation == TypeOperation.conjunction) && (last == 0)){
            Writer.write("0\n");
            was_zero = 1;
        }
        last = treeWalkCNFdimacs(root.right, Writer);
        if((root.operation == TypeOperation.conjunction) && (last == 0)){
            Writer.write("0\n");
            was_zero = 1;
        }
        if(root.operation == TypeOperation.conjunction){
            was_zero = 1;
        }
        return was_zero;
    }

    // writing .cnf file in dimacs

    static void writeDimacsCNF(NodeFormula root, String filename)  {
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

    static void traverseGraphNodes(NodeFormula root, Graph<NVertex, NEdge> graph, Visitor myVisitor){
        Set<NVertex> vertices = graph.vertexSet();
        for(NVertex vertex : vertices) {
            NodeFormula placeCurrent = findPlace(root);
            myVisitor.visitNode(placeCurrent, vertex, graph);
        }
    }

    static void traverseGraphEdges(NodeFormula root, Graph<NVertex, NEdge> graph, Visitor myvisitor){
        Set<NEdge> edges = graph.edgeSet();
        for(NEdge edge : edges) {
            NodeFormula placeCurrent = findPlace(root);
            myvisitor.visitEdge(placeCurrent, edge, graph);
        }
    }

    static void traverseGraphNonEdges(NodeFormula root, Graph<NVertex, NEdge> graph, Visitor myVisitor){
        Set<NVertex> vertices = graph.vertexSet();
        for(NVertex first : vertices) {
            for(NVertex second : vertices){
                if(first.getId() < second.getId()){
                    if(graph.getAllEdges(first, second).isEmpty()){
                        NodeFormula placeCurrent = findPlace(root);
                        myVisitor.visitNonEdge(placeCurrent, first, second, graph);
                    }
                }
            }
        }
    }

    static void traverseGraph(NodeFormula root, Graph<NVertex, NEdge> graph, Visitor myVisitor){
        NodeFormula placeCurrent = findPlace(root);
        myVisitor.visitGraph(placeCurrent, graph);
    }

    // reads result file with SAT solver ansver and gets true variables
    static Vector<Integer> getAnsDimacs(String filename){
        Vector<Integer> true_var = new Vector<>();
        try{
            File res = new File(filename);
            Scanner Reader = new Scanner(res);
            while(Reader.hasNextLine()){
                String line = Reader.nextLine();
                String[] arr = line.split(" ");
                if(arr[0].equals("s")){
                    if(arr[1].equals("UNSATISFIABLE")){
                        System.out.println("Unsatisfiable formula");
                        return true_var;
                    }
                }
                if(arr[0].equals("v")){
                    for (int i = 1; i < arr.length; ++i) {
                        int elem = Integer.parseInt(arr[i]);
                        if (elem > 0) {
                            true_var.addElement(elem);
                        }
                    }
                }
            }
        } catch(FileNotFoundException e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return true_var;
    }

    static Vector<String> getAnsSMT(String filename){
        Vector<String> true_var = new Vector<>();
        try{
            File res = new File(filename);
            Scanner Reader = new Scanner(res);
            String line = Reader.nextLine();
            if(line.equals("unsat")){
                System.out.println("Unsatisfiable formula");
                return true_var;
            }
            if(!line.equals("sat")){
                System.out.println("Bad format");
                return true_var;
            }
            while(Reader.hasNextLine()){
                line = Reader.nextLine();
                if(line.contains("define-fun")){
                    String variable = line.replace("  (define-fun ", "");
                    variable = variable.replace(" () Bool", "");
                    line = Reader.nextLine();
                    if(line.contains("true")){
                        true_var.addElement(variable);
                    }

                }
            }
        } catch(FileNotFoundException e){
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

    static void solveSMTZ3(String path, String resultFile, String questionFile) throws IOException, InterruptedException {
        File result = new File(resultFile);
        result.createNewFile();
        ProcessBuilder b = new ProcessBuilder(path,"-smt2", questionFile);
        b.redirectOutput(result);
        Process p = b.start();
        p.waitFor();
    }

    static void solveCNFPainless(String path, String resultFile, String questionFile) throws IOException, InterruptedException {
        File result = new File(resultFile);
        result.createNewFile();
        ProcessBuilder b = new ProcessBuilder(path, questionFile);
        b.redirectOutput(result);
        Process p = b.start();
        p.waitFor();
    }

    // gets args: 0 - path to .graphml, 1 - path to Z3 SMT/SAT solver
//    static public void main(String[] argv) throws IOException, SAXException, ParserConfigurationException, InterruptedException {
//        Graph<NVertex, NEdge> graph = new DefaultUndirectedGraph<>(NEdge.class);
//        LinkedHashMap<String, NVertex> nodes = new LinkedHashMap<>();
//
//
//        //String path1 = "/home/nastya/Documents/project/graph_examples/graph2.graphml";
//        getGraph(getDocRoot(argv[0]), nodes,  graph);
//
//        NodeFormula start = new NodeFormula();
//        start.operation = TypeOperation.conjunction;
//
//        VisitorDefault myVisitor = new VisitorDefault();
//        traverseGraphNodes(start, graph, myVisitor);
//        //traverseGraphEdges(start, graph, myVisitor);
//        //traverseGraphNonEdges(start, graph, myVisitor);
//        traverseGraph(start, graph, myVisitor);
//        //String questionFile = "newFormulaFile.cnf";
//
////        FileWriter Writer1 = new FileWriter("treeFileOne.txt");
////        TsTreeWalk(start, Writer1, 0);
////        Writer1.close();
//
//
////        String questionFile = "newFormulaFileSmt.cnf";
////        writeSmtCNF(start, questionFile);
//
//        String problemFile = "formula.cnf";
//        writeDimacsCNF(start, problemFile);
//
//
//        String path = "/home/nastya/Downloads/painless-v2/painless"; //argv[1];
//        String resultFile = "res.sat";
//
//        solveCNFPainless(path, resultFile, problemFile);
//
//        //solveSMTZ3(path, resultFile, questionFile);
//
//        Vector<Integer> trueVar = getAnsDimacs(resultFile);
//        for(Integer v : trueVar){
//            System.out.println(v);
//
//        }
////        Vector<String> true_var = getAnsSMT(resultFile);
////        //Vector<Integer> true_var = getAnsDimacs(result);
////        System.out.println("True variables:");
////        for(String v : true_var){
////            //String a = v.split("_")[0];
////            //System.out.println(a);
////            // if(nodes.containsKey(a)){
////            System.out.println(v);// + " " + nodes.get(a).getName());
////            //}
////        }
//    }



    // testing Painless usage

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, InterruptedException {
        Graph<NVertex, NEdge> graph = new DefaultUndirectedGraph<>(NEdge.class);
        LinkedHashMap<String, NVertex> nodes = new LinkedHashMap<>();

        getGraph(getDocRoot(args[0]), nodes,  graph);
        boolean done = false;
        int i = 3;
        while(!done){
            NodeFormula start = new NodeFormula();
            start.operation = TypeOperation.conjunction;
            CSVisitor myVisitor = new CSVisitor();
            myVisitor.cliqueSize = i;
            traverseGraphNodes(start, graph, myVisitor);
            traverseGraphEdges(start, graph, myVisitor);
            traverseGraphNonEdges(start, graph, myVisitor);
            traverseGraph(start, graph, myVisitor);

//            String questionFile = "newFormulaChrSmt.cnf";
//            writeSmtCNF(start, questionFile);
//            String path = args[1];
//            String resultFile = "resChr.sat";
//            solveSMTZ3(path, resultFile, questionFile);
//            Vector<String> trueVar = getAnsSMT(resultFile);

            String questionFile = "newFormulaCliqueCnf.cnf";
            writeDimacsCNF(start, questionFile);

            String path = "/home/nastya/Downloads/painless-v2/painless";
            String resultFile = "resClique.sat";
            solveCNFPainless(path, resultFile, questionFile);
            Vector<Integer> trueVar = getAnsDimacs(resultFile);

            if(!trueVar.isEmpty()){
                System.out.println("Clique size: " + i);
                System.out.println("In clique:");
                System.out.println("True variables:");
                for(int v : trueVar){
//                    String a = v.split("_")[0];
//                    if(nodes.containsKey(a)){
//                        System.out.println(v + " " + nodes.get(a).getName());
//                    }
                    System.out.println(v);
                }
                ++i;
            } else {
                System.out.println("No more cliques");
                done = true;
            }
        }
    }
}