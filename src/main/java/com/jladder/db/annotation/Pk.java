package com.jladder.db.annotation;

public @interface Pk {
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