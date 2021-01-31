package com.jladder.datamodel;

/***
 * 动态数据模型的类型枚举
 */
public enum DataModelType {
    /**
     * 实表
     */
    Table("table",0),

    /***
     * sql语句
     */
    Sql("sql",1),

    /***
     * 存储过程
     */
    Pro("pro",2),

    /***
     * 存储过程
     */
    Exec("exec",3),

    /***
     * 静态数据
     */
    Data("data",4),

    /***
     * 自定义表单
     */
    Magic("magic",5);


    DataModelType(String magic, int i) {
    }
}
