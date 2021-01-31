package com.jladder.db;

import com.jladder.db.enums.DbParameterDataType;

public class DbParameter {

    public DbParameterDataType type;

    public boolean out;

    public String name;

    public DbParameterDataType getType() {
        return type;
    }

    public void setType(DbParameterDataType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object value;

    public DbParameter(DbParameterDataType type,Object value){
        this.type=type;
        this.value=value;
    }
    public DbParameter(Object value){
        type=DbParameterDataType.Default;
        this.value=value;
    }
    public DbParameter(String name,Object value){
        type=DbParameterDataType.Default;
        this.name = name;
        this.value=value;
    }
    public DbParameter(String name,Object value,boolean out){
        type=DbParameterDataType.Default;
        this.name = name;
        this.value=value;
        this.out=out;
    }
    public DbParameter(){


    }
}
