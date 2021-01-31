package com.jladder.db.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Table {
    /**
     * 表名
     * @return
     */
    String value() default "";




//    /** 表名前缀 */
//    String prefix() default "";
//
//    /** 表名后缀 */
//    String suffix() default "";


}