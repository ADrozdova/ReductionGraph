package com.toposat;


import org.jgrapht.Graph;

import java.util.Set;

public class VisitorMaxCliqueSearch implements VisitorGraphToFormula {

    int cliqueSize = 3;

    private void processPos(NodeFormula curr, Graph<NVertex, NEdge> graph, int pos){
        Set<NVertex> vertices = graph.vertexSet();
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

    public void visitGraph (NodeFormula curr, Graph<NVertex, NEdge> graph){
        for(int i = 1; i < cliqueSize; ++i){
            curr.operation = TypeOperation.conjunction;
            curr.right = new NodeFormula();
            processPos(curr.right, graph, i);
            curr.left = new NodeFormula();
            curr = curr.left;
        }
        processPos(curr, graph, cliqueSize);
    }

    public void visitNonEdge(NodeFormula curr, NVertex first, NVertex second, Graph<NVertex, NEdge> graph){
        int count = 1;
        int last = cliqueSize * cliqueSize;
        for(int i = 1; i <= cliqueSize; ++i){
            for(int j = 1; j <= cliqueSize; ++j){
                if(count < last){
                    curr.operation = TypeOperation.conjunction;
                    curr.right = new NodeFormula();
                    processPair(curr.right, first, second, i, j);
                    curr.left = new NodeFormula();
                    curr = curr.left;
                } else {
                    processPair(curr, first, second, i, j);
                }
                ++count;
            }
        }
    }

    private void processPair(NodeFormula curr, NVertex first, NVertex second, int i, int j){
        curr.operation = TypeOperation.disjunction;

        curr.right = new NodeFormula();
        curr.right.operation = TypeOperation.not;
        curr.right.left = new NodeFormula();
        curr.right.left.operation = TypeOperation.variable;
        curr.right.left.varName = first.getGMLId() + "_" + j;

        curr.left = new NodeFormula();
        curr.left.operation = TypeOperation.not;
        curr.left.left = new NodeFormula();
        curr.left.left.operation = TypeOperation.variable;
        curr.left.left.varName = second.getGMLId() + "_" + i;
    }

    public void visitNode(NodeFormula curr, NVertex vertex, Graph<NVertex, NEdge> graph){
        int count = 1;
        int last = cliqueSize * (cliqueSize - 1) / 2;
        for(int i = 1; i <= cliqueSize; ++i){
            for(int j = 1; j <= cliqueSize; ++j){
                if(i < j) {
                    if (count < last) {
                        curr.operation = TypeOperation.conjunction;
                        curr.right = new NodeFormula();
                        processPair(curr.right, vertex, vertex, i, j);
                        curr.left = new NodeFormula();
                        curr = curr.left;
                    } else {
                        processPair(curr, vertex, vertex, i, j);
                    }
                    ++count;
                }
            }
        }
    }

    public void visitEdge(NodeFormula curr, NEdge edge, Graph<NVertex, NEdge> graph) {
//        System.out.println("visit edge");
        NVertex first = edge.getFirst();
        NVertex second = edge.getSecond();
        for (int i = 1; i < cliqueSize; ++i) {
            processPair(curr, first, second, i, i);
            curr.operation = TypeOperation.conjunction;
            curr.right = new NodeFormula();
            processPair(curr.right, first, second, i, i);
            curr.left = new NodeFormula();
            curr = curr.left;
        }
        processPair(curr, first, second, cliqueSize, cliqueSize);

    }
}