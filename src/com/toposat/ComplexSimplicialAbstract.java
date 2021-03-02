package com.toposat;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultUndirectedGraph;

import java.util.Set;

public class ComplexSimplicialAbstract {

    public Graph<NVertex, NEdge> m_graph = new DefaultUndirectedGraph<>(NEdge.class);


    public void traverseGraphNodes(Visitor myVisitor, NodeFormula nodeFormulaRoot) {
        Set<NVertex> vertices = m_graph.vertexSet();
        for (NVertex vertex : vertices) {
            NodeFormula placeCurrent = TseytinTransformation.findPlace(nodeFormulaRoot);
            myVisitor.visitNode(placeCurrent, vertex, m_graph);
        }
    }

    public void traverseGraphEdges(Visitor myvisitor, NodeFormula nodeFormulaRoot) {
        Set<NEdge> edges = m_graph.edgeSet();
        for (NEdge edge : edges) {
            NodeFormula placeCurrent = TseytinTransformation.findPlace(nodeFormulaRoot);
            myvisitor.visitEdge(placeCurrent, edge, m_graph);
        }
    }

    // Traverse all non-adjacent vertex pairs
    public void traverseGraphNonEdges(Visitor myVisitor, NodeFormula nodeFormulaRoot) {
        Set<NVertex> vertices = m_graph.vertexSet();
        for (NVertex first : vertices) {
            for (NVertex second : vertices) {
                if (first.getId() < second.getId()) {
                    if (m_graph.getAllEdges(first, second).isEmpty()) {
                        NodeFormula placeCurrent = TseytinTransformation.findPlace(nodeFormulaRoot);
                        myVisitor.visitNonEdge(placeCurrent, first, second, m_graph);
                    }
                }
            }
        }
    }

    public void traverseGraph(Visitor myVisitor, NodeFormula nodeFormulaRoot) {
        NodeFormula placeCurrent = TseytinTransformation.findPlace(nodeFormulaRoot);
        myVisitor.visitGraph(placeCurrent, m_graph);
    }
}
