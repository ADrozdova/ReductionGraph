package com.toposat;

public class NodeFormula { // node in cnf
    public TypeOperation operation = TypeOperation.undefined; // just some default value
    int var; // variable id
    public String varName; // used in writing formula to SMT, if not defined...
    public NodeFormula left; // left child
    public NodeFormula right; // right child
    public String addVar; // additional variable

    // Create a new conjunction operand in formula tree
    public NodeFormula findPlace() {
        NodeFormula newNode = createNodeFormulaConjunction();
        if(left == null){
            left = newNode;
            return left;
        } else if(right == null){
            right = newNode;
            return right;
        }
        newNode.left = left;
        left = newNode;
        newNode.right = createNodeFormulaConjunction();
        return newNode.right;
    }

    static NodeFormula createNodeFormulaConjunction() {
        NodeFormula res = new com.toposat.NodeFormula();
        res.operation = TypeOperation.conjunction;
        return res;
    }

    static NodeFormula createNodeFormulaNot() {
        NodeFormula res = new com.toposat.NodeFormula();
        res.operation = TypeOperation.not;
        return res;
    }

    static NodeFormula createNodeFormulaVariable(String nameVar) {
        NodeFormula res = new com.toposat.NodeFormula();
        res.operation = TypeOperation.variable;
        res.varName = nameVar;
        return res;
    }

}

