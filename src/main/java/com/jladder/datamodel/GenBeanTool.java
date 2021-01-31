package com.jladder.datamodel;

import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.db.enums.DbSqlDataType;
import com.jladder.lang.Collections;
import com.jladder.lang.Core;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.lang.Times;

import java.util.*;
import java.util.regex.Matcher;

public class GenBeanTool {

    public static Record GenBean(IDataModel dm, Record beanDic, DbSqlDataType option, StringBuilder reMessage) {
        if(reMessage==null)reMessage = new StringBuilder();
        Record saveEntity = new Record();
        //逃逸字段
        Map<String, String> freedic = null;
        if (beanDic != null)
        {
            freedic = new HashMap<String,String>();
            Map<String, String> finalFreedic = freedic;
            beanDic.forEach((k, v)->{
                if(Regex.isMatch(k,"^[\\$@#\\*]")){
                    finalFreedic.put(k.substring(1),k);
                }
            });
        }
        List<Map<String,Object>> columnsMapping = com.jladder.lang.Collections.where(dm.ParseColumsList(), x->{
            String isext = com.jladder.lang.Collections.getString(x,"isext",true);
            if(Strings.isBlank(isext))return true;
            if("1".equals(isext))return false;
            if("true".equals(isext.toLowerCase()))return false;
            return true;
        });

        for (Map<String, Object> map : columnsMapping)
        {
            String  fieldname = com.jladder.lang.Collections.getString(map,"fieldname,name,oldname");
            //字段名称为空
            if (Strings.isBlank(fieldname)) continue;

            String asname = com.jladder.lang.Collections.getString(map, "as");
            String dataType = com.jladder.lang.Collections.getString(map, "type").toLowerCase();
            String freekey = "";
            if(freedic!=null)freekey = com.jladder.lang.Collections.haveKey(freedic,fieldname,asname);
            if (Strings.hasValue(freekey))
            {
                String  rawKey = freedic.get(freekey);
                //                    saveEntity.Put(rawKey, beanDic[rawKey]);
                saveEntity.put("*" + fieldname, beanDic.get(rawKey));
                continue;
            }
            //自动生成字段
            String gen = com.jladder.lang.Collections.getString(map,"gen").toLowerCase();

            if (Strings.hasValue(gen) && !Core.is(map.get("autonum"),true,1,"true"))
            {
                //生成类型存在并且默认值为空才启动生产
                if (Strings.isBlank(com.jladder.lang.Collections.getString(map,"dvalue,defaultvalue,default")))
                {
                    switch (gen)
                    {
                        case "id":
                            //如果id为整数型，一般由数据库自增
                            if (option == DbSqlDataType.Insert)
                                saveEntity.put(fieldname, "{sql:'(select Max(" + fieldname + ")+1 from " + dm.GetTableName() + ")'}");
                            break;
                        case "uuid":
                            if (option == DbSqlDataType.Insert)
                            {
                                saveEntity.put(fieldname, Strings.isBlank(com.jladder.lang.Collections.getString(beanDic,fieldname)) ? Core.genUuid() : com.jladder.lang.Collections.getString(beanDic,fieldname));
                            }
                            break;
                        case "nuid":
                            if (option == DbSqlDataType.Insert)
                            {
//                                saveEntity.put(fieldname, beanDic.GetString(fieldname).IsBlank() ? Core.GenNuid() : beanDic.GetString(fieldname));
                            }
                            break;
                        case "sysdate":
                        case "date":
                        case "datetime":
                            if (Regex.isMatch(com.jladder.lang.Collections.getString(map,"sign"), "updatetime"))
                            {
                                saveEntity.put(fieldname, Times.getNow());
                                break;
                            }
                            //bean数据没有值
                            if (Strings.isBlank(com.jladder.lang.Collections.getString(beanDic,fieldname)))
                            {
                                //createtime字段特殊性，只插入的时候进行数据填充
                                if (!Regex.isMatch(com.jladder.lang.Collections.getString(map,"sign"), "createtime") || option.getIndex() == 1)
                                {
                                    if (dataType.equals("date"))
                                    {
                                        Object _date = Times.getDate();
                                        saveEntity.put(fieldname, _date);
                                    }
                                    else saveEntity.put(fieldname,Times.getNow());
                                }
                            }
                            else
                            {//if template hava value
                                String value = com.jladder.lang.Collections.getString(beanDic,fieldname);
                                if (dataType.equals("date"))
                                {
                                    long _t = Times.ams(value);
                                    if (_t > 0)
                                    {
                                        Object obj = Times.D(_t);
                                        saveEntity.put(fieldname, obj);
                                    }
                                    else { reMessage.append("时间字段不能阅读"); return null; }
                                }
                                else saveEntity.put(fieldname, value);
                            }
                            break;
                    }
                }
            }
            //非自动生成字段
            else
            {
                //如果原Bean字典中不含有字段键 则continue
                String key = com.jladder.lang.Collections.haveKey(beanDic,fieldname + (Strings.hasValue(asname) ? "," + asname : ""));
                if (Strings.isBlank(key))
                {
                    //非新增时，跳过
                    if (option != DbSqlDataType.Insert) continue;
                    String dvalue = com.jladder.lang.Collections.getString(map,"dvalue,defaultvalue,default");
                    //如默认值为空也跳过
                    if (Strings.isBlank(dvalue)) continue;
                    key = fieldname;
                    //默认值并替换环境变量
                    beanDic.put(key, com.jladder.lang.Collections.getString(map,"sign") != "rawdata" ? Strings.mapping(dvalue) : dvalue);
                }
                String str = com.jladder.lang.Collections.getString(beanDic,key);
                //检查bean实体中key的值是否为空
                if (Strings.hasValue(str))
                {
                    Object value = beanDic.get(key);
                    //如果是文本进行环境变量替换
                    if (value instanceof String &&  !("rawdata".equals(com.jladder.lang.Collections.getString(map,"sign"))))
                    {
                        value = Strings.mapping((value.toString()));
                    }
                    if (Strings.isBlank(dataType)) saveEntity.put(fieldname, value);
                    else
                    {
                        //判断字段类型
                        switch (dataType)
                        {
                            case "date":
                            case "datetime":
                                if (value instanceof Date)
                                {
                                    saveEntity.put(fieldname, value);
                                    break;
                                }
                                if (!Strings.isBlank(str.trim()) && str.length() > 4)
                                {
                                    long _t = Times.ams(str);
                                    if (_t > 0)
                                    {
                                        Object obj = Times.D(_t);
                                        saveEntity.put(fieldname, obj);
                                    }
                                    else
                                    {
                                        reMessage.append("时间字段不能阅读");
                                        return null;
                                    }
                                }
                            break;
                            case "number":
                            case "int":
                            case "double":
                            case "float":
                            case "integer":
                            case "long":
                                saveEntity.put(fieldname, Strings.isBlank(str) ? 0 : value);
                                break;
                            default:
                                saveEntity.put(fieldname, value);
                                break;
                        }
                    }
                }
                else
                {
                    if (option == DbSqlDataType.Update || option == DbSqlDataType.Save)
                    {
                        saveEntity.put(fieldname, null);
                    }
                }
            }
        }
        return saveEntity;
    }
    public static Record GenBean(IDataModel dm, Record bean, DbSqlDataType option) {
        return GenBean(dm,bean,option,null);
    }
    public static Record GenBean(IDataModel dm, String data, int option) {
        return GenBean(dm,Record.parse(data),DbSqlDataType.get(option),null);
    }

    public static Receipt<Record> DataMatch(Record source, Map<String,Object> target){

        Record bean = new Record();
        //循环体
        for (String k : target.keySet())
        {
            String value = Collections.getString(target,k);
            //形式如下:
            // 1)@@node.id@@
            // 2)@@node@@.id
            // 3)@@node.id
            // 4)${node.id}
            // 5)${id}---->留给其他方法实现
            Matcher match = Regex.match(value, "^(@@(\\w*)(@@)?.(\\w*)(@@)?)|(\\$\\{(\\w*).(\\w*)})$");
            if (match.find())
            {
                String noeName = "";
                if (Strings.hasValue(match.group(2))) noeName = match.group(2);
                if (Strings.hasValue(match.group(7))) noeName = match.group(7);
                String fieldname = "";
                if (Strings.hasValue(match.group(4))) fieldname = match.group(4);
                if (Strings.hasValue(match.group(8))) fieldname = match.group(8);
                if (!Strings.isBlank(noeName) && !Strings.isBlank(fieldname)){
                    if (source.containsKey(noeName)  && source.get(noeName) != null && source.get(noeName) instanceof Record && ((Record)source.get(noeName)).containsKey(fieldname)){
                        bean.put(k, target.get(k).toString().replace(match.group(0), ((Record)source.get(noeName)).get(fieldname).toString()));//((Record)source[noeName])[fieldname]
                        //bean.Put(k, ((Record)source[noeName])[fieldname]);
                    }
                    else return new Receipt(false, "数据变量不能阅读");
                }
                else bean.put(k, target.get(k));
            }
            else bean.put(k, target.get(k));
        }
        return new Receipt().setData(bean);


    }

}
