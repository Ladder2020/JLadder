package com.jladder.db;

import com.jladder.data.Record;
import com.jladder.db.bean.FieldInfo;
import com.jladder.db.enums.DbSqlDataType;
import com.jladder.db.jdbc.GroupBy;
import com.jladder.db.jdbc.OrderBy;
import com.jladder.lang.func.Tuple2;
import com.jladder.lang.*;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// 便捷sql处理类，
/// 主要是DML操作和部分DDL
/// </summary>
public class Sqls
{
    /// <summary>
    ///  数据访问对象
    /// </summary>
    public IDao Dao= null;

    /// <summary>
    /// sql动作
    /// </summary>
    public DbSqlDataType Action = DbSqlDataType.Query;
    /// <summary>
    /// 表名
    /// </summary>
    public String TableName;
    /// <summary>
    /// 条件对象
    /// </summary>
    public Cnd Condition;
    /// <summary>
    /// 排序对象
    /// </summary>
    public OrderBy Order;
    /// <summary>
    /// 分组对象
    /// </summary>
    public GroupBy Group;
    /// <summary>
    /// 片选字段
    /// </summary>
    public List<FieldInfo> Columns;

    /// <summary>
    /// 基本构造
    /// </summary>
    public Sqls()
    {
        Group=new GroupBy();
        Order=new OrderBy();
        Columns=new ArrayList<FieldInfo>();
        Condition=new Cnd();
    }
    /// <summary>
    /// 从文本解码
    /// </summary>
    /// <param name="sqltext"></param>
    /// <returns></returns>
    public static Sqls Decoder(String sqltext)
    {
        return new Sqls(sqltext);
    }
    /// <summary>
    /// 以YFdao构造
    /// </summary>
    /// <param name="dao">Dao对象</param>
    public Sqls(IDao dao)
    {
        this.Dao = dao;
        Group = new GroupBy();
        Order = new OrderBy();
        Columns = new ArrayList<FieldInfo>();
        Condition = new Cnd();
    }
    /// <summary>
    /// 以文本方式构造
    /// </summary>
    /// <param name="sqltext">sql文本</param>
    public Sqls(String sqltext)
    {
        sqltext = sqltext.trim();
        sqltext = Regex.replace(sqltext,"\\s*\\n\\s*"," ");
        //json对象
        if (sqltext.startsWith("{")&&sqltext.endsWith("}"))
        {
            //分别解析 字段 分组  条件 排序等部分
            Record sRecord = Json.toObject(sqltext,Record.class);
            this.TableName = sRecord.getString("tableName,table", true);
            this.AddColumn(sRecord.getString("columns,column", true));
            String tempText = sRecord.getString("condition,where", true);
            if(!Strings.isBlank(tempText))this.Condition=new Cnd(tempText);
            tempText = sRecord.getString("orderby,order", true);
            if (!Strings.isBlank(tempText)) this.Order = new OrderBy(tempText);
            tempText = sRecord.getString("groupby,group", true);
            if (!Strings.isBlank(tempText)) this.Group = new GroupBy(tempText);
        }
        else
        //纯文本sql语句，注意：此代码不能100%应对需要，别太复杂，2015-12-29
        {
            throw Core.makeThrow("未实现");
//            //各种匹配正则
//            int length = sqltext.length();
//            //select
//            var selectMatch = Regex.Match(sqltext, "\\^s*select\\s+", RegexOptions.IgnoreCase);
//            //from
//            var fromMatch = Regex.Match(sqltext, "\\s*from\\s*", RegexOptions.IgnoreCase);
//            //where,以第一个where
//            var whereMatch = Regex.Match(sqltext, "\\s*where\\s*", RegexOptions.IgnoreCase);
//            //order 以最后一个order
//            var orderMatch = Regex.Match(sqltext, "\\s*order\\sby\\s*([\\s\\S]*)$", RegexOptions.IgnoreCase);
//            //group 以最后一个分组
//            var groupMatch = Regex.Match(sqltext, "\\s*group\\sby\\s*([\\s\\S]*?)(order|$)", RegexOptions.IgnoreCase);
//            if (selectMatch.Success && fromMatch.Success)
//            {
//                this.AddColumn(Strings.SubString(sqltext, selectMatch.Index + selectMatch.Length, fromMatch.Index));
//            }
//            if (whereMatch.Success)
//            {
//                this.TableName=Strings.SubString(sqltext, fromMatch.Index + fromMatch.Length, whereMatch.Index);
//                int endindex = -1;
//                if (orderMatch.Success) endindex = orderMatch.Index;
//                if (groupMatch.Success) endindex = groupMatch.Index;
//                if (endindex < 1) endindex = length;
//                var where = Strings.SubString(sqltext, whereMatch.Index + whereMatch.Length, endindex);
//                this.Condition=new Cnd(where);
//
//            }
//            //如果分组匹配成功
//            if (groupMatch.Success)
//            {
//                if (String.IsNullOrEmpty(this.TableName)&&fromMatch.Success)
//                    this.TableName = Strings.SubString(sqltext, fromMatch.Index + fromMatch.Length, groupMatch.Index);
//                this.Group=new GroupBy(groupMatch.Groups[1].Value);
//            }
//            //如果排序匹配成功
//            if (orderMatch.Success)
//            {
//                if (String.IsNullOrEmpty(this.TableName) && fromMatch.Success)
//                    this.TableName = Strings.SubString(sqltext, fromMatch.Index + fromMatch.Length, orderMatch.Index);
////                    var order = Strings.SubString(sqltext, orderMatch.Index + orderMatch.Length, length);
//                this.Order=new OrderBy(orderMatch.Groups[1].Value);
//            }
//            if (String.IsNullOrEmpty(this.TableName)&&fromMatch.Success)
//            {
//                this.TableName = Strings.SubString(sqltext,fromMatch.Index+fromMatch.Length, length);
//            }
//            if (String.IsNullOrEmpty(this.TableName) && !fromMatch.Success)
//            {
//                this.TableName = Strings.SubString(sqltext,0, length);
//            }
        }
    }
    /// <summary>
    /// 获取条件文本
    /// </summary>
    /// <returns></returns>
    public String GetWhere()
    {
        return Condition == null ? "" : Condition.getWhere(false,true);
    }
    /// <summary>
    /// 获取分组文本
    /// </summary>
    /// <returns></returns>
    public String GetGroup()
    {
        if (this.Group == null) return "";
        return this.Group.toString();
    }
    /// <summary>
    /// 获取排序文本
    /// </summary>
    /// <returns></returns>
    public String GetOrder()
    {
        if (this.Order == null) return "";
        return this.Order.toString();
    }
    /// <summary>
    /// 添加字段
    /// </summary>
    /// <param name="columnStr">列文本</param>
    public Sqls AddColumn(String columnStr)
    {
        //空字符或者*则返回
        if (Strings.isBlank(columnStr)) return this;
        if (Regex.isMatch(columnStr, "^\\s*\\*\\s*"))
        {
            Columns.clear();
            return this;
        }
        String[] cols = columnStr.split(",");
        if(Columns==null)Columns=new ArrayList<FieldInfo>();
        for (String col : cols)
        {
            String[] fieldArray = Regex.split(col, "(@@)|(\\s+as\\s+)");
            String fieldname = fieldArray[0];
            String asname = fieldArray[fieldArray.length-1];
            Tuple2<Boolean, FieldInfo> firstOrDefault = Collections.first(Columns, x -> Core.is( x.fieldname,fieldname) && Core.is( x.as , asname));
            if (firstOrDefault.item1)
            {
                Columns.add(new FieldInfo(fieldname,asname));
            }
            else
            {
                firstOrDefault.item2.as = asname;
                firstOrDefault.item2.fieldname = fieldname;
            }
        }
        return this;
    }
    /// <summary>
    /// 添加字段
    /// </summary>
    /// <param name="field">字段信息对象</param>
    /// <returns></returns>
    public Sqls AddColumn(FieldInfo field)
    {
        if (field == null || Strings.isBlank(field.fieldname)) return this;
        if(Columns==null)Columns=new ArrayList<FieldInfo>();
        Tuple2<Boolean, FieldInfo> oldfield = Collections.first(Columns, a -> field.fieldname.equals(a.fieldname) && a.as.equals(field.as));
        if (!oldfield.item1)
        {
            Columns.add(field);
        }
        else
        {
            throw Core.makeThrow("未实现[0233]");
            //Core.CopyData(oldfield, field);
        }
        return this;
    }
    /// <summary>
    /// 获取sql组成语句
    /// </summary>
    /// <returns></returns>
    public String SqlText()
    {
        throw Core.makeThrow("未实现");
//        String sqltext = "";
//
//        switch (Action)
//        {
//            case Query:
//                sqltext = "select " +
//                        ((Columns==null||Columns.size()<1)?"*":
//                                (Columns.Aggregate("",(c, fieldinfo) =>
//                                        c +=fieldinfo.FieldName
//                                + ((string.IsNullOrEmpty(fieldinfo.As) || fieldinfo.As.Equals(fieldinfo.FieldName)) ? "" : " as " + fieldinfo.As)
//                                + ",").RightLess(1)))
//                +" from " +TableName+Condition.GetWhere(true, false) + Group.ToString()+Order.ToString();
//                break;
//        }
//
//        return sqltext;
    }
    /// <summary>
    /// 设置数据动作
    /// </summary>
    /// <param name="action">数据动作</param>
    /// <returns></returns>
    public Sqls SetAction(DbSqlDataType action)
    {
        Action = action;
        return this;
    }
//    /// <summary>
//    /// 查询
//    /// </summary>
//    public List<Record> Query(IDao dao=null) => (dao??Dao)?.Query(new SqlText(SetAction(SqlDataAction.Query).SqlText()));
//
//    /// <summary>
//    /// 查询
//    /// </summary>
//    public String GetValue() => Dao?.GetValue<string>(new SqlText(SetAction(SqlDataAction.Query).SqlText()));


    /// <summary>
    /// 获取列文本
    /// </summary>
    /// <returns></returns>
    public String GetColumns()
    {
        throw Core.makeThrow("未实现");
//        if (this.Columns == null) return "*";
//        String orderText = ((Columns == null || Columns.Count < 1) ? "*" :
//                (Columns.Aggregate("", (c, fieldinfo) =>
//                        c += fieldinfo.FieldName
//                + ((string.IsNullOrEmpty(fieldinfo.As) || fieldinfo.As.Equals(fieldinfo.FieldName)) ? "" : " as " + fieldinfo.As)
//                + ",").RightLess(1)));
//        return orderText;
    }


}
