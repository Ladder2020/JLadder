package com.jladder.db.enums;

public enum  DbSqlDataType
{
    /**
     * 结构化查询
     */
    Query("query", 0),
    /**
     * 插入数据
     */
    Insert("insert", 1),
    /**
     * 更新数据
     */
    Update("update", 2),
    /***
     * 保存数据(主键存在更新，主键不存在新增)
     */
    Save("save", 3),
    /***
     * 彻底删除数据
     */
    Truncate("truncate", -1111),

    /***
     * 删除数据
     */
    Delete("delete",  -1),

    /***
     * 创建表
     */
    Create("create",  101),

    /***
     * 获取数据列表
     */
    GetData("getdata",  11),

    /***
     * 获取一条记录对象
     */
    GetBean("getbean",  10),
    /***
     * 获取分页数据
     */
    GetPageData("getpagedata",  12),

    /***
     * 集成查询
     */
    QueryData("querydata",  22),

    /***
     * 获取值
     */
    GetValue("getvalue",  111),


    /***
     * 获取记录数
     */
    GetCount("getcount",  123),

    /***
     * 多查询
     */
    Querys("querys",  1000),

    /***
     * 多对象保存
     */
    SaveBeans("savebeans",  1111),


    /***
     * 保存数据
     */
    SaveBean("savebean",  333),


    /***
     * 执行脚本
     */
    Script("script",  4),


    /***
     * 执行内部程序
     */
    Program("program",  5),


    /***
     * http访问
     */
    Http("http",  6),

    /***
     * 接口服务
     */
    Service("service",  1222),


    /***
     * 一对一级联
     */
    OneToOne("onettone",  15),

    /***
     * 一对多级联
     */
    OneToMany("onetomany",  16),

    /***
     * 多对多级联
     */
    ManyToMany("manytomany",  17);

    private String name;
    private int index;
    DbSqlDataType(String name, int index) {
        this.name = name;
        this.index = index;
    }
    // 普通方法
    public static String getName(int index) {
        for (DbSqlDataType c : DbSqlDataType.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return null;
    }
    public static DbSqlDataType get(int index){
        for (DbSqlDataType c : DbSqlDataType.values()) {
            if (c.getIndex() == index) {
                return c;
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

    public static int getIndex(DbSqlDataType type) {
        for (DbSqlDataType c : DbSqlDataType.values()) {
            if (type.equals(c)) {
                return c.index;
            }
        }
        return 0;
    }
}