package com.toposat;

import org.jgrapht.Graph;

import java.io.IOException;

public interface Visitor {
    void visitGraph(com.toposat.NodeFormula nodeFormula, ComplexSimplicialAbstract complex) throws IOException, InterruptedException;
    void visitNode(com.toposat.NodeFormula nodeFormula, NVertex vertex, ComplexSimplicialAbstract complex);
    void visitEdge(com.toposat.NodeFormula nodeFormula, NEdge edge, ComplexSimplicialAbstract complex);
    void visitNonEdge(NodeFormula nodeFormula, NVertex first, NVertex second, ComplexSimplicialAbstract complex);
}
