package com.jladder.data;

/***
 * 操作结果回执类
 */

public class Receipt<T>
{
    /**
     * 信息数据
     */
    public String message;
    /**
     * 结果
     */

    public boolean result;
    /**
     *对象数据
     */
    public T data;
    /// <summary>
    /// 基本构造
    /// </summary>
    public Receipt(){
        result = true;
        message = "";
    }
    public Receipt(Exception e){
        result=false;
        message=e.getMessage();
    }
    /// <summary>
    /// 基本构造
    /// </summary>
    /// <param name="msg">消息文本</param>
    public Receipt(String msg){
        result = false;
        message = msg;
    }
    /// <summary>
    /// 基本构造
    /// </summary>
    /// <param name="result">结果</param>
    /// <param name="message">消息文本</param>
    public Receipt(boolean result, String message){
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
    /**
     * 设置携带数据
     * @param obj 携带数据
     * @return com.jladder.data.Receipt<T>
     * @author YiFeng
     * @date 2022/4/19 15:35
     */
    public Receipt<T> setData(T obj){
        data = obj;
        return this;
    }
    /**
     * 是否成功
     * @return boolean
     * @author YiFeng
     * @date 2022/4/19 15:34
     */
    public boolean isSuccess(){
        return result;
    }

    /**
     * 置错误信息
     * @param message  错误信息
     * @param data 数据
     * @return com.jladder.data.Receipt<T>
     * @author YiFeng
     * @date 2022/4/19 15:34
     */

    public Receipt<T> error(String message,T data){
        this.message = message;
        result = false;
        this.data = data;
        return this;
    }
    /**
     * 置错误信息
     * @param message 错误信息
     * @return com.jladder.data.Receipt<T>
     * @author YiFeng
     * @date 2022/4/19 15:33
     */

    public Receipt<T> error(String  message){
        this.message = message;
        this.result = false;
        return this;
    }
    /**
     * 置错误信息
     * @return com.jladder.data.Receipt<T>
     * @author YiFeng
     * @date 2022/4/19 15:33
     */

    public Receipt<T> Error(){
        message = "出现错误";
        result = false;
        return this;
    }
    /**
     * 设置成功
     * @return com.jladder.data.Receipt<T>
     * @author YiFeng
     * @date 2022/4/19 15:33
     */

    public Receipt<T> ok(){
        result = true;
        return this;
    }
    /**
     * 设置成功
     * @param data 携带数据
     * @return com.jladder.data.Receipt<T>
     * @author YiFeng
     * @date 2022/4/19 15:32
     */

    public Receipt<T> ok(T data){
        result = true;
        this.data = data;
        message = "";
        return this;
    }
    /**
     * 转换AjaxResult对象
     * @return com.jladder.data.AjaxResult
     * @author YiFeng
     * @date 2022/4/19 15:32
     */

    public AjaxResult toResult(){
        AjaxResult ret = new AjaxResult(this.result?200:500,this.message);
        if(!this.result)ret.setDataType(AjaxResultDataType.Error);
        ret.data = this.data;
        return ret;
    }
    /**
     * 创建回执对象
     * @param e 异常对象
     * @return com.jladder.data.Receipt<T> 
     * @author YiFeng
     * @date 2022/4/19 15:32
     */
    
    public static <T> Receipt<T> create(Exception e){
        e.printStackTrace();
        return new Receipt(false,e.getMessage());
    }
}




