package com.company;

import org.jgrapht.Graph;

import java.util.Set;

public class VisitorDefault implements Visitor {

    // for the first formula
    void clauseType1(NodeFormula curr, NVertex vertex, int start, NEdge clId, int reverse, Graph<NVertex, NEdge> graph) { //0 - no reverse, 1 - reverse, 2 - both
        curr.operation = TypeOperation.disjunction;
        curr.left = new NodeFormula();
        curr.right = new NodeFormula();
        curr.right.operation = TypeOperation.not;
        curr.right.left = new NodeFormula();
        curr.right.left.operation = TypeOperation.variable;
        curr.right.left.var = -vertex.getId();
        curr.right.left.varName = "x" + (vertex.getId());
        curr = curr.left;
        Set<NEdge> in = graph.edgesOf(vertex);
        int n = in.size();
        int i = 0;
        int indicator = 0;
        for (NEdge curPos : in) {
            if((indicator == 0) && (clId != null) && !curPos.equals(clId) && (i == n - 2)){
                i = n - 1;
            }
            if(i < n - 1) {
                if (!curPos.equals(clId)) {
                    assert curr != null;
                    curr.operation = TypeOperation.disjunction;
                    curr.left = new NodeFormula();
                    curr.right = new NodeFormula();
                    if (reverse == 0 || reverse == 2) {
                        curr.right.operation = TypeOperation.variable;
                        if(graph.getEdgeSource(curPos).equals(vertex)){
                            curr.right.var = curPos.getId() + start;
                            curr.right.varName = "x" + (curPos.getId() + start);
                        } else {
                            curr.right.var = curPos.getReverseId() + start;
                            curr.right.varName = "x" + (curPos.getReverseId() + start);
                        }
                        curr = curr.left;
                    }
                    if (reverse == 2) {
                        curr.operation = TypeOperation.disjunction;
                        curr.left = new NodeFormula();
                        curr.right = new NodeFormula();
                    }
                    if (reverse > 0) {
                        curr.right.operation = TypeOperation.variable;
                        if(graph.getEdgeSource(curPos).equals(vertex)){
                            curr.right.var = curPos.getReverseId() + start;
                            curr.right.varName = "x" + (curPos.getReverseId() + start);

                        } else {
                            curr.right.var = curPos.getId() + start;
                            curr.right.varName = "x" + (curPos.getId() + start);
                        }
                        if (curr.left == null) {
                            curr.left = new NodeFormula();
                        }
                        curr = curr.left;
                    }
                } else if(curPos.equals(clId)){
                    indicator = 1;
                }
            } else {
                if(curr == null){
                    System.out.println("oh shit1");
                }
                if(!curPos.equals(clId)){
                    NodeFormula other = new NodeFormula();
                    if(reverse == 2) {
                        assert curr != null;
                        curr.operation = TypeOperation.disjunction;
                        curr.left = new NodeFormula();
                        curr.right = new NodeFormula();
                        other = curr.right;
                        curr = curr.left;
                    }
                    if(reverse == 0 || reverse == 2) {
                        assert curr != null;
                        curr.operation = TypeOperation.variable;
                        if(graph.getEdgeSource(curPos).equals(vertex)){
                            curr.var = curPos.getId() + start;
                            curr.varName = "x" + (curPos.getId() + start);
                        } else {
                            curr.var = curPos.getReverseId() + start;
                            curr.varName = "x" + (curPos.getReverseId() + start);
                        }
                    }
                    if(reverse == 2){
                        curr = other;
                    }
                    if(reverse > 0) {
                        assert curr != null;
                        curr.operation = TypeOperation.variable;
                        //curr.right = new NodeFormula();
                        if(graph.getEdgeSource(curPos).equals(vertex)){
                            curr.var = curPos.getReverseId() + start;
                            curr.varName = "x" + (curPos.getReverseId() + start);

                        } else {
                            curr.var = curPos.getId() + start;
                            curr.varName = "x" + (curPos.getId() + start);
                        }
                    }
                }
            }
            ++i;
        }
    }

    // adds nodes to formula tree from root, for a node in graph, for the first formula
    public void visitNode(NodeFormula curr, NVertex vertex, Graph<NVertex, NEdge> graph){
        int start = graph.vertexSet().size();
        curr.operation = TypeOperation.conjunction;
        curr.right = new NodeFormula();
        clauseType1(curr.right, vertex, start, null, 0, graph);
        curr.left = new NodeFormula();
        curr = curr.left;
        curr.operation = TypeOperation.conjunction;
        curr.right = new NodeFormula();
        clauseType1(curr.right, vertex,  start, null, 1, graph);
        curr.left = new NodeFormula();
        curr = curr.left;
        Set<NEdge> in = graph.edgesOf(vertex);
        int n = in.size();
        int i = 0;
        for(NEdge it : in) {
            curr.operation = TypeOperation.conjunction;
            NodeFormula pos = curr;
            if (i != n - 1) {
                curr.right = new NodeFormula();
                pos = curr.right;
            }
            if (n > 1) {
                clauseType1(pos, vertex, start, it, 2, graph);
            }
            if (i != n - 1) {
                curr.left = new NodeFormula();
                curr = curr.left;
            }
            ++i;
        }
    }

    // for the first formula
    public void visitGraph (NodeFormula curr, Graph<NVertex, NEdge> graph){
        int n = graph.vertexSet().size();
        for(int i = 1; i < n + 1; ++i){
            curr.operation = TypeOperation.disjunction;
            curr.right = new NodeFormula();
            curr.right.operation = TypeOperation.variable;
            curr.right.var = i;
            curr.right.varName = "x" + i;
            curr.left = new NodeFormula();
            curr = curr.left;
        }
        curr.operation = TypeOperation.variable;
        curr.var = n;
        curr.varName = "x" + n;
    }

    public void visitEdge(NodeFormula curr, NEdge edge, Graph<NVertex, NEdge> graph) {

    }
    public void visitNonEdge(NodeFormula curr, NVertex first,  NVertex second, Graph<NVertex, NEdge> graph){

    }
}