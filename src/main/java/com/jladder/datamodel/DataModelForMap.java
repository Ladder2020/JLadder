package com.jladder.datamodel;

import com.jladder.Ladder;
import com.jladder.actions.Curd;
import com.jladder.actions.impl.LatchAction;
import com.jladder.data.Pager;
import com.jladder.data.Record;
import com.jladder.db.*;
import com.jladder.db.enums.DbDialectType;
import com.jladder.db.enums.DbSqlDataType;
import com.jladder.db.jdbc.OrderBy;
import com.jladder.db.jdbc.impl.Dao;
import com.jladder.entity.DBMagic;
import com.jladder.entity.DataModelTable;
import com.jladder.lang.Collections;
import com.jladder.lang.*;
import com.jladder.lang.func.Tuple2;
import com.jladder.web.WebScope;
import org.w3c.dom.Element;
import java.util.*;
import java.util.regex.Matcher;

/**
 * 以Map为存储原型的数据模型
 */
public class DataModelForMap extends IDataModel{

    private static Object lock = new Object();

    private  Map<String, String> columns = new HashMap<String, String>();
    /// <summary>
    /// 列上的表达式数组
    /// </summary>
    // ReSharper disable once InconsistentNaming
    private  List<String> expressList = new ArrayList<String>();
    /**
     * 事件列表
     */
    public Map<String, List<Record>> Events = new HashMap<String, List<Record>>();
    /**
     * 全字段
     */
    public List<Map<String, Object>> FullColumns;
    /***
     * 全场景字段
     */
    public List<Map<String, Object>> AllColumns;

    /// <summary>
    /// 数据库类型
    /// </summary>

    /// <summary>
    /// 基本构造
    /// </summary>
    public DataModelForMap() { }

    /// <summary>
    /// 从JS文件构造
    /// </summary>
    /// <param name="jspath">js文件路径</param>
    /// <param name="nodeName">节点名称</param>
    /// <param name="result">结构</param>
    public DataModelForMap(String jsPath, String nodeName,boolean result) { result = fromJsonFile(jsPath, nodeName); }
    /// <summary>
    /// 从数据库sys_data表中获取模版
    /// </summary>
    public DataModelForMap(CharSequence tableName, String param)
    {
        fromDataTable(Dao,tableName, param);
    }
    /// <summary>
    /// 构造方法
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="param">参数</param>
    /// <param name="ispass">是否通过</param>
    public DataModelForMap(CharSequence tableName, String param, int ret)
    {
        ret = fromDataTable(Dao,tableName, param);
    }
    /// <summary>
    /// 从数据库sys_data表中获取模版
    /// </summary>
    /// <param name="dao">数据库操作对象</param>
    /// <param name="tableName">键表名</param>
    public DataModelForMap(IDao dao, CharSequence tableName)
    {
        fromDataTable(dao, tableName, null);
    }
    /// <summary>
    /// 从数据库sys_data表中获取模版
    /// </summary>
    /// <param name="dao">数据库操作对象</param>
    /// <param name="tableName">键表名</param>
    /// <param name="param">参数文本列表</param>
    public DataModelForMap(IDao dao, CharSequence tableName, String param)
    {
        if (Strings.isBlank(tableName)) return;
        fromDataTable(dao, tableName, param);
    }
    /// <summary>
    /// 从数据键名构造
    /// </summary>
    /// <param name="tableName"></param>
    public DataModelForMap(CharSequence tableName)
    {
        fromDataTable(Dao, tableName, null);
    }
    /// <summary>
    /// 以DBdataTable构造
    /// </summary>
    /// <param name="dt"></param>
    public DataModelForMap(DataModelTable dt) { fromDataTable(dt); }
    /// <summary>
    /// 以动态表单构造
    /// </summary>
    /// <param name="magic"></param>
    public DataModelForMap(DBMagic magic) { fromMagic(magic,null); }
    /// <summary>
    /// 以原型数据构造
    /// </summary>
    /// <param name="raw">原型数据</param>
    /// <param name="param">参数</param>
    public DataModelForMap(DataModelForMapRaw raw,String param)
    {
        fromRaw(raw,param);
    }
    /// <summary>
    /// 从xml节点构造
    /// </summary>
    /// <param name="xElement">xml节点</param>
    /// <param name="param">参数列表</param>
    public DataModelForMap(Element xElement, String param)
    {
        fromXml(xElement,param);
    }
    /**
     * 从JSON文件初始化
     * @param path 文件路径
     * @param nodeName 节点名称
     * @return
     */
    public boolean fromJsonFile(String path, String nodeName){
        Raw.Scheme = "json";
        String content = Json.FromFile(path);
        try{
            Map<String, Map<String, Object>> jMap = Json.toObject(content, new TypeReference<Map<String, Map<String, Object>>>() {});
            if (jMap.containsKey(nodeName)){
                DataModelTable dt = Collections.toClass(jMap.get(nodeName), DataModelTable.class);
                fromDataTable(dt);
            }
        }
        catch (Exception e){
            return false;
        }
        return true;
    }
    /**
     * 从xml元素解析
     * @param element 元素节点
     * @param param 参数列表
     * @return
     */
    public boolean fromXml(Element element,String param){
        Raw.Scheme = "xml";
        if (element == null) throw Core.makeThrow("片选节点为空");
        Raw.Name = element.getAttribute("name");
        Raw.Type = element.getAttribute("type");
        Raw.Enable= element.getAttribute("enable");
        if (Strings.isBlank(Raw.Name)) throw  Core.makeThrow("映射名称不能空");
        try{
            Element node = Xmls.getElement(element,"tableName,table,tablename");
            Raw.Table = (node==null?"":node.getTextContent());
            node = Xmls.getElement(element,"conn");
            Raw.Conn = (node==null?"":node.getTextContent());
            node = Xmls.getElement(element,"data");
            Raw.Data = (node==null?"":node.getTextContent());
            node = Xmls.getElement(element, "columns");
            if (node != null && node.hasChildNodes()){
                ColumnList = new ArrayList<Map<String,Object>>();
                Xmls.getElements(node,"column").forEach(x -> ColumnList.add(Xmls.toMap(x)));
            }
            List<Element> ps = Xmls.getElements(element, "param");
            if (ps!=null && ps.size()>0){
                Raw.Params = new ArrayList<Map<String,Object>>();
                ps.forEach(x -> Raw.Params.add(Xmls.toMap(x,(e,d) ->{
                    if (Strings.hasValue(e.getTextContent())) d.put("dvalue", e.getTextContent());
                })));
            }
            List<Element> events = Xmls.getElements(element, "events");
            if (events != null && events.size()>0){
                //Raw.Events =new Dictionary<string, List<Record>>();
                HashMap<String, List<Record>> eventdic = new HashMap<String, List<Record>>();
                events.forEach(x ->{
                    List<Element> es = Xmls.getElements(x, null);
                    if (es==null && es.size()>0){
                        List<Record> evs = new ArrayList<Record>();
                        es.forEach(y -> evs.add(Record.parse(Xmls.toMap(y))));
                        //Raw.Events.Put(x.Name.LocalName, evs);
                        eventdic.put(x.getTagName(), evs);

                    }
                });
                Raw.Events = Json.toJson(eventdic);
                Events = eventdic;
            }
            List<Element> queryforms = Xmls.getElements(element,"queryforms");;
            //先不解析
            if (queryforms != null && queryforms.size()>0)
            {
//                    QueryForms = new List<ISqlQueryForm>();
//                    queryforms.Elements().ForEach(x => QueryForms.Add(x.ToClass<SqlQueryForm>(b => b.Dvalue = x.Value)));
            }
            Raw.AllColumns = ColumnList;
            AllColumns = ColumnList;
            fromRaw(Raw,param);
        }
        catch (Exception e){
            e.printStackTrace();
            throw Core.makeThrow("解析过程发生异常");
        }
        return true;
    }
    /// <summary>
    /// 从魔法实体类中获取
    /// </summary>
    public boolean fromMagic(DBMagic magic,String client){
        Raw.Scheme = "magic";
        if (magic == null) return false;
        Raw.Put("maigc", magic);
        Raw.Put("client", client);
        Raw.Name = magic.Name;
        Raw.Type = "maigc";
        Type = DataModelType.Magic;
        switch (magic.RelationType.toLowerCase()){
            case "rawtable":
            case "raw":
                Raw.Table = magic.TableName.replace("*","");
                TableName = Raw.Table;
                break;
            case "datamodel":
                Raw.Table = getTableName(magic.TableName,null);
                TableName = Raw.Table;
                break;
            case "self":
                Raw.Table = magic.TableName.replace("*", "");
                TableName = Raw.Table;
                break;
            default:
                break;
        }
        if (Strings.isBlank(client)) client = "pc";
        switch (client)
        {
            case "mobile":
            case "phone":
            case "h5":
                ColumnList = Json.toObject(magic.MColumns,List.class);
                break;
            default:
                ColumnList = Json.toObject(magic.PColumns,List.class);
                break;
        }
        Raw.AllColumns = ColumnList;
        AllColumns = ColumnList;
            //解析并填充原型中列模型
        parseColumsList(null);
        FullColumns = filterColumns(null);
        return true;
    }
    /// <summary>
    /// 从数据库键名解析
    /// </summary>
    /// <param name="name">键名表名</param>
    /// <returns></returns>
    public int fromTemplate(String name){
        return fromDataTable(Dao,name,null);
    }
    /// <summary>
    /// 从数据库实表解析
    /// </summary>
    /// <param name="table">表名或sql语句</param>
    /// <returns></returns>
    public int fromDbTable(String table){
        Raw.Scheme = "table";
        //2017-08-15修改
        IDao dao = Dao != null ? Dao : DaoSeesion.GetDao();
        Tuple2<Boolean, String> changecon = WebScope.MappingConn(dao.getMarkCode(), table);
        if (changecon.item1 && changecon.item2 != dao.getMarkCode()){
            dao = DaoSeesion.GetDao(changecon.item2);
        }
        List<Map<String, Object>> fields = dao.getFieldInfo(table);
        if (fields == null || fields.size() < 1) return -1;
        List rs=new ArrayList<Record>();
        fields.forEach(c->rs.add(Record.parseWithAction(c, x -> x.key = x.key.toLowerCase())));
        DataModelTable dt = new DataModelTable();
        dt.columns = Json.toJson(rs);
        dt.tablename = table;
        dt.type = "table";
        dt.enable = 1;
        return fromDataTable(dt);
    }

    public int fromDbTable(String table, String conn){
        if (Strings.isBlank(conn)) return fromDbTable(table);
        else{
            IDao dao = DaoSeesion.NewDao(conn);
            try{
                List<Map<String, Object>> fields = dao.getFieldInfo(table);
                if (fields == null || fields.size() < 1) return -1;
                List<Record> rs = new ArrayList<Record>();
                fields.forEach(c -> rs.add(Record.parseWithAction(c, x -> x.key = x.key.toLowerCase())));
                DataModelTable dt = new DataModelTable();
                dt.columns = Json.toJson(rs);
                dt.tablename = table;
                dt.type = "table";
                dt.enable = 1;
                dt.conn=conn;
                return fromDataTable(dt);
            }
            finally { dao.close();}
         }
    }


    /// <summary>
    /// 从原型数据解析
    /// </summary>
    /// <param name="raw">原型数据</param>
    /// <param name="param">参数数据</param>
    @Override
    public void fromRaw(DataModelForMapRaw raw,String param){
        if (raw == null) return;
        Order = null;
        Group = null;
        Raw = raw;
        ColumnList = raw.AllColumns;
        AllColumns = raw.AllColumns;
        //处理一下事件
        if (Strings.hasValue(raw.Events)){
            String eventstring = matchParam(Strings.mapping(raw.Events), param, true);
            if (Strings.isJson(eventstring,1)) Events = Json.toObject(eventstring,new TypeReference<Map<String, List<Record>>>(){});
        }

        TableName = raw.Table;
        Conn = raw.Conn;
        if (((Object)true).equals(raw.Get("el_columns")) || raw.Params != null || !Strings.isBlank(param)){
            String columnsString = Json.toJson(raw.AllColumns);
            columnsString = matchParam(columnsString, param, true);
            ColumnList = Json.toObject(columnsString,new TypeReference<List<Map<String, Object>>>(){});
            AllColumns = Json.toObject(columnsString,new TypeReference<List<Map<String, Object>>>(){});
        }
        Condition.clear();
        Condition.dialect = (DbDialectType.Default.equals(DbDialect) || DbDialect==null ?DaoSeesion.GetDialect(Conn): DbDialect);
        //解析列模型到条件字段
        Condition.initFieldMapping(parseColumsList());

        if (Strings.isBlank(Raw.Type)) Raw.Type = "table";
        switch (Raw.Type.toLowerCase()){
            case "table":
            Type = DataModelType.Table;
            TableName =Strings.mapping( Raw.Table);
            break;
        case "data":
            Type = DataModelType.Data;
    //                    if (Raw.Data.HasValue() && Raw.Data.IsJson(2))
    //                    {
    //                        Raw.CacheItems = "TableRAM";
    //                        LatchAction.SetData(this, Json.FromJson<List<Record>>(Raw.Data));
    //                    }
            break;
        //执行函数
        case "exec":
            Type = DataModelType.Exec;
            break;
        //存储过程
        case "pro":
            Type = DataModelType.Pro;
            break;
        //sql语句
        case "sql":
            {
                Type = DataModelType.Sql;
                //sql自定义语句 可以存放在data段，也可以直接在tableName，此时为级联字串
                if (Strings.isBlank(Raw.Data) && Strings.isBlank(Raw.Table)) return;
                if (Strings.isBlank(Raw.Table)) //不在tableName   在data区
                {
                    String data = Strings.mapping(matchParam(Raw.Data, param));
                    if (Strings.isBlank(data)) return;
                    Sqls sqls = Sqls.Decoder(data);
                    TableName = sqls.TableName;
                    if(sqls.Condition!=null){
                        Condition.put(sqls.Condition.getWhere(false,true));
                    }
                    Order = sqls.Order;
//                    Order.UpdateAllProperty("fixed", true);
                    Group = sqls.Group;
                }
                else//在tableName区，只替换param，不进行sql解析
                {
                    TableName = Strings.mapping(matchParam(Raw.Table, param));
                    if (Strings.isBlank(TableName)) return;
                }
            }
            break;
        case "maigc":
            Type = DataModelType.Magic;
            break;
        }
        FullColumns = filterColumns();
        if (Strings.hasValue(raw.Permission)) checkPermission(raw.Permission);
    }
    /**
     * 从模版实体类中获取
     * @param dao 数据库操作对象
     * @param table 键表名
     * @param param 参数列表
     * @return
     */
    public int fromDataTable(IDao dao, CharSequence table, String param){
        if(Strings.isBlank(table))return -1;
        boolean isuseTemplateConn = false;
        if (dao == null) dao = Dao;
        if (dao == null){
            isuseTemplateConn = true;
            dao = DaoSeesion.GetDao(Ladder.Settings().getTemplateConn());
        }
        if (dao != null) Dao = dao;
        String tableName=table.toString();
        if (tableName.startsWith("{") && tableName.endsWith("}")){
            Record dic = Json.toObject(tableName,Record.class);
            int result = -1;
            if (dic == null) return -1;
            else {
                String type = dic.getString("type");
                String conn = dic.getString("conn");
                switch (type.toLowerCase()){
                    case "magic":
                        {
                            Cnd cnd = new Cnd();
                            if (Strings.hasValue(dic.getString("id"))) cnd.put("id", dic.getString("id"));
                            if (Strings.hasValue(dic.getString("name"))) cnd.put("name", dic.getString("name"));
                            if (cnd.isBlank()) return -1;
                            if (dao != null){
                                DBMagic magic = dao.fetch(Ladder.Settings().getMagicTableName(), cnd).toClass(DBMagic.class);
                                if (magic == null) return -1;
                                result = fromMagic(magic,dic.getString("client")) ? 1 : -1;
                            }
                        }
                        break;
                    case "event":
                        {
                            String v = dic.getString("tableName,table,name", true);
                            if (Strings.isBlank(v)) return -1;
                            if (Strings.isBlank(conn)){
                                result = fromDataTable(null,v,dic.getString("param", true));
                            }
                            else{
                                Conn = conn;
                                result = fromDataTable(DaoSeesion.NewDao(conn),v,dic.getString("param", true));
                            }
                            List<Record> events = Json.toObject(dic.getString("events"), new TypeReference<List<Record>>() {});
                            if(!Rs.isBlank(events)){
                                events.forEach(event->{
                                    String ty=event.getString("type");
                                    if(Strings.isBlank(ty))ty="resultset";
                                    if(Events.containsKey(ty)){
                                        Events.get(ty).add(event);
                                    }else{
                                        ArrayList<Record> nes = new ArrayList<Record>();
                                        nes.add(event);
                                        Events.put(ty, nes);
                                    }
                                });
                            }
                            Raw.Events=Json.toJson(Events);
                        }
                        break;
                    default:
                        String v = dic.getString("tableName,table,name", true);
                        if (Strings.isBlank(v)) return -1;
                        if (Strings.isBlank(conn)){
                            result = fromDbTable(v);
                        }
                        else{
                            Conn = conn;
                            result = fromDbTable(v, Conn);
                        }
                         break;
                }
            }
            return result;
        }
        else
        {
            if (Regex.isMatch(tableName, "^[\\*@\\$]")){
                if (isuseTemplateConn) dao = DaoSeesion.GetDao("");
                IDao temp_dao = dao != null ? dao : DaoSeesion.GetDao();
                Tuple2<Boolean, String> changecon = WebScope.MappingConn(temp_dao.getMarkCode(), tableName);
                String thatconn = temp_dao.getMarkCode();
                if (changecon.item1 && changecon.item2 != temp_dao.getMarkCode()){
                    thatconn = changecon.item2;
                }
                switch (tableName.substring(0, 1)){
                    case "@":
                        {
                            com.jladder.db.jdbc.impl.Dao newDao = new Dao(thatconn);
                            try{
                                DBMagic magic = newDao.fetch(Ladder.Settings().getMagicTableName(), new Cnd("name", tableName.substring(1))).toClass(DBMagic.class);
                                if (magic == null) return -1;
                                return fromMagic(magic,"1") ? 1 : -1;
                            }
                            finally
                            {
                                newDao.close();
                            }

                        }
                        //return 0;
                    case "*":
                        return fromDbTable(tableName.substring(1), thatconn);
                }

            }
            Record re;
            synchronized (lock){
                dao.setTag("DataModel");
                String wraptext = "";
                DbDialectType _dialect = dao.getDialect();
                if(DataModelType.Table.equals(Type) && _dialect!=null){
                    switch (_dialect){
                        case MYSQL:
                            wraptext = "`";
                            break;
                        case PostgreSql:
                            wraptext = "\"";
                            break;
                    }
                }
                re = dao.fetch(new SqlText("select * from " +Ladder.Settings().getTemplateTableName()  + " where "+wraptext+"name"+wraptext+"=@name","name",tableName));
            }
            if (re == null) return 0;
            Raw.Scheme = "template";
            DataModelTable dt = re.toClass(DataModelTable.class);
            return fromDataTable(dt, param);
        }
     }
    /// <summary>
    /// 从模版实体类中获取
    /// </summary>
    public int fromDataTable(DataModelTable dt)
    {
        return fromDataTable(dt, null);
    }
    /// <summary>
    /// 从模版实体类中获取
    /// </summary>
    /// <param name="dt">数据库模版类</param>
    /// <param name="param">参数列表</param>
    /// <returns></returns>
    public int fromDataTable(DataModelTable dt, String param) {
        Order = null;
        Group = null;
        if (dt == null) return 0;
        if (dt.enable < 1) return -1;
        //计算原始el表表达式替代符
        Raw.Put("el_columns", Strings.hasValue(dt.columns) && Regex.isMatch(dt.columns, "\\$\\{([\\W\\w])*?\\}"));
        Raw.Put("el_evnets", Strings.hasValue(dt.events) &&  Regex.isMatch(dt.events, "\\$\\{([\\W\\w])*?\\}"));
        Raw.Put("el_tablename", Strings.hasValue(dt.tablename) && Regex.isMatch(dt.tablename, "\\$\\{([\\W\\w])*?\\}"));
        Raw.Params = Json.toObject(dt.params,new TypeReference<List<Map<String, Object>>>(){});
        Raw.AllColumns = Json.toObject(dt.columns,new TypeReference<List<Map<String, Object>>>(){});
        Raw.QueryForm = dt.queryform;
        Raw.Type = dt.type;
        Raw.Script = dt.script;
        Raw.CacheItems = dt.cacheitems;
        dt.columns = matchParam(dt.columns, param, true);
//        Raw.Events = Json.toObject(dt.events,new TypeReference<Map<String, List<Record>>>());
        Raw.Events = dt.events;
        //处理一下事件
        if (Strings.hasValue(dt.events)){
            String eventstring = matchParam(Strings.mapping(dt.events), param, true);
            if (Strings.isJson(eventstring,1)) Events = Json.toObject(eventstring,new TypeReference<Map<String, List<Record>>>(){});
        }
        Raw.Conn = dt.conn;
        Conn = dt.conn;
        Raw.Table = dt.tablename;
        Raw.Data = dt.data;
        Raw.Permission = dt.permission;
        Raw.Name = dt.name;
        Raw.AnalyzeItems = dt.analyzeitems;
        ColumnList = Json.toObject(dt.columns,new TypeReference<List<Map<String, Object>>>(){});
        AllColumns = ColumnList;
        Condition.clear();
        Condition.dialect = ( DbDialectType.Default.equals(DbDialect) || DbDialect == null ? DaoSeesion.GetDialect(Conn) : DbDialect);
        //解析列模型到条件字段
        Condition.initFieldMapping(parseColumsList());
        if (Strings.isBlank(dt.type) || dt.type.equals("table")){
            Type = DataModelType.Table;
            dt.type = "table";
            TableName = dt.tablename;
        }
        switch (dt.type.toLowerCase()){
            case "table":
                TableName = Strings.mapping(Raw.Table);
                break;
            case "data":
                Type = DataModelType.Data;
                if (Strings.hasValue(Raw.Data)&&Strings.isJson(Raw.Data,2)){
                    Raw.CacheItems = "TableRAM";
                    LatchAction.setData(this, Json.toObject(Raw.Data,new TypeReference<List<Record>>(){}));
                }
                break;
            //执行函数
            case "exec":
                Type = DataModelType.Exec;
                break;
            //存储过程
            case "pro":
                Type = DataModelType.Pro;
                break;
            //sql语句
            case "sql":
                {
                    Type = DataModelType.Sql;
                    //sql自定义语句 可以存放在data段，也可以直接在tableName，此时为级联字串
                    if (Strings.isBlank(dt.data) && Strings.isBlank(dt.tablename)) return -1;
                    if (Strings.isBlank(dt.tablename)){ //不在tableName   在data区

                        String data = matchParam(Strings.mapping(Raw.Data), param,false);
                        if (Strings.isBlank(data)) return -1;
                        Sqls sqls = Sqls.Decoder(data);
                        TableName = sqls.TableName;
                        if(sqls.Condition!=null){
                            Condition.put(sqls.Condition.getWhere(false,true));
                        }
                        Order = sqls.Order;
                        if(Order!=null)Order.UpdateAllProperty("fixed", true);
                        Group = sqls.Group;
                    }
                    else//在tableName区，只替换param，不进行sql解析
                    {
                        TableName = matchParam(Strings.mapping(Raw.Table), param);
                        if (Strings.isBlank(TableName)) return -1;
                    }
                }
                break;
            default:
                TableName = Raw.Table;
                break;
        }
        FullColumns = filterColumns();
        if (Strings.hasValue(Raw.Permission)) checkPermission(Raw.Permission);
        return 1;
    }
    /***
     * 添加列模型
     * @param fieldname 列名称
     * @param title 列标题
     * @param as 列别名
     */
    @Override
    public void addColumn(String fieldname, String title, String as) {
        if(Strings.isBlank(fieldname))return;

        Record column = new Record("fieldname",fieldname);
        if(Strings.hasValue(title))column.put("title",title);
        if(Strings.hasValue(as))column.put("as",as);
        if(ColumnList==null)ColumnList = new ArrayList<Map<String,Object>>();
        if(AllColumns==null)AllColumns = new ArrayList<Map<String,Object>>();
        ColumnList.add(column);
        AllColumns.add(column);
        FullColumns = filterColumns();
    }
    /***
     * 添加列模型
     * @param column 列模型
     */
    @Override
    public void addColumn(Map<String, Object> column) {
        if(column==null)return;
        if(ColumnList==null) ColumnList = new ArrayList<Map<String,Object>>();
        if(AllColumns==null)AllColumns = new ArrayList<Map<String,Object>>();
        ColumnList.add(column);
        AllColumns.add(column);
        FullColumns = filterColumns();
    }
    /***
     * 添加列模型
     * @param json JSON数据，支持Array Object，String
     */
    @Override
    public void addColumn(String json) {
        if(Strings.isBlank(json))return;
        if(Strings.isJson(json)){
            if(ColumnList==null)ColumnList = new ArrayList<Map<String,Object>>();
            if(AllColumns==null)AllColumns = new ArrayList<Map<String,Object>>();
            if(Strings.isJson(json,1)){
                Map<String, Object> map = Json.toObject(json, new TypeReference<Map<String, Object>>() {});
                if(map!=null){
                    ColumnList.add(map);
                    AllColumns.add(map);
                }
            }
            else{
                List<Map<String, Object>> array = Json.toObject(json, new TypeReference<List<Map<String, Object>>>() { });
                for (Map<String, Object> map : array) {
                    if(map==null)continue;
                    ColumnList.add(map);
                    AllColumns.add(map);
                }
            }
        }else{
            addColumn(json,null,null);
        }
        FullColumns = filterColumns();
    }
    /***
     * 获取原始数据模板
     * @param ordername 排序属性
     * @return
     */
    public List<Map<String, Object>> getRawColumnList(String ordername){
        List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
        if (AllColumns == null) return ret;
        for (Map<String, Object> map : AllColumns){
            String _as = com.jladder.lang.Collections.getString(map,"as",null,true);
            if (Strings.hasValue(_as)){
                Map<String, Object> temp = Core.clone(map);
                temp.put("fieldname", _as);
                ret.add(temp);
            }
            else{
                ret.add(map);
            }
        }
        if (Strings.isBlank(ordername)) return ret;
        else{
            java.util.Collections.sort(ret,Comparator.comparing(x-> Convert.toInt(x.get(ordername)==null?1:x.get(ordername)==null)));
            return ret;
        }
    }

    @Override
    public List<Map<String, Object>> getColumnList(String propname) {
        List<Map<String, Object>> re_list = new ArrayList<Map<String, Object>>();
        if(ColumnList==null)return re_list;
        for (Map<String, Object> map : ColumnList){
            String _as = com.jladder.lang.Collections.getString(map,"as",null,true);
            if (Strings.hasValue(_as)) {
                Map<String, Object> temp = Core.clone(map);
                temp.put("fieldname", _as);
                re_list.add(temp);
            }
            else {
                re_list.add(map);
            }
        }
        if (Strings.isBlank(propname)) return re_list;
        else{
            java.util.Collections.sort(re_list,Comparator.comparingInt(x-> com.jladder.lang.Collections.getInt(x,propname,true)));
            return re_list;
        }
    }
    public List<Map<String, Object>> filterColumns(){
        return filterColumns(null);
    }
    /// <summary>
    /// 过滤字段
    /// </summary>
    /// <param name="filterCName">过滤字段</param>
    /// <returns></returns>
    public List<Map<String, Object>> filterColumns(String filterCName){
        if (ColumnList == null || ColumnList.size() < 1) return null;
        if (Strings.hasValue(filterCName)) filterCName = ","+ filterCName+",";
        //清空一些计算型的数组
        columns.clear();
        if(Order==null)Order = new OrderBy();
        Order.Clear();
        expressList.clear();
        List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();//新的列模型
        for (Map<String, Object> cmap : ColumnList){
            Record config = Record.parse(cmap);
            String strFn = com.jladder.lang.Collections.getString(cmap,"fieldname,name,oldname","",true);
            String strAs = com.jladder.lang.Collections.getString(cmap,"as,name","", true);
            String strOrder = com.jladder.lang.Collections.getString(cmap,"order","",true);
            String showText = config.getString("isshow",true);
            boolean ishow=true;
            if(!Strings.isBlank(showText)){
                switch (showText.toLowerCase()){
                    case "false":
                        ishow=false;
                    default:
                        ishow =true;
                        break;
                }
            }
            //列表达式如果存在
            String express = com.jladder.lang.Collections.getString(cmap, "expression",null);
            if (Strings.hasValue(express)){
                if (!Strings.isJson(express,0)) expressList.add(Strings.mapping(express));
                else{
                    Record expresstoevent = Record.parse(express);
                    //级联条件
                    if (expresstoevent != null && expresstoevent.size() > 1 && Strings.hasValue(com.jladder.lang.Collections.haveKey(expresstoevent,"tableName")) && Strings.hasValue(com.jladder.lang.Collections.haveKey(expresstoevent,"relation")))
                    {
                        if (Strings.isBlank(com.jladder.lang.Collections.getString(cmap,"type",null))||Regex.isMatch(com.jladder.lang.Collections.getString(cmap,"type",""), "list|rs"))
                            expresstoevent.put("option", DbSqlDataType.OneToMany.getIndex());
                        if (Regex.isMatch(com.jladder.lang.Collections.getString(cmap,"type",""), "record|bean"))
                            expresstoevent.put("option", DbSqlDataType.GetBean.getIndex());

                        switch (expresstoevent.getInt("option")){
                            case 0:
                            case 16:
                                addEvent("resultset", expresstoevent.put("fieldname", strFn).put("option", DbSqlDataType.OneToMany.getIndex()));
                                break;
                            case 15:
                                addEvent("resultset", expresstoevent.put("fieldname", strFn).put("option", DbSqlDataType.OneToOne.getIndex()));
                                break;
                            case 11:
                                addEvent("resultset", expresstoevent.put("fieldname", strFn).put("option", DbSqlDataType.GetData.getIndex()));
                                break;
                        }
                        continue;
                    }
                }
            }
            if ("isdelete".equals(com.jladder.lang.Collections.getString(cmap,"sign",null)) && Strings.hasValue(strFn)){
                expressList.add(strFn + "=0");
            }
            if (!Strings.isBlank(strFn)){
                //如果于过滤文本匹配->不显示
                if (Strings.hasValue(filterCName) && filterCName.indexOf(",!"+strFn + ",") >= 0) continue;
                //如果与AS做比较
                if (Strings.hasValue(filterCName) && Strings.hasValue(strAs) && filterCName.indexOf(",!"+strAs + ",") >= 0) continue;
                //if field is display
                if (ishow){
                    //是否是额外字段
                    String isext = com.jladder.lang.Collections.getString(cmap, "isext",null, true);
                    if(!Strings.isBlank(isext))isext =  "" + isext.toLowerCase().trim();
                    if (Strings.isBlank(isext) || ( !"true".equals(isext) && "0".equals(isext))){
                        newList.add(Core.clone(config));
                        columns.put(strFn, Strings.isBlank(strAs) ? strFn : strAs);
                    }
                }
                if (!Strings.isBlank(strOrder)){
                    Order.Put(strFn, strOrder, ishow?strAs: null);
                }
            }
        }
        ColumnList = newList;
        return ColumnList;
    }
    /// <summary>
    ///  过滤列模型数据，此方法在初始化过程首次执行一次，以此过滤isshow=false的字段
    /// </summary>
    /// <param name="columnString">文本型以,分割</param>
    /// <returns></returns>
    /// <summary>
    /// 字段匹配，默认以fieldname,支持排序，自序，反向,变名
    /// </summary>
    public List<Map<String, Object>> matchColumns(String columnString)
    {
        return matchColumns(columnString, null);
    }
    /// <summary>
    /// 具体执行字段匹配，默认以fieldname,支持排序，自序，反向,变名
    /// </summary>
    public List<Map<String, Object>> matchColumns(String columnString, String propName){

        //if (!allowMatch) return list_column;
        if (ColumnList == null) return null;
        if (Strings.isBlank(columnString)) return ColumnList;
        if(Order!=null)Order.RemoveAllAlias();
        //default by jsondata prop:'order'
        boolean defaultorder = true;//默认以模版排序
        //如果匹配字段含有:,取消默认排序
        if (Regex.isMatch(columnString,":((asc)|(desc))")){
            defaultorder = false;//如果匹配字段含有排序，清除模版排序，以匹配字段为准
            if(Order==null)Order = new OrderBy();
            Order.Clear();
        }
        List<String> cstrs = Strings.splitByComma(columnString);
        //检索排除字段,排除字段以！开头
        if (("," + columnString).indexOf("!") > 0){
            String filterstr = "";//过滤的字段
            String matchstr = "";//匹配的字段
            for (String str : cstrs){
                if (Strings.isBlank(str) || (str.startsWith("!") && str.length() < 2)) continue;
                if (str.startsWith("!")) filterstr += str + ",";
                else matchstr += str + ",";
            }
            if (matchstr.endsWith(",")) matchstr=matchstr.substring(0, matchstr.length() - 1);
            if (Strings.isBlank(matchstr)) return filterColumns(filterstr);
            else{
                cstrs = new ArrayList<String>();
                cstrs.addAll(Strings.splitByComma(matchstr));
            }
        }
        //只排序不节选
        List<String> onlyOrderList = com.jladder.lang.Collections.where(cstrs, x -> x.startsWith("#"));

        // ReSharper disable once PossibleMultipleEnumeration
        if (com.jladder.lang.Collections.any(onlyOrderList)){
        AllColumns.forEach(x ->{
                String one = com.jladder.lang.Collections.first(onlyOrderList, y ->{
                    if (Strings.isBlank(y)) return false;
                    String f = Regex.replace(y.replace("#",""), ":[\\w\\W]*$", "");
                    if (Strings.isBlank(f)) return false;
                    return f.equals(com.jladder.lang.Collections.getString(x,"fieldname",null)) || f.equals(com.jladder.lang.Collections.getString(x,"as",null));
                }).item2;
                if (Strings.hasValue(one)){
                    // ReSharper disable once PossibleNullReferenceException
                    String[] orders = one.split(":");
                    Order.Put(com.jladder.lang.Collections.getString(x,"fieldname",""), orders.length>1?orders[1]:"asc", com.jladder.lang.Collections.getString(x,"as","", true));
                }
            });
            // ReSharper disable once PossibleMultipleEnumeration
            if(onlyOrderList.size()==cstrs.size())return ColumnList;
            List<String> finalCstrs = cstrs;
            onlyOrderList.forEach(x -> finalCstrs.remove(x));
        }
        columns.clear();
        if (Strings.isBlank(propName)){
            propName= com.jladder.lang.Collections.haveKey(ColumnList.get(0),"fieldname|name|oldname");
            if(propName==null)throw Core.makeThrow("字段属性不存在");
        }
        List<Map<String, Object>> newColumnList = com.jladder.lang.Collections.select(ColumnList, x -> x);
        Map<String, Map<String, Object>> cmap = Collections.reverse(newColumnList, propName);
        Map<String, Map<String, Object>> asmap = Collections.reverse(newColumnList, "as");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (String temp : cstrs){
            String str = temp.trim();//置换一下，temp不能写
            String orderSuffix = null;//排序后缀
            //此字段有排序值
            if (str.contains(":")){
                Matcher regmatch = Regex.match(str, ":(\\w*\\d*)$");
                if (regmatch.find()){
                    orderSuffix = regmatch.group(1);
                    if (Strings.isBlank(orderSuffix)) orderSuffix = "asc";
                }
                str = Regex.replace(str, ":(\\w*\\d*)$", ""); //剩下纯洁的字段名啦！
            }
            String findstr = str;//findstr为假设原始字段名
            String asStr = "";//查询替代字符
            Matcher match = Regex.match(str, "([\\w\\W]+?)(@@|##|\\$\\$|&&)(\\w*)");
            if (match.find()){
                findstr = match.group(1);
                asStr = match.group(3);
                //str = str.Replace("@@", " as ");
            }
            //如果是函数字段或者逃逸字段
            if (Regex.isMatch(findstr, "^[\\$@\\*]")){
                if(DataModelType.Table.equals(Type))Type=DataModelType.Sql;
                Record fieldMap =new Record();
                String fieldname = findstr.substring(1);
                if(Strings.hasValue(asStr)) fieldMap.put("as", asStr);
                fieldMap.put("fieldname", fieldname);
                if (!defaultorder && !Strings.isBlank(orderSuffix)){
                    Order.Put(fieldname, orderSuffix, Strings.isBlank(asStr)?fieldname: asStr);
                }
                list.add(fieldMap);
                if (fieldMap.getString("isext", true) != "true")
                columns.put(fieldname, Strings.isBlank(asStr) ? fieldname : asStr);
                continue;
            }
            boolean isInAs = false;
            //在as字典里查找
            if (asmap != null && asmap.containsKey(findstr)){
                Map<String, Object> t = asmap.get(findstr);
                //-->变更真字段,而后字段字典里处理相关
                if (t !=null && t.containsKey("fieldname")) {
                    isInAs=true;
                    findstr = (String)t.get("fieldname");
                }
            }
            //统一处理,其实在asmap是可以进行处理的，反正效率不太影响
            String findedkey = isInAs ? findstr : com.jladder.lang.Collections.haveKey(cmap,findstr);//需要全小写判定
            if (Strings.hasValue(findedkey)){
                //字段字典
                Record fieldMap = Record.parse(cmap.get(findedkey));
                if (Strings.hasValue(asStr)) fieldMap.put("as", asStr);
                //此步判断其实必要性不大，主要是放置，后期方法改变colunmlist
                //bool ishow = (Core.Get(fieldMap, "isshow") == null || Core.GetString(fieldMap, "isshow").Equals("true")) ? true : false;
                String showtext = fieldMap.getString("isshow",true);
                boolean ishow = Strings.isBlank(showtext)? true : !showtext.toLowerCase().equals("false");
                if (ishow){
                    String astemp = fieldMap.getString("as", true);
                    Order.UpdateAlias(findedkey, astemp);
                    list.add(fieldMap);
                    if (fieldMap.getString("isext", true) != "true")columns.put(findedkey, Strings.isBlank(astemp) ? findedkey : astemp);
                }
                //处理排序属性--->2015-12-17肖昭阳
                if (!defaultorder && !Strings.isBlank(orderSuffix))
                {
                    Order.Put(findedkey, orderSuffix, fieldMap.getString("as",true));
                }
            }
        }
        ColumnList = list;
        return ColumnList;
    }

    @Override
    public List<Map<String, Object>> parseColumsList() {
        return parseColumsList(null);
    }

    /// <summary>
    /// 从模版中取出Columns段
    /// <para>注意：这是字段原型数据</para>
    ///  </summary>
    public List<Map<String, Object>> parseColumsList(Object rawData){
        if (AllColumns == null && rawData == null) return null;
        if (AllColumns != null && rawData == null){
            return AllColumns;
//            List ret=new ArrayList<Map<String,Object>>();
//            Record currentColumns = Record.turn(ColumnList, "fieldname",false);
//            Record oldColumns = Record.turn(AllColumns,"fieldname");
//            oldColumns.forEach((x,y) ->
//            {
//                if(currentColumns.containsKey(x))
//                    ret.add((Map<String,Object>)currentColumns.get(x));
//                else
//                    ret.add((Map<String,Object>)oldColumns.get(x));
//            });
//            return ret;
        }
        if (rawData instanceof Map){
            Record map = Record.parse(rawData);
            String ck = map.haveKey("columns");
            if (Strings.isBlank(ck)) return null;
            Object _columns = map.getObject(ck);
            if (_columns == null) return null;
            List<Map<String, Object>> _cs = null;
            if (_columns instanceof String)
            {
                _cs = Json.toObject(_columns.toString(),new TypeReference<List<Map<String, Object>>>(){});
            }
            else if (_columns instanceof List)
            {
                _cs = (List<Map<String, Object>>)_columns;
                return _cs;
            }
            return _cs;
        }
        return null;
    }
    /// <summary>
    /// 生成Bean实体对象
    /// </summary>
    /// <param name="bean">默认文本</param>
    /// <param name="option">选项</param>
    /// <param name="message">回馈信息</param>
    /// <returns></returns>
    public Record genBean(String bean, int option, StringBuilder message){
       return GenBeanTool.gen(this, Record.parse(bean), DbSqlDataType.get(option), message);
    }
    /// <summary>
    /// 获取重复检查的字段(bean中字段)
    /// </summary>
    /// <param name="bean"></param>
    /// <returns></returns>
    public List<String> hasUniqueFields(Record bean){
        List<String> rList = new ArrayList<String>();
        if (columns == null) return rList;
        //linq太长啦
        for (Map<String,Object> field : parseColumsList()){

            String key = com.jladder.lang.Collections.haveKey(field,"unique");
            if (Strings.isBlank(key)) continue;
            if (com.jladder.lang.Collections.getString(field,key,"").toLowerCase()!="true")continue;
            String fieldName = com.jladder.lang.Collections.getString(field,"fieldname","",true);
            String beankey = com.jladder.lang.Collections.haveKey(bean,fieldName);
            if(Strings.isBlank(beankey))continue;
            rList.add(beankey);
        }
        return rList;
    }
    /// <summary>
    /// 获取重复检查的字段
    /// </summary>
    /// <returns></returns>
    public List<String> hasUniqueFields(){
        List<String> rList = new ArrayList<String>();
        if (columns == null) return rList;
        for (Map<String,Object> field : parseColumsList()){
            String key = com.jladder.lang.Collections.haveKey(field,"unique");
            if(Strings.isBlank(key))continue;

            if(Core.is(field.get(key),true,"true",1))
            rList.add(com.jladder.lang.Collections.getString(field,"fieldname","",true));
        }
        return rList;
    }
    /**
     * 根据属性以及属性数值获取字段组，可变参数为或的关系
     * @param propName 属性名
     * @param val 值,如果为null，所有含有此属性的
     * @return
     */
    public List<String> getFields(String propName, Object ... val){
        List<String> rList = new ArrayList<>();
        if (columns == null) return rList;
        for (Map<String, Object> field : ColumnList){
            if (!field.containsKey(propName)) continue;
            if (val == null || val.length < 1) rList.add(com.jladder.lang.Collections.getString(field,"fieldname",""));
            else{
                if (com.jladder.lang.Collections.any(val, o -> field.get(propName).equals(o)))
                    rList.add(com.jladder.lang.Collections.getString(field,"fieldname",""));
            }
        }
        return rList;
    }


    /// <summary>
    /// 获取字段配置
    /// </summary>
    /// <param name="fieldname">字段名称,可以是别名</param>
    /// <returns></returns>
    public Map<String,Object> getFieldConfig(String fieldname)
    {
        List<Map<String, Object>> list = parseColumsList();
        if(list==null)return null;
        return com.jladder.lang.Collections.first(list, x-> fieldname.equals(com.jladder.lang.Collections.getString(x,"fieldname,name,as","",true))).item2;
    }
    /// <summary>
    /// 更新字段配置,注意最后是字段名
    /// </summary>
    /// <param name="propName">欲修改属性名</param>
    /// <param name="value">属性值</param>
    /// <param name="fields">字段名称集合</param>
    public void updateFieldConfig(String propName, Object value, List<String> fields){
        if (fields == null || fields.size()<1 || ColumnList==null || ColumnList.size()<1) return;
        ColumnList.forEach(c ->{
            String  fieldname = com.jladder.lang.Collections.getString(c,"fieldname,name","");
            if(fieldname!=null)fieldname=fieldname.toLowerCase();
            String finalFieldname = fieldname;
            if (com.jladder.lang.Collections.any(fields, x -> finalFieldname.equalsIgnoreCase(x))){
                c.put(propName, value);
            }
        });
    }
    /// <summary>
    /// 更新字段配置,注意最后是字段名
    /// </summary>
    /// <param name="propName">欲修改属性名</param>
    /// <param name="value">属性值</param>
    /// <param name="fields">字段名数组</param>
    public void updateFieldConfig(String propName, Object value, String ... fields){
        if (fields == null || fields.length<1 || ColumnList==null || ColumnList.size()<1) return;
        ColumnList.forEach(c ->{
            String  fieldname = com.jladder.lang.Collections.getString(c,"fieldname,name",null);
            if(fieldname!=null)fieldname=fieldname.toLowerCase();
            String finalFieldname = fieldname;
            if (com.jladder.lang.Collections.any(fields, x -> finalFieldname.equals(x.toLowerCase()))){
                c.put(propName, value);
            }
        });
    }
    /// <summary>
    /// 寻找字段
    /// </summary>
    /// <param name="matchStr">
    /// 欲匹配的字段串
    /// <example>id,name</example>
    /// </param>
    /// <returns></returns>
    public String matchFieldName(String matchStr){
        throw Core.makeThrow("未实现");
//        if (matchStr.IsBlank()) return null;
//        List<Map<string, object>> fulList = ParseColumsList();
//        return fulList.Select(item => item.GetString("fieldname")).FirstOrDefault(fieldName => !fieldName.IsBlank() && matchStr.IndexOf(fieldName, StringComparison.Ordinal) > -1);
    }
    /// <summary>
    /// 添加事件
    /// </summary>
    /// <param name="name">事件名称</param>
    /// <param name="action">事件配置</param>
    public void addEvent(String name, Curd action){
        addEvent(name, Record.parse(action));
    }
    /// <summary>
    /// 添加事件
    /// </summary>
    /// <param name="name">事件名称</param>
    /// <param name="action">事件配置</param>
    public void addEvent(String name, Record action){
        if(Strings.isBlank(name)||action==null)return;
        if(Events==null)Events=new HashMap<String, List<Record>>();
        String key = com.jladder.lang.Collections.haveKey(Events,name);
        if (Strings.isBlank(key)){
            List<Record> event =  new ArrayList<Record>();
            event.add(action);
            Events.put(name, event);
        }
        else{
            List<Record> oldlist = Events.get(key);
            if(oldlist!=null&&oldlist.size()>0) oldlist.add(action);
            else{
                List<Record> event =  new ArrayList<Record>();
                event.add(action);
                Events.put(key,event);
            }
        }
    }
    /// <summary>
    /// 获取关联动作
    /// </summary>
    /// <param name="key">键名</param>
    /// <returns></returns>
    public List<Record> getRelationAction(String key){
        if (Strings.isBlank(key)) return null;
        if (Events == null) return null;
        //if (key != "query" && Type != DataModelType.Table) return null;

        key = com.jladder.lang.Collections.haveKey(Events,key);
        if (Strings.isBlank(key)) return null;
        return Events.get(key);
    }
    /// <summary>组装sql字符串，conditionText可为
    /// <para>1,[]数组文本,</para>
    /// <para>2,{}对象文本</para>
    /// <para>3,name='ddd' 键值文本</para>
    /// </summary>
    public void setCondition(String conditionText){
        if (Strings.isBlank(conditionText)) return;
        if (Condition == null){
            Condition = new Cnd(parseColumsList(), (DbDialectType.Default.equals(DbDialect) || DbDialect == null ? DaoSeesion.GetDialect(Conn) : DbDialect));
        }
        Condition.put(conditionText);
    }

    /***
     * 设置条件
     * @param sqldic 键值条件
     */
    public void setCondition(Map<String, Object> sqldic){
        if (sqldic == null) return;
        if (Condition == null) Condition = new Cnd(parseColumsList(), (DbDialectType.Default.equals(DbDialect) || DbDialect == null ? DaoSeesion.GetDialect(Conn) : DbDialect));
        Condition.put(sqldic);
    }
    /// <summary>
    /// 插入条件
    /// </summary>
    /// <param name="cnd">条件对象</param>
    public void setCondition(Cnd cnd)
    {
        if (cnd == null) return;
        if (Condition == null) Condition = cnd;
        else  Condition.and(cnd);
    }

    /// <summary>
    /// 获取文本化sql语句
    /// </summary>
    /// <returns></returns>
    public SqlText getSqlText(){
        SqlText where =getWhere();
        return new SqlText("select " + getColumn() + " from "
                + getTableName()
                + where.getCmd()
                + getGroup()
                + getOrder(),
                where.getParameters());
    }
    /// <summary>
    /// 获取模版的分页语句(为什么需要一个数据库方言呢)
    /// </summary>
    /// <param name="dialect">数据库方言</param>
    /// <param name="pager">分页对象</param>
    /// <returns></returns>
    public SqlText pagingSqlText(DbDialectType dialect, Pager pager){
        switch (dialect){
        case ORACLE:
            String last =") T WHERE ROWNUM <= "+pager.getOffset() + pager.getPageSize()+") WHERE RN > "+pager.getOffset();
            return new SqlText("SELECT * FROM (SELECT T.*, ROWNUM RN FROM (" + getSqlText() + last,Condition.parameters);
        case Mssql2000:
        case SQLSERVER:
        //兼容2000数据库，效率不高
            if (pager.getOffset() <= 0){
                return new SqlText("select top "+pager.getPageSize()+" " + getColumn() + " from " + getTableName() + getWhere().cmd +
                    getGroup() + getOrder(),Condition.parameters);
            }
            else{
                List<String> genfield = getFields("gen", "id", "uuid", "autonum");
                String gen = genfield.size() > 0 ? genfield.get(0) : "id";
                SqlText where = getWhere();
                String noinclude= gen+" not in (select top "+ pager.getOffset()+" " + gen+" from "+ getTableName()+ where.cmd + getGroup() + getOrder()+")";
                return new SqlText("select top "+pager.getPageSize() +" "+ getColumn() + " from " + getTableName()
                    + (Strings.hasValue(where.cmd)?(where.cmd + " and "+noinclude):(" where "+ noinclude))
                    + getGroup() + getOrder(), Condition.parameters);
            }
        case Mssql2005:
        case Mssql2008:
        case Mssql2012:
             return new SqlText("select * from (select row_number() over (order by __tc__)__rn__,* from (select top "+pager.getOffset() + pager.getPageSize()+" 0 __tc__, " +
                 getSqlText().cmd.substring(6) + ") t) tt where __rn__ > "+pager.getOffset(),Condition.parameters);
        case MYSQL:
            return new SqlText(getSqlText().cmd + " limit " + (pager.getPageNumber() - 1) * pager.getPageSize() + "," + pager.getPageSize(),Condition.parameters);
        case SQLITE:
             return new SqlText(getSqlText().cmd + " limit " + (pager.getPageNumber() - 1) * pager.getPageSize() + "," + pager.getPageSize(),Condition.parameters);
        }
        return getSqlText();
    }
    /// <summary>
    /// 清空条件语句
    /// </summary>
    public void clearCondition()
    {
        Condition.clear();
    }
    /// <summary>
    /// 返回模版的列模型
    /// </summary>
    public List<Map<String, Object>> getColumnList(){
        return getColumnList("");
    }
    /// <summary>
    /// 获取当前的条件文本
    /// </summary>
    /// <returns></returns>
    public SqlText getWhere(){
        Cnd where = Condition;
        //检查表达式
        String expressSqlText = "";
        if (expressList.size() > 0){
            Cnd cnd = new Cnd(parseColumsList(), DbDialect);
            for (String expressStr : expressList){
                cnd.clear();
                String  condtiontext = cnd.put(expressStr).getWhere(false,false);
                if (where.hasValue()){
                    //全部去掉空格，检查查询条件里是否含有表达式
                    String wheretrim = Regex.replace(where.getWhere(false, false), "\\s*", "").toLowerCase();
                    if (wheretrim.indexOf(Regex.replace(condtiontext, "\\s*", "").toLowerCase()) < 0){
                        expressSqlText += condtiontext + " and ";
                    }
                }
                else expressSqlText += condtiontext + " and ";
            }
            if (Strings.hasValue(expressSqlText) && expressSqlText.endsWith(" and "))
            expressSqlText = Strings.rightLess(expressSqlText,5);
        }
        //无任何听见则返回空
        if (where.isBlank() && Strings.isBlank(expressSqlText)) return new SqlText();
        else {
            // ReSharper disable once AssignNullToNotNullAttribute
            String whereText = where.getWhere(false, false);//去掉头部的where
            if (Strings.hasValue(expressSqlText) && Strings.hasValue(whereText)){
                expressSqlText += " and ";
            }
            return new SqlText(" where " + expressSqlText + whereText,Condition.parameters);
        }
    }
    /// <summary>
    /// 获取表名
    /// </summary>
    /// <returns></returns>
    public String getTableName() {
        return TableName;
    }
    /// <summary>
    /// 获取节选的字段和别名
    /// </summary>
    /// <returns></returns>
    public Map<String, String> getSelect()
    {
        return columns;
    }

    /// <summary>
    /// 获取字段文本串
    /// </summary>
    /// <returns></returns>
    public String getColumn() { return getColumn("", ","); }
    /// <summary>
    /// 获取字段文本串
    /// </summary>
    /// <param name="prefix">前缀文本</param>
    /// <param name="splitStr">分隔符</param>
    /// <returns></returns>
    public String getColumn(String prefix, String splitStr) {
        final String[] reT = {""};
        if (columns == null || columns.size() == 0) return reT[0];
        String wraptext = "";
        if (DataModelType.Table.equals(Type) && DbDialectType.MYSQL.equals(DbDialect)){
            wraptext = "`";
        }
        if (DataModelType.Table.equals(Type) && DbDialectType.PostgreSql.equals(DbDialect)){
            wraptext = "\"";
        }
        String finalWraptext = wraptext;
        columns.forEach((x, y)->{
            reT[0] = reT[0] + (finalWraptext + prefix + x + finalWraptext + (Strings.isBlank(y) ? "" : " as " +finalWraptext+ y+finalWraptext) + splitStr);
        });
        return reT[0].length() > splitStr.length() ? reT[0].substring(0, reT[0].length() - splitStr.length()) : reT[0];
     }
    /// <summary>
    /// 获取分组文本串
    /// </summary>
    /// <returns></returns>
    public String getGroup() {
        return Group != null?Group.toString() : "";
    }
    /// <summary>
    /// 获取排序的串
    /// </summary>
    public String getOrder(){
        return Order!= null?Order.toString() : "";
    }

    /**
     * 判断是否为空模版
     * @return
     */
    @Override
    public boolean isNull() {
        return AllColumns == null || ColumnList == null||ColumnList.size()<1;
    }
    /// <summary>
    /// 从模版中取出queryform段
    /// </summary>
    public Object getQueryForm(){
        return Raw.QueryForm;
    }
    /// <summary>
    /// 获取可用于过滤查询的列
    /// <para>1,和FullColumn之处：顺序，负值隐藏</para>
    /// </summary>
    public List<Map<String, Object>> getAllQueryColumns(){
        java.util.Collections.sort(FullColumns, Comparator.comparingInt(x -> com.jladder.lang.Collections.getInt(x,"isshow",true)));
        return com.jladder.lang.Collections.where(FullColumns, x->!Regex.isMatch(com.jladder.lang.Collections.getString(x,"ishow",""),"^-"));
    }
    /// <summary>
    /// 获取全字段
    /// </summary>
    /// <returns></returns>
    public List<Map<String, Object>> getFullColumns(){
        return FullColumns;
    }

    /// <summary>
    /// 获取模版的真实表名
    /// </summary>
    /// <param name="name">键表名</param>
    /// <param name="dao">数据库连接对象</param>
    /// <returns></returns>
    /// <remarks>过期</remarks>
    public static String getTableName(String name, IDao dao){
        //throw Core.makeThrow("未实现");
        IDataModel dm = DaoSeesion.getDataModel(dao, name, null);
        return dm.TableName;
    }
    public String matchParam(String source, String paramDataDic){
        return matchParam(source,paramDataDic,false);
    }
    /// <summary>
    /// 匹配参数数据
    /// </summary>
    /// <param name="source">待匹配的数据</param>
    /// <param name="paramDataDic">匹配的数据字典</param>
    /// <param name="ignore">忽略严格匹配，3个参数，3个param数据</param>
    /// <returns></returns>
    public String matchParam(String source, String paramDataDic, boolean ignore){

        if (Strings.isBlank(source)) return null;
        if (Strings.isBlank(paramDataDic) && Raw.Params == null) return Strings.mapping(source);
        Record matchDic = new Record();
        if (Strings.hasValue(paramDataDic)) matchDic = Json.toObject(paramDataDic,Record.class);
        if (Raw.Params != null && Raw.Params.size() > 0){
        for (Map<String,Object> col : Raw.Params)
        {
            String fieldName = com.jladder.lang.Collections.getString(col,"fieldname","", true);
            String key = com.jladder.lang.Collections.haveKey(matchDic,fieldName);
            if (Strings.hasValue(key)){
                switch (com.jladder.lang.Collections.getString(col,"type","")){
                    case "where":
                        source = Strings.mapping(source,fieldName, Cnd.parse(matchDic.getString(key)).getWhere(true,true));
                        break;
                    case "cnd":
                        source = Strings.mapping(source,fieldName, Cnd.parse(matchDic.getString(key)).getWhere(false, true));
                        break;
                    default:
                        source = Strings.mapping(source,fieldName, matchDic.getString(key));
                        break;
                }
                matchDic.delete(key);
            }
            else{
                key = com.jladder.lang.Collections.haveKey(col,"defaultValue,default,dvalue");
                if (Strings.hasValue(key)) source = Strings.mapping(source,fieldName, com.jladder.lang.Collections.getString(col,key,""));
            }
        }
        }
        if (ignore && matchDic.size() < 1) return Strings.mapping(source);
        source=Strings.mapping(source,matchDic);
        if (ignore) return source;
        //严格匹配，正则替换规则，如果还是存有，就返回null
        return Regex.isMatch(source, "(@@(\\w *)@@)|(\\$\\{(\\w*)})|(&(\\w*)\\b)") ? null : source;
    }
    /// <summary>
    /// 重置模版
    /// </summary>
    /// <param name="param"></param>
    public void reset(String param){
        fromRaw(Raw, param);
    }
    /// <summary>
    /// 获取模版连接器对应的数据库连接操作对象
    /// </summary>
    /// <returns></returns>
    public IDao fetchConnDao(){
        if (Strings.isBlank(Raw.Conn)) return Dao !=null ? Dao : DaoSeesion.GetDao(Ladder.Settings().getTemplateConn());
        else return DaoSeesion.NewDao(Raw.Conn);
    }
    /// <summary>
    /// 验证权限密钥
    /// </summary>
    /// <param name="permission">权限配置文本</param>
    private void checkPermission(String permission){

        if (permission.indexOf(".") > 0){
            String[] hander = permission.split(".");
//            var assembly = Assembly.Load(hander[0]);
//            var index = permission.LastIndexOf(".", StringComparison.Ordinal);
//            var pclass = assembly.GetType(permission.Substring(0, index));
//            var method = pclass?.GetMethod(hander[hander.Length - 1], BindingFlags.Instance | BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Static);
//            if (method != null){
//                var instance = Activator.CreateInstance(pclass, true);
//                var ret = method.Invoke(instance,
//                ArgumentMapping.MappingMethodParam(method, ArgumentMapping.GetRequestParams().Put("tableName", Raw.Name).Put("raw", Raw)));
//                if (ret instanceof Boolean && !(Boolean)ret) ColumnList = null;
//            }
        }
        else
        {
            //var token = Https.GetContextParam("token");
            String token = "";
            if (Strings.isBlank(token) || token != permission) ColumnList = null;
        }
    }
    /// <summary>
    /// 获取原型数据
    /// </summary>
    /// <returns></returns>
    public DataModelForMapRaw getRaw(){
        return Raw;
    }
    /// <summary>
    /// 获取脚本代码
    /// </summary>
    /// <returns></returns>
    public String getScript(){
        return Raw.Script;
    }
    /// <summary>
    /// 是否可用
    /// </summary>
    /// <returns></returns>
    public boolean enable(){
        if (Strings.hasValue(Raw.Enable) && Regex.isMatch(Raw.Enable.trim().toLowerCase(), "^(false)|0")) return false;
        else return true;
    }

    /// <summary>
    /// 根据文件生成模版
    /// </summary>
    /// <param name="fileName"></param>
    /// <param name="node"></param>
    /// <returns></returns>
    public static List<IDataModel> genDataModels(String fileName,String node){
        throw Core.makeThrow("未实现");
//        if (node.HasValue()) node = "," + node + ",";
//        var ret=new List<IDataModel>();
//        if (fileName.IsBlank()) return ret;
//        var path = Files.RepairPath(fileName);
//        var suffix = Path.GetExtension(path);
//        switch (suffix)
//        {
//        case "js":
//        case "json":
//        {
//        var content = Json.FromFile(path);
//        var jMap = Json.ToObject<Map<String, Map<String, object>>>(content);
//        jMap.ForEach(x =>
//        {
//        var name = x.Key;
//        if (name.IsBlank()) return;
//        if(node.HasValue() && !node.Contains(","+name+","))return;
//        var enable = x.Value.GetString("enable");
//        if (enable != null && (enable.ToLower() == "false" || enable.ToLower() == "0")) return;
//        var dt = x.Value.ToClass<DataModelTable>();
//        var dm=new DataModelForMap();
//        dm.FromDataTable(dt);
//        ret.Add(dm);
//        });
//        }
//        break;
//        case "config":
//        case "xml":
//        {
//        XDocument xdoc = XDocument.Load(path);
//        if (xdoc.Root != null && xdoc.Root.HasElements)
//        {
//        var elements = xdoc.Root.Elements("mapping");
//        if (elements.Any())
//        {
//        foreach (var xElement in elements)
//        {
//        var name = xElement.Attribute("name")?.Value;
//        if (name.IsBlank()) continue;
//        if (node.HasValue() && !node.Contains("," + name + ",")) continue;
//        var enable = xElement.Attribute("enable")?.Value;
//        if (enable != null && (enable.ToLower() == "false"||enable.ToLower()=="0")) continue;
//        var dm=new DataModelForMap(xElement);
//        ret.Add(dm);
//        }
//        }
//        var includes = xdoc.Root.Elements("include");
//        if (includes.Any())
//        {
//        includes.ForEach(x =>
//        {
//        var res = GenDataModels(x.Value.ToString(), node);
//        if(!res.IsBlank())ret.AddRange(res);
//        });
//        }
//
//        }
//        }
//        break;
//        }
//
//
//
//
//
//        return ret;
    }


}