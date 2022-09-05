package com.jladder.openapi30;

import com.jladder.lang.Regex;
import com.jladder.proxy.ProxyMapping;

import java.util.HashMap;
import java.util.Map;

public class Parameter {


    private String name;
    /**
     * 可能的值有 "query", "header", "path" 或 "cookie"
     */
    private String in="query";
    /**
     * 对此参数的简要描述，这里可以包含使用示例。CommonMark syntax可以被用来呈现富文本格式.
     */
    private String description;
    /**
     * 标明此参数是否是必选参数。如果 参数位置 的值是 path，那么这个参数一定是 必选 的因此这里的值必须是true。其他的则视情况而定。此字段的默认值是false
     */
    private boolean required;
    /**
     * 标明一个参数是被弃用的而且应该尽快移除对它的使用
     */
    private boolean deprecated;
    /**
     * 设置是否允许传递空参数，这只在参数值为query时有效，默认值是false。如果同时指定了style属性且值为n/a（无法被序列化）,那么此字段 allowEmptyValue应该被忽略
     */
    private boolean allowEmptyValue;

    private Map<String,String> schema;
    public Parameter(){
        schema=new HashMap();
        schema.put("type","string");
        schema.put("format","string");
    }
    public Parameter(String name){
        this.name=name;
        schema.put("type","string");
        schema.put("format","string");
    }
    public Parameter(ProxyMapping mapping){
        if(mapping==null)return;
        this.name = mapping.paramname;
        this.description = mapping.express;
        this.required = Regex.isMatch(mapping.valid,"required");
        this.allowEmptyValue= mapping.ignore.equals("1");
        schema=new HashMap();
        schema.put("type",mapping.datatype);
        schema.put("format",mapping.format);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public boolean isAllowEmptyValue() {
        return allowEmptyValue;
    }

    public void setAllowEmptyValue(boolean allowEmptyValue) {
        this.allowEmptyValue = allowEmptyValue;
    }

    public Map<String, String> getSchema() {
        return schema;
    }

    public void setSchema(Map<String, String> schema) {
        this.schema = schema;
    }




}
