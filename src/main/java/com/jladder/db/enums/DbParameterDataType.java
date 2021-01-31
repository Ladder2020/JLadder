package com.jladder.db.enums;

public enum DbParameterDataType {
    Default("default",0),
    String("string",1),
    Number("number",2),
    Array("array",3),
    File("file",4);




    private String name;
    private int index;
    DbParameterDataType(String name, int index) {
        this.name = name;
        this.index = index;
    }
    // 普通方法
    public static String getName(int index) {
        for (DbParameterDataType c : DbParameterDataType.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }
    // get set 方法
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
}
