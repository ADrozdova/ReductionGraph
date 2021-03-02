package com.toposat;

import java.io.IOException;
import java.util.Vector;

public interface Solver {
    String getInputFilepath();

    String getResultFilepath();

    String getName();

    void Solve(NodeFormula nodeFormulaRoot) throws IOException, InterruptedException;

    void SolveContinue() throws IOException, InterruptedException;

    Vector<Integer> getResultsIntegerVector();

    Vector<String> getResultsStringVector();

    Vector<String> getStringVectorFalseVariables();

    Vector<String> getStringVectorTrueVariables();
}
