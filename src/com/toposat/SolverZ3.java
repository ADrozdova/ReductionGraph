package com.toposat;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class SolverZ3 implements Solver {

    private String m_solverPath;

    private SolverResult m_Result = new SolverResult();

    //private Vector<String> m_vecStr_TrueVariables;
    //private Vector<String> m_vecStr_FalseVariables;

    private String m_inputFilepath;
    private String m_resultFilepath;

    @Override
    public String getInputFilepath() {
        return m_inputFilepath;
    }
    @Override
    public String getResultFilepath() {
        return m_resultFilepath;
    }



    public SolverZ3(String solverPath) {
        this.m_solverPath = solverPath;
    }
    public void setSolverPath(String solverPath) {
        this.m_solverPath = solverPath;
    }


    @Override
    public String getName() {
        return "z3";
    }

    @Override
    public void Solve(NodeFormula nodeFormulaRoot) throws IOException, InterruptedException {
        m_inputFilepath = "z3InputFormulaCNF.smtlib";
        m_resultFilepath = "z3Result.z3";

        TseytinTransformation.writeSmtCNF(nodeFormulaRoot, m_inputFilepath);

        SolveContinue();

//            solveSMTZ3(path, resultFile, questionFile);
//            Vector<String> trueVar = getAnsSMT(resultFile);
//            System.out.println("True variables:");
//            for(String v : trueVar) {
//                String a = v.split("_")[0];
//                System.out.println(a);
//                if (nodes.containsKey(a)) {
//                    System.out.println(v + " " + nodes.get(a).getName());
//                }
//            }

//            Vector<Vector<String>> results = AllCliqueZ3(path, resultFile, questionFile);
//            System.out.println(results.size());
//            int i = 0;
//            for (Vector<String> trueVar : results) {
//                System.out.println("Solution " + i);
//                for (String v : trueVar) {
//                    String a = v.split("_")[0];
////                    System.out.println(a);
//                    if (nodes.containsKey(a)) {
//                        System.out.println(v + " " + nodes.get(a).getName());
//                    }
//                }
//                ++i;
//            }
    }

    @Override
    public void SolveContinue() throws IOException, InterruptedException {
        File fileResult = new File(m_resultFilepath);
        fileResult.createNewFile();
        ProcessBuilder b = new ProcessBuilder(m_solverPath, "-smt2", m_inputFilepath);
        b.redirectOutput(fileResult);
        Process p = b.start();
        p.waitFor();
        ProcessorFiles.readResultFileSMTLIB(m_resultFilepath, m_Result);
    }

    @Override
    public Vector<Integer> getResultsIntegerVector() {
        return null;
    }

    @Override
    public Vector<String> getNamesFalseVariables() {
        return m_Result.m_vecStr_FalseVariables;
    }

    @Override
    public Vector<String> getNamesTrueVariables() {
        return m_Result.m_vecStr_TrueVariables;
    }

    @Override
    public SolverResult getResult() {
        return m_Result;
    }
}
