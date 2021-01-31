package com.jladder.db.enums;

public enum DbGenType {
    NoGen("none", 0), UUID("uuid", 1), ID("id",2), DATE("date", 3), NUID("nuid",  4), TimeCode("timecode", 101), AutoNum("autonum",  -1);
    // 成员变量
    private String name;
    private int index;
    // 构造方法
    private DbGenType(String name, int index) {
        this.name = name;
        this.index = index;
    }
    // 普通方法
    public static String getName(int index) {
        for (DbGenType c : DbGenType.values()) {
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
