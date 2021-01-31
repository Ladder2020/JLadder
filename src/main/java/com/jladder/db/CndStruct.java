package com.jladder.db;

public class CndStruct {
    public String Name;



    public String Op;
    public Object Value;

    public CndStruct(String name, String op, Object value) {
        Name = name;
        Op = op;
        Value = value;
    }

    public CndStruct(String name, Object value) {
        Name = name;
        Value = value;
    }

    public String getName() {
        return Name;
    }

    public String getOp() {
        return Op;
    }

    public Object getValue() {
        return Value;
    }
}
