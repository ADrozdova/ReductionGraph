package com.toposat;


import java.util.Set;

import org.jgrapht.*;

public class VisitorCliqueSearch implements VisitorGraphToFormula {

    int cliqueSize = 3;

    private void processPos(com.toposat.NodeFormula curr, ComplexSimplicialAbstract complex, int pos){
        Set<NVertex> vertices = complex.m_graph.vertexSet();
        int n = vertices.size();
        int i = 0;
        for(NVertex vertex : vertices) {
            if(i < n - 1){
                curr.operation = com.toposat.TypeOperation.disjunction;
                curr.right = new com.toposat.NodeFormula();
                curr.right.operation = com.toposat.TypeOperation.variable;
                curr.right.varName = vertex.getGMLId() + "_" + pos;
                curr.left = new com.toposat.NodeFormula();
                curr = curr.left;
            } else {
                curr.operation = com.toposat.TypeOperation.variable;
                curr.varName = vertex.getGMLId() + "_" + pos;
            }
            ++i;
        }
    }

    public void visitGraph (com.toposat.NodeFormula curr, ComplexSimplicialAbstract complex){
        for(int i = 1; i < cliqueSize; ++i){
            curr.operation = com.toposat.TypeOperation.conjunction;
            curr.right = new com.toposat.NodeFormula();
            processPos(curr.right, complex, i);
            curr.left = new com.toposat.NodeFormula();
            curr = curr.left;
        }
        processPos(curr, complex, cliqueSize);
    }

    public void visitNonEdge(com.toposat.NodeFormula curr, NVertex first, NVertex second, ComplexSimplicialAbstract complex){
        int count = 1;
        int last = cliqueSize * cliqueSize;
        for(int i = 1; i <= cliqueSize; ++i){
            for(int j = 1; j <= cliqueSize; ++j){
                if(count < last){
                    curr.operation = com.toposat.TypeOperation.conjunction;
                    curr.right = new com.toposat.NodeFormula();
                    processPair(curr.right, first, second, i, j);
                    curr.left = new com.toposat.NodeFormula();
                    curr = curr.left;
                } else {
                    processPair(curr, first, second, i, j);
                }
                ++count;
            }
        }
    }

    private void processPair(com.toposat.NodeFormula curr, NVertex first, NVertex second, int i, int j){
        curr.operation = com.toposat.TypeOperation.disjunction;

        curr.right = new com.toposat.NodeFormula();
        curr.right.operation = com.toposat.TypeOperation.not;
        curr.right.left = new com.toposat.NodeFormula();
        curr.right.left.operation = com.toposat.TypeOperation.variable;
        curr.right.left.varName = first.getGMLId() + "_" + j;

        curr.left = new com.toposat.NodeFormula();
        curr.left.operation = com.toposat.TypeOperation.not;
        curr.left.left = new com.toposat.NodeFormula();
        curr.left.left.operation = com.toposat.TypeOperation.variable;
        curr.left.left.varName = second.getGMLId() + "_" + i;
    }

    public void visitNode(com.toposat.NodeFormula curr, NVertex vertex, ComplexSimplicialAbstract complex){
        int count = 1;
        int last = cliqueSize * (cliqueSize - 1) / 2;
        for(int i = 1; i <= cliqueSize; ++i){
            for(int j = 1; j <= cliqueSize; ++j){
                if(i < j) {
                    if (count < last) {
                        curr.operation = TypeOperation.conjunction;
                        curr.right = new com.toposat.NodeFormula();
                        processPair(curr.right, vertex, vertex, i, j);
                        curr.left = new com.toposat.NodeFormula();
                        curr = curr.left;
                    } else {
                        processPair(curr, vertex, vertex, i, j);
                    }
                    ++count;
                }
            }
        }
    }

    public void visitEdge(NodeFormula curr, NEdge edge, ComplexSimplicialAbstract complex) {
//        System.out.println("visit edge");
        NVertex first = edge.getFirst();
        NVertex second = edge.getSecond();
        for (int i = 1; i < cliqueSize; ++i) {
            processPair(curr, first, second, i, i);
            curr.operation = com.toposat.TypeOperation.conjunction;
            curr.right = new com.toposat.NodeFormula();
            processPair(curr.right, first, second, i, i);
            curr.left = new com.toposat.NodeFormula();
            curr = curr.left;
        }
        processPair(curr, first, second, cliqueSize, cliqueSize);

    }
}