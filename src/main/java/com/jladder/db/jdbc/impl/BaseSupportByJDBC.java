package com.jladder.db.jdbc.impl;
import com.jladder.Ladder;
import com.jladder.data.KeyValue;
import com.jladder.data.Record;
import com.jladder.db.DbDifferentcs;
import com.jladder.db.DbParameter;
import com.jladder.db.SqlText;
import com.jladder.db.datasource.DataSourceFactory;
import com.jladder.db.datasource.Database;
import com.jladder.db.datasource.Global;
import com.jladder.db.enums.DbDialectType;
import com.jladder.db.jdbc.DbDriver;
import com.jladder.db.jdbc.IBaseSupport;
import com.jladder.hub.DataHub;
import com.jladder.lang.Core;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.lang.func.Func2;
import com.jladder.lang.func.Func3;
import com.jladder.lang.func.Tuple2;
import com.jladder.logger.LogForSql;
import com.jladder.logger.LogWriter;
import com.jladder.logger.Logs;
import com.jladder.web.WebScope;
import com.jladder.web.WebScopeOption;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.function.Function;
import java.util.regex.Matcher;

public class BaseSupportByJDBC extends IBaseSupport {
    private Database db;
    private Connection transaction;
    public BaseSupportByJDBC(){
        isWriteLog= Ladder.Settings().isSqlDebug();
        DataSource datasource = DataHub.getDataSource();
        if(datasource!=null){
            db = new Database(datasource,null,null);
            dialect= DbDialectType.MYSQL;
        }else{
            db = Global.get().getDataBase();
            dialect = DbDriver.getDialect(db.getInfo().getDialect());
        }
    }
    public BaseSupportByJDBC(String name){
        isWriteLog= Ladder.Settings().isSqlDebug();
        DataSource datasource = DataHub.getDataSource(name);
        if(datasource!=null){
            db = new Database(datasource,null,null);
            dialect= DbDialectType.MYSQL;
        }else{
            DataSourceFactory factory = Global.get();
            if(factory==null){
                throw Core.makeThrow("["+name+"]数据库配置不存在");
            }
            db = Global.get().getDataBase(name);
            dialect = DbDriver.getDialect(db.getInfo().getDialect());
        }
    }

    @Override
    public List<Record> query(SqlText sqltext) {
        return query(sqltext,true,null);
    }

    @Override
    public List<Record> query(SqlText sqltext, boolean serialize, Func2<Record, Boolean> callback) {
        LogForSql log = new LogForSql(sqltext).setTag(tag).setConn(maskcode);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean createConn=true;
        try{
            ResultSetMetaData md = null;
            KeyValue<String, Object[]> sql = sqltext.getSql();
            if(isTraning()){
                conn=transaction;
                createConn=false;
            }else{
                conn = db.getConnection();
            }
            if(conn==null)throw Core.makeThrow("获取数据库连接失败");
            List<Record> records = new ArrayList<>();
            ps = conn.prepareStatement(sql.key);
            if(sql.value!=null){
                for (int i = 0; i < sql.value.length; i++) {
                    ps.setObject(i+1,sql.value[i]);
                }
            }
            rs = ps.executeQuery();

            while (rs.next()){
                Record record = new Record();
                if(md == null) md = rs.getMetaData();
                for(int i=0;i<md.getColumnCount();i++){
                    if(serialize)record.put(md.getColumnLabel(i+1).toLowerCase(),handler(rs.getObject(i+1),rs,md,i+1));
                    else record.put(md.getColumnLabel(i+1),rs.getObject(i+1));
                }
                if(callback!=null && !callback.invoke(record)){
                   continue;
                }
                records.add(record);
            }
            log.setEnd();
            return records;
        }
        catch (Exception e){
            error=e.getMessage();
            log.setException(e);
            return null;
        }
        finally {
            release(createConn?conn:null,ps,rs,log);
        }
    }

    private void release(Connection conn, Statement state, ResultSet rs,LogForSql log){
        if(rs!=null){
            try {
                rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if(state!=null){
            try {
                state.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if(conn!=null){
            if(db==null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else db.closeConnection(conn);
        }
        if(log != null){
            //是否打印sql日志
            Object isSqlDebug = WebScope.getValue(WebScopeOption.SqlDebug);
            if((isSqlDebug == null || isSqlDebug.equals(true))&&isWriteLog) Logs.write(log);
        }
    }
    @Override
    public List<List<Record>> querys(SqlText sqlcmd, Collection<DbParameter> dbParameters, Function<Record, Boolean> callback) {
        return null;
    }

    @Override
    public <T> List<T> query(SqlText sqltext, Class<T> clazz, Func2<T, Boolean> callback) {
        LogForSql log = new LogForSql(sqltext).setTag(tag).setConn(maskcode);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean createautoconn=true;
        try{
            Record fm = new Record();
            for (Field field : clazz.getDeclaredFields()) {
                fm.put(field.getName(),field.getName().toLowerCase());
            }
            ResultSetMetaData md = null;
            KeyValue<String, Object[]> sql = sqltext.getSql();
            if(isTraning()){
                createautoconn=false;
                conn=transaction;
            }else{
                conn = db.getConnection();
            }
            List<T> records = new ArrayList<T>();
            ps = conn.prepareStatement(sql.key);
            if(sql.value!=null){
                for (int i = 0; i < sql.value.length; i++) {
                    ps.setObject(i+1,sql.value[i]);
                }
            }
            rs = ps.executeQuery();
            while (rs.next()){
                //目标对象：
                T bean = clazz.newInstance();//无参数构造器：
                if(md == null) md = rs.getMetaData();
                for(int i=0;i<md.getColumnCount();i++){
                    String fieldname = md.getColumnName(i+1);
                    fieldname = fm.haveKey(fieldname);
                    if(Strings.isBlank(fieldname))continue;
                    Field field = clazz.getDeclaredField(fieldname);
                    if(field!=null){
                        Object fieldValue = null;
                        switch (field.getType().getName()){
                            case "java.util.Date":
                                fieldValue = new Date(rs.getTimestamp(fieldname).getTime());
                                break;
                            default:
                                fieldValue = rs.getObject(fieldname,field.getType());
                                break;
                        }
                        field.setAccessible(true);
                        if(fieldValue == null && Core.isBaseType(field.getType(),false)){
                            field.set(bean, 0);
                        }else{
                            field.set(bean, fieldValue);
                        }

                    }
                }
                records.add(bean);
            }
            log.setEnd();
            return records;
        }
        catch (Exception e){
            error=e.getMessage();
            log.setException(e);
            return null;
        }
        finally {
            release(createautoconn?conn:null,ps,rs,log);
        }
    }

    @Override
    public <T> T getValue(SqlText sqltext, Class<T> clazz) {
        LogForSql log = new LogForSql(sqltext).setTag(tag).setConn(maskcode).setType("value");
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean createautoconn=true;
        try{
            KeyValue<String, Object[]> sql = sqltext.getSql();
            if(isTraning()){
                conn=transaction;
                createautoconn=false;
            }else{
                conn = db.getConnection();
            }
            if(conn==null){
                return null;
            }
            ps = conn.prepareStatement(sql.key);
            if(sql.value!=null){
                for (int i = 0; i < sql.value.length; i++) {
                    ps.setObject(i+1,sql.value[i]);
                }
            }
            rs = ps.executeQuery();
            if (rs.next()){
                if(String.class.equals(clazz))return (T)rs.getString(1);
                if(Integer.class.equals(clazz))return (T)Integer.valueOf(rs.getInt(1));
                if(Long.class.equals(clazz))return (T)Long.valueOf(rs.getLong(1));
                else {
                    log.setEnd();
                    return rs.getObject(1,clazz);
                }
            }
            else{
                log.setEnd();
                if(clazz.equals(Integer.class)||clazz.equals(Long.class)) return (T)((Object) 0);
                return null;
            }
        }
        catch (Exception e){
            error=e.getMessage();
            log.setException(e);
            if(clazz.equals(Integer.class))return (T)(Integer.valueOf(-1));
            if(clazz.equals(Long.class))return (T) Long.valueOf(-1);
            return null;
        }
        finally {
            release(createautoconn?conn:null,ps,rs,log);
        }
    }

    @Override
    public <T> List<T> getValues(SqlText sqltext, Class<T> clazz) {
        LogForSql log = new LogForSql(sqltext).setTag(tag).setConn(maskcode).setType("values");
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean createConn=true;
        try{
            KeyValue<String, Object[]> sql = sqltext.getSql();
            if(isTraning()){
                createConn=false;
                conn=transaction;
            }else{
                conn = db.getConnection();
            }
            ps = conn.prepareStatement(sql.key);
            if(sql.value!=null){
                for (int i = 0; i < sql.value.length; i++) {
                    ps.setObject(i+1,sql.value[i]);
                }
            }
            rs = ps.executeQuery();
            List<T> ret = new ArrayList<T>();
            while (rs.next()){
                ret.add(rs.getObject(1,clazz));
            }
            log.setEnd();
            return ret;
        }
        catch (Exception e){
            error=e.getMessage();
            log.setException(e);
            return null;
        }
        finally {
            release(createConn?conn:null,ps,rs,log);
        }
    }

    @Override
    public List<Record> getTables(String schemaName, String tableName) {
        return null;
    }

    @Override
    public boolean exists(String tableName) {
        String  sql = "SELECT COUNT(1) FROM " + tableName + " where 1!=1";
        int count = getValue(new SqlText(sql),int.class);
        return count >= 0;
    }

    @Override
    public int exec(SqlText sqltext) {
        return exec(sqltext,null);
    }

    @Override
    public int exec(SqlText sqltext, Func3<Integer, Connection, Integer> callback) {
        LogForSql log = new LogForSql(sqltext).setTag(tag).setConn(maskcode);
        KeyValue<String, Object[]> sql = sqltext.getSql();
        Connection conn = null;
        PreparedStatement ps = null;
        boolean autocreate=true;
        int rows=-1;
        try{
            if(transaction!=null){
                autocreate=false;
                conn=transaction;
            }else{
                conn = db.getConnection();
            }
            if(conn==null)throw Core.makeThrow("获取数据库连接失败[324]");
            //conn = db.getConnection();

            ps = conn.prepareStatement(sql.key);
            if(sql.value!=null){
                for (int i = 0; i < sql.value.length; i++) {
                    ps.setObject(i+1,sql.value[i]);
                }
            }
            rows = ps.executeUpdate();
            if(callback!=null) {
                try {
                    rows = callback.invoke(rows,conn);
                } catch (Exception e) {
                    e.printStackTrace();
                    error=e.getMessage();
                    log.setEnd(true).setCause(error);
                }
            }else{
                log.setEnd();
            }
            return rows;

        }catch (Exception e){
            error=e.getMessage();
            log.setException(e);
            return -1;
        }
        finally {
            release(autocreate?conn:null,ps,null,log);
        }


    }

    @Override
    public boolean beginTrain() {
        if(transaction!=null)return true;
        try {
            transaction = db.getConnection();
            if(transaction==null)return false;
            transaction.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
            error=e.getMessage();
            return false;
        }
        return true;
    }

    @Override
    public void rollback() {
        if(transaction!=null){
            try {
                transaction.rollback();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                error=throwables.getMessage();
            }finally {
                release(transaction,null,null,null);
                //transaction=null;
            }
        }
    }

    @Override
    public boolean commitTran() {
        if(transaction!=null){
            try {
                transaction.commit();
                return false;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                error=throwables.getMessage();
                return false;
            }finally {
                release(transaction,null,null,null);
                //transaction=null;
            }
        }else  return false;
    }

    @Override
    public List<Map<String, Object>> getFieldInfo(String tableName) {
        Connection conn = null;
        List<Map<String,Object>> ret = new ArrayList<>();
        ResultSet rs=null;
        PreparedStatement pre=null;
        try{

            conn =  db.getConnection();
            pre = conn.prepareStatement(Regex.isMatch(tableName, "^\\s*select\\b")?
                    tableName :"select * from "+tableName+" where 1<>1");
            rs = pre.executeQuery();

            String[] ts = Regex.split(tableName, "\\s*(?i)join\\s*");

            long count = rs.getMetaData().getColumnCount();
            ResultSetMetaData meta = rs.getMetaData();
            Record dic = new Record();
            for (int i = 0; i < count; i++) {
                Record map = new Record();
                String countName = meta.getColumnLabel(i + 1);
                map.put("fieldname", countName);
                map.put("name", meta.getColumnName(i+1));
                map.put("type", DbDifferentcs.MappingType(meta.getColumnClassName(i+1).replace("java.lang.","").toLowerCase()));
                map.put("table",meta.getTableName(i + 1));
                map.put("schema",meta.getCatalogName(i + 1));

                //如果有级联的情况
                if (ts.length > 1)
                {
                    String tn = meta.getTableName(i + 1);
                    String key = dic.haveKey(tn);
                    if (Strings.isBlank(key))
                    {
                        Tuple2<Boolean, String> first = com.jladder.lang.Collections.first(Arrays.asList(ts), x -> Regex.isMatch(" " + x + " ", "[\\s\\.]" + tn + "\\s"));
                        Matcher match = Regex.match(" " + first.item2 + " ", "[\\s\\.]+" + tn + "\\s+(\\w+)\\s+");
                        if (match.find())
                        {
                            String asname = match.group(1);
                            //                            newdic.Put("name", asname + "." + r["ColumnName"].ToString().ToLower());
                            map.put("fieldname", asname + "." + meta.getColumnName(i+1));
                            map.put("as", asname + meta.getColumnName(i+1));
                            dic.put(tn, asname);
                        }
                    }
                    else
                    {
                        String asname = dic.getString(key);
                        map.put("name", asname + "." + meta.getColumnName(i+1));
                        map.put("fieldname", asname + "." + meta.getColumnName(i+1));
                        map.put("as", asname + meta.getColumnName(i+1));
                    }
                }
                ret.add(map);
            }
            return ret;

        }catch (Exception e){
            e.printStackTrace();
            error=e.getMessage();
            return null;
        }
        finally {
            release(conn,pre,rs,null);
        }
    }

    @Override
    public List<Object> pro(String name, List<DbParameter> parameters) {
        LogForSql log = new LogForSql(name).setTag(tag).setConn(maskcode).setType("pro");
        List<Object> ret = new ArrayList<Object>();
        Connection  conn =null;
        CallableStatement stmt=null;
        try {
            conn =  db.getConnection();
            //int count = Collections.count(parameters,x->!x.out);
            stmt = conn.prepareCall("call " + name + "("+String.join(",", java.util.Collections.nCopies(parameters.size(), "?"))+")");
            int index = 1;
            //int index_out = 1;
            for(DbParameter p : parameters){

                if(!p.out){
                    switch (p.type){

                        case Number:
                            stmt.setInt(index,(int)p.value);
                            break;
                        default:
                            stmt.setString(index,(String)p.value);
                            break;
                    }
                }else{
                    switch (p.type){
                        case Number:
                            stmt.registerOutParameter(index, Types.INTEGER);
                            break;
                        default:
                            stmt.registerOutParameter(index, Types.VARCHAR);
                            break;
                    }

                }
                index++;
            }
            stmt.execute();
            index=1;
            for(DbParameter p : parameters){
                if(p.out){
                    switch (p.type){
                        case Number:
                            ret.add(stmt.getInt(index));
                            break;
                        default:
                            ret.add(stmt.getString(index));
                            break;
                    }
                }
                index++;
            }
            log.setEnd();
        } catch (SQLException e) {
            error=e.getMessage();
            log.setException(e);
            return null;
        }
        finally {
            release(conn,stmt,null,log);
        }
        return ret;
    }

    @Override
    public boolean isTraning() {
        return this.transaction!=null;
    }

    @Override
    public void close() {
        if(transaction!=null) {
            try {
                transaction.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Object handler(Object dat,ResultSet rs,ResultSetMetaData md,int index){
        Object ret = dat;
        if(ret==null)return ret;
        String type="";
        try {
            type = md.getColumnClassName(index);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return ret;
        }
        switch (type){
            case "java.sql.Timestamp":
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String str = df.format(dat);
                ret = str;
                break;
            case "java.sql.Date":
                ret = ((java.sql.Date)dat).toString();
                break;
            case "java.time.LocalDateTime":
                ret = ((LocalDateTime)dat).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                break;
            case "java.time.LocalDate":
                ret = ((LocalDate)dat).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                break;
        }
        return ret;
    }
}
