package com.jladder.lang.func;
@FunctionalInterface
public interface Func4<T1,T2,T3,T4> {
    /**
     * 执行函数
     * @param p1 参数1
     * @param p2 参数2
     * @param p3 参数3
     * @return 函数执行结果
     * @throws Exception 自定义异常
     */
    T4 invoke(T1 p1,T2 p2,T3 p3) throws Exception;

    /**
     * 执行函数，异常包装为RuntimeException
     *
     * @param p1 参数1
     * @param p2 参数2
     * @param p3 参数3
     * @return 函数执行结果
     */
    default T4 callWithException(T1 p1,T2 p2,T3 p3){
        try {
            return invoke(p1,p2,p3);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
