package com.jladder.db.jdbc.impl;


import com.jladder.Ladder;
import com.jladder.data.KeyValue;
import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.db.DbDifferentcs;
import com.jladder.db.DbParameter;
import com.jladder.db.SqlText;
import com.jladder.db.datasource.DataSourceUtils;
import com.jladder.db.datasource.Database;
import com.jladder.db.jdbc.IBaseSupport;
import com.jladder.hub.DataHub;
import com.jladder.lang.Core;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.lang.func.Func2;
import com.jladder.lang.func.Func3;
import com.jladder.lang.func.Tuple2;
import com.jladder.logger.LogForSql;
import com.jladder.logger.Logs;
import com.jladder.web.WebScope;
import com.jladder.web.WebScopeOption;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;

public class BaseSupportByTemplate  extends IBaseSupport {
    private JdbcTemplate db;
    private Connection transaction;
    public BaseSupportByTemplate(){
        isWriteLog= Ladder.Settings().isSqlDebug();
        db = DataHub.getJdbcTemplate();
        dialect= Database.getDialect(db.getDataSource());
    }
    public BaseSupportByTemplate(String name){
        isWriteLog= Ladder.Settings().isSqlDebug();
        db = DataHub.getJdbcTemplate(name);
        dialect= Database.getDialect(db.getDataSource());
    }
    @Override
    public List<Record> query(SqlText sqltext) {
        return query(sqltext,true,null);
    }

    @Override
    public List<Record> query(SqlText sqltext, boolean serialize, Func2<Record, Boolean> callback) {
        LogForSql log = new LogForSql(sqltext).setTag(tag).setConn(maskcode);
        KeyValue<String, Object[]> sql = sqltext.getSql();
        try{
            List<Map<String, Object>> rs = db.queryForList(sql.key, sql.value);
            List<Record> ret= new ArrayList<Record>();
            rs.forEach(x->{
                Record record = new Record();
                x.forEach((k,v)->{
                    String key = k;
                    if(serialize){
                        key=key.toLowerCase();
                        if(v!=null){
                            switch (v.getClass().getName()){
                                case "java.sql.Timestamp":
                                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String str = df.format(v);
                                    v = str;
                                    break;
                                case "java.sql.Date":
                                    v = ((java.sql.Date)v).toString();
                                    break;

                            }
                        }
                    }
                    record.put(key,v);
                });
                if(callback!=null){
                    try {
                        if(callback.invoke(record))ret.add(record);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    ret.add(record);
                }
            });
            return ret;
        }finally {
            release(null,null,null,log);
        }

    }

    @Override
    public List<List<Record>> querys(SqlText sqlcmd, Collection<DbParameter> dbParameters, Function<Record, Boolean> callback) {
        return null;
    }

    @Override
    public <T> List<T> query(SqlText sqltext, Class<T> clazz, Func2<T, Boolean> callback) {
        KeyValue<String, Object[]> sql = sqltext.getSql();
        LogForSql log = new LogForSql(sqltext).setTag(tag).setConn(maskcode);
        try{
            List<T> ret = db.queryForList(sql.key, clazz, sql.value);
            if(callback!=null){
                List<T> newarray = new ArrayList<T>();
                for(T entry : ret){
                    try {
                        if(callback.invoke(entry))newarray.add(entry);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return newarray;
            }
            return ret;
        }finally {
            release(null,null,null,log);
        }

    }

    @Override
    public <T> T getValue(SqlText sqltext, Class<T> clazz) {
        KeyValue<String, Object[]> sql = sqltext.getSql();
        LogForSql log = new LogForSql(sqltext).setTag(tag).setConn(maskcode).setType("value");
        try {
            List<T> ret = db.query(sql.key, new RowMapper<T>() {
                @Override
                public T mapRow(ResultSet resultSet, int i) throws SQLException {
                    if(String.class.equals(clazz))return (T)resultSet.getString(1);
                    if(Integer.class.equals(clazz))return (T)Integer.valueOf(resultSet.getInt(1));
                    if(Long.class.equals(clazz))return (T)Long.valueOf(resultSet.getLong(1));
                    else {
                        log.setEnd();
                        return resultSet.getObject(1,clazz);
                    }
                }
            },sql.value);
            if(ret==null||ret.size()<1){
                if(clazz.equals(Integer.class))return (T)((Object) (-1));
                if(clazz.equals(Long.class))return (T)((Object) (-1));
                return null;
            }
            return ret.get(0);
        }
        catch (Exception e){
            e.printStackTrace();
            error=e.getMessage();
            log.setEnd(true).setCause(error);
            if(clazz.equals(Integer.class))return (T)((Object) (-1));
            if(clazz.equals(Long.class))return (T)((Object) (-1));
            return null;
        }
        finally {
            release(null,null,null,log);
        }
    }

    @Override
    public <T> List<T> getValues(SqlText sqltext, Class<T> clazz) {
        LogForSql log = new LogForSql(sqltext).setTag(tag).setConn(maskcode).setType("values");;
        try{
            KeyValue<String, Object[]> sql = sqltext.getSql();
            List<T> ret = db.query(sql.key, new RowMapper<T>() {
                @Override
                public T mapRow(ResultSet resultSet, int i) throws SQLException {
                    Object val = resultSet.getObject(1);
                    if(String.class.equals(clazz))return (T)resultSet.getString(1);
                    if(Integer.class.equals(clazz))return (T)Integer.valueOf(resultSet.getInt(1));
                    if(Long.class.equals(clazz))return (T)Long.valueOf(resultSet.getLong(1));
                    else {
                        log.setEnd();
                        return resultSet.getObject(1,clazz);
                    }
                }
            },sql.value);
            return ret;
        }
        finally {
            release(null,null,null,log);
        }
    }

    @Override
    public List<Record> getTables(String schemaName, String tableName) {
        return null;
    }

    @Override
    public boolean exist(String tableName) {
        String  sql = "SELECT COUNT(1) FROM " + tableName + " where 1!=1";
        int count = getValue(new SqlText(sql),int.class);
        return count >= 0;
    }

    @Override
    public boolean exist(String tableName, Cnd cnd) {
        Integer ret=-1;
        switch (dialect){
            case MYSQL:
            case SQLITE:
                ret = getValue(new SqlText("select 1 from "+tableName+cnd.getWhere(true,false)+" limit 1",cnd.parameters),Integer.class);
                break;
            case ORACLE:
                ret = getValue(new SqlText("select 1 from "+tableName+cnd.getWhere(true,false)+" rownum < 2",cnd.parameters),Integer.class);
                break;
            case Mssql2000:
            case Mssql2005:
            case Mssql2008:
            case SQLSERVER:
                ret = getValue(new SqlText("select top 1 1 from "+tableName+cnd.getWhere(true,false),cnd.parameters),Integer.class);
                break;
            default:
                throw Core.makeThrow("未实现");
        }
        return ret>0?true:false;
    }

    @Override
    public boolean exist(String tableName, SqlText where) {
        Integer ret=-1;
        String whereText = where.getCmd();
        boolean has = Strings.hasValue(whereText);
        switch (dialect){
            case MYSQL:
            case SQLITE:
                ret = getValue(new SqlText("select 1 from "+tableName+(has?" where ":"")+where.getCmd()+" limit 1",where.getParameters()),Integer.class);
                break;
            case ORACLE:
                ret = getValue(new SqlText("select 1 from "+tableName+(has?" where ":"")+where.getCmd()+" rownum < 2",where.parameters),Integer.class);
                break;
            case Mssql2000:
            case Mssql2005:
            case Mssql2008:
            case SQLSERVER:
                ret = getValue(new SqlText("select top 1 1 from "+tableName+(has?" where ":"")+where.getCmd(),where.parameters),Integer.class);
                break;
            default:
                throw Core.makeThrow("未实现");
        }
        return ret>0?true:false;
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
                conn = db.getDataSource().getConnection();
                if(dialect==null)dialect= Database.getDialect(conn);
            }
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
            }
            return rows;

        }catch (Exception e){
            log.setException(e);
            return -1;
        }
        finally {
            release(autocreate?conn:null,ps,null,log);
        }

    }

    @Override
    public boolean beginTrain() {
        try{
            if(transaction!=null)return true;
            transaction=db.getDataSource().getConnection();
            if(dialect==null)dialect= Database.getDialect(transaction);
            if(transaction==null)return false;
            transaction.setAutoCommit(false);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void rollback() {
        if(transaction!=null){
            try {
                transaction.rollback();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }finally {
                release(transaction,null,null,null);
                transaction=null;
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
                return false;
            }finally {
                release(transaction,null,null,null);
                transaction=null;
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

            conn =  db.getDataSource().getConnection();
            if(dialect==null)dialect= Database.getDialect(conn);
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
            conn =  db.getDataSource().getConnection();
            if(dialect==null)dialect= Database.getDialect(conn);
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
        } catch (SQLException throwables) {
            log.setException(throwables);
            throwables.printStackTrace();
            return null;
        }
        finally {
            release(conn,stmt,null,log);
        }
        return ret;
    }

    @Override
    public boolean isTransacting() {
        return this.transaction!=null;
    }

    @Override
    public void close() {
        if(transaction!=null){
            try {
                transaction.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private void release(Connection conn, Statement state, ResultSet rs, LogForSql log){
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
            try {
                DataSourceUtils.releaseConnection(conn,db.getDataSource());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(log != null){
            //是否打印sql日志
            Object isSqlDebug = WebScope.getValue(WebScopeOption.SqlDebug);
            if((isSqlDebug == null || isSqlDebug.equals(true))&&isWriteLog) Logs.write(log);
        }
    }
}
