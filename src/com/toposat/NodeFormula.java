package com.toposat;

public class NodeFormula { // node in cnf

    TypeOperation operation = TypeOperation.undefined; // just some default value
    int var; // variable id
    String varName; // used in writing formula to SMT, if not defined...
    NodeFormula left; // left child
    NodeFormula right; // right child
    String addVar; // additional variable
}