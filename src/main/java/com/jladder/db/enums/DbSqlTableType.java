package com.jladder.db.enums;

public class DbSqlTableType {
    /// <summary>用于创建</summary>
    public static final int Create = 0;
    /// <summary>用于修改</summary>
    public static final int Alter = 1;
    /// <summary>用于加列</summary>
    public static final int Add = 2;
    /// <summary>用于删列</summary>
    public static final int Delete = 3;
    /// <summary>用于改名</summary>
    public static final int  Rename = 4;
    /// <summary>用于删表</summary>
    public static final int Drop = -1;
    /// <summary>用于变长</summary>
    public static final int  Lenth = 5;

}
