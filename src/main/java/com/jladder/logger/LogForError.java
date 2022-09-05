package com.jladder.logger;

/**
 * 错误日志
 */
public class LogForError{


    /**
     * 模块
     */
    private String module;

    /**
     * 类型
     */
    private String type;

    /**
     * 错误消息
     */
    private String message;
    /**
     * 调用堆栈
     */
    private String stacktrace;

    /**
     * 设置引发原因
     * @param message 错误信息
     * @return
     */
    public LogForError setCause(String message){
        this.message = message;
        return this;
    }


    /**
     * 设置调用堆栈
     * @param trace 堆栈
     * @return
     */
    public LogForError setStacktrace(String trace) {
        stacktrace = trace;
        return this;
    }


    /**
     * 设置调用堆栈
     * @param type 执行类型
     * @return
     */
    public LogForError setType(String type){
        this.type = type;
        return this;
    }

    /**
     * 设置应用模块
     * @param module 应用模块
     * @return
     */
    public LogForError setModule(String module){
        this.module = module;
        return this;
    }

    /**
     * 实例化
     */
    public LogForError(){ }
    /**
     * 实例化
     * @param message 错误消息
     */
    public LogForError(String message)
    {
       this.message = message;
    }

    /**
     * 实例化
     * @param message 错误消息
     * @param type 请求类型
     */
    public LogForError(String message, String type) {
        this.message = message;
        this.type = type;
    }

    /**
     * 获取模块
     * @return
     */
    public String getModule() {
        return module;
    }

    /**
     * 获取类型
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * 获取错误消息
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置错误消息
     * @param message 错误消息
     * @return
     */
    public LogForError setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * 获取错误堆栈
     * @return
     */
    public String getStacktrace() {
        return stacktrace;
    }
}