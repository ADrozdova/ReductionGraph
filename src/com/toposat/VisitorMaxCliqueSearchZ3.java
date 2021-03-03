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

    private void processPos(NodeFormula nodeFormula, ComplexSimplicialAbstract complex, int pos){
        Set<NVertex> vertices = complex.m_graph.vertexSet();
        int n = vertices.size();
        int i = 0;
        for(NVertex vertex : vertices) {
            if(i < n - 1){
                nodeFormula.operation = TypeOperation.disjunction;
                nodeFormula.right = NodeFormula.createNodeFormulaVariable(
                        vertex.getGMLId() + "_" + pos);

                nodeFormula.left = new NodeFormula();
                nodeFormula = nodeFormula.left;
            } else {
                nodeFormula.operation = TypeOperation.variable;
                nodeFormula.varName = vertex.getGMLId() + "_" + pos;
            }
            ++i;
        }
    }

    public void visitGraph (NodeFormula nodeFormula, ComplexSimplicialAbstract complex) throws IOException, InterruptedException {

        int size = complex.m_verticesGraph.size();
        m_cliqueSize = 2;

        while (m_cliqueSize <= size)
        {
            NodeFormula nodeFormulaRoot = NodeFormula.createNodeFormulaConjunction();

            nodeFormula = nodeFormulaRoot;

            complex.traverseGraphNodes(this, nodeFormula);
            complex.traverseGraphEdges(this, nodeFormula);
            complex.traverseGraphNonEdges(this, nodeFormula);

            nodeFormula = nodeFormula.findPlace();

            //=====================================================================
            // Enforce a node-variable in each of k parts of k-clique
            for(int i = 1; i < m_cliqueSize; ++i){
                nodeFormula.operation = TypeOperation.conjunction;
                nodeFormula.right = new NodeFormula();
                processPos(nodeFormula.right, complex, i);
                nodeFormula.left = new NodeFormula();
                nodeFormula = nodeFormula.left;
            }
            processPos(nodeFormula, complex, m_cliqueSize);
            // End: Enforce a node-variable in each of k parts of k-clique
            //=====================================================================

            m_solverZ3.Solve(nodeFormulaRoot);
            if (m_solverZ3.getResult().m_UNSAT) {
                m_cliqueSize--;
                break;
            }
            if (m_solverZ3.getResult().m_SAT) {
                Vector<String> trueVar = m_solverZ3.getNamesTrueVariables();
                System.out.println("Solution Clique:" + m_cliqueSize);
                for (String v : trueVar) {
                    String a = v.split("_")[0];
                    if (complex.m_verticesGraph.containsKey(a)) {
                        System.out.println(v + " " + complex.m_verticesGraph.get(a).getGMLLabel());
                    }
                }

                m_cliqueSize++;
            } else {
                throw new RuntimeException("Unknown behaviour: VisitorMaxCliqueSearchZ3");
            }
        }
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

        nodeFormula.right = NodeFormula.createNodeFormulaNot();
        nodeFormula.right.left = NodeFormula.createNodeFormulaVariable(
                first.getGMLId() + "_" + j);

        nodeFormula.left = NodeFormula.createNodeFormulaNot();
        nodeFormula.left.left = NodeFormula.createNodeFormulaVariable(
                second.getGMLId() + "_" + i);
    }

    public void visitNode(NodeFormula nodeFormula, NVertex vertex, ComplexSimplicialAbstract complex){
        //Prohibits a variable in two parts simultaneously
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