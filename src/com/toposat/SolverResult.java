package com.toposat;

import java.util.Vector;

public class SolverResult {
    public boolean m_SAT = false;
    public boolean m_UNSAT = false;
    public Vector<Integer> m_vecInt_TrueVariables;
    public Vector<String> m_vecStr_TrueVariables;
    public Vector<String> m_vecStr_FalseVariables;
}
