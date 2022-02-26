package com.jladder.data;

/// <summary>
/// 操作结果回执类
/// </summary>
public class Receipt<T>
{
    /**
     * 信息数据
     */
    public String message;
    /// <summary>
    /// 结果
    /// </summary>
    public boolean result;
    /// <summary>
    /// 对象数据
    /// </summary>
    public T data;
    /// <summary>
    /// 基本构造
    /// </summary>
    public Receipt()
    {
        result = true;
        message = "";
    }
    /// <summary>
    /// 基本构造
    /// </summary>
    /// <param name="msg">消息文本</param>
    public Receipt(String msg)
    {
        result = false;
        message = msg;
    }
    /// <summary>
    /// 基本构造
    /// </summary>
    /// <param name="result">结果</param>
    /// <param name="message">消息文本</param>
    public Receipt(boolean result, String message)
    {
        this.result = result;
        this.message = message;
    }
    /// <summary>
    /// 基本构造
    /// </summary>
    /// <param name="result">结果</param>
    public Receipt(boolean result)
    {
        this.result = result;
        message = result ? "" : "执行错误";
    }
    public T getData(){
        return data;
    }
    /// <summary>
    /// 设置携带数据
    /// </summary>
    /// <param name="obj">携带数据</param>
    /// <returns></returns>
    public Receipt<T> setData(T obj)
    {
        data = obj;
        return this;
    }
    /// <summary>
    /// 是否成功
    /// </summary>
    /// <returns></returns>
    public boolean isSuccess()
    {
        return result;
    }

    /// <summary>
    /// 置错误信息
    /// </summary>
    /// <param name="message">错误信息</param>
    /// <param name="data">数据</param>
    /// <returns></returns>
    public Receipt<T> error(String message,T data)
    {
        this.message = message;
        result = false;
        this.data = data;
        return this;
    }
    /// <summary>
    /// 置错误信息
    /// </summary>
    /// <param name="message">错误信息</param>
    /// <returns></returns>
    public Receipt<T> error(String  message)
    {
        this.message = message;
        this.result = false;
        return this;
    }
    /// <summary>
    /// 置错误信息
    /// </summary>
    /// <returns></returns>
    public Receipt<T> Error()
    {
        message = "出现错误";
        result = false;
        return this;
    }
    /// <summary>
    /// 设置成功
    /// </summary>
    /// <returns></returns>
    public Receipt<T> ok()
    {
        result = true;
        return this;
    }
    /// <summary>
    /// 设置成功
    /// </summary>
    /// <param name="data">携带数据</param>
    /// <returns></returns>
    public Receipt<T> ok(T data)
    {
        result = true;
        this.data = data;
        message = "";
        return this;
    }
    public  AjaxResult toResult(){
        AjaxResult ret = new AjaxResult(this.result?200:500,this.message);
        ret.data = this.data;
        return ret;
    }
}




