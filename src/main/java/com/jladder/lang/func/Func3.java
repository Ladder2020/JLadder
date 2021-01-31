package com.jladder.lang.func;


@FunctionalInterface
public interface Func3<T1,T2,T3> {


    /**
     * 执行函数
     * @param p1 参数1
     * @param p2 参数2
     * @return 函数执行结果
     * @throws Exception 自定义异常
     */
    T3 invoke(T1 p1,T2 p2) throws Exception;

    /**
     * 执行函数，异常包装为RuntimeException
     *
     * @param p1 参数1
     * @param p2 参数2
     * @return 函数执行结果
     */
    default T3 callWithException(T1 p1,T2 p2){
        try {
            return invoke(p1,p2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
