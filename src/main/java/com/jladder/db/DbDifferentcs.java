package com.jladder.db;

import com.jladder.data.Record;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;

import java.util.List;
import java.util.regex.Matcher;

public class DbDifferentcs {
    /// <summary>
    /// 映射类型
    /// </summary>
    /// <param name="type">类型名</param>
    /// <returns></returns>
    public static String MappingType(String type)
    {
        if (Strings.isBlank(type)) return "string";
        switch (type)
        {
            case "VARCHAR":
                return "string";
            case "integer":
                return "int";
            case "mysql.data.types.mysqldate":
                return "date";
            case "java.sql.timestamp":
                return "datetime";
            default:
                return type;
        }
    }
    /// <summary>
    /// mysql数据库获取字段映射以及备注
    /// </summary>
    /// <param name="tableName">数据库表名</param>
    /// <param name="schemaName">模式名称</param>
    /// <param name="conn">连接器</param>
    /// <returns></returns>
    public static List<Record> GetTableInfoForMysql(String tableName, String schemaName, String conn)
    {
        if (Strings.isBlank(tableName)) return null;
        String sqltext = "select column_name fieldname,data_type type,column_type,column_comment as title," +
                "extra as auto,CHARACTER_MAXIMUM_LENGTH as lenth, IS_NULLABLE as isNull, column_default as dvalue " +
                "from INFORMATION_SCHEMA.COLUMNS Where table_name like '"+tableName+"' and table_schema like '"+schemaName+"'";


        IDao dao = DaoSeesion.GetDao(conn);
        try
        {
            List<Record> result = dao.query(new SqlText(sqltext), x ->
            {
                x.put("fieldname", x.getString("fieldname").toLowerCase());
//                x.put("type", FieldAdaptor.FindFieldTypeText(x.GetString("type")));
                x.put("type", x.getString("type"));
                if (Strings.hasValue(x.getString("auto"))) x.put("gen", "autonum");
                Matcher match = Regex.match(x.getString("column_type"), x.getString("type") + "\\((\\d*)\\,?(\\d*)\\)");

                if (match.find() && Strings.isBlank(x.getString("length"))) {
                    x.put("length",match.group(1));
                    if (Strings.hasValue(match.group(2))) x.put("holden", match.group(2));
                }
                x.put("isnull", x.getString("isNull") == "YES");
                x.delete("column_type", "auto");
                String dvalue = x.getString("dvalue");
                if (dvalue.equals("NULL")  || dvalue .equals( "''") || dvalue.equals("CURRENT_TIMESTAMP") || dvalue.equals("uuid()") ){
                    x.delete("dvalue");
                }
                return true;
            });
            return result;
        }
        finally
        {
            dao.close();
        }
    }
}
