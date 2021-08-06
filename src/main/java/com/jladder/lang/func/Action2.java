package com.jladder.lang.func;
@FunctionalInterface
public interface Action2<T1,T2> {


    /**
     * 执行函数
     * @param p1 参数1
     * @param p2 参数3=2
     * return 函数执行结果
     * @throws Exception 自定义异常
     */
    void invoke(T1 p1,T2 p2) throws Exception;

    /**
     * 执行函数，异常包装为RuntimeException
     * return 函数执行结果
     */
    default void callWithException(T1 p1,T2 p2){
        try {
            invoke(p1,p2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
