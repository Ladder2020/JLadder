package com.jladder.lang.func;

@FunctionalInterface
public interface Action1<T> {

    /**
     * 执行函数
     *
     * return 函数执行结果
     * @throws Exception 自定义异常
     */
    void invoke(T p) throws Exception;

    /**
     * 执行函数，异常包装为RuntimeException
     *
     * return 函数执行结果
     */
    default void callWithException(T p){
        try {
            invoke(p);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
