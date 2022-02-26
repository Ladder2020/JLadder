package com.jladder.data;

/// <summary>
/// 置回结构体
/// </summary>
/// <typeparam name="TM">泛型a</typeparam>
/// <typeparam name="TN">泛型b</typeparam>
public class ReStruct<TM,TN>
{
    /// <summary>
    /// 是否成功
    /// </summary>
    private boolean success = false;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public TM getA() {
        return a;
    }

    public void setA(TM a) {
        this.a = a;
    }

    public TN getB() {
        return b;
    }

    public void setB(TN b) {
        this.b = b;
    }

    public String getMessage() {
        return message;
    }

    public ReStruct<TM,TN> setMessage(String message) {
        this.message = message;
        return this;
    }

    /// <summary>
    /// 结果a
    /// </summary>
    private TM a;
    /// <summary>
    /// 结果a
    /// </summary>
    private TN b;
    /// <summary>
    /// 消息
    /// </summary>
    private String message;
    /// <summary>
    /// 初始化
    /// </summary>
    public ReStruct() { }
    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="success">是否成功</param>
    public ReStruct(boolean success)
    {
        this.success = success;
    }
    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="message">消息</param>
    public ReStruct(String message) {
        this.success = false;
        this.message = message;
    }

    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="success">是否成功</param>
    /// <param name="resulta">结果1</param>
    /// <param name="resultb">结果2</param>
    public ReStruct(boolean success, TM a,TN b){
        this.success = success;
        this.a = a;
        this.b = b;
    }

    /// <summary>
    /// 设置返回结果
    /// </summary>
    /// <param name="result"></param>
    /// <returns></returns>
    public static <TM,TN>  ReStruct setResult(TM a,TN b){
        ReStruct restruct = new ReStruct<TM, TN>(true, a,b);
        return restruct;
    }
    public boolean isSuccess(){
        return success;
    }

}