package com.jladder.lang.func;

@FunctionalInterface
public interface Func2<T1,T2> {
    /***
     * 执行函数
     * @param p1 参数1
     * @return 函数执行结果
     * @throws Exception 自定义异常
     */
    T2 invoke(T1 p1) throws Exception;

    /**
     * 执行函数，异常包装为RuntimeException
     *
     * @param parameter 参数
     * @return 函数执行结果
     */
    default T2 callWithException(T1 parameter){
        try {
            return invoke(parameter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
