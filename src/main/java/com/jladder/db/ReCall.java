package com.jladder.db;

import com.jladder.actions.impl.QueryAction;
import com.jladder.datamodel.IDataModel;
import com.jladder.lang.Core;
import com.jladder.lang.Strings;

import java.util.List;

public class ReCall
{
    /// <summary>
    /// 键表名
    /// </summary>
    public String TableName;
    /// <summary>
    /// 节选列
    /// </summary>
    public String Columns;
    /// <summary>
    /// 条件对象
    /// </summary>
    public Object Condition;
    /// <summary>
    /// 扩展参数
    /// </summary>
    public String Param;
    /// <summary>
    /// 是否进行查询
    /// </summary>
    public boolean Query;

    public ReCall()
    {
    }
    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="columns">节选列</param>
    /// <param name="condition">条件对象</param>
    /// <param name="param">扩展参数</param>
    public ReCall(String tableName, String columns, Object condition)
    {
        TableName = tableName;
        Columns = columns;
        Condition = condition;
    }
    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="columns">节选列</param>
    /// <param name="condition">条件对象</param>
    /// <param name="param">扩展参数</param>
    public ReCall(String tableName, String columns, Object condition,String param)
    {
        TableName = tableName;
        Columns = columns;
        Condition = condition;
        Param = param;

    }
    /// <summary>
    /// 设置是否查值
    /// </summary>
    /// <param name="query"></param>
    /// <returns></returns>
    public ReCall setQuery(boolean query)
    {
        Query = query;
        return this;
    }
    /// <summary>
    /// 设置查询条件对象
    /// </summary>
    /// <param name="condition">条件对象</param>
    /// <returns></returns>
    public ReCall setCondition(Object condition)
    {
        Condition = condition;
        return this;
    }
    /// <summary>
    /// 设置扩展参数
    /// </summary>
    /// <param name="param">扩展参数</param>
    /// <returns></returns>
    public ReCall setParam(String param)
    {
        Param = param;
        return this;
    }
    public List<String> getValues(){
        return QueryAction.getValues(TableName,Columns,Condition,Param);
    }
    public String getSql(){
        IDataModel dm = DaoSeesion.getDataModel(TableName, Param);
        if (dm == null || dm.isNull()) throw Core.makeThrow("回溯数据查询异常[0101]");
        dm.matchColumns(Columns);
        dm.setCondition(Cnd.parse(Condition,dm));
        return dm.getSqlText().toString();
    }
    public SqlText toSqlText(){
        if(Query){
            List<String> rst = QueryAction.getValues(TableName,Columns,Condition,Param);
            if (rst == null) throw Core.makeThrow("回溯数据查询异常[097]");
            return new SqlText(Strings.arraytext(rst));
        }else{
            IDataModel dm = DaoSeesion.getDataModel(TableName, Param);
            if (dm == null || dm.isNull()) throw Core.makeThrow("回溯数据查询异常[0101]");
            dm.matchColumns(Columns);
            dm.setCondition(Cnd.parse(Condition,dm));
            return dm.getSqlText();
        }
    }
}