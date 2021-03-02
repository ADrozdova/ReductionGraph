package com.toposat;

public class NVertex {
    private final String m_GML_Id; // label - String id
    private final int id;
    private final String name; // another label

    public NVertex(String m_GML_Id, int id, String name) {
        this.m_GML_Id = m_GML_Id;
        this.id = id;
        this.name = name;
    }
    public String getGMLId(){ return m_GML_Id; }
    public int getId(){ return id; }
    public String getGMLLabel(){ return name; }
    public String toString(){ return "NVertex" + id + m_GML_Id; }
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
    @Override
    public boolean equals(Object obj){
        boolean result;
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != String.class) {
            if (obj.getClass() != this.getClass()) {
                return false;
            }
            NVertex v = (NVertex) obj;
            result = (this.id == (v.getId()));
        }
        else {
            String s = (String) obj;
            result = this.toString().equals(s);
        }
        return result;
    }
}