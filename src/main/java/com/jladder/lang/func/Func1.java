package com.jladder.lang.func;

@FunctionalInterface
public interface Func1<T1> {

    /**
     * 执行函数
     *
     * @return 函数执行结果
     * @throws Exception 自定义异常
     */
    T1 invoke() throws Exception;

    /**
     * 执行函数，异常包装为RuntimeException
     *
     * @return 函数执行结果
     */
    default T1 callWithException(){
        try {
            return invoke();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
