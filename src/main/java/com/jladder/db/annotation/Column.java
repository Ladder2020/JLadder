package com.jladder.db.annotation;

import com.jladder.db.enums.DbGenType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface Column {

//    public Object value() default "";

    /**
     * 数据长度,-1代表自动
     * @return
     */
    public int length()  default  -1;
//        /// <summary>
//        /// 数据类型
//        /// </summary>
//        public DbFieldDataType DataType;

    /**
     * 是否为主键
     * @return
     */
    public boolean pk() default false;

    /**
     * 生成类型
     * @return
     */
    public DbGenType gen() default DbGenType.NoGen;

    /**
     * 默认值
     */
    public String dvalue()  default "";
    /**
     *字段只读性
     */
    public boolean readonly() default false;
        /// <summary>
        /// 字段名称
        /// </summary>
        public String fieldname() default "";

        /// <summary>
        /// 保留位
        /// </summary>
        public int holden = -1;
        /// <summary>
        /// 是否容许为空
        /// </summary>
        public boolean isNull=true;



        /// <summary>
        /// 数据唯一性
        /// </summary>
        public boolean unique  = false;

        /// <summary>
        /// 是否是外键字段
        /// </summary>
        public boolean isFK  = false;

        /// <summary>
        /// 描述
        /// </summary>
        public String descr= null;

    /**
     * 是否扩展属性
     * @return
     */
    public boolean isExt() default false;

//    /**
//     * 字段的自定义组装
//     */
//    public Func2<Object, Boolean> diy() default null;
    /**
     * 是否为删除位
     */
    public boolean isDeleteMark() default false;

    /***
     * 是否静默模式，即以数据库默认值为主
     */
    public boolean isQuiet()  default false;

    /**
     * 是否建立索引
     */
    public boolean isIndex() default false;

}

