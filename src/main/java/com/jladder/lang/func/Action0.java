package com.jladder.lang.func;

public interface Action0 {
    /**
     * 执行函数
     *
     * @return 函数执行结果
     * @throws Exception 自定义异常
     */
    void invoke() throws Exception;

    /**
     * 执行函数，异常包装为RuntimeException
     *
     * @return 函数执行结果
     */
    default void callWithException(){
        try {
            invoke();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
