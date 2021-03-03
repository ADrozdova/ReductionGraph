package com.toposat;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultUndirectedGraph;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Set;

public class ComplexSimplicialAbstract {

    public Graph<NVertex, NEdge> m_graph = new DefaultUndirectedGraph<>(NEdge.class);
    public LinkedHashMap<String, NVertex> m_verticesGraph = new LinkedHashMap<>();

    public void traverseGraphNodes(Visitor myVisitor, NodeFormula nodeFormulaRoot) {
        if (nodeFormulaRoot == null)
        {
            throw new RuntimeException("Exception: traverseGraphNodes." +
                    "Create a new node as was in TseytinTransformation.findPlace();");
        }
        Set<NVertex> vertices = m_graph.vertexSet();
        for (NVertex vertex : vertices) {
            NodeFormula placeCurrent = nodeFormulaRoot.findPlace();
            myVisitor.visitNode(placeCurrent, vertex, this);
        }
    }

    public void traverseGraphEdges(Visitor myvisitor, NodeFormula nodeFormulaRoot) {
        if (nodeFormulaRoot == null)
        {
            throw new RuntimeException("Exception: traverseGraphEdges." +
                    "Create a new node as was in TseytinTransformation.findPlace();");
        }
        Set<NEdge> edges = m_graph.edgeSet();
        for (NEdge edge : edges) {
            NodeFormula placeCurrent = nodeFormulaRoot.findPlace();
            myvisitor.visitEdge(placeCurrent, edge, this);
        }
    }

    // Traverse all non-adjacent vertex pairs
    public void traverseGraphNonEdges(Visitor myVisitor, NodeFormula nodeFormulaRoot) {
        if (nodeFormulaRoot == null)
        {
            throw new RuntimeException("Exception: traverseGraphNonEdges." +
                    "Create a new node as was in TseytinTransformation.findPlace();");
        }
        Set<NVertex> vertices = m_graph.vertexSet();
        for (NVertex first : vertices) {
            for (NVertex second : vertices) {
                if (first.getId() < second.getId()) {
                    if (m_graph.getAllEdges(first, second).isEmpty()) {
                        NodeFormula placeCurrent = nodeFormulaRoot.findPlace();
                        myVisitor.visitNonEdge(placeCurrent, first, second, this);
                    }
                }
            }
        }
    }

    public void traverseGraph(Visitor myVisitor, NodeFormula nodeFormulaRoot) throws IOException, InterruptedException {
        if (nodeFormulaRoot == null)
        {
            throw new RuntimeException("Exception: traverseGraphNonEdges." +
                    "Create a new node as was in TseytinTransformation.findPlace();");
        }
        NodeFormula placeCurrent = nodeFormulaRoot.findPlace();
        myVisitor.visitGraph(placeCurrent, this);
    }
}
