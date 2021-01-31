package com.jladder.db.enums;

public enum DbDialectType{
    /**
     * 默认数据库
     */
    Default ,
    /***
     * oracle数据库
     */
    ORACLE,
    /***
     * sqlite数据库
     */
    SQLITE,
    /// <summary>
    /// sqlserver数据库
    /// </summary>
    SQLSERVER,
    /// <summary>
    /// sqlserver数据库
    /// </summary>
    Mssql2000,
    /// <summary>
    /// sqlserver2005数据库
    /// </summary>
    Mssql2005,
    /// <summary>
    /// sqlserver2008数据库
    /// </summary>
    Mssql2008,
    /// <summary>
    /// sqlserver2012数据库
    /// </summary>
    Mssql2012,
    /// <summary>
    /// mysql数据库
    /// </summary>
    MYSQL,
    /// <summary>
    /// H2 内存数据库
    /// </summary>
    H2,
    /// <summary>
    /// access数据库
    /// </summary>
    ACCESS,
    /// <summary>
    /// 其他类型的数据库
    /// </summary>
    ///
    OleDb,
    ODBC,
    /// <summary>
    /// Firebird数据库
    /// </summary>
    Firebird,
    /// <summary>
    /// PostgreSql数据库
    /// </summary>
    PostgreSql,
    /// <summary>
    /// DB2数据库
    /// </summary>
    // ReSharper disable once InconsistentNaming
    DB2,
    /// <summary>
    /// Informix数据库
    /// </summary>
    Informix,
    /// <summary>
    /// SqlServerCe数据库
    /// </summary>
    SqlServerCe,
    /// <summary>
    /// Excel表
    /// </summary>
    Excel,
    /**
     *其它数据库
     */
    Other

}

