package com.toposat;

import org.jgrapht.Graph;

import java.io.IOException;

public interface VisitorGraphToFormula extends Visitor{
    com.toposat.NodeFormula m_resFormula = null;
    //void setFormulaOutput();
    void visitNode(com.toposat.NodeFormula curr, NVertex vertex, ComplexSimplicialAbstract complex);
    void visitEdge(com.toposat.NodeFormula curr, NEdge edge, ComplexSimplicialAbstract complex);
    void visitGraph(com.toposat.NodeFormula curr, ComplexSimplicialAbstract complex) throws IOException, InterruptedException;
    void visitNonEdge(com.toposat.NodeFormula curr, NVertex first, NVertex second, ComplexSimplicialAbstract complex);
}
