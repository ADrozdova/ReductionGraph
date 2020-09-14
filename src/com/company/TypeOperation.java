package com.company;

public enum TypeOperation { // string representations
    conjunction, // &&
    disjunction, // ||
    variable, // anything else
    undefined, // ?
    not, // !
    implication, // ->
    equivalence, // <->
    xor, // +
    open, // (
    close, // )
}