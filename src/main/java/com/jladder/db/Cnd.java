package com.jladder.db;
import com.alibaba.fastjson.JSONObject;
import com.jladder.Ladder;
import com.jladder.actions.impl.QueryAction;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.datamodel.FieldMapping;
import com.jladder.datamodel.IDataModel;
import com.jladder.db.enums.DbDialectType;
import com.jladder.lang.Collections;
import com.jladder.lang.func.Func2;
import com.jladder.lang.func.Tuple2;
import com.jladder.lang.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Cnd {

    public final int AND=0;
    public final int OR=1;
    public DbDialectType dialect;
    public List<List<CndStruct>> Most = new ArrayList<List<CndStruct>>();
    public List<DbParameter>  parameters = new ArrayList<>();;

    private String whereText = "";

    public FieldMapping mapping = new FieldMapping();

    public String asKey = "as";
    public String typeKey = "type";
    public String fieldnameKey = "fieldname";

    public Cnd In(String propname, ReCall recall)
    {
        return put(propname, "in", recall);
    }

    /***
     * 且条件
     * @param fieldname 字段
     * @param op 操作符
     * @param value 数值
     * @return
     */
    public Cnd And(String fieldname, String op, Object value)
    {
        put(fieldname, op, value, AND);
        return this;
    }

    /**
     * 并且
     * @param cnds 组合
     * @return {@link Cnd}
     */
    public Cnd and(Cnd ... cnds){

        if (cnds == null || cnds.length < 1) return this;
        final String[] wstr = {whereText};
        boolean haswstr = false;
        if (Strings.hasValue(wstr[0]))
        {
            haswstr = true;
            if(!wstr[0].startsWith("(") || !wstr[0].endsWith(")"))wstr[0] = "(" + wstr[0] + ")";
        }
        AtomicBoolean finalHaswstr = new AtomicBoolean(haswstr);
        com.jladder.lang.Collections.where(cnds, one->one != null && Strings.hasValue(one.whereText)).forEach(one->{
            wstr[0] += (finalHaswstr.get() ? " and " : "")+"("+one.whereText+")";
            Most.addAll(one.Most);
            parameters.addAll(one.parameters);
            finalHaswstr.set(true);
        });
        this.whereText = wstr[0];
        return this;
    }

    public Cnd(){}

    /**
     * 基本构造
     * @param dm 数据模型
     */
    public Cnd(IDataModel dm)
    {
        dialect = DaoSeesion.GetDao().getDialect();
        initFieldMapping(dm.parseColumsList());
    }
    public Cnd(String conditionText)
    {
        dialect = Ladder.Settings().getDbDialect();
        this.put(conditionText);
    }

    /**
     * 初始化
     * @param fullColumn 全字段列
     * @param dialect 数据库方言
     */
    public Cnd(List<Map<String, Object>> fullColumn,DbDialectType dialect){
        this.dialect = dialect;
        initFieldMapping(fullColumn);
    }
    public Cnd(boolean can,String propnames,Object value){
        if(can)this.put(propnames,"=",value);
    }
    public Cnd(boolean can,String propnames,String op,Object value){
        if(can)this.put(propnames,op,value);
    }
    public Cnd(String propnames,Object value){
        this.put(propnames,"=",value);
    }
    public Cnd(String propnames,String op,Object value){
        this.put(propnames,op,value);
    }
    /**
     * 放置条件
     * @param cndDic 条件键值对象
     * @return
     */
    public Cnd put(Map<String, Object> cndDic){
        if (cndDic == null) return this;
        for (Map.Entry<String, Object> item : cndDic.entrySet()){
            if (item.getValue() == null&& !Regex.isMatch(item.getKey(), ":is")) continue;
            String[] keys=null;
            //var keys = Regex.Split(item.Key, @"\||,");
            if (item.getKey().indexOf("|") > 0){
                keys = item.getKey().split("|");
            }
            else{
                List<String> ks = Strings.splitByComma(item.getKey());
                keys = ks.toArray(new String[ks.size()]);
            }
            if(Core.isEmpty(keys))return this;
            if (keys.length == 1 && !Regex.isMatch(keys[0], "((\\$and\\d*)|(\\$or\\d*))$")){
                String[] cstr = keys[0].split(":");
                String fileName = cstr[0];
                String op = cstr.length > 1 ? cstr[1] : "=";
                if ((cstr.length > 2 &&  "or".equals(cstr[2].toLowerCase()))){
                    put(fileName, op, item.getValue(), OR);
                }
                else {
                    put(fileName, op, item.getValue(), AND);
                }
                continue;
            }
            Cnd cnd = new Cnd();
            cnd.mapping = mapping;
            cnd.asKey=asKey;
            cnd.dialect=dialect;
            for (String key : keys){
                String[] cstr = key.split(":");
                String fileName = cstr[0];
                if (Regex.isMatch(fileName, "\\$and\\d*$")) fileName = "and";
                if (Regex.isMatch(fileName, "\\$or\\d*$")) fileName = "or";
                String op = cstr.length > 1 ? cstr[1] : "=";
                if (cstr.length < 3 || (cstr.length > 2 && "or".equals(cstr[2].toLowerCase()))) {
                    cnd.put(fileName, op, item.getValue(), OR);
                }
                else {
                    cnd.put(fileName, op, item.getValue(), AND);
                }
            }
            if (Regex.isMatch(item.getKey(), "\\$or\\d*$")) or(cnd);
            else  and(cnd);
        }
        return this;
    }

    /**
     * 放置条件
     * @param area 多组条件
     * @return
     */
    public Cnd put(List<Map<String, Object>> area){
        for (Map<String, Object> dic : area){
            String name = com.jladder.lang.Collections.getString(dic,"fieldname",null);
            if (Strings.isBlank(name)) name = com.jladder.lang.Collections.getString(dic,"name",null);
            if (Strings.isBlank(name)) continue;
            this.put(
                    name,
                    com.jladder.lang.Collections.getString(dic,"op","") + com.jladder.lang.Collections.getString(dic,"option",""),
                    dic.get("value"),
                    AND
            );
        }
        return this;
    }
    /**
     * 放置条件
     * @param condition 条件文本
     * @return
     */
    public Cnd put(String condition){
        if (Strings.isBlank(condition)) return this;
        condition = condition.trim();
        if (condition.length() < 1) return this;
        condition = Regex.replace(condition, "^\\s*where\\s*", "");
        if (Strings.isJson(condition,1)){
            Map<String, Object> cmap = Json.toObject(condition,new TypeReference<Map<String,Object>>(){});
            return this.put(cmap);
        }
        if (Strings.isJson(condition,2)){
            if (Regex.isMatch(condition, "^\\s*\\[\\s*\\[")){
                List<List<Map<String, Object>>> list = Json.toObject(condition, new TypeReference<List<List<Map<String, Object>>>>(){});
                Cnd list_condition=new Cnd();
                list.forEach(li ->{
                    Cnd cnd = new Cnd();
                    cnd.mapping = mapping;
                    cnd.dialect = dialect;
                    cnd.put(li);
                    list_condition.or(cnd);
                });
                and(list_condition);
                return this;
            }
            else{
                List<Map<String, Object>> list = Json.toObject(condition,new TypeReference<List<Map<String, Object>>>(){});
                for (Map<String, Object> dic : list){
                    String name = com.jladder.lang.Collections.getString(dic,"fieldname",null);
                    if (Strings.isBlank(name)) name = com.jladder.lang.Collections.getString(dic,"name",null);
                    if (Strings.isBlank(name)) continue;
                    this.put(
                            name,
                            com.jladder.lang.Collections.getString(dic,"op","") + com.jladder.lang.Collections.getString(dic,"option",""),
                            dic.get("value"),
                            AND
                    );
                }
            }
            return this;
        }
        if (Strings.hasValue(condition)){
            pushWhereString(condition, AND);
        }
        return this;
    }

    public Cnd put(String fields,Object val){
        return put(fields,"=",val,this.AND);
    }

    public Cnd put(String fields, String op, Object val){
        return put(fields,op,val,this.AND);
    }
    /**
     * 解析条件
     * @param cnd 原数据对象
     * @param mapping 全字段
     * @param dialect 数据库方言
     * @return
     */
    public static Cnd parseRecord(Record cnd, Map<String, Map<String, Object>> mapping, DbDialectType dialect){
        if (cnd == null || Strings.isBlank(cnd.toString())) return null;
        if (mapping == null) return parse(cnd, null, dialect);
        Cnd ret = new Cnd();
        ret.mapping = new FieldMapping(mapping);
        ret.dialect = dialect;
        return ret.put(cnd);
    }

    public Cnd put(boolean can, String fields, Object val) {
        if (can) return put(fields, val);
        return this;
    }
    public Cnd put(boolean can, String fields, String op, Object val)
    {
        if (can) return put(fields, op, val);
        return this;
    }
    public Cnd put(boolean can, String fields, String op, Object val, int option)
    {
        if (can) return put(fields, op, val, option);
        return this;
    }


    /***
     * 放置条件
     * @param fields 属性名
     * @param op 符号
     * @param val 值
     * @param option 选项，Cnd.AND和Cnd.OR
     * @return
     */
    public Cnd put(String fields, String op, Object val, int option) {
        if (Strings.isBlank(fields))throw Core.makeThrow("条件对象属性值不能为空");
        if ("and".equalsIgnoreCase(fields.trim())){
            and((val instanceof Record) || (val instanceof JSONObject)  ? Cnd.parseRecord(Record.parse(val),mapping.fm,dialect):Cnd.parse(val));
            return this;
        }
        if ("or".equalsIgnoreCase(fields.trim())){
            or((val instanceof Record) || (val instanceof JSONObject) ? Cnd.parseRecord(Record.parse(val), mapping.fm, dialect) : Cnd.parse(val));
            return this;
        }
        if (Strings.isBlank(op)) op = "=";
        if (val == null && !Regex.isMatch(op, "is")){
            throw Core.makeThrow("查询数据条件不能为null");
            //return this;
        }
        if(val!=null&&Regex.isMatch(val.toString(), "^undefined$")&& !Regex.isMatch(op, "is")) return this;
        if (Regex.isMatch(op, "is") && val == null) val = "null";
        List<String> nameArray;
        nameArray = Strings.splitByComma(fields);
        //如果含有括号包裹


//            if (Regex.IsMatch(propnames, "(\\([\\s\\w]*\\))||([^\\w\\s\\.])"))
//            {
//                nameArray = new[] { propnames };
//            }
//            else
//            {
//                nameArray = propnames.SplitByComma();
//            }
        List<CndStruct>  canparse=new ArrayList<CndStruct>();
        String currentText = "";//当此条件文本
        for (String t : nameArray)
        {
            String propname = t;
            String _q = "'";
            String _h = "'";
            //如果对照表不为空
            if (this.mapping != null && this.mapping.size() > 0)
            {
                String clolumnName = propname;
                //如果字段含有函数或者是组合，不进行类型判断  int(dddd)   name+age
                if (Regex.isMatch(propname,"(\\w*\\(([\\s\\S]*)\\))|(\\w*[^\\w\\.]\\w*)"))
                {
//                    List<Map<String,Object>> asmappings = FieldMapping.where(x -> x.containsKey(asKey) && x.get(asKey)!= null && x.get(fieldnameKey).equals(x.get(asKey)));
//                    for (Map<String,Object> kv : asmappings)
//                    {
//                        var match = Regex.Match(propname, "[^\\w\\.]*(" + kv.get(fieldnameKey) + ")[^\\w\\.]*");
//                        if (match.Success)
//                        {
//                            clolumnName = match.Groups[0].Value;
//                            clolumnName = clolumnName.Replace(kv.Key, kv.Value[fieldnameKey].ToString());
//                            propname = propname.Replace(match.Groups[0].Value, clolumnName);
//                        }
//                    }
                }
                else
                {
                    //假如字段映射里含有
                    if (mapping.hasKey(propname))
                    {
                        String type="string";
                        if(mapping.get(propname).get(typeKey) !=null){
                            type =  com.jladder.lang.Collections.getString(mapping.get(propname),typeKey,"");
                        }

                        //置换成实字段
                        propname = mapping.get(propname).get(fieldnameKey).toString();
                        //如果是时间类型,进行时间转换
                        if (Regex.isMatch(type, "date($|[^t])"))
                        {
                            if (val instanceof String)
                            {
                                val = Strings.mapping(val.toString());
                            }
                            if (Regex.isMatch(val.toString(), "^\\$date$"))
                                val = Times.getNow();
                            if (Regex.isMatch(val.toString(), "^\\$((datetime)|(now))$"))
                                val = Times.getNow();
                            if (Regex.isMatch(val.toString(), "^\\d+$"))
                            {
                                val = Times.ts2D(Long.parseLong(val.toString()));
                            }
                            if (Regex.isMatch(val.toString(), "\\([\\s\\S]*\\)"))
                            {
                                _q = "";
                                _h = "";
                            }
                            else
                            {
                                long _t = Times.ams(val.toString());
                                if (_t == 0) return this;
                                val =Times.sD(Times.D(_t));
                                _q = "";
                                _h = "";
                                switch (dialect)
                                {
                                    case ORACLE:
                                        propname = "to_char(" + propname + ",'yyyy-mm-dd')";
                                        break;
                                    case MYSQL:
                                        propname = "date_format(" + propname + ", '%Y-%m-%d')";
                                        break;
                                    case SQLSERVER:
                                    case Mssql2000:
                                    case Mssql2005:
                                    case Mssql2008:
                                    case Mssql2012:
                                        propname = "CONVERT(varchar," + propname + ", 23)";
                                        break;
                                    default:

                                        break;
                                }
                            }
                        }
                        else if (Regex.isMatch(type, "time"))
                        {
                            if (val instanceof String)
                            {
                                val = Strings.mapping(val.toString());
                            }
                            if (val !=null && Regex.isMatch(val.toString(), "^\\$((datetime)|(now))$"))
                                val = Times.getNow();
                            //是整数时间戳
                            if (val != null &&  Regex.isMatch(val.toString(), "^\\d+$"))
                            {
                                val = Times.ts2D(Long.parseLong(val.toString()));
                            }
                            //视为含有函数
                            if (val != null && Regex.isMatch(val.toString(), "\\([\\s\\S]*\\)"))
                            {
                                _q = "";
                                _h = "";
                            }
                            else
                            {
                                Date _t = Times.convert(val.toString());
                                if (_t==null && _t.getTime()<=0) return this;
                                switch (dialect)
                                {
                                    case ORACLE:
                                        val = (val.toString().length() < 11 ? "to_date('" + Times.sD(_t) + "','yyyy-mm-dd')" : "to_date('" + Times.sDT(_t) + "','yyyy-mm-dd hh24:mi:ss')");
                                        _q = "";
                                        _h = "";
                                        break;
                                    case MYSQL:
                                        propname = (val.toString().length() < 11 ? "date_format(" + propname + ", '%Y-%m-%d')" : "date_format(" + propname + ", '%Y-%m-%d %H:%i:%S')");
                                        break;
                                    case SQLSERVER:
                                    case Mssql2000:
                                    case Mssql2005:
                                    case Mssql2008:
                                    case Mssql2012:
                                        propname = (val.toString().length() < 11 ? "CONVERT(varchar," + propname + ", 23)" : "CONVERT(varchar," + propname + ", 120)");
                                        break;
                                    default:
                                        val = Times.sDT(_t);
                                        _q = "'";
                                        _h = "'";
                                        break;
                                }
                            }
                        }
                    }
                }
            }
            op = op.toLowerCase().trim();
            //如果是字段原型数据
            if (Regex.isMatch(propname.trim(), "^\\*"))
            {
                _q = _h = "";
                propname = Regex.replace(propname, "^\\*", "");
                currentText += propname + " " + op + " " + val + " or ";
            }
            else
            {
                //获取条件回溯
                Receipt<SqlText> recall = getDataModelSql(val);
                if (recall.result)
                {
                    _q = "("; _h = ")";
                    currentText += propname + " " + op + " " + _q + " " + recall.data.getCmd() + _h + " or ";
                    parameters.addAll(recall.data.getParameters());
                    // val = recall;
                }
                else
                {
                    String key = Core.genUuid();
                    switch (op)
                    {
                        case "=":
                        case ">":
                        case ">=":
                        case "<":
                        case "<=":
                            currentText += propname + " " + op + " " + "@" + key + " or ";
                            parameters.add(new DbParameter(key,val));
                            break;
                        case "like":
                        case "not like":
                            _q = "%"; _h = "%";
                            currentText += propname + " " + op + " " +"@" + key + " or ";
                            parameters.add(new DbParameter(key,_q +val+_h));
                            break;
                        case "leftlike":
                        case "left like":
                            op = " like "; _q = "'"; _h = "%'";
                            currentText += propname + " " + op + " " +"@" + key + " or ";
                            parameters.add(new DbParameter(key,_q + val + _h));
                            break;
                        case "rightlike":
                        case "right like":
                            op = " like "; _q = "'%"; _h = "'";
                            currentText += propname + " " + op + " " +"@" + key +  " or ";
                            parameters.add(new DbParameter(key,_q + val + _h));
                            break;
                        case "in":
                        case "not in":
                            _q = "("; _h = ")";
                            if (Regex.isMatch(val.toString(), "^'*$"))
                            {
                                currentText += propname + " in ('') or ";
                            }
                            else
                            {
                                Tuple2<ArrayList<String>,Boolean> ts = toArrayText(val);
                                if (ts.item1.size() > 0){
                                    currentText += propname + " " + op + " (";
                                    for (String tt : ts.item1){
                                        String inkey = Core.genUuid();
                                        currentText += "@" + inkey + ",";
                                        parameters.add(new DbParameter(inkey,tt));
                                    }
                                    currentText = Strings.rightLess(currentText,1)+") or ";
                                }
                            }
                            // val = Regex.IsMatch(val.ToString(), "^'*$") ? "''" : Strings.ArrayText(val);
                            //  "@" + key + ") or ";
                            // Parameters.Put("@" + key, val);
                            break;
                        case "between":
                        {
                            Tuple2<ArrayList<String>,Boolean> ts = toArrayText(val);
                            switch (ts.item1.size()){
                                case 0:
                                    throw Core.makeThrow("between数据条件不足[0497]");
                                case 1:
                                    currentText += propname + "  between @" + key + " and @" + key + " or ";
                                    parameters.add(new DbParameter(key,ts.item1.get(0)));
                                    // Parameters.Put(key2, ts.Item1[1]);
                                    break;
                                default:
                                    String key2 = Core.genUuid();
                                    currentText += propname + "  between @" + key + " and @" + key2 + " or ";
                                    parameters.add(new DbParameter(key,ts.item1.get(0)));
                                    parameters.add(new DbParameter(key2,ts.item1.get(1)));
                                    break;
                            }
                        }
                        break;
                        case "not":
                            if (val.equals("null")) { _q = ""; _h = ""; op = "is not"; }
                            else op = "<>";
                            currentText += propname + " " + op + " " + _q + val + _h + " or ";
                            break;
                        case "is":
                            _q = ""; _h = "";
                            if (val == null || val.equals("null") || Strings.isBlank(val.toString()))
                                val = "null";
                            else Core.makeThrow("条件对象定义值和操作符不匹配");
                            currentText += propname + " " + op + " " + _q + val + _h + " or ";
                            break;
                        case "is not":
                            _q = ""; _h = "";
                            currentText += propname + " " + op + " " + _q + val + _h + " or ";
                            break;
                        default:
                            if (val!=null && !(val instanceof String)  && Strings.isNumber(val.toString()))
                            {
                                _q = "";
                                _h = "";
                            }

                        currentText += propname + " " + op + " " + _q + val + _h + " or ";
                        break;
                    }
                }
            }
            canparse.add(new CndStruct(propname,op,val));
        }

        if (!Strings.isBlank(currentText))
        {

            currentText =Strings.rightLess(currentText,4);
            pushWhereString(currentText, option);
            if (option == AND) {
                Most.add(canparse);
            }
            else{
                if(Most.size()>0){
                    Most.get(Most.size()-1).addAll(canparse);
                }
                else {
                    Most.add(canparse);
                }
            }
        }
        return this;
    }
    /// <summary>
    /// 获取模版的条件sql语句(条件回溯)
    /// </summary>
    /// <param name="data">数据</param>
    /// <returns></returns>
    public static Receipt<SqlText> getDataModelSql(Object data)
    {
        Func2<Map<String, Object>, SqlText> func = (dic) ->{
            if (dic == null) return null;
            String tableName = dic.get("tableName")!=null?dic.get("tableName").toString():"";
            //条件回溯了
            if (!Strings.isBlank(tableName)){
                String query = com.jladder.lang.Collections.getString(dic,"query",null, true);
                if (Strings.hasValue(query)&& Regex.isMatch(query,"^(1)|(true)$")){
//                    IDataModel dm = DaoSeesion.getDataModel(tableName, com.jladder.lang.Collections.getString(dic,"param",""));
//                    if (dm == null || dm.isNull()) throw Core.makeThrow("回置的数据库不存在");
                    List<String> rst = QueryAction.getValues(tableName, com.jladder.lang.Collections.getString(dic, "columns,column,columnstring","", true), com.jladder.lang.Collections.getString(dic, "condition,where","", true), com.jladder.lang.Collections.getString(dic, "param",""), String.class);
                    if(rst==null)throw Core.makeThrow("回溯的数据查询异常[0601]");
                    return new SqlText(Strings.arraytext(rst));
                }
                else{
                    //throw new Exception("需要确认逻辑");
                    IDataModel dm = DaoSeesion.getDataModel(tableName, com.jladder.lang.Collections.getString(dic,"param",""));
                    if (dm == null || dm.isNull()) throw Core.makeThrow("回置的数据库不存在");
                    dm.matchColumns(com.jladder.lang.Collections.getString(dic,"columns,column,columnstring","", true));
                    Object condition = com.jladder.lang.Collections.get(dic,"condition,where", true);
                    dm.setCondition(Cnd.parse(condition,dm));
                    return dm.getSqlText();
                }
            }
            return null;
        };
        Map<String, Object> record = null;
        if (data instanceof String && Strings.isJson(data.toString(),1)){
            String str = data.toString();
            if (Strings.isJson(str,1)){
                record = Json.toObject(str,new TypeReference<Map<String, Object>>(){});
            }
            if(Strings.isJson(str,2)){
                data = Json.toObject(str,new TypeReference<List<Map<String, Object>>>(){});
            }
        }
        if (Core.isType(data,new TypeReference<Map<String,Object>>(){})){
            record = (Map<String,Object>)data;
        }
        if (Core.isType(data,new TypeReference<List<Map<String, Object>>>(){})){
            List<Map<String, Object>> datas = (List<Map<String, Object>>)data;
            List<SqlText> p = com.jladder.lang.Collections.where(com.jladder.lang.Collections.select(datas, x->func.invoke(x)), x->!x.isBlank());
            List<DbParameter> parameters = new ArrayList<DbParameter>();
            p.forEach(x -> parameters.addAll(x.getParameters()));
            return new Receipt<SqlText>().setData(new SqlText(Strings.ArrayToString(com.jladder.lang.Collections.select(p, x -> x.getCmd())," union ",""), parameters));
        }
        if (data instanceof Record){
            record = (Map<String, Object>) data;
        }
        if(data instanceof JSONObject){
            record = Record.parse(data);
        }
//        if (data is IEnumerable<Dictionary<string, object>>)
//        {
//            var datas = data as IEnumerable<Dictionary<string, object>>;
//            var p = datas.Select(x => func(x)).Where(x => x.Text.HasValue()).ToList();
//            var sqltext =  p.Select(x => x.Text).ToString(" union ");
//            var parameters = new Record();
//            p.ForEach(x => parameters.Merge(x.Parameters));
//            return sqltext.HasValue() ? new Receipt<SqlText>().SetData(new SqlText(sqltext,parameters)) : new Receipt<SqlText>(false);
//        }
//        if (data is ArrayList)
//        {
//            var datas = data as ArrayList;
//            string sqltext = "";
//            var parameters = new Record();
//            foreach (var o in datas)
//            {
//                var sql = GetDataModelSql(o);
//
//                if (sql.Result)
//                {
//                    sqltext += sql.Data.Text + " union ";
//                    parameters.Merge(sql.Data?.Parameters);
//                }
//            }
//            return sqltext.HasValue() ? new Receipt<SqlText>().SetData(new SqlText(sqltext.RightLess(7), parameters)) : new Receipt<SqlText>(false);
//        }
        if (data instanceof Object[])
        {
            Object[] datas = (Object[])data;
            String sqltext = "";
            List<DbParameter> parameters = new ArrayList<DbParameter>();
            for (Object o : datas)
            {
                Receipt<SqlText> sql = getDataModelSql(o);
                if (sql.result){
                    sqltext += sql.message + " union ";
                    parameters.addAll(sql.data.getParameters());
                }
            }
            return Strings.hasValue(sqltext) ? new Receipt<SqlText>().setData(new SqlText(Strings.rightLess(sqltext,7),parameters)) : new Receipt<SqlText>(false);
        }
        if (data instanceof ReCall){
            ReCall recall = (ReCall) data;
            record =new Record("tableName", recall.TableName)
                    .put("columns", recall.Columns)
                    .put("param",recall.Param)
                    .put("condition", recall.Condition)
                    .put("query",recall.Query);
        }
        SqlText ret = null;
        try {
            ret = func.invoke(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret == null ? new Receipt<SqlText>(false) : new Receipt<SqlText>(true).setData(ret);
    }

    /**
     * 整理数据数组
     * @param obj 数据对象
     * @return
     */
    private Tuple2<ArrayList<String>,Boolean> toArrayText(Object obj){
        final boolean[] isnumber = {true};
        if(obj==null)return new Tuple2(new ArrayList<String>(Arrays.asList(obj.toString())),false);
        List array = new ArrayList();
        if(obj instanceof Collection){
            Collection list = (Collection)obj;
            isnumber[0] = true;
            for (Object o : list)
            {
                if (o == null) continue;
                String data = o.toString();
                if (isnumber[0] && !Regex.isMatch(data, "^-?\\d*\\.?\\d*$"))
                {
                    isnumber[0] = false;
                }
                array.add(data);
            }
            return new Tuple2(array, isnumber[0]);
        }
        if (obj instanceof List){
            List list = (List)obj;
            isnumber[0] = true;
            for (Object o : list){
                if (o == null) continue;
                String data = o.toString();
                if (isnumber[0] && !Regex.isMatch(data, "^-?\\d*\\.?\\d*$")){
                    isnumber[0] = false;
                }
                array.add(data);
            }
            return new Tuple2(array, isnumber[0]);
        }
        if (obj instanceof String){
            String str = (String)obj;
            if (Strings.isBlank(str)) return new Tuple2(new ArrayList(), false);
            str = Regex.replace(str, "^\\s*[,\\[\\(]?", "");
            str = Regex.replace(str, "^[,\\]\\)]?\\s*$", "");
            str = str.replace("'", "");
            List<String>  raw = Strings.splitByComma(str);
            raw.forEach(x->{
                if (isnumber[0] && !Regex.isMatch(x, "^-?\\d*\\.?\\d*$")) isnumber[0] = false;
                array.add(x);
            });
            return new Tuple2(array, isnumber[0]);
        }
        //数组时
        if(obj.getClass().isArray()){
            List<Object> list = Json.toObject(Json.toJson(obj),List.class);
            isnumber[0] = true;
            for (Object o : list){
                if (o == null) continue;
                String data = o.toString();
                if (isnumber[0] && !Regex.isMatch(data, "^-?\\d*\\.?\\d*$")){
                    isnumber[0] = false;
                }
                array.add(data);
            }
            return new Tuple2(array, isnumber[0]);
        }
        return new Tuple2(new ArrayList<String>(Arrays.asList(obj.toString())),false);
    }
    private void pushWhereString(String sqlStr, int option)
    {
        if (Strings.isBlank(sqlStr)) return;
        sqlStr = Regex.replace(sqlStr, "^\\s*(where)[\\s]*", "");
        if (Strings.isBlank(sqlStr)) return;
        if (Strings.isBlank(whereText)) whereText = sqlStr;
        else
        {
            if (option == 1) this.whereText += " or " + sqlStr;
            else this.whereText += " and " + sqlStr;
        }
    }

    /***
     *
     * @return
     */
    public DbDialectType getDialectType() {
        return dialect;
    }

    /**
     *
     * @param dialect
     * @return
     */
    public Cnd setDialect(DbDialectType dialect) {
        this.dialect = dialect;
        return this;
    }

    public static Cnd parse(Object cnd){
        return parse(cnd,null,DbDialectType.Default);
    }


    /// <summary>
    /// 解析条件字段
    /// </summary>
    /// <param name="cnd">条件字段</param>
    /// <param name="dm">动态数据模型</param>
    /// <returns></returns>
    public static Cnd parse(Object cnd, IDataModel dm)
    {
        Cnd result= Cnd.parse(cnd, dm.parseColumsList(),dm.getDialect()!=null ? dm.getDialect() : DbDialectType.Default);
        return result;
    }
    public boolean hasValue() {
        return Strings.hasValue(whereText);
    }
    /**
     * 解析条件
     * @param cnd 原数据对象
     * @param fullcolumn 全字段
     * @param dialect 数据库方言
     * @return
     */
    public static Cnd parse(Object cnd, List<Map<String, Object>> fullcolumn,DbDialectType dialect)
    {

        if (cnd == null || Strings.isBlank(cnd.toString())) return null;
        if (cnd instanceof String) {
            return new Cnd(fullcolumn, dialect).put(cnd.toString());
        }
        //是record类型
        if (cnd instanceof Record) {
            return new Cnd(fullcolumn,dialect).put((Record)cnd);
        }
        //是字典类型
        if ( (new HashMap<String,Object>()).getClass().isAssignableFrom(cnd.getClass())){
            return new Cnd(fullcolumn, dialect).put((Map<String, Object>)cnd);
        }
        //是条件类型
        if (cnd instanceof Cnd){
            Cnd ret = (Cnd)cnd;
            if(dialect!=DbDialectType.Default)ret.dialect =dialect;
            if(fullcolumn!=null)ret.initFieldMapping(fullcolumn);
            return ret;
        }
        //是条件列表
        if ((new ArrayList<Cnd>()).getClass().isAssignableFrom(cnd.getClass())) {
            List<Cnd> cnds = (List<Cnd>)cnd;
            if (cnds.size() < 1) return null;
            Cnd _cnd = cnds.get(0);
            if (cnds.size() < 2) return _cnd;
            for (int i = 1; i < cnds.size(); i++){
                _cnd.or(cnds.get(i));
            }
            return _cnd;
        }
        //是字典列表
        if (new  ArrayList<Map<String, Object>>().getClass().isAssignableFrom(cnd.getClass())){
            List<Map<String, Object>> cnds = (List<Map<String, Object>>)cnd;
            if (cnds.size() < 1) return null;
            Cnd _cnd = new Cnd(fullcolumn,dialect).put(cnds.get(0));
            if (cnds.size() < 2) return _cnd;
            for (int i = 1; i < cnds.size(); i++){
                _cnd.or(new Cnd(fullcolumn, dialect).put(cnds.get(i)));
            }
            return _cnd;
        }
        //是记录列表
        if ((new ArrayList<Record>().getClass().isAssignableFrom(cnd.getClass()))) {
            List<Record> cnds = ((List<Record>)cnd);
            if (cnds.size() < 1) return null;
            Cnd _cnd = new Cnd(fullcolumn, dialect).put(cnds.get(0));
            if (cnds.size() < 2) return _cnd;
            for (int i = 1; i < cnds.size(); i++)
            {
                _cnd.or(new Cnd(fullcolumn, dialect).put(cnds.get(i)));
            }
            return _cnd;
        }

        if (cnd instanceof SqlText){
            Cnd _cud = new Cnd(fullcolumn,dialect);
            SqlText sqltex = (SqlText)cnd;
            _cud.whereText = sqltex.getCmd();
            _cud.parameters = sqltex.getParameters();
            return _cud;
        }
        //解析普通的类形式
        Record record = Record.parse(cnd);
        return new Cnd(fullcolumn, dialect).put(record);
    }
    /**
     * 获取Where串
     * @param hasWhere
     * @param isFill
     * @return
     */
    public String getWhere(boolean hasWhere,boolean isFill){
        final String[] sqlcmd = {Strings.isBlank(whereText) ? "" : (hasWhere ? " where " : "") + Regex.replace(this.whereText, "^\\s*where\\s*", "")};
        if (!isFill || Strings.isBlank(sqlcmd[0])) return sqlcmd[0];
        if(parameters!=null){
            parameters.forEach(x -> sqlcmd[0] =sqlcmd[0].replace("@"+x.name, "'" + x.value + "'"));
        }
        return sqlcmd[0];
    }

    /***
     * 获取数据参数列表
     * @return
     */
    public List<DbParameter> getParameters(){
        return this.parameters;
    }

    public void clear() {
        this.whereText="";
        if(this.parameters!=null)this.parameters.clear();
        if(this.Most!=null)this.Most.clear();
    }

    public Cnd or(Cnd ... cnds) {
        if (cnds == null || cnds.length < 1) return this;
        final String[] wstr = {this.getWhere(false, false)};
        AtomicBoolean ishaswstr = new AtomicBoolean(false);
        if (Strings.hasValue(wstr[0])){
            ishaswstr.set(true);
            if(!wstr[0].startsWith("(") || !wstr[0].endsWith(")"))wstr[0] = "(" + wstr[0] + ")";
        }
        //final boolean[] finalHaswstr = {};
        com.jladder.lang.Collections.where(cnds, one -> one != null && Strings.hasValue(one.whereText)).forEach(one->{
            wstr[0] += (ishaswstr.get() ? " or " : "") +"("+ one.whereText+")";
            parameters.addAll(one.parameters);
            ishaswstr.set(true);
        });
        this.whereText = wstr[0];
        if (Most.size() < 1){
            if(Most.size()>0){
                Most.add(cnds[0].Most.get(0));
                for(int i=1;i<cnds.length;i++){
                    for (List<CndStruct> cndStructs : cnds[i].Most){
                        Most.get(0).addAll(cndStructs);
                    }
                }
            }
        }
        else{
            for (Cnd cnd1 : cnds){
                for (List<CndStruct> cndStructs : cnd1.Most){
                    Most.forEach(x -> x.addAll(cndStructs));
                }
            }
        }
        return this;
    }

    /**
     * 条件是否为空
     * @return
     */
    public boolean isBlank() {
        return Strings.isBlank(whereText);
    }

    /***
     * 初始化对照表
     * @param fullcolumn 列模型
     */
    public void initFieldMapping(List<Map<String, Object>> fullcolumn) {
        if (fullcolumn == null || fullcolumn.size() < 1) return;
        for (Map<String, Object> dic : fullcolumn){
            String fieldval = com.jladder.lang.Collections.getString(dic,fieldnameKey,null);
            if (Strings.isBlank(fieldval)) continue;
            String asval = Collections.getString(dic,asKey,null);
            mapping.put(fieldval.toLowerCase(), dic);
            if (Strings.hasValue(asval)) mapping.put(asval.toLowerCase(), dic);
        }

    }
}
