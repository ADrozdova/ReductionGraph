package com.company;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class numberVariables {

    private HashMap<String, Integer> varNumbers = null;
    private HashSet<String> varNames = null ;
    private FileWriter Writer;
    private int varcnt = 0;
    private int clcnt = 0;
    private int count;

    public int getVarcnt(){
        return varcnt;
    }

    public int getClcnt(){
        return clcnt;
    }

    private void recursiveDeclareSMT(NodeFormula root) throws IOException {
        if(root == null){
            return;
        }
        if(root.varName != null){
            if(!varNames.contains(root.varName)){
                varNames.add(root.varName);
                if(root.varName == null){
                    System.out.println("root.var = " + root.var);
                }
                Writer.write("(declare-const " + root.varName + " Bool)\n");
            }
            return;
        }
        recursiveDeclareSMT(root.left);
        recursiveDeclareSMT(root.right);
    }

    void declareVariablesSMT(NodeFormula root, FileWriter fw) throws IOException {
        varNames = new HashSet<>();
        Writer = fw;
        recursiveDeclareSMT(root);
    }

    private void recursiveNumber(NodeFormula root,  int not){
        if(root == null){
            return;
        }
        if(root.varName != null){
            if(!varNumbers.containsKey(root.varName)){
                varNumbers.put(root.varName, count);
                root.var = count;
                if(not == 1){
                    root.var = -count;
                }
                //System.out.println("root.varName " + root.varName);
                //System.out.println("root.var " + root.var);
                count += 1;
            } else {
                root.var = varNumbers.get(root.varName);
                //System.out.println("root.varName " + root.varName);
                //System.out.println("root.var " + root.var);
                if(not == 1){
                    root.var = -varNumbers.get(root.varName);;
                }
            }
            return;
        }
        if(root.operation == TypeOperation.conjunction){
            if(root.left != null && root.right != null &&
                    (root.left.operation != TypeOperation.conjunction ||  root.right.operation != TypeOperation.conjunction)){
                clcnt += 1;
            }
        }
        not = 0;
        if(root.operation == TypeOperation.not){
            not = 1;
        }
        recursiveNumber(root.left, not);
        recursiveNumber(root.right, not);
    }

    void declareVariablesCNF(NodeFormula root){
        varNumbers = new HashMap<>();
        count = 1;
        recursiveNumber(root, 0);
        varcnt = varNumbers.size();
    }


}
