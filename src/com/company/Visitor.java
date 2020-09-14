package com.company;

import org.jgrapht.Graph;


public interface Visitor {
    void visitNode(NodeFormula curr, NVertex vertex, Graph<NVertex, NEdge> graph);
    void visitEdge(NodeFormula curr, NEdge edge, Graph<NVertex, NEdge> graph);
    void visitGraph(NodeFormula curr, Graph<NVertex, NEdge> graph);
    void visitNonEdge(NodeFormula curr, NVertex first,  NVertex second, Graph<NVertex, NEdge> graph);
}