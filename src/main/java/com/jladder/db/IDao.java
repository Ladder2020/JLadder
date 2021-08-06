package com.jladder.db;
import com.jladder.data.Pager;
import com.jladder.data.Record;
import com.jladder.db.bean.BaseEntity;
import com.jladder.db.enums.DbDialectType;
import com.jladder.db.enums.DbGenType;
import com.jladder.lang.func.Action0;
import com.jladder.lang.func.Func2;
import com.jladder.lang.func.Func3;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface IDao
{

    DbDialectType getDialect();
//    /// <summary>
//    /// 从默认配置初始化
//    /// </summary>
//    void create(String conn);
//
//    /// <summary>
//    /// 从数据库连接信息对象初始化
//    /// </summary>
//    /// <param name="dbInfo">数据库连接信息对象</param>
//    void create(DbInfo dbInfo);

    /// <summary>
    /// 基本查询接口
    /// </summary>
    /// <typeparam name="T">泛型类型</typeparam>
    /// <param name="tableName">表名</param>
    /// <param name="cnd">条件对象</param>
    /// <param name="pager">分页对象</param>
    /// <returns></returns>
    <T> List<T> query(String tableName, Cnd cnd, Pager pager, Class<T> clazz);


    /// <summary>
    /// 查询数据(Record)
    /// </summary>
    /// <param name="sqltext"></param>
    /// <returns></returns>
    List<Record> query(SqlText sqltext);

//    /// <summary>
//    /// 查询数据(Record)
//    /// </summary>
//    /// <param name="sqltext"></param>
//    /// <param name="callback"></param>
//    /// <returns></returns>
     List<Record> query(SqlText sqltext, Func2<Record, Boolean> callback);

//    /// <summary>
//    /// 查询数据
//    /// </summary>
//    /// <param name="sqltext">sql语句</param>
//    /// <param name="serialize"> 是否序列化</param>
//    /// <param name="callback">回调处理委托</param>
//    /// <returns></returns>
    List<Record> query(SqlText sqltext, Boolean serialize, Func2<Record, Boolean> callback);

    /// <summary>
    /// 查询数据(实体对象)
    /// </summary>
    /// <typeparam name="T">实体类型</typeparam>
    /// <param name="sqltext">查询语句</param>
    /// <returns></returns>
    <T> List<T> query(SqlText sqltext,Class<T> clazz);

//    /// <summary>
//    /// 查询数据(实体对象)
//    /// </summary>
//    /// <typeparam name="T">实体类型</typeparam>
//    /// <param name="sqltext">查询语句</param>
//    /// <param name="callback">回调委托</param>
//    /// <returns></returns>
    <T> List<T> query(SqlText sqltext,Class<T> clazz, Func2<T, Boolean> callback);
    /// <summary>
    /// 查询类记录
    /// </summary>
    /// <typeparam name="T"></typeparam>
    /// <param name="cnd"></param>
    /// <returns></returns>
    <T> List<T> query(Cnd cnd,Class<T> clazz);
    /// <summary>
    /// 查询实体列表
    /// </summary>
    /// <typeparam name="T">实体类型</typeparam>
    /// <param name="table">表名</param>
    /// <param name="cnd">条件</param>
    /// <returns></returns>
    <T> List<T> query(String table, Cnd cnd,Class<T> clazz);

    List<Record> query(String tableName, Cnd cnd, Pager pager);
    /// <summary>
    /// 数据库查询
    /// </summary>
    /// <param name="tableName">实表名</param>
    /// <param name="cnd">查询条件</param>
    /// <returns></returns>
    List<Record> select(String tableName, Cnd cnd);

    /// <summary>
    /// 数据库查询
    /// </summary>
    /// <typeparam name="T">实体类型</typeparam>
    /// <param name="cnd">条件</param>
    /// <returns></returns>
    <T> List<T> select(Cnd cnd,Class<T> clazz);
    /// <summary>
    /// 按分页查询数据
    /// </summary>
    /// <param name="sqltext">sql语句</param>
    /// <param name="pager">分页</param>
    /// <returns>结果集</returns>
    List<Record> queryByPage(SqlText sqltext, Pager pager);

    /// <summary>
    /// 按分页查询数据
    /// </summary>
    /// <param name="sqltext">sql语句</param>
    /// <param name="pager">分页</param>
    /// <param name="handle">记录处理函数</param>
    /// <returns>结果集</returns>
//    List<Record> queryByPage(SqlText sqltext, Pager pager, Func<Record, boolean> handle);

    /// <summary>
    /// 获取单条记录
    /// </summary>
    /// <param name="sqltext">查询语句</param>
    /// <returns></returns>
    Record fetch(SqlText sqltext);

    /// <summary>
    /// 获取一条记录
    /// </summary>
    /// <param name="tableName">表名</param>
    /// <param name="value">ID值</param>
    /// <returns>Record对象</returns>
    Record fetch(String tableName, String value);

    /// <summary>
    /// 获取一条记录
    /// </summary>
    /// <param name="tableName">表名</param>
    /// <param name="key">索引字段</param>
    /// <param name="value">索引值</param>
    /// <returns>Record对象</returns>
    Record fetch(String tableName, String key, String value);

    /// <summary>
    /// 获取一条记录
    /// </summary>
    /// <param name="tableName">表名</param>
    /// <param name="cnd">条件对象</param>
    /// <returns></returns>
    Record fetch(String tableName, Cnd cnd);

    /// <summary>
    /// 获取实体对象
    /// </summary>
    /// <typeparam name="T">实体类型</typeparam>
    /// <param name="cnd">条件</param>
    /// <returns>对象</returns>
    <T> T fetch(Cnd cnd,Class<T> clazz);

    /// <summary>
    /// 获取实体对象
    /// </summary>
    /// <typeparam name="T">实体类型</typeparam>
    /// <param name="id">id索引值</param>
    /// <returns>对象</returns>
    <T> T fetch(String id,Class<T> clazz);

//    /// <summary>
//    /// 获取实体对象
//    /// </summary>
//    /// <typeparam name="T">实体类型</typeparam>
//    /// <param name="value">索引值</param>
//    /// <param name="propName">属性名</param>
//    /// <returns></returns>
//    <T> T fetch(String value, String propName);

    /// <summary>
    /// 获取记录数
    /// </summary>
    /// <param name="tableName"></param>
    /// <param name="cnd">条件对象</param>
    /// <returns>记录数</returns>
    int count(String tableName, Cnd cnd);

    /// <summary>
    /// 获取记录数
    /// </summary>
    /// <param name="tableName">表名</param>
    /// <param name="where">条件</param>
    /// <returns>记录数</returns>
    int count(String tableName, SqlText where);

    /// <summary>
    /// 删除记录
    /// </summary>
    /// <param name="tableName">表名</param>
    /// <param name="wherestr">条件文本</param>
    /// <returns>影响数量</returns>
    int delete(String tableName, SqlText wherestr);
    /// <summary>
    /// 删除数据
    /// </summary>
    /// <param name="tableName"></param>
    /// <param name="cnd"></param>
    /// <returns></returns>
    int delete(String tableName, Cnd cnd);
    /// <summary>
    /// 删除对象
    /// </summary>
    /// <typeparam name="T">泛型类型</typeparam>
    /// <param name="bean">实体类</param>
    /// <returns></returns>

    <T extends BaseEntity> int delete(T bean);

    /// <summary>
    /// 更新数据
    /// </summary>
    /// <param name="tableName">表名</param>
    /// <param name="data">数据</param>
    /// <param name="cnd">条件</param>
    /// <param name="columns">列填充</param>
    /// <param name="adjust">自动纠正</param>
    /// <returns></returns>
    int update(String tableName, Object data, Object cnd,String columns, boolean adjust);
    /// <summary>
    /// 更新数据
    /// </summary>
    /// <param name="tableName">表名</param>
    /// <param name="record">记录</param>
    /// <param name="cnd">条件对象</param>
    /// <returns></returns>
    int update(String tableName, Map<String, Object> record, Cnd cnd);
    /// <summary>
    /// 更新实体
    /// </summary>
    /// <typeparam name="T">实体泛型</typeparam>
    /// <param name="bean">实体</param>
    /// <param name="columns">列填充</param>
    /// <returns></returns>
    <T extends BaseEntity> int update(T bean, String columns);



    int insert(String tableName, Object bean);

    /***
     * 添加数据
     * @param tableName 表名
     * @param bean 记录数据
     * @param columns 列填充
     * @param adjust 自动纠正
     * @return
     */
    int insert(String tableName, Object bean,String columns, boolean adjust);

//    /// <summary>
//    /// 添加数据
//    /// </summary>
//    /// <param name="tableName">表名</param>
//    /// <param name="record">记录对象</param>
//    /// <param name="callback">执行回调</param>
//    /// <returns>影响行数,-1出错</returns>

    int insertData(String tableName, Map<String,Object> record, Func3<Integer, Connection,Integer> callback);


    /// <summary>
    /// 插入数据记录(根据实体对象)
    /// </summary>
    /// <typeparam name="T">实体类型</typeparam>
    /// <param name="bean">实体对象</param>
    /// <param name="columns">列填充</param>
    /// <returns></returns>
    <T extends BaseEntity> int insert(T bean, String columns);
    /// <summary>
    /// 保存对象
    /// </summary>
    /// <typeparam name="T">泛型类型</typeparam>
    /// <param name="tableName">表名</param>
    /// <param name="bean">记录数据</param>
    /// <param name="cnd">条件对象</param>
    /// <param name="columns">列填充</param>
    /// <returns></returns>
    <T> int save(String tableName, T bean, Cnd cnd, String columns);

    /// <summary>
    /// 保存对象(有注解)
    /// </summary>
    /// <typeparam name="T">实体类型</typeparam>
    /// <param name="bean">实体对象</param>
    /// <returns></returns>
    <T extends BaseEntity> int save(T bean);


    /// <summary>
    /// 保存对象(有注解)
    /// </summary>
    /// <typeparam name="T">实体类型</typeparam>
    /// <param name="bean">实体对象</param>
    /// <param name="cnd">条件</param>
    /// <param name="columns">列填充</param>
    /// <returns></returns>
    <T extends BaseEntity> int save(T bean,Cnd cnd,String columns);

    /// <summary>
    /// 保存对象(普通对象)
    /// </summary>
    /// <typeparam name="T">实体类型</typeparam>
    /// <param name="tableName">数据库表名</param>
    /// <param name="bean">实体对象</param>
    /// <returns></returns>
    <T> int save(String tableName, T bean);





//    /// <summary>
//    /// 保存数据记录
//    /// </summary>
//    /// <param name="tableName">表名</param>
//    /// <param name="record">记录数据</param>
//    /// <param name="keyName">主键名</param>
//    /// <param name="gentype">主键生成类型</param>
//    /// <returns></returns>
//    int save(String tableName, Dictionary<String, Object> record, String keyName, GenType gentype);

    ///// <summary>
    ///// 保存对象
    ///// </summary>
    ///// <typeparam name="T">泛型类型</typeparam>
    ///// <param name="tableName">表名</param>
    ///// <param name="bean">记录数据</param>
    ///// <param name="keyName">主键名</param>
    ///// <param name="genid">主键生成类型</param>
    ///// <returns></returns>
    //int save<T>(String tableName, T bean, String keyName, String genid) where T : BaseEntity, new();


    <T> int save(String tableName, T bean, String keyName, DbGenType gen);

    /// <summary>
    /// 获取分页的sql语句
    /// </summary>
    /// <param name="sqltext">sql语句</param>
    /// <param name="getCount">获取的记录数</param>
    /// <returns></returns>
    SqlText pagingSqlText(SqlText sqltext, int getCount);



    /// <summary>
    /// 当前数据库访问操作对象是否是默认连接
    /// </summary>
    /// <returns></returns>
    boolean isDefaultDataBase();

    /// <summary>
    ///  是否有数据库事物
    /// </summary>
    /// <returns></returns>


    //        void SetBaseSupport(string connStr,DbProviderFactory factory);

//        IDataReader CreateReader(string sqltext, DbParameter[] parameters);

//    /// <summary>
//    /// 创建sql参数
//    /// </summary>
//    /// <param name="key"></param>
//    /// <param name="val"></param>
//    /// <returns></returns>
//    DbParameter CreateParameter(String key, Object val);

//    /// <summary>
//    /// 创建sql命令
//    /// </summary>
//    /// <param name="sqltext"></param>
//    /// <param name="parameter"></param>
//    /// <returns></returns>
//    DbCommand CreateCommand(string cmd, IEnumerable<DbParameter> parameter);

//    /// <summary>
//    /// 创建sql命令
//    /// </summary>
//    /// <param name="cmd"></param>
//    /// <returns></returns>
//    DbCommand CreateCommand(string cmd);

    int exec(SqlText sqltext, Func3<Integer, Connection, Integer> callback);

    //    /// <summary>
//    /// 创建sql命令，这是基本方法
//    /// </summary>
//    /// <param name="cmd"></param>
//    /// <param name="parameter"></param>
//    /// <param name="transaction"></param>
//    /// <returns></returns>
//    PreparedStatement CreateCommand(String cmd, IEnumerable<DbParameter> parameter, DbTransaction transaction);
    /// <summary>
    /// 执行Sql
    /// </summary>
    /// <param name="cmd">SQL命令</param>
    /// <returns></returns>
    int exec(SqlText cmd);

    /***
     * 执行存储过程
     * @param name 函数名称
     * @param parameters 参数集合
     * @return
     */
    List<Object> pro(String name,List<DbParameter> parameters);

//    /// <summary>
//    /// 执行存储过程
//    /// </summary>
//    /// <param name="proName">过程名</param>
//    /// <param name="parameters">过程的参数</param>
//    /// <returns>
//    /// <para>0，false:执行失败</para>
//    /// <para>1，true:执行成功，没有无输出和返回值</para>
//    /// <para>2，单对象:一个输出和返回值对象</para>
//    /// <para>2，List，多个返回值</para>
//    /// </returns>
//    Object Pro(String proName, params DbParameter[] parameters);

    /// <summary>
    /// 开始数据库事务
    /// </summary>
    /// <returns></returns>
    boolean beginTran();

    /// <summary>
    /// 数据库事务回滚
    /// </summary>
    void rollback();

    /// <summary>
    /// 提交数据库事务
    /// </summary>
    boolean commitTran();

    List<String> getValues(SqlText sqltext,String columns);
    List<String> getValues(SqlText sqltext);
    /// <summary>
    /// 获取查询值
    /// </summary>
    /// <typeparam name="T">泛型</typeparam>
    /// <param name="sqltext">sql语句</param>
    /// <returns>数据值</returns>
    <T> T getValue(SqlText sqltext,Class<T> clazz);

    <T> T getValue(String tableName, String columnName, Cnd cnd,Class<T> clazz);

    /// <summary>
    /// 生成ID最大的值
    /// </summary>
    int getMaxId(String tableName, String columnName);

    /// <summary>
    /// 获取一条的sqltext
    /// 1）oracle数据库比较特例，在取第一条记录时需要对原语句折叠
    /// </summary>
    /// <param name="sqltext">原sqltext</param>
    /// <param name="isAppend">是否在原语句追加</param>
    /// <returns>sqlText</returns>
    SqlText onlySqltext(SqlText sqltext, boolean isAppend);

    /// <summary>
    /// 获取一条的sqltext
    /// 1）oracle数据库尽量不要使用本方法（默认取一条记录的追加方式为false）
    /// 2）mysql sqlite可以使用本方法
    /// </summary>
    /// <param name="sqltext"></param>
    /// <returns></returns>
    SqlText onlySqltext(SqlText sqltext);

    /// <summary>
    /// 获取一条的sqlText
    /// </summary>
    /// <param name="sqltext">原语句</param>
    /// <param name="type">数据库类型</param>
    /// <param name="isAppend">追加方法</param>
    /// <returns></returns>
    SqlText onlySqltext(SqlText sqltext, DbDialectType type, boolean isAppend);

    /// <summary>
    /// 分页的sqlText
    /// </summary>
    /// <param name="sqltext">原语句</param>
    /// <param name="pager">pager对象</param>
    /// <returns>sqlText</returns>
    SqlText pagingSqlText(SqlText sqltext, Pager pager);

    ///<summary>处理分页
    /// sqltext 是原sql语句 pager是分页对象 filterRn 是否过滤行号 DBtype是数据库类型
    /// </summary>
    /// <param name="sqltext">原语句</param>
    /// <param name="pager">pager对象</param>
    /// <param name="filterRn">是否过滤行号</param>
    /// <param name="type">数据库类型</param>
    /// 
    SqlText pagingSqlText(SqlText sqltext, Pager pager,DbDialectType type);

    /// <summary>
    /// 数据库表是否存在
    /// </summary>
    /// <param name="tableName">表名</param>
    /// <returns>逻辑型</returns>
    boolean exists(String tableName);

    /// <summary>
    /// 关闭数据库连接
    /// </summary>
    void close();

    boolean isTraning();

    String getMarkCode();

    void setTag(String tag);


    String getErrorMessage();

    <T extends BaseEntity> boolean create(Class<T> clazz);


//    <T extends BaseEntity> List<T> query(SqlText sqltext, Class<? extends BaseEntity> aClass);

//    /// <summary>
//    /// 执行Sql
//    /// </summary>
//    /// <param name="sqlcmd">SQL命令</param>
//    /// <param name="parameters">参数</param>
//    /// <param name="callback">执行回调函数</param>
//    /// <returns></returns>
//    int Exec(String sqlcmd, IEnumerable<DbParameter> parameters,Func<int,DbConnection,int> callback);
//
//    /// <summary>
//    /// 获取表的字段信息
//    /// </summary>
//    /// <param name="tableName">表名或记录集</param>
//    /// <param name="split">是否sql拆分</param>
//    /// <returns></returns>
//    List<Dictionary<String, Object>> GetFieldInfo(String tableName,bool split=true);
//
//    /// <summary>
//    /// 获取当前数据库下的所有数据库表
//    /// </summary>
//    /// <param name="schemaName">模式名称</param>
//    /// <param name="tableName">表名</param>
//    /// <returns></returns>
//    List<Record> GetTables(String schemaName, String tableName = "%%");
//    /// <summary>
//    /// 添加回滚事件
//    /// </summary>
//    /// <param name="action"></param>
    void AddRollbackEvent(Action0 action);
//    /// <summary>
//    /// 添加提交事件
//    /// </summary>
//    /// <param name="action"></param>
    void AddCommitEvent(Action0 action);



    List<Map<String, Object>> getFieldInfo(String table);
}