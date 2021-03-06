package com.toposat;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;


public class ReductionSimplicialComplex {

    // ====================================================================
    // Configuration
    // -e solver -p solverPath graphFile
    static String m_strUsage =
            """
                    Usage: java -jar ReductionGraph.jar [OPTION]... [FILE]...Options:
                        -e [SOLVER]     where SOLVER = painless|z3
                        -p [PATH TO SOLVER]
                        --path-to-engine [PATH TO SOLVER]
                    """;

    static String m_solverName = "default";
    static String m_solverPath = "";
    static String m_filepathGraphInput = "";
    // End of: Configuration
    // ====================================================================

    // ====================================================================
    // Static members
    static private NodeFormula m_nodeFormulaRoot = new NodeFormula();
    static private final ComplexSimplicialAbstract m_complexAS = new ComplexSimplicialAbstract();

    static private Visitor m_visitorCurrent;
    private static Solver m_solver;

    // End of: Static members
    // ====================================================================
    // ====================================================================
    // Getters/Setters
    public NodeFormula getRoot() {
        return m_nodeFormulaRoot;
    }
    public void setRoot(NodeFormula newRoot) {
        m_nodeFormulaRoot = newRoot;
    }

    public Visitor getVisitor() {
        return m_visitorCurrent;
    }
    public void setVisitor(Visitor newVisitor) {
        m_visitorCurrent = newVisitor;
    }
    // End of: Getters/Setters
    // ====================================================================

    // ====================================================================
    // Initialize
    private static void initialize() throws ParserConfigurationException, IOException, SAXException {
        Element docElement = ProcessorFiles.readXMLFile(m_filepathGraphInput);
        ProcessorFiles.extractGraph(docElement, m_complexAS);

        m_nodeFormulaRoot.operation = TypeOperation.conjunction;

        if (m_solverName.equals("z3"))
        {
            m_solver = new SolverZ3(m_solverPath);
        } else {
            // "default" "painless"
            m_solver = new SolverPainless(m_solverPath);
        }

    }
    // End of: Initialize
    // ====================================================================


    // ====================================================================
    // Main entry point
    public static void out(String str) {
        System.out.println(str);
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, InterruptedException {

        parseArguments(args);

        //TODO:remove
        //m_filepathGraphInput = "Graphs/4-8-tetrahedron.graphml";
        m_filepathGraphInput = "Graphs/Full8.graphml";

        // Initialization
        initialize();

        if (true) {
            //VisitorNCycle myVisitor = new VisitorNCycle();
            VisitorMaxCliqueSearchZ3 viz = new VisitorMaxCliqueSearchZ3(m_solver);
            viz.visitGraph(m_nodeFormulaRoot, m_complexAS);

            out("Good");
            return;
        }

        VisitorCliqueSearch myVisitor = new VisitorCliqueSearch();
        myVisitor.cliqueSize = 3;

        m_complexAS.traverseGraphNodes(myVisitor, m_nodeFormulaRoot);
        m_complexAS.traverseGraphEdges(myVisitor, m_nodeFormulaRoot);
        m_complexAS.traverseGraphNonEdges(myVisitor, m_nodeFormulaRoot);
        m_complexAS.traverseGraph(myVisitor, m_nodeFormulaRoot);

        m_solver.Solve(m_nodeFormulaRoot);

        if (m_solver.getName().equals("painless"))
        {
            Vector<Integer> trueVar = m_solver.getResultsIntegerVector();
            if (trueVar!=null && !trueVar.isEmpty()) {
                for (int v : trueVar) {
                    System.out.println(v);
                }
            }
        }

        if (m_solver.getName().equals("z3"))
        {
            Vector<Vector<String>> results = solveALLSATZ3();
            System.out.println(results.size());
            int i = 0;
            for (Vector<String> trueVar : results) {
                System.out.println("Solution " + i);
                for (String v : trueVar) {
                    String a = v.split("_")[0];
//                    System.out.println(a);
                    if (m_complexAS.m_verticesGraph.containsKey(a)) {
                        System.out.println(v + " " + m_complexAS.m_verticesGraph.get(a).getGMLLabel());
                    }
                }
                ++i;
            }

            // Simple SAT test

//            Vector<String> trueVar = m_solver.getStringVectorTrueVariables();
//            System.out.println("True variables:");
//            for(String v : trueVar) {
//                String a = v.split("_")[0];
//                System.out.println(a);
//                if (m_nodesGraph.containsKey(a)) {
//                    System.out.println(v + " " + m_nodesGraph.get(a).getName());
//                }
//            }

            // Simple ALL-CLIQUE test

//            Vector<Vector<String>> results = AllCliqueZ3();
//            System.out.println(results.size());
//            int i = 0;
//            for (Vector<String> trueVar : results) {
//                System.out.println("Solution " + i);
//                for (String v : trueVar) {
//                    String a = v.split("_")[0];
////                    System.out.println(a);
//                    if (m_nodesGraph.containsKey(a)) {
//                        System.out.println(v + " " + m_nodesGraph.get(a).getGMLLabel());
//                    }
//                }
//                ++i;
//            }

        }

    }
    // End of: Main entry point
    // ====================================================================

    // ====================================================================
    // Arguments processing
    private static void parseArguments(String[] args) {
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

                m_solverName = args[i + 1].toLowerCase();
                ++i; // pass argument

                if (!(m_solverName.equals("painless") || m_solverName.equals("z3"))) {
                    System.out.println("Solver " + m_solverName + " is not supported.");
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
                ++i; // pass argument
                m_solverPath = args[i];
                continue;
            }

            if (i != args.length - 1) {
                System.out.println("Too many arguments.");
                System.out.println(m_strUsage);
                return;
            }
            m_filepathGraphInput = args[i];
        }

        if (m_filepathGraphInput.equals("")) {
            System.out.println("RG: path to input file is not defined.");
            System.out.println(m_strUsage);
            return;
        }

        if (m_solverPath.equals("")) {
            System.out.println("RG: path to solver not defined.");
            System.out.println(m_strUsage);
            return;
        }

        if (m_solverName.equals("default")) {
            System.out.println("Warning: 'painless' is selected as a solver by default.");
        }
    }
    // End of: Arguments processing
    // ====================================================================

    // ====================================================================
    private static void writePermutation(BufferedWriter bw, String[] elements, int n) throws IOException {
        bw.write("(assert (or ");
        for (int i = 1; i <= n; ++i) {
            bw.write("(not " + elements[i-1] + "_" + i + " ) ");
        }
        bw.write("))\n");
    }

    private static void getPermutationsHeap(BufferedWriter bw, String[] elements, int n) throws IOException {
        int[] indexes = new int[n];
        for (int i = 0; i < n; i++) {
            indexes[i] = 0;
        }

        writePermutation(bw, elements, n);

        int i = 0;
        while (i < n) {
            if (indexes[i] < i) {
                Auxiliary.swap(elements, i % 2 == 0 ?  0: indexes[i], i);
                writePermutation(bw, elements, n);
                indexes[i]++;
                i = 0;
            }
            else {
                indexes[i] = 0;
                i++;
            }
        }
    }

    // ====================================================================
    // Solve ALL-SAT problem with Z3
    public static Vector<Vector<String>> solveALLSATZ3() throws IOException, InterruptedException {

        m_solver.Solve(m_nodeFormulaRoot);
        Vector<String> trueVar = m_solver.getNamesTrueVariables();
        Vector<String> falseVar = m_solver.getNamesFalseVariables();

        Vector<Vector<String>> solutions = new Vector<>();

        String inputFilepath = m_solver.getInputFilepath();

        int numSolutions = 0;
        while (!trueVar.isEmpty() || !falseVar.isEmpty()) {
            ++numSolutions;
            solutions.addElement(trueVar);

            ProcessorFiles.cutFileTail(inputFilepath);

            FileWriter fw = new FileWriter(inputFilepath, true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("(assert (or ");
            for (String var : trueVar) {
                bw.write("(not " + var + " ) ");
            }
            for (String var : falseVar) {
                bw.write(var + " ");
            }

            bw.write("))\n");
            bw.write("(check-sat)\n");
            bw.write("(get-model)\n");
            bw.close();

            m_solver.SolveContinue();
            trueVar = m_solver.getNamesTrueVariables();
            falseVar = m_solver.getNamesFalseVariables();

        }
        return solutions;
    }
    // ====================================================================

    // ====================================================================
    public static Vector<Vector<String>> solveAllCliqueZ3() throws IOException, InterruptedException {

        m_solver.Solve(m_nodeFormulaRoot);

        Vector<String> trueVar = m_solver.getNamesTrueVariables();
        Vector<Vector<String>> solutionsAll = new Vector<>();

        while (!trueVar.isEmpty()) {
            solutionsAll.addElement(trueVar);

            String inputFile = m_solver.getInputFilepath();
            ProcessorFiles.cutFileTail(inputFile);
            FileWriter fw = new FileWriter(inputFile, true);
            BufferedWriter bw = new BufferedWriter(fw);

//            if (trueVar.size() > n) {
//                System.out.println("here " + n + " " + trueVar.size());
//                for (int i = trueVar.size() - 1; i > n - 1; --i) {
//                    trueVar.remove(i);
//                }
//                System.out.println("and here " + n + " " + trueVar.size());
//            }

            String[] variables = new String[trueVar.size() ];
            for (int i = 0; i < trueVar.size() ; ++i) {
                variables[i] = trueVar.get(i).split("_")[0];
            }

            getPermutationsHeap(bw, variables, trueVar.size() );

            bw.write("(check-sat)\n");
            bw.write("(get-model)\n");
            bw.close();

            m_solver.SolveContinue();

            trueVar = m_solver.getNamesTrueVariables();
        }
        return solutionsAll;
    }
    // ====================================================================

    void callVisitorNCycle()
    {

    }

}
