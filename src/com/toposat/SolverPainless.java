package com.toposat;

import java.io.IOException;
import java.util.Vector;

public class SolverPainless implements Solver {

    // ====================================================================
    // Fields
    private String m_solverPath = "";
    private Vector<Integer> m_trueVar;
    private String m_inputFilepath = "painlessInputFormulaCNF.cnf";
    private String m_resultFilepath = "painlessResult.sat";
    // ====================================================================

    // ====================================================================
    // Constructors
    public SolverPainless(String solverPath) {
        this.m_solverPath = solverPath;
    }
    // ====================================================================

    // ====================================================================
    // Getters/Setters
    public void setSolverPath(String solverPath) {
        this.m_solverPath = solverPath;
    }
    @Override
    public String getInputFilepath() {
        return null;
    }
    @Override
    public String getResultFilepath() {
        return null;
    }
    @Override
    public String getName() {
        return "painless";
    }
    // ====================================================================

    @Override
    public void Solve(NodeFormula nodeFormulaRoot) throws IOException, InterruptedException {
        ProcessorFiles.writeDimacsCNF(nodeFormulaRoot, m_inputFilepath);
        SolveContinue();
    }

    @Override
    public void SolveContinue() throws IOException, InterruptedException {
        LauncherSATSolver.solveCNFPainless(m_solverPath, m_resultFilepath, m_inputFilepath);
        m_trueVar = ProcessorFiles.readResultFileDimacs(m_resultFilepath);
    }

    @Override
    public Vector<Integer> getResultsIntegerVector() {
        return m_trueVar;
    }

    @Override
    public Vector<String> getNamesFalseVariables() {

        return null;
    }

    @Override
    public Vector<String> getNamesTrueVariables() {

        return null;
    }

    @Override
    public SolverResult getResult() {
        return null;
    }
}
