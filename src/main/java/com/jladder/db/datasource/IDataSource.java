package com.jladder.db.datasource;


import com.jladder.configs.Configs;
import com.jladder.db.DbInfo;
import com.jladder.lang.Strings;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class IDataSource extends DataSourceFactory{

    protected final Map<String, Database> dsMap;
    public IDataSource() {
        dsMap = new ConcurrentHashMap<>();
    }

    @Override
    public Database getDataBase(){
        return getDataBase(DefaultDatabase);
    }
    @Override
    public Database getDataBase(String name){
        Database database = dsMap.get(name);
        if(database!=null)return database;
        DbInfo info = Configs.getValue(name,DbInfo.class);
        if(info != null){
            database = new Database(createDataSource(info),info);
            dsMap.put(info.getName(),database);
        }
        return database;
    }
    @Override
    public DataSource getDataSource(String group) {
        return getDataBase(Strings.isBlank(group)? DefaultDatabase :group).getRaw();
    }

    @Override
    public void close(String group) {
        if (Strings.isBlank(group)) {
            group = DefaultDatabase;
        }
        Database ds = dsMap.get(group);
        if (ds != null) {
            try {
                ds.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            dsMap.remove(group);
        }
    }

    @Override
    public void destroy() {
        dsMap.forEach((k,v)->{
            try {
                v.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        dsMap.clear();
    }

    protected abstract DataSource createDataSource(DbInfo info);

}
