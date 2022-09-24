package com.jladder.data;

import com.jladder.lang.Collections;
import com.jladder.lang.Json;
import com.jladder.lang.Stopwatch;
import com.jladder.lang.Times;

import java.io.Serializable;
import java.util.*;
/***
 * 返回指定结构体
 * @param <T>
 * @param <TXt>
 */
public class AjaxResult<T,TXt> implements Serializable {
    /***
     * 状态码 200:成功返回;500+服务程序码，400+结果代码 ：404不存在 403,密码错误，401：输入无有效数据，402:数据存在或重复
     */
    public int statusCode=200;



    /***
     * 留言信息
     */
    public String message;
    /**
     * 索引资源
     */
    public String rel;

    /***
     * 数据体
     */
    public T data;
    /**
     * 数据名称或者资源名
     */
    public String dataName;

    /***
     * 数据类型
     */
    public String datatype = "undefined";



    /**
     *  时长
     */
    public String duration = "0ms";
    /***
     * 请求时间
     */
    public long starttime= Times.getTS();

    /***
     * 是否切除结构体，只保留data区
     */
    protected boolean cut = false;
    protected Map<String, String> config;
    /**
     * 扩展数据
     */
    public TXt xdata;
    public boolean success;

    /***
     * 自动识别
     * @param result 结果
     */
    public AjaxResult(Object result){
        if (result == null){
            statusCode = 400;
            message = "未有处理结果";
            success=false;
            return;
        }
        if (result instanceof Integer){
            statusCode = (int)result;
            success = (statusCode) == 200;
            if (statusCode != 200){
                success = false;
                message = AjaxResultCode.getMessage(statusCode);
            }
            else message = "操作成功";
            return;
        }
        if (result instanceof AjaxResult) {
            AjaxResult ret = (AjaxResult)result;
            this.success = ret.success;
            statusCode = ret.statusCode;
            message = ret.message;
            this.data = (T) ret.data;
            rel = ret.rel;

            return;
        }
        if (result instanceof Boolean){
            boolean re = (boolean)result;
            if (re){
                statusCode = 200;
                success=true;
            }
            else{
                statusCode = 500;
                success=false;
                message = "操作失败";
            }
            return;
        }

        if (result instanceof AjaxResultCode){
            AjaxResultCode dic = ((AjaxResultCode)result);
            statusCode = dic.getCode();
            if (statusCode != 200){
                success = false;
                message = dic.getMessage();
            }
            else {
                success = true;
                message = "操作成功";
            }
            return;
        }
        if (result.getClass().isInstance(result)) {
            success = true;
            message = "操作成功";
            statusCode=200;
            data = (T)result;
            return;
        }
        if (!(result instanceof String)) return;
        success =  (statusCode==200);
        message = result.toString();
    }

    /***
     * 基本构造
     */

    public AjaxResult(){
        statusCode = 200;
        success = true;
        message = "操作成功";
    }

    /***
     * 基本构造，通过状态码和消息文本
     * @param status 状态码 200:成功返回;500+服务程序码，400+结果代码 ：404不存在 403,密码错误，401：输入无有效数据，402:数据存在或重复
     * @param msg
     */
    public AjaxResult(int status, String msg){
        this.message = msg;
        statusCode = status;
        success = (statusCode == 200);
    }

    /***
     * 设置回馈信息
     * @param code
     * @param msg
     * @return
     */
    public AjaxResult<T, TXt> set(int code, String msg){
        this.statusCode = code;
        this.success = (statusCode == 200);
        this.message = msg;
        return this;
    }

    /***
     * 设置扩展数据
     * @param xdata
     * @return
     */
    public AjaxResult<T,TXt> setXData(TXt xdata) {
        this.xdata = xdata;
        return this;
    }

    /***
     * 获取状态码
     * @return
     */
    public int getStatusCode()
    {
        return statusCode;
    }

    /***
     * 设置状态码
     * @param status
     * @return
     */
    public AjaxResult<T, TXt> setStatusCode(int status){
        statusCode = status;
        success = (statusCode == 200);
        if(AjaxResultCode.getMessage(status)!=null){
            message = AjaxResultCode.getMessage(status);
        }
        return this;
    }

    /***
     * 设置消息
     * @param msg
     * @return
     */
    public AjaxResult<T, TXt> setMessage(String msg){
        message = msg;
        return this;
    }

    /***
     * 设置资源索引
     * @param r
     * @return
     */
    public AjaxResult<T, TXt> setRel(String r){
        rel = r;
        return this;
    }

    /***
     * 设置结束时间点
     * @param starttime
     * @return
     */
    public AjaxResult<T, TXt> setDuration(Date starttime){
        long time = new Date().getTime() - starttime.getTime();
        duration = time+"ms";
//        TimeSpan ts = Date.Now.Subtract(starttime);
//        duration = $"{(int)ts.TotalMilliseconds}ms";
        return this;
    }

    /***
     * 设置结束时间点
     * @param times
     * @return
     */
    public AjaxResult<T, TXt> setDuration(int times){
        duration = times+"ms";
        return this;
    }

    /***
     * 获取数据体
     * @return
     */
    public T getData()
    {
        return data;
    }

    /***
     * 设置数据体
     * @param dreobj
     * @return
     */
    public AjaxResult<T, TXt> setData(T dreobj) {
        data = dreobj;
        return this;
    }

    /***
     * 设置数据源名称
     * @param name
     * @return
     */
    public AjaxResult<T, TXt> setDataName(CharSequence name){
        dataName = name==null?"":name.toString();
        return this;
    }


    /***
     * 设置数据类型
     * @param dt
     * @return
     */
    public AjaxResult<T, TXt> setDataType(String dt) {
        datatype = dt;
        return this;
    }

    /***
     * 设置成功
     * @return
     */
    public AjaxResult<T, TXt> ok(){
        statusCode = 200;
        message = "操作成功";
        return this;
    }

    /***
     * 文本格式化
     * @return
     */
    public  String toString() {
        if (cut) return Json.toJson(this.data);
        if (config == null) return Json.toJson(this);
        else{
            Record record = Record.parse(this);
            config.forEach((k,v) -> record.re(k, v));
            return Json.toJson(record);
        }
    }

    /***
     * 设置是否切除本身结构
     * @param isCut 是否切除
     * @return
     */
    public AjaxResult<T, TXt> cut(boolean isCut){
        cut = isCut;
        return this;
    }

    /***
     * 获取是否切除本身结构
     * @return
     */
    public boolean cut(){
        return cut;
    }

    /***
     * 设置结束点
     * @return
     */
    public AjaxResult<T, TXt> start(){
       this.starttime=Times.getTS();
        return this;
    }
    /***
     * 设置结束点
     * @return
     */
    public AjaxResult<T, TXt> end() {
        long now = Times.getTS();
        duration = (now - starttime) + "ms";
        return this;
    }
    /***
     * 设置结束时间点
     * @param stopWatch 码表
     * @return
     */
    public AjaxResult<T, TXt> setDuration(Stopwatch stopWatch){
        if (stopWatch == null) return this;
        duration = stopWatch.getDuration() + "ms";
        return this;
    }

    /***
     * 设置结束时间点
     * @param times
     * @return
     */
    public AjaxResult<T,TXt> setDuration(long times) {
        duration = times+"ms";
        return this;
    }

    /***
     * 设置映射节点
     * @param node
     * @param name
     * @return
     */
    public AjaxResult<T, TXt> setMapping(String node, String name) {
        if (config == null) config = new HashMap<String,String>();
        config.put(node, name);
        return this;
    }

    /***
     * 转成RequesResult格式
     * @return
     */
    public RequestResult toRequestResult(){
        return new RequestResult(this);

    }

    /***
     * 转化成Receipt对象
     * @return
     */
    public Receipt toReceipt(){
        return new Receipt(statusCode == 200 ? true : false,message).setData(data);
    }

    /***
     *
     * @param obj
     * @return
     */
    public AjaxResult pushData(Object obj){
        if (data == null)
        {
            data = (T)new ArrayList<Object>();
        }
        if (data instanceof List)
        {
            ((List<Object>)data).add(obj);
            return this;
        }
        List<Object> li = Collections.create(data, obj);
        data = (T)li;
        return this;
    }
    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public boolean isSuccess(){
        return statusCode==200;
    }


    public String getMessage() {
        return message;
    }


}
