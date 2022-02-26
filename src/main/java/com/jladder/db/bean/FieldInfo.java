package com.jladder.db.bean;
import com.jladder.db.enums.DbConst;
import com.jladder.lang.Collections;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import java.util.Map;

public class FieldInfo {
    /// <summary>
    /// 字段名称
    /// </summary>
    public String name;
    /// <summary>
    /// 数据类型
    /// </summary>
    public String type;
    /// <summary>
    /// 类型对应码
    /// </summary>
    public int datatype= -1;
    /// <summary>
    /// 默认值,defalut为关键字
    /// </summary>
    public String dvalue;
    /// <summary>
    /// 长度,-1时默认适配器的字段长度
    /// </summary>
    public int length = -1;

    /// <summary>
    /// 保留长度，用于float和double类型
    /// </summary>

    public int holden;
    /// <summary>
    /// 主键
    /// </summary>
    public boolean pk ;
    /// <summary>
    /// 是否可空
    /// </summary>
    public boolean isNull;


    /// <summary>
    /// 原名称,用于字段名的数据切换，在查询时用于as
    /// </summary>
    public String oldname;
    /// <summary>
    /// 说明描述
    /// </summary>
    public String descr;
    /// <summary>
    /// 数据自动生成类型
    /// </summary>
    public int gen;
    /// <summary>
    /// 当对字段进行操作时，记录字段的状态，可以是CREATE Alter 等
    /// </summary>
    public int actioncode  = 0;
    /// <summary>
    /// 字段值
    /// </summary>
    public Object value;
    /// <summary>
    /// 唯一性
    /// </summary>
    public boolean unique  = false;

    /// <summary>
    /// 字段名称，和name属性功能相同
    /// </summary>
    public String fieldname;
    /// <summary>
    /// 字段别名
    /// </summary>
    public String as;
    /// <summary>
    /// 标题
    /// </summary>
    public String title;


    public FieldInfo(){}

    public FieldInfo(String fieldname,String as){

    }
    /**
     * 解析字段信息
     * @param source 源数据
     * @return
     */
    public static FieldInfo parse(Map<String, Object> source){
        FieldInfo fi = new FieldInfo();
        fi.fieldname= Collections.getString(source,"fieldname","", true);
        fi.type= Collections.getString(source,"type","", true);
        fi.length= Collections.getInt(source,"length", true);
        if (fi.length < 1) fi.length = -1;
        String gen =Collections.getString(source,"gen","", true);
        fi.dvalue = Collections.getString(source,"dvalue","", true);
        fi.descr = Collections.getString(source,"descr","", true);
        fi.actioncode = Collections.getInt(source,"actioncode", true);
        if (Strings.hasValue(gen)){
            if (Regex.isMatch(gen, "autonum")){
                fi.pk = true;
                fi.gen = DbConst.Gen_AutoNum;
            }
        }
        if (Strings.hasValue(gen) && Regex.isMatch(gen, "(uuid)|(id)")){
            fi.isNull = false;
        }
        return fi;
    }
}
