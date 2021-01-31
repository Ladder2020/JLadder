package com.jladder.lang;

import com.jladder.convert.ConverterRegistry;

import java.lang.reflect.Type;
import java.util.Date;

public class Convert {
    public static <T> T convert(Class<T> type, Object value){
        return convert((Type)type, value);
    }
    public static <T> T convert(Type type, Object value){
        return convert(type, value, null,true);
    }
    /**
     * 转换值为指定类型，可选是否不抛异常转换<br>
     * 当转换失败时返回默认值
     *
     * @param <T> 目标类型
     * @param type 目标类型
     * @param value 值
     * @param defaultValue 默认值
     * @param quietly 是否静默转换，true不抛异常
     * @return 转换后的值
     * @since 5.3.2
     */
    public static <T> T convert(Type type, Object value, T defaultValue, boolean quietly) {
        final ConverterRegistry registry = ConverterRegistry.getInstance();
        try {
            return registry.convert(type, value, defaultValue);
        } catch (Exception e) {
            if(quietly){
                return defaultValue;
            }
            throw e;
        }
    }

    public static int toInt(Object value) {
        return convert(int.class,value);
    }

    public static Date toDate(Object value) {
        return convert(Date.class,value);
    }

    public static Double toDouble(Object value) {
        return convert(Double.class,value);
    }

    public static Boolean toBool(Object value) {
        return convert(Boolean.class,value);
    }




}
