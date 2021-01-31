package com.jladder.data;

/**
 * 结果码
 */
public enum AjaxResultCode{

    Success("成功",200),
    Ok("成功",200),
    NoFound("资源未找到",404),
    Limit("未登陆或者权限不足",401),
    Undfind("未知错误",400),
    ParamsNoPass("参数未通过",444),
    Exception("程序异常",500),
    DataTransaction("数据转换错误",300),


    /**
     * 数据模块类
     */
    DataModelNoHave("未找到模版", 700),
    DataModelError("模版解析错误", 701),
    DataModelOption("模版类型操作选项错误", 702),
    DataModelEnable("模版禁用", 703),


    /**
     * 接口平台
     */
    ProxyMethodRefused("接口请求方式不支持", 801),
    ProxyAuthEncrypt("接口认证加密不对称", 802),
    ProxyAuthToken("接口密钥认证失败", 803),
    ProxyAuthNoPass("接口认证失败", 804),
    ProxyPermission("接口权限不足", 805);

    private String message;
    private int code;

    AjaxResultCode(String message, int code) {
        this.message = message;
        this.code = code;
    }

    // 普通方法
    public static String getMessage(int code) {
        for (AjaxResultCode c : AjaxResultCode.values()) {
            if (c.getCode() == code) {
                return c.message;
            }
        }
        return null;
    }

    // get set 方法
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setIndex(int code) {
        this.code = code;
    }



}
