package com.jladder.db.enums;

public class FieldAdaptor {

    public static String findFieldTypeText(String type,DbDialectType dialect){

        if("varchar".equals(type)){
            return "string";
        }
        return type;
    }
}
