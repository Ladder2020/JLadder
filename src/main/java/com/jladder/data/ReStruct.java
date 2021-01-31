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
    public boolean Success = false;
    /// <summary>
    /// 结果a
    /// </summary>
    public TM ResultA;
    /// <summary>
    /// 结果a
    /// </summary>
    public TN ResultB;
    /// <summary>
    /// 消息
    /// </summary>
    public String Message;
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
        Success = Success;
    }
    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="message">消息</param>
    public ReStruct(String message)
    {
        Success = false;
        Message = message;
    }

    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="success">是否成功</param>
    /// <param name="resulta">结果1</param>
    /// <param name="resultb">结果2</param>
    public ReStruct(boolean success, TM resulta,TN resultb)
    {
        Success = success;
        ResultA = resulta;
        ResultB = resultb;
    }

    /// <summary>
    /// 设置返回结果
    /// </summary>
    /// <param name="result"></param>
    /// <returns></returns>
    public static <TM,TN>  ReStruct SetResult(TM resulta,TN resultb)
    {
        ReStruct restruct = new ReStruct<TM, TN>(true, resulta,resultb);
        return restruct;
    }
}