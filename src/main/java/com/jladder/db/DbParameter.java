package com.jladder.db;

import com.jladder.db.enums.DbParameterDataType;
import com.jladder.lang.Core;
import com.jladder.lang.Json;

import java.io.InputStream;
import java.util.Date;

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
        if(value==null || Core.isBaseType(value.getClass(),true))this.value=value;
        else{
            if(value instanceof Date){
                this.value = value;
                return;
            }
            if(value instanceof InputStream){
                this.value = value;
                return;
            }
            if(value instanceof byte[] || value instanceof Byte[]){
                this.value = value;
                return;
            }
            this.value = Json.toJson(value);
        }
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
