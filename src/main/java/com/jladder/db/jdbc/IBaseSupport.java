package com.jladder.db.jdbc;

import com.jladder.data.Record;
import com.jladder.db.DbParameter;
import com.jladder.db.SqlText;
import com.jladder.db.enums.DbDialectType;
import com.jladder.lang.func.Func2;
import com.jladder.lang.func.Func3;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class IBaseSupport {

    public String maskcode;
    public String connectstring;
    public String tag;
    public boolean isWriteLog;
    public DbDialectType dialect;
    public String error;


    abstract public List<Record> query(SqlText sqltext);

    /***
     * 基本数据查询
      * @param sqltext sql语句
     * @param serialize 是否进行数据文本化
     * @param callback 回调处理
     * @return 返回记录集
     */
    abstract public List<Record> query(SqlText sqltext, boolean serialize, Func2<Record, Boolean> callback);

    /***
     * 多查询
     * @param sqlcmd sql语句
     * @param dbParameters 参数数据
     * @param callback 回调处理
     * @return 返回多记录集
     */
    abstract public List<List<Record>> querys(SqlText sqlcmd, Collection<DbParameter> dbParameters, Function<Record, Boolean> callback);

    /***
     *实体列表查询
     * @param sqltext sql语句
     * @param callback 回调处理
     * @param <T> 实体对象（包括普通bean对象）
     * @return
     */
    abstract public <T> List<T> query(SqlText sqltext,Class<T> clazz, Func2<T, Boolean> callback);

    /**
     * 获取值
     * @param sqltext sql语句
     * @param <T> 返回数据类型
     * @return 返回数据
     */
    abstract public <T> T getValue(SqlText sqltext,Class<T> clazz);
    abstract public <T> List<T> getValues(SqlText sqltext,Class<T> clazz);

    /***
     * 获取数据库表列表
     * @param schemaName 模式名称
     * @param tableName 表名
     * @return
     */
    abstract public List<Record> getTables(String schemaName, String tableName);

    /***
     * 表是否存在
     * @param tableName 表名
     * @return
     */
    abstract public boolean exists(String tableName);
    abstract public int exec(SqlText sqltext);
    /**
     * 执行语句
     * @param sqltext sql语句
     * @param callback 回调处理
     * @return 影响数据行数
     */
    abstract public int exec(SqlText sqltext, Func3<Integer, Connection, Integer> callback);


    /***
     * 开始事务
     * @return
     */
    abstract public boolean beginTrain();

    /***
     * 回滚事务
     */
    abstract public void rollback();

    /***
     * 提交事务
     * @return
     */
    abstract public boolean commitTran();

    public abstract List<Map<String,Object>> getFieldInfo(String tableName);



    public abstract List<Object> pro(String name,List<DbParameter> parameters);


    public abstract boolean isTraning();

}
