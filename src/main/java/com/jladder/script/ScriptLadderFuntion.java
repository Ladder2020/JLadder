package com.jladder.script;

import com.jladder.actions.impl.QueryAction;
import com.jladder.actions.impl.SaveAction;
import com.jladder.data.AjaxResult;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.db.*;
import com.jladder.db.jdbc.impl.Dao;
import com.jladder.lang.Json;
import com.jladder.lang.Refs;
import com.jladder.lang.Strings;
import com.jladder.net.http.HttpHelper;
import com.jladder.proxy.ProxyService;

import java.util.List;

public class ScriptLadderFuntion{

    /**
     * 网络请求
     * @param url 路径
     * @return
     */
    public AjaxResult http(String url) {
        Receipt<String> ret = HttpHelper.request(url, null, "GET");
        return ret.toResult();
    }


    public AjaxResult http(String url, Object data)
    {
        Receipt<String> ret = HttpHelper.request(url, Record.parse(data), "POST");
        return ret.toResult();
    }
    /// <summary>
    /// httppost请求
    /// </summary>
    /// <param name="url">路径</param>
    /// <param name="data">数据</param>
    /// <returns></returns>
    public AjaxResult http(String url, Object data,String method,Object head)
    {
        Receipt<String> result = HttpHelper.request(url, Record.parse(data), method, Record.parse(head));
        return result.toResult();
    }

    /// <summary>
    /// 获取单对象
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="columns">列字段</param>
    /// <param name="condition">条件</param>
    /// <param name="param"></param>
    /// <param name="rel"></param>
    /// <returns></returns>
    public AjaxResult getBean(Object tableName, Object condition, String columns, Object param, String rel)
    {
        return QueryAction.getBean(Json.toJson(tableName), columns, Record.parse(condition).toString(), Json.toJson(param), rel);
    }
    public AjaxResult getData(Object tableName){
        return getData(tableName,null,null,null);
    }
    public AjaxResult getData(Object tableName, Object condition){
        return getData(tableName,condition,null,null);
    }

    /// <summary>
    /// 获取数据
    /// </summary>
    /// <param name="tableName"></param>
    /// <param name="columns"></param>
    /// <param name="condition"></param>
    /// <param name="param"></param>
    /// <param name="rel"></param>
    /// <returns></returns>
    public AjaxResult getData(Object tableName,Object condition,String columns,  Object param){
        return QueryAction.getData(Json.toJson(tableName),Json.toJson(condition), columns, Json.toJson(param),null);
    }
    /// <summary>
    /// 获得记录数量
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="condition">条件</param>
    /// <param name="param">扩展参数</param>
    /// <returns></returns>
    public AjaxResult getCount(String tableName, Object condition, String param)
    {
        long count =  QueryAction.getCount(tableName, condition, param);
        return new AjaxResult().setData(count);
    }

    /// <summary>
    /// 获取值方法
    /// </summary>
    /// <param name="tableName"></param>
    /// <param name="columns"></param>
    /// <param name="condition"></param>
    /// <param name="param"></param>
    /// <param name="rel"></param>
    /// <returns></returns>
    public AjaxResult getValue(String tableName, Object condition,String columns,  String param, String rel)
    {
        return QueryAction.getValue(tableName, columns, Record.parse(condition).toString(), param, rel);
    }
    /// <summary>
    /// 集成查询
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="condition">条件</param>
    /// <param name="param">参数</param>
    /// <param name="pagesize">分页大小</param>
    /// <param name="rel">资源回馈</param>
    /// <returns></returns>
    public AjaxResult queryData(Object tableName, Object condition, Object param,int pagesize)
    {
        if (pagesize == 0) pagesize = 20;
        return QueryAction.queryData(Json.toJson(tableName), Json.toJson(condition), pagesize, Json.toJson(param),null);
    }
    /// <summary>
    /// 获取分页数据
    /// </summary>
    /// <param name="tableName"></param>
    /// <param name="condition"></param>
    /// <param name="columns"></param>
    /// <param name="param"></param>
    /// <param name="start"></param>
    /// <param name="pageNumber"></param>
    /// <param name="pagesize"></param>
    /// <param name="recordCount"></param>
    /// <returns></returns>
    public AjaxResult getPageData(Object tableName,Object condition,String columns, Object param,int start,int pageNumber, int pagesize, String recordCount)
    {
        AjaxResult result = QueryAction.getPageData(Json.toJson(tableName), Json.toJson(condition), columns, Json.toJson(param), start, pageNumber, pagesize, recordCount);
        return result;
    }

    /// <summary>
    /// 插入数据
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="bean">实体对象</param>
    /// <param name="condition">条件</param>
    /// <returns></returns>
    public AjaxResult insert(Object tableName,Object bean,Object condition)
    {
        String cndstr = Json.toJson(condition);
        AjaxResult result = Strings.isBlank(cndstr) ? SaveAction.insert(Json.toJson(tableName), bean) : SaveAction.insert(Json.toJson(tableName), bean, Cnd.parse(condition));
        return result;
    }
    /// <summary>
    /// 更新数据
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="bean">实体数据</param>
    /// <param name="condition">条件</param>
    /// <param name="rel">资源回执</param>
    /// <returns></returns>
    public AjaxResult update(Object tableName, Object bean,Object condition)
    {
        return SaveAction.saveBean(Json.toJson(tableName), Json.toJson(bean), Json.toJson(condition), 2,"");
    }
    /// <summary>
    /// 删除数据
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="condition">条件</param>
    /// <param name="rel">资源回执</param>
    /// <returns></returns>
    public AjaxResult delete(Object tableName,Object condition)
    {
        return SaveAction.saveBean(Json.toJson(tableName), null, Json.toJson(condition), -1, "");
    }
    /// <summary>
    /// 保存数据
    /// </summary>
    /// <param name="tableName">键表名</param>
    /// <param name="bean">实体数据</param>
    /// <param name="option">动作类型</param>
    /// <param name="condition">条件</param>
    /// <param name="rel">资源回执</param>
    /// <returns></returns>
    public AjaxResult save(Object tableName, Object bean, Object condition)
    {
        return SaveAction.saveBean(Json.toJson(tableName),Json.toJson(bean) ,Json.toJson(condition), 3,"");
    }


    /**
     * 持续保存
     * @param pool
     * @param tableName
     * @param bean
     * @param option
     * @param condition
     * @param rel
     * @return
     */
    public AjaxResult keepsave(KeepDaoPool pool, String tableName, String bean, int option, String condition, String rel){
        if (option == 0) option = 1;
        return SaveAction.saveBean(pool, tableName, bean, option, condition, rel);
    }

    /**
     * 执行查询方法
     * @param sqltext sql语句
     * @return
     */
    public AjaxResult query(String sqltext){
        Dao dao = new Dao();
        try{
            List<Record>  result = dao.query(new SqlText(sqltext));
            return new AjaxResult(!Rs.isBlank(result)).setMessage(dao.getErrorMessage()).setData(result);
        }
        finally{
            dao.close();
        }
    }
    public AjaxResult query(String sqltext, String name){
        Dao dao = new Dao(name);
        try{
            List<Record>  result = dao.query(new SqlText(sqltext));
            return new AjaxResult(!Rs.isBlank(result)).setMessage(dao.getErrorMessage()).setData(result);
        }
        finally{
            dao.close();
        }
    }

    /**
     * 执行sql方法
     * @param sqltext sql语句
     * @param name 数据库连接
     * @return
     */
    public int exec(String sqltext, String name)
    {
        IDao dao = new Dao(name);
        try
        {
            int result = dao.exec(new SqlText(sqltext));
            return result;
        }
        finally
        {
            dao.close();
        }
    }


    /**
     * 调用接口服务代理
     * @param name 接口名称
     * @param data 数据
     * @return
     */
    public AjaxResult proxy(String name, Object data)
    {
        return ProxyService.execute(name, Record.parse(data));
    }

    public AjaxResult invoke(String path){
        return Refs.invoke(path,"",null).toResult();
    }
    public AjaxResult invoke(String path,String method){
        return Refs.invoke(path,method,null).toResult();
    }
    /**
     * 调用内部方法
     * @param path 路径
     * @param method 方法名
     * @param data 执行参数
     * @return
     */
    public AjaxResult invoke(String path,String method,Object data){
        return Refs.invoke(path,method,Record.parse(data)).toResult();
    }
}