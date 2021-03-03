package com.toposat;


import java.io.IOException;
import java.util.Set;
import java.util.Vector;

public class VisitorMaxCliqueSearchZ3 implements VisitorGraphToFormula {

    int m_cliqueSize = 0;
    Solver m_solverZ3;

    public VisitorMaxCliqueSearchZ3(Solver solverZ3) {
        m_solverZ3 = solverZ3;
    }

    private void processPos(NodeFormula curr, ComplexSimplicialAbstract complex, int pos){
        Set<NVertex> vertices = complex.m_graph.vertexSet();
        int n = vertices.size();
        int i = 0;
        for(NVertex vertex : vertices) {
            if(i < n - 1){
                curr.operation = TypeOperation.disjunction;
                curr.right = new NodeFormula();
                curr.right.operation = TypeOperation.variable;
                curr.right.varName = vertex.getGMLId() + "_" + pos;
                curr.left = new NodeFormula();
                curr = curr.left;
            } else {
                curr.operation = TypeOperation.variable;
                curr.varName = vertex.getGMLId() + "_" + pos;
            }
            ++i;
        }
    }

    public void visitGraph (NodeFormula nodeFormula, ComplexSimplicialAbstract complex) throws IOException, InterruptedException {

        NodeFormula nodeFormulaRoot = nodeFormula;
        m_cliqueSize = 2;

        complex.traverseGraphNodes(this, nodeFormula);
        complex.traverseGraphEdges(this, nodeFormula);
        complex.traverseGraphNonEdges(this, nodeFormula);

        //complex.traverseGraph(this, nodeFormula);
        nodeFormula = TseytinTransformation.findPlace(nodeFormula);

        m_solverZ3.Solve(nodeFormula);

        Vector<Vector<String>> results = solveALLSATZ3();
        System.out.println(results.size());
        int i = 0;
        for (Vector<String> trueVar : results) {
            System.out.println("Solution " + i);
            for (String v : trueVar) {
                String a = v.split("_")[0];
//                 System.out.println(a);
                if (m_nodesGraph.containsKey(a)) {
                    System.out.println(v + " " + m_nodesGraph.get(a).getGMLLabel());
                }
            }
            ++i;
        }

        //old
        for(int i = 1; i < m_cliqueSize; ++i){
            nodeFormula.operation = TypeOperation.conjunction;
            nodeFormula.right = new NodeFormula();
            processPos(nodeFormula.right, graph, i);
            nodeFormula.left = new NodeFormula();
            nodeFormula = nodeFormula.left;
        }
        processPos(nodeFormula, graph, m_cliqueSize);
    }

    public void visitNonEdge(NodeFormula nodeFormula, NVertex first, NVertex second, ComplexSimplicialAbstract complex){
        int count = 1;
        int last = m_cliqueSize * m_cliqueSize;
        for(int i = 1; i <= m_cliqueSize; ++i){
            for(int j = 1; j <= m_cliqueSize; ++j){
                if(count < last){
                    nodeFormula.operation = TypeOperation.conjunction;
                    nodeFormula.right = new NodeFormula();
                    processPair(nodeFormula.right, first, second, i, j);
                    nodeFormula.left = new NodeFormula();
                    nodeFormula = nodeFormula.left;
                } else {
                    processPair(nodeFormula, first, second, i, j);
                }
                ++count;
            }
        }
    }

    private void processPair(NodeFormula nodeFormula, NVertex first, NVertex second, int i, int j){
        nodeFormula.operation = TypeOperation.disjunction;

        nodeFormula.right = new NodeFormula();
        nodeFormula.right.operation = TypeOperation.not;
        nodeFormula.right.left = new NodeFormula();
        nodeFormula.right.left.operation = TypeOperation.variable;
        nodeFormula.right.left.varName = first.getGMLId() + "_" + j;

        nodeFormula.left = new NodeFormula();
        nodeFormula.left.operation = TypeOperation.not;
        nodeFormula.left.left = new NodeFormula();
        nodeFormula.left.left.operation = TypeOperation.variable;
        nodeFormula.left.left.varName = second.getGMLId() + "_" + i;
    }

    public void visitNode(NodeFormula nodeFormula, NVertex vertex, ComplexSimplicialAbstract complex){
        int count = 1;
        int last = m_cliqueSize * (m_cliqueSize - 1) / 2;
        for(int i = 1; i <= m_cliqueSize; ++i){
            for(int j = 1; j <= m_cliqueSize; ++j){
                if(i < j) {
                    if (count < last) {
                        nodeFormula.operation = TypeOperation.conjunction;
                        nodeFormula.right = new NodeFormula();
                        processPair(nodeFormula.right, vertex, vertex, i, j);
                        nodeFormula.left = new NodeFormula();
                        nodeFormula = nodeFormula.left;
                    } else {
                        processPair(nodeFormula, vertex, vertex, i, j);
                    }
                    ++count;
                }
            }
        }
    }

    public void visitEdge(NodeFormula nodeFormula, NEdge edge, ComplexSimplicialAbstract complex) {
//        System.out.println("visit edge");
        NVertex first = edge.getFirst();
        NVertex second = edge.getSecond();
        for (int i = 1; i < m_cliqueSize; ++i) {
            processPair(nodeFormula, first, second, i, i);
            nodeFormula.operation = TypeOperation.conjunction;
            nodeFormula.right = new NodeFormula();
            processPair(nodeFormula.right, first, second, i, i);
            nodeFormula.left = new NodeFormula();
            nodeFormula = nodeFormula.left;
        }
        processPair(nodeFormula, first, second, m_cliqueSize, m_cliqueSize);

    }
}