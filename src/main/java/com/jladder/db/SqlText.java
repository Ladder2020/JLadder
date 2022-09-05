package com.jladder.db;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jladder.data.KeyValue;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * Sql语句
 */
public class SqlText {
    /**
     *sql源文本
     */
    public String cmd="";
    public List<DbParameter> parameters = new ArrayList<>();

    public SqlText(){

    }

    public SqlText(String text){
        cmd = text;
    }

    /**
     * 初始化
     * @param cmd
     * @param parameters
     */
    public SqlText(String cmd,List<DbParameter> parameters){
        this.cmd=cmd;
        this.parameters = parameters;
    }

    public SqlText(String cmd,String name,Object ... value ){
        this.cmd = cmd;
        if (Strings.isBlank(cmd)) return;
        String[] names = Regex.split(name, ",|\\|");
        int index = 0;
        int length = value.length;
        for (String s : names){
            parameters.add(new DbParameter(s,index >= length ? value[value.length-1]:value[index]));
            index++;
        }
    }

    public String getCmd() {
        return cmd;
    }

    /***
     * 获取数据字典
     * @return
     */
    public Map<String,Object> getMap(){
        if(this.parameters!=null){
            Map<String,Object> ret = new HashMap<>();
            this.parameters.forEach((x)->ret.put(x.name, x.value));
            return ret;
        }
        return null;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public List<DbParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<DbParameter> parameters) {
        parameters = parameters;
    }

    public String toString(){
        if(Strings.isBlank(cmd))return "";
        String[] ret = new String[]{cmd};
        if(parameters!=null){
            parameters.forEach(x -> ret[0] =ret[0].replace("@"+x.name, "'" + x.value + "'"));
        }
        return ret[0];
    }

    /***
     * 获取sql预编译数据
     * @return
     */
    public KeyValue<String,Object[]> getSql(){

        KeyValue<String,Object[]> ret = new KeyValue<String,Object[]>();
        if(Strings.isBlank(this.cmd))return null;
        ret.key = this.cmd;
        if(parameters==null || parameters.size()<1)return ret;
        Map<String,Object> paramMap = new HashMap<>();
        List<Object> params  =  new ArrayList();
        parameters.forEach(x->{
            if(x.value instanceof JSONObject){
                paramMap.put(x.name, ((JSONObject)x.value).toString());
                return;
            }
            if(x.value instanceof JSONArray){
                paramMap.put(x.name, ((JSONArray)x.value).toString());
                return;
            }
            paramMap.put(x.name, x.value);
        });
        int len = this.cmd.length();

        final StringBuilder name = new StringBuilder();
        final StringBuilder sqlBuilder = new StringBuilder();
        char c;
        Character nameStartChar = null;
        for (int i = 0; i < len; i++) {
            c = this.cmd.charAt(i);
            if (c == ':' || c == '@' || c == '?') {
                nameStartChar = c;
            } else if (null != nameStartChar) {
                // 变量状态
                if (isGenerateChar(c)) {
                    // 变量名
                    name.append(c);
                } else {
                    // 变量结束
                    String nameStr = name.toString();
                    if(paramMap.containsKey(nameStr)) {
                        // 有变量对应值（值可以为null），替换占位符
                        final Object paramValue = paramMap.get(nameStr);
                        sqlBuilder.append('?');
                        params.add(paramValue);
                    } else {
                        // 无变量对应值，原样输出
                        sqlBuilder.append(nameStartChar).append(name);
                    }
                    nameStartChar = null;
                    name.delete(0, name.length());
                    sqlBuilder.append(c);
                }
            } else {
                sqlBuilder.append(c);
            }
        }

        if (name.length()>0) {
            // SQL结束依旧有变量名存在，说明变量位于末尾
            Object paramValue = paramMap.get(name.toString());
            if (null != paramValue) {
                // 有变量对应值，替换占位符
                sqlBuilder.append('?');
                params.add(paramValue);
            } else {
                // 无变量对应值，原样输出
                sqlBuilder.append(nameStartChar).append(name);
            }
            name.delete(0, name.length());
        }

        ret.key = sqlBuilder.toString();
        ret.value = params.toArray();
        return ret;
    }
    /**
     * 是否为标准的字符，包括大小写字母、下划线和数字
     *
     * @param c 字符
     * @return 是否标准字符
     */
    private static boolean isGenerateChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || (c >= '0' && c <= '9');
    }

    /**
     * 创建SqlText
     * @param cmd
     * @return
     */
    public static SqlText create(String cmd){
        return new SqlText(cmd);
    }
    /**
     * 创建SqlText
     * @param cmd
     * @return
     */
    public static SqlText create(String cmd,String name,Object ... value){
        return new SqlText(cmd,name,value);
    }
    /**
     * 创建SqlText
     * @param cmd
     * @return
     */
    public static SqlText create(String cmd,List<DbParameter> parameters){
        return new SqlText(cmd,parameters);
    }

    public boolean isBlank() {
        return Strings.isBlank(cmd);
    }
}
