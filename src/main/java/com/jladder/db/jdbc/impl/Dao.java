package com.jladder.db.jdbc.impl;

import com.jladder.data.Pager;
import com.jladder.data.Record;
import com.jladder.db.*;
import com.jladder.db.annotation.Pk;
import com.jladder.db.annotation.Table;
import com.jladder.db.bean.BaseEntity;
import com.jladder.db.bean.FieldInfo;
import com.jladder.db.enums.DbDialectType;
import com.jladder.db.enums.DbGenType;
import com.jladder.db.jdbc.IBaseSupport;
import com.jladder.lang.*;
import com.jladder.lang.func.Action0;
import com.jladder.lang.func.Func2;
import com.jladder.lang.func.Func3;
import com.jladder.lang.func.Tuple3;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public  class Dao implements IDao {

    public IBaseSupport support;

    public Dao(){
        //support = new BaseSupportByJDBC();
        support = new BaseSupportByJDBC();
    }
    public Dao(String conn){
//        support = new BaseSupportByJDBC(conn);
//        support.maskcode = conn;
        support = new BaseSupportByJDBC(conn);
        support.maskcode = conn;
    }


    public Dao(DbInfo dbInfo){

    }

    /**
     * 是否写日志
     */
    public Boolean isWriteLog = null;
    /// <summary>
    /// 数据库连接标记码
    /// </summary>
    public String MarkCode = null;

    /// <summary>
    /// 标签名称,数据模型标签
    /// </summary>
    public String Tag =null;
    /// <summary>
    /// 执行的sql语句数组
    /// </summary>
    List<String> SqlTexts=null;
    /// <summary>
    /// 连接文本
    /// </summary>
    String ConnectString=null;

    /// <summary>
    /// 数据库方言
    /// </summary>
    public DbDialectType Dialect=null;

    /// <summary>
    /// 错误文本
    /// </summary>
    String ErrorString=null;


    /// <summary>
    /// 是否处于事务中
    /// </summary>
    boolean isTraning=false;


    Connection connection=null;


    @Override
    public DbDialectType getDialect() {
        return this.support.dialect;
    }

    @Override
    public void create(String conn) {
        support = new BaseSupportByJDBC();
    }

    @Override
    public void create(DbInfo dbInfo) {
        //support = new BaseSupportByHu(conn);
    }
    
    @Override
    public <T> List<T> query(String tableName, Cnd cnd, Pager pager, Class<T> clazz) {
        //if (Strings.isBlank(tableName)) tableName = BeanHelper.TableName(typeof(T));
        String sql = "select * from " + tableName + (cnd == null ? "" : cnd.getWhere(true, false));
        SqlText sqltext = pagingSqlText(new SqlText(sql,cnd.parameters), pager);
        return support.query(sqltext,clazz,null);
    }
    @Override
    public List<Record> query(String tableName, Cnd cnd, Pager pager) {
        String sql = "select * from " + tableName + (cnd == null ? "" : cnd.getWhere(true, false));
        SqlText sqltext = pagingSqlText(new SqlText(sql,cnd.parameters), pager);
        return support.query(sqltext);
    }
    /***
     * 查询数据
     * @param sqltext
     * @return
     */
    @Override
    public List<Record> query(SqlText sqltext) {
        return support.query(sqltext);
//        try{
//            PreparedStatement pre = connection.prepareStatement(sqltext.getText());
//            if(sqltext.Parameters!=null && sqltext.Parameters.size()>0){
//                for (int i = 0; i < sqltext.Parameters.size(); i++) {
//                    pre.setString(i+1,sqltext.Parameters.get(i).value.toString());
//                }
//            }
//            ResultSet rs = pre.executeQuery();
//            System.out.println("sql:"+pre.toString());
//            long count = rs.getMetaData().getColumnCount();
//            List<Record> list=new ArrayList<Record>();
//            while (rs.next()) {
//                Record map = new Record();
//                for (int i = 0; i < count; i++) {
//                    Object values = rs.getObject(i + 1);
//                    String countName = rs.getMetaData().getColumnLabel(i + 1);
//                    map.put(countName, values);
//                }
//                list.add(map);
//            }
//            return list;
//
//        }catch (Exception e){
//            System.out.println(e.getMessage()+e.getCause()+e.getStackTrace());
//        }
//
//        return null;
    }

    @Override
    public List<Record> query(SqlText sqltext, Func2<Record, Boolean> callback) {
        return support.query(sqltext,true,callback);
    }

    @Override
    public List<Record> query(SqlText sqltext, Boolean serialize, Func2<Record, Boolean> callback) {
        return support.query(sqltext,true,callback);
    }

    @Override
    public <T> List<T> query(SqlText sqltext, Class<T> clazz) {
        return query(sqltext,clazz,null);
    }

    @Override
    public <T> List<T> query(SqlText sqltext, Class<T> clazz, Func2<T, Boolean> callback) {
        return support.query(sqltext,clazz,null);
    }
    @Override
    public <T> List<T> query(Cnd cnd,Class<T> clazz) {
        String tablename = clazz.getAnnotation(Table.class).value();
        return support.query(new SqlText("select * from "+tablename+cnd.getWhere(true,false),cnd.getParameters()),clazz,null);
    }

    @Override
    public <T> List<T> query(String table, Cnd cnd,Class<T> clazz) {
        return support.query(new SqlText("select * from "+table + cnd.getWhere(true,false),cnd.getParameters()),clazz,null);
    }

    @Override
    public List<Record> select(String tableName, Cnd cnd) {
        return support.query(new SqlText("select * from "+tableName + cnd.getWhere(true,false),cnd.getParameters()),true,null);
    }

    @Override
    public <T> List<T> select(Cnd cnd,Class<T> clazz) {
        String tablename = clazz.getAnnotation(Table.class).value();
        return support.query(new SqlText("select * from "+tablename+cnd.getWhere(true,false),cnd.getParameters()),clazz,null);
    }

    @Override
    public List<Record> queryByPage(SqlText sqltext, Pager pager) {
        return support.query(pagingSqlText(sqltext, pager));
    }

    @Override
    public Record fetch(SqlText sqltext) {
        List<Record> one = support.query(sqltext);
        if(one!=null&&one.size()>0)return one.get(0);
        return null;
    }

    @Override
    public Record fetch(String tableName, String value) {
        return fetch(new SqlText("select * from " + tableName + " where id" + "=@value","@value", "'" + value + "'"));
    }

    @Override
    public Record fetch(String tableName, String key, String value) {
        return fetch(new SqlText("select * from " + tableName + " where "+key + "=@value","@value", "'" + value + "'"));
    }

    @Override
    public Record fetch(String tableName, Cnd cnd) {
        if (cnd != null) return fetch(new SqlText("select * from " + tableName + " " + cnd.getWhere(true, false), cnd.getParameters()));
        else return fetch(onlySqltext(new SqlText("select * from " + tableName)));
    }

    @Override
    public <T> T fetch(Cnd cnd,Class<T> clazz) {
        String tablename = clazz.getAnnotation(Table.class).value();
        List<T> rs;
        if (cnd != null) {
            rs = support.query(new SqlText("select * from " + tablename + " " + cnd.getWhere(true, false), cnd.getParameters()),clazz,null);
        }
        else {
            rs = support.query(new SqlText("select * from " + tablename),clazz,null);
        }
        return rs==null ? null : rs.get(0);
    }

    @Override
    public <T> T fetch(String id,Class<T> clazz) {
        String idname = clazz.getAnnotation(Pk.class).value();
        return fetch(new Cnd().put(idname,"=",id),clazz);
    }

    @Override
    public int count(String tableName, Cnd cnd) {
        return support.getValue(new SqlText("select count(*) from "+tableName+cnd.getWhere(true,false),cnd.getParameters()),int.class);
    }

    @Override
    public int count(String tableName, SqlText where) {
        String whereText = where.getCmd();
        if (!Strings.isBlank(whereText) && !Regex.isMatch(whereText, "^\\s*where"))
        {
            whereText = " where " + whereText;
        }
        return support.getValue(new SqlText("select count(*) from " + tableName + " " + whereText , where.getParameters()),int.class);
    }

    @Override
    public int delete(String tableName, SqlText where) {
        return support.exec(new SqlText("delete from "+tableName+" "+where.getCmd(),where.parameters));
    }

    @Override
    public int delete(String tableName, Cnd cnd) {
        return support.exec(new SqlText("delete from "+tableName+" "+cnd.getWhere(true,false),cnd.parameters));
    }

    @Override
    public <T extends BaseEntity> int delete(T bean) {

        return bean.delete(this);
    }

    @Override
    public int update(String tableName, Object data, Object cnd, String columns, boolean adjust) {

        if (Strings.isBlank(tableName) || data == null) return 0;
        Map<String,Object> record = SaveColumn.Clip(Record.parse(data), columns);
        if (record == null || record.size() < 1) return 0;
        Cnd _cnd = Cnd.parse(cnd);
        if (!adjust) return update(tableName, record, _cnd);
        List<Map<String, Object>> fileds = support.getFieldInfo(tableName);
        Record entry = new Record();
        fileds.forEach(x ->{
            String name = com.jladder.lang.Collections.getString(x,"name");
            Tuple3<Boolean, String, Object> key = com.jladder.lang.Collections.first(record, (k, v) -> Regex.isMatch(k, "^[\\$@#\\*]?" + name));
            if (key.item1) entry.put(key.item2, key.item3);
        });
        return update(tableName,entry,_cnd);
    }

    @Override
    public int update(String tableName, Map<String, Object> record, Cnd cnd) {
        if (record == null || record.size() < 1) return 0;
        String wherestr = "";
        if (cnd != null)
        {
            wherestr = cnd.getWhere(true, false);
        }
        String sqltext = "update " + tableName + " set ";

        List<DbParameter> vals = new ArrayList<>();
        if (!Core.isEmpty(cnd.parameters))
        {
            vals.addAll(cnd.parameters);
        }
        Record changeRecord = new Record();
        int i = 0;
        for (Map.Entry<String, Object> kv : record.entrySet())
        {
            Object obj = kv.getValue();
            String key = kv.getKey();
            if (Regex.isMatch(key, "^!")) continue;
            if (Regex.isMatch(key, "^[\\$@#\\*][\\w]*") && obj != null)
            {
                sqltext += key.substring(1) + " = " + com.jladder.lang.Collections.getString(record,key) + ",";
                continue;
            }
            sqltext += key + " = " + "@" + key + ",";

            if (obj == null) obj = null;
            else
            {
                //如果文本串是一个sql语句，提取并执行，注意只能有一个sql键
                if ((obj instanceof String) && Regex.isMatch((String)obj, "^\\s*\\{\\s*sql:[\\w\\W]*}$"))
                {
                    Record hands = Json.toObject((String)obj,Record.class);
                    if (hands != null && hands.size() == 1)
                    {
                        String handlekey = com.jladder.lang.Collections.haveKey(hands,"sql");
                        if (Strings.hasValue(handlekey))
                        {
                            obj = this.getValue(new SqlText(com.jladder.lang.Collections.getString(hands,handlekey)),Object.class);
                            changeRecord.put(key, obj);
                        }
                    }
                }
            }
            vals.add(new DbParameter(key, obj));
        }
        if (changeRecord.size() > 0) changeRecord.forEach((x,y) -> record.put(x, y));
        sqltext = sqltext.substring(0, sqltext.length() - 1) + wherestr;
        return support.exec(new SqlText(sqltext,vals));
    }

    @Override
    public <T extends BaseEntity> int update(T bean, String columns) {
        return bean.update(this,columns);
    }

//    @Override
//    public int update(String tableName, Map<String, Object> record, String wherestr) {
//        return 0;
//    }
    @Override
    public int insert(String tableName, Object bean){
        return insert(tableName,bean,null,false);
    }
    @Override
    public int insert(String tableName, Object bean, String columns, boolean adjust) {
        if (Strings.isBlank(tableName)) return -1;
        //是字典且不纠正自动 就直接执行
        if ((bean instanceof Map) && !adjust)
        {
            return insertData(tableName, SaveColumn.Clip((Map<String, Object>)bean, columns), null);
        }
        //转化数据类型到Record类型
        Map<String, Object> record = SaveColumn.Clip(Record.parse(bean), columns);
        if (record == null || record.size() < 1) return 0;
        if (!adjust) return insertData(tableName, record, null);
        List<Map<String, Object>> fileds = support.getFieldInfo(tableName);
        Record entry = new Record();
        fileds.forEach(x ->{
            String name = com.jladder.lang.Collections.getString(x,"name");
            Tuple3<Boolean, String, Object> key = com.jladder.lang.Collections.first(record, (k, v) -> Regex.isMatch(k, "^[\\$@#\\*]?" + name));
            if (key.item1) entry.put(key.item2, key.item3);
        });
        return insertData(tableName, entry, null);
    }

    @Override
    public int insertData(String tableName, Map<String, Object> record, Func3<Integer, Connection, Integer> callback) {
        if (record == null || record.size() < 1) return 0;
        String sqltext = "insert into " + tableName + " (";//字段语句
        String valtext = " values (";// 值语句
        List<DbParameter> vals = new ArrayList<>();
        Record changeRecord = new Record();

        for (Map.Entry<String, Object> entry  : record.entrySet())
        {
            String key = entry.getKey();
            Object obj = entry.getValue();
            if (Regex.isMatch(key, "^[\\$@#\\*][\\w]*") && obj != null)
            {
                sqltext += key.substring(1) + ",";
                valtext += com.jladder.lang.Collections.getString(record,key) + ",";
                continue;
            }
            sqltext += key + ",";
            valtext += "@" + key + ",";

            if (obj == null) obj = null;
            else
            {
                //如果文本串是一个sql语句，提取并执行，注意只能有一个sql键
                if ((obj instanceof String) && Regex.isMatch((String)obj, "^\\s*\\{\\s*sql:[\\w\\W]*}$"))
                {
                    Record hands = Json.toObject((String)obj,Record.class);
                    if (hands != null && hands.size() == 1)
                    {

                        String handlekey = com.jladder.lang.Collections.haveKey(hands,"sql");
                        if (!Strings.isBlank(handlekey))
                        {
                            obj = this.getValue(new SqlText(com.jladder.lang.Collections.getString(hands,handlekey)),Object.class);
                            changeRecord.put(key, obj);
                        }
                    }
                }
            }
            vals.add(new DbParameter(key,obj));
        }
        if (changeRecord.size() > 0) changeRecord.forEach((xk,xv) -> record.put(xk,xv));
        sqltext = sqltext.substring(0, sqltext.length() - 1) + ")";
        valtext = valtext.substring(0, valtext.length()  - 1) + ")";
        return this.exec(new SqlText(sqltext + valtext, vals), callback);
    }

    @Override
    public <T extends BaseEntity> int insert(T bean, String columns) {
        return 0;
//        String tablename = bean.getClass().getAnnotation(Table.class).value();
//        return insert(tablename,bean,SaveColumn.Clip(bean,),true);
    }

    @Override
    public <T> int save(String tableName, T bean, Cnd cnd, String columns) {
        SqlText sql = new SqlText("select 1 from "+tableName+cnd.getWhere(true,false),cnd.getParameters());
        support.error="";
        List<Integer> dt = support.getValues(onlySqltext(sql), int.class);
        if(Strings.hasValue(support.error))return -1;
        //新增
        if(dt.size()==0){
            return insert(tableName,bean,columns,false);
        }
        //更新
        if(dt.size()>0){
            return update(tableName,bean,cnd,columns,false);
        }
        return 0;
    }

    @Override
    public <T extends BaseEntity> int save(T bean) {
        FieldInfo field = bean.getPk();
        if(field==null){
            support.error="主键不存在";
            return -1;
        }
        Record record = Record.parse(bean);
        //如果bean里不含键，或者 键的值为空
        if(!record.containsKey(field.fieldname) || Core.isEmpty(record.get(field.fieldname))){
            return bean.insert(this);
        }else{
            return bean.update(this);
        }
    }

    @Override
    public <T extends BaseEntity> int save(T bean, Cnd cnd, String columns) {
        String tableName = bean.getTableName();
        if(Strings.isBlank(tableName)){
            support.error="实体对象的关联表未配置";
            return -1;
        }
        SqlText sql = new SqlText("select 1 from "+tableName+cnd.getWhere(true,false),cnd.getParameters());
        support.error="";
        List<Integer> dt = support.getValues(onlySqltext(sql), int.class);
        if(Strings.hasValue(support.error))return -1;
        //新增
        if(dt.size()==0){
            return insert(tableName,bean,columns,false);
        }
        //更新
        if(dt.size()>0){
            return update(tableName,bean,cnd,columns,false);
        }
        return 0;
    }

    @Override
    public <T> int save(String tableName, T bean) {
        return save(tableName,Record.parse(bean),"id", DbGenType.UUID);
    }
    @Override
    public <T> int save(String tableName, T bean, String keyName, DbGenType gen) {
        return save(tableName,Record.parse(bean),keyName,gen);
    }
    public int save(String tableName, Map<String, Object> record, String keyName, DbGenType gen){
        //主键内容为空，为新增
        if (Strings.isBlank(Collections.getString(record,keyName)))
        {
            Object id = null;
            switch (gen)
            {
                case UUID:
                    id = Core.genUuid();
                    break;
                case ID:
                    id = "{ sql: 'select IFNULL(Max(" + keyName + "),0)+1 from " + tableName + "'}";
                    break;
                case TimeCode:
                    id = Times.TimeCode();
                    break;
            }
            record.put(keyName, id);
            return insert(tableName, record);
        }
        else
        {
            return update(tableName, record, new Cnd(keyName,record.get(keyName)));
        }

    }


    @Override
    public SqlText pagingSqlText(SqlText sqltext, int getCount) {
        return null;
    }

    @Override
    public boolean isDefaultDataBase() {
        return false;
    }

    @Override
    public int exec(SqlText sqltext, Func3<Integer, Connection, Integer> callback) {
       return support.exec(sqltext,callback);
    }
    @Override
    public int exec(SqlText cmd) {
        return support.exec(cmd,null);
    }

    @Override
    public List<Object> pro(String name, List<DbParameter> parameters) {
        return support.pro(name,parameters);
    }

    @Override
    public boolean beginTran() {
        return support.beginTrain();
    }

    @Override
    public void rollback() {
        support.rollback();
    }

    @Override
    public boolean commitTran() {
        return support.commitTran();
    }

    @Override
    public <T> T getValue(SqlText sqltext,Class<T> clazz) {
        return support.getValue(sqltext,clazz);
    }

    @Override
    public <T> T getValue(String tableName, String columnName, Cnd cnd,Class<T> clazz) {
        String sqlcmd = "select " + columnName + " from " + tableName + (cnd == null ? "" : cnd.getWhere(true, false));
        return null;
    }

    @Override
    public int getMaxId(String tableName, String columnName) {
        String sql = "select Max(" + columnName + ")+1 from " + tableName;
        return getValue(new SqlText(sql),int.class);
    }

    @Override
    public SqlText onlySqltext(SqlText sqltext, boolean isAppend){
        return onlySqltext(sqltext, Dialect, isAppend);
    }

    @Override
    public SqlText onlySqltext(SqlText sqltext) {
        return onlySqltext(sqltext, Dialect, false);
    }

    /***
     * 单记录分页语句
     * @param sqltext sql语句
     * @param dialect 数据方言
     * @param isAppend 是否源语句追加
     * @return
     */
    @Override
    public SqlText onlySqltext(SqlText sqltext, DbDialectType dialect, boolean isAppend) {

        if (sqltext == null) return null;
        if(dialect==null)dialect=getDialect();
        if(dialect==null)return sqltext;
        switch (dialect){
            case  ORACLE:
                return !isAppend ? new SqlText("select * from (" + sqltext.cmd + ") where rownum=1",sqltext.parameters) : new SqlText(sqltext.cmd + " and rownum=1",sqltext.parameters);
            case SQLITE:
            case MYSQL:
                return new SqlText(sqltext.cmd + " limit 0,1",sqltext.parameters);
        }
        return sqltext;
    }

    /***
     * 解析分页语句
     * @param sqltext sql语句
     * @param pager 分页对象
     * @return
     */
    @Override
    public SqlText pagingSqlText(SqlText sqltext, Pager pager) {
        return pagingSqlText(sqltext,pager,support.dialect);
    }

    /***
     * 解析分页语句
     * @param sqltext sql语句
     * @param pager 分页对象
     * @param dialect 数据库方言
     * @return
     */
    @Override
    public SqlText pagingSqlText(SqlText sqltext, Pager pager, DbDialectType dialect) {

        if(pager==null)return sqltext;
        switch (dialect){
            case MYSQL:
            case SQLITE:
                sqltext.setCmd(sqltext.cmd + " limit " + (pager.getPageNumber() - 1) * pager.getPageSize() + "," + pager.getPageSize());
                break;
            case ORACLE:

                String _tsqltext = "*";
                String pre = "SELECT " + _tsqltext + " FROM (SELECT T.*, ROWNUM v__rn__ FROM (";
                String last = ") T WHERE ROWNUM <= "+(pager.getOffset() + pager.getPageSize())+") WHERE v__rn__ > "+pager.getOffset();
                sqltext.setCmd(pre + sqltext.cmd + last);
                break;
            case Mssql2000:
            case SQLSERVER:
                String rawTable = "(" + sqltext.cmd + ") T";
                if (pager.getOffset() <= 0)
                {
                    return new SqlText("select top " + pager.getPageSize() + " * " + " from " + rawTable, sqltext.parameters);
                }
                else
                {
                    if(Strings.isBlank(pager.field)) throw Core.makeThrow("分页字段不能为空");
                    String noinclude = pager.field + " not in (select top " + pager.getOffset() + " " + pager.field + " from (" +sqltext.cmd + ") R )";
                    return new SqlText("select top " + pager.getPageSize() + " T.* from " + rawTable + " where " + noinclude, sqltext.parameters);
                }
            case Mssql2005:
            case Mssql2008:
            case Mssql2012:
                if (!Regex.isMatch(sqltext.cmd, "^\\s*select"))
                {
                    sqltext.setCmd("select * from " + sqltext.cmd);
                }
                pre = "select * from (select row_number() over (order by __tc__)__rn__,* from (select top "+pager.getOffset() + pager.getPageSize()+" 0 __tc__, ";
                last = ") t) tt where __rn__ > "+pager.getOffset();
                sqltext.setCmd(pre + sqltext.cmd.substring(6) + last);
                break;
            default:
                break;

        }
        return sqltext;

    }

    @Override
    public boolean exists(String tableName) {
        return support.exists(tableName);
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isTraning() {
        return false;
    }

    @Override
    public String getMarkCode() {
        return support.maskcode;
    }

    @Override
    public void setTag(String tag) {
        this.Tag = tag;
        support.tag = tag;
    }

    @Override
    public String getErrorMessage() {
        return support.error;
    }

    @Override
    public void AddRollbackEvent(Action0 action) {

    }

    @Override
    public void AddCommitEvent(Action0 action) {

    }

    @Override
    public List<Map<String, Object>> getFieldInfo(String table) {
        return support.getFieldInfo(table);
    }


}