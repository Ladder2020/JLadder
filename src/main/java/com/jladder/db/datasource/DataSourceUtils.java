package com.jladder.db.datasource;

import com.jladder.db.enums.DbDialectType;
import com.jladder.lang.Refs;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class DataSourceUtils {
    /**
     * 关闭事件方法集合
     */
    private static final Map<DataSource,Method> closesMethods = new HashMap<DataSource,Method>();

    public static void releaseConnection(@Nullable Connection conn, @Nullable DataSource dataSource){
        try{
            Method method=closesMethods.get(dataSource);
            if(method!=null){
                method.invoke(dataSource,conn);
                return;
            }
            switch (dataSource.getClass().getName()){
                case "com.alibaba.druid.pool.DruidDataSource":
                    method = Refs.getMethod(dataSource.getClass(),"discardConnection");
                    closesMethods.put(dataSource,method);
                    method.invoke(dataSource,conn);
                    break;
                default:
                    conn.close();
                    break;
            }

        }catch (Exception e){

        }
    }




}
