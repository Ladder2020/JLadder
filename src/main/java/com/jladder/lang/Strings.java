package com.jladder.lang;
import com.jladder.actions.impl.EnvAction;
import com.jladder.actions.impl.QueryAction;
import com.jladder.configs.Configs;
import com.jladder.data.AjaxResult;
import com.jladder.data.Receipt;
import com.jladder.data.Record;

import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;

public class Strings {
    protected Strings() {}

    public static List<String> list(String ...str){
        if(str==null)return null;
        List<String> array = new ArrayList<>();
        for (String s : str) {
            array.add(s);
        }
        return array;
    }
    public static boolean isNumber(String str)
    {

        return !isBlank(str) && Regex.isMatch(str, "^-?\\d*\\.?\\d*$");
    }
    public static int toInt(CharSequence source){
        if(isBlank(source))return 0;
        String data = source.toString().trim();
        if(Regex.isMatch(data, "^-?\\d*$"))return Integer.parseInt(data);
        return 0;
    }

    /***
     *
     * @param src
     * @param len
     * @param ch
     * @return
     */
    public static String padLeft(String src, int len, char ch) {
        int diff = len - src.length();
        if (diff <= 0) {
            return src;
        }

        char[] charr = new char[len];
        System.arraycopy(src.toCharArray(), 0, charr, 0, src.length());
        for (int i = src.length(); i < len; i++) {
            charr[i] = ch;
        }
        return new String(charr);
    }
    /**
     * 获得StringReader
     * @param str 字符串
     * @return StringReader
     */
    public static StringReader getReader(CharSequence str) {
        if (null == str) {
            return null;
        }
        return new StringReader(str.toString());
    }
    /***
     *
     * @param src
     * @param len
     * @param ch
     * @return
     */
    public static String padRight(String src, int len, char ch) {
        int diff = len - src.length();
        if (diff <= 0) {
            return src;
        }

        char[] charr = new char[len];
        System.arraycopy(src.toCharArray(), 0, charr, diff, src.length());
        for (int i = 0; i < diff; i++) {
            charr[i] = ch;
        }
        return new String(charr);
    }

    /// <summary>
    /// 取右截去文本
    /// </summary>
    /// <param name="str">原文本</param>
    /// <param name="len">舍去的长度</param>
    /// <returns>剩下的文本</returns>
    public static String rightLess(CharSequence str, int len)
    {
        if (isBlank(str)) return "";
        if (str.length() <= len) return "";
        return str.subSequence(0,str.length() - len).toString();
    }

    /**
     * 复制字符
     * @param c 字符
     * @param num 数量
     * @return 新字符串
     */
    public static String dup(char c, int num) {
        if (c == 0 || num < 1) return "";
        StringBuilder sb = new StringBuilder(num);
        for (int i = 0; i < num; i++)sb.append(c);
        return sb.toString();
    }

    /**
     * 在字符串左侧填充一定数量的特殊字符
     *
     * @param o
     *            可被 toString 的对象
     * @param width
     *            字符数量
     * @param c
     *            字符
     * @return 新字符串
     */
    public static String prepend(Object o, int width, char c) {
        if (null == o)
            return null;
        String s = o.toString();
        int len = s.length();
        if (len >= width)
            return s;
        return new StringBuilder().append(dup(c, width - len)).append(s).toString();
    }

    /**
     * 去掉字符串前后空白字符。空白字符的定义由Character.isWhitespace来判断
     * @param cs 字符串
     * @return 去掉了前后空白字符的新字符串
     */
    public static String trim(CharSequence cs) {
        if (null == cs)
            return null;
        int length = cs.length();
        if (length == 0)
            return cs.toString();
        int l = 0;
        int last = length - 1;
        int r = last;
        for (; l < length; l++) {
            if (!Character.isWhitespace(cs.charAt(l)))
                break;
        }
        for (; r > l; r--) {
            if (!Character.isWhitespace(cs.charAt(r)))
                break;
        }
        if (l > r)
            return "";
        else if (l == 0 && r == last)
            return cs.toString();
        return cs.subSequence(l, r + 1).toString();
    }

    /***
     * 判断字符串是否为空
     * @param cs 字符串
     * @return
     */
    public static boolean isBlank(CharSequence cs) {
        if (null == cs)
            return true;
        int length = cs.length();
        for (int i = 0; i < length; i++) {
            if (!(Character.isWhitespace(cs.charAt(i))))
                return false;
        }
        return true;
    }
    public static boolean hasValue(CharSequence cs){
        return !isBlank(cs);
    }

    /**
     * 对指定对象进行 toString 操作；如果该对象为 null 或者 toString 方法为空串（""），则返回默认值
     * @param obj 指定的对象
     * @param davlue 默认值
     * @return
     */
    public static String def(Object obj,String davlue){
        if (null == obj)return davlue;
        String s = obj.toString();
        return Strings.isBlank(s) ? davlue : s;
    }


    /**
     * 将字符串按半角逗号，拆分成数组，空元素将被忽略
     * @param s 字符串
     * @return 字符串数组
     */
    public static String[] splitIgnoreBlank(String s) {
        return Strings.splitIgnoreBlank(s, ",");
    }

    /**
     * 根据一个正则式，将字符串拆分成数组，空元素将被忽略
     * @param s 字符串
     * @param regex 正则式
     * @return 字符串数组
     */
    public static String[] splitIgnoreBlank(String s, String regex) {
        if (null == s)
            return null;
        String[] ss = s.split(regex);
        List<String> list = new LinkedList<String>();
        for (String st : ss) {
            if (isBlank(st))
                continue;
            list.add(trim(st));
        }
        return list.toArray(new String[list.size()]);
    }
    public static int repeatCount(String str, String word)
    {
        if (Strings.isBlank(str)) return 0;
        int count = 0;
        int index = str.indexOf(word);
        while (index > -1)
        {
            count++;
            index = str.indexOf(word, index + word.length());
        }
        return count;
    }

    /***
     * 以，分割文本并查看小括号是否完整闭合
     * @param text 数据文本
     * @return
     */
    public static List<String> splitByComma(String text)
    {
        if (Strings.isBlank(text)) return null;
        List<String> gs = new ArrayList<>();
        int index = -1;
        index = text.indexOf(",", 0);
        while (index > -1)
        {
            String cstr = text.substring(0, index);
            if (Strings.repeatCount(cstr,"(") ==Strings.repeatCount(cstr,")"))
            {
                if (!Strings.isBlank(cstr)) gs.add(cstr);
                text = text.substring(index + 1);
                if (Strings.isBlank(text)) break;
                index = text.indexOf(",", 0);
            }
            else index = text.indexOf(",", index + 1);
        }
        if (!Strings.isBlank(text))gs.add(text);
        return gs;
    }
    public static String ArrayToString(String[] array, String separate, String wrap) {
        return array2string(array,separate,wrap);
    }
    public static String ArrayToString(List<String> array, String separate, String wrap) {
        return array2string(array,separate,wrap);
    }
    private static String array2string(Object array, String separate, String wrap){
        if (array == null) return "";
        if (Strings.isBlank(separate)) separate = ",";
        if (Strings.isBlank(wrap)) wrap = "";
        StringBuilder re_t = new StringBuilder();
        Class type = array.getClass();
        String finalWrap = wrap;
        String finalSeparate = separate;
        boolean isHandle = false;
        if (type.isArray())
        {
            Collections.forEach((String[])array, e->re_t.append(finalWrap + e + finalWrap + finalSeparate));
            isHandle=true;
        }
        if (array instanceof List && !isHandle)
        {
            ((List<String>)array).forEach(e->re_t.append(finalWrap + e + finalWrap + finalSeparate));
        }
        return re_t.length() > 0 ? re_t.toString().substring(0, re_t.length() - separate.length()) : "";
    }

    public static String arraytext(Object obj){
        return arraytext(obj,true);
    }
    /***
     * 整理成数组文本 1,2,3或'a','b','c'
     * @param obj 原数据对象
     * @param auto 自动推导数值和文本类型
     * @return
     */
    public static String arraytext(Object obj, boolean auto)
    {
        if (obj instanceof List)
        {
            List list = (List)obj;
            StringBuilder ret = new StringBuilder();
            boolean isnumber = true;
            for (Object o : list)
            {
                if (o == null) continue;
                String data = o.toString();
                if (isnumber && !Regex.isMatch(data, "^-?\\d*\\.?\\d*$"))
                {
                    isnumber = false;
                }
                ret.append("@&@" + data + "@&@,");
            }

            String str = Strings.rightLess(ret.toString(),1);
            if (isnumber)
            {
                return auto ? str.replace("@&@", "") : str.replace("@&@", "'");
            }
            else
            {
                return str.replace("@&@", "'");
            }
        }
        if (obj instanceof String[])
        {
            if (auto) return ArrayToString((String[])obj, ",", "'");
            else return ArrayToString((String[])obj, ",", "");
        }
        if (obj instanceof int[])
        {
            StringBuilder ret = new StringBuilder();
            for (int i : (int[])obj)
            {
                ret.append("" + i + ",");
            }
            return ret.length() > 0 ? ret.toString().substring(0, ret.length() - 1) : "";
        }

        if (obj instanceof String)
        {
            String str = (String)obj;
            if (Strings.isBlank(str)) return "";
            str = Regex.replace(str, "^\\s*[,\\[\\(]?", "");
            str = Regex.replace(str, "^[,\\]\\)]?\\s*$", "");
            str = str.replace("'", "");
            boolean isnumber = true;
            List<String> raw = splitByComma(str);
            if (auto)
            {
                for (String s : raw)
                {
                    if (isnumber && !Regex.isMatch(s, "^-?\\d*\\.?\\d*$"))
                    {
                        isnumber = false;
                        break;
                    }
                }
                return arraytext(raw, !isnumber);
            }
            else return arraytext(raw, false);
        }
        return obj.toString();
    }
    /// <summary>
    /// 获取映射字符
    /// </summary>
    /// <param name="oldStr">字符串</param>
    /// <returns></returns>
    public static List<String> getMapping(String oldStr)
    {
        if (Strings.isBlank(oldStr)) return null;
        List<String> ret = new ArrayList<String>();
        //以对象型替换  @@d.id@@
        Matcher res = Regex.match(oldStr, "@@(\\w+)\\.(\\w+)@@");
        while (res.find()){
            String key1 = res.group(1);
            String key2 = res.group(2);
            ret.add(key1 + "." + key2);
        }
        //以对象型替换 形式：${dd.name}
        res = Regex.match(oldStr, "\\$\\{(\\w+)\\.(\\w+)\\}");
        while (res.find())
        {
            String key1 = res.group(1);
            String key2 = res.group(2);
            ret.add(key1 + "." + key2);
        }

        //@@name@@
        Matcher mms = Regex.match(oldStr, "@@(\\w+)@@");
        while (mms.find())
        {
            for (int i = 1; i < mms.groupCount(); i++)
            {
                ret.add(mms.group(i));
            }
        }
        //${name}
        Matcher ms = Regex.match(oldStr, "\\$\\{(\\w+)}");
        while (ms.find())
        {
            for (int i = 1; i < mms.groupCount(); i++)
            {
                ret.add(mms.group(i));
            }
        }
        //&name
        Matcher s = Regex.match(oldStr, "&(\\w+)\\b");
        while (s.find())
        {
            for (int i = 1; i < s.groupCount(); i++)
            {
                ret.add(s.group(i));
            }
        }
        return ret;
    }

    public static String mapping(CharSequence str){
        if(isBlank(str))return "";
        return mapping(str,true);
    }
    public static String mapping(CharSequence oldStr,CharSequence keyword,CharSequence value) {
        if (Strings.isBlank(oldStr)) return "";
        String data = oldStr.toString();
        data = data.replace("@@" + keyword + "@@", value);
        data = data.replace("${" + keyword + "}", value);
        data = data.replace("&" + keyword + " ", value + " ");
        return data;
    }
    public static String mapping(CharSequence source, boolean ispadding) {
        if(Strings.isBlank(source))return "";
        String oldStr = source.toString();
        if (Strings.isBlank(oldStr)) return "";
        //环境变量部分##name##
        Matcher match = Regex.match(oldStr, "##(\\w+)##");
        while (match.find()){
            for (int i = 1; i <= match.groupCount(); i++)
            {
                String keyword = match.group(i);
                String value = EnvAction.GetEnvValue(keyword);
                if(isBlank(value))value = "";
                if (ispadding || Strings.hasValue(value)) oldStr = oldStr.replace("##" + keyword + "##", value);
            }
        }
        //环境变量部分带参数的 ##name&&age
        match = Regex.match(oldStr, "##(\\w+)&&(\\w+)##");
        while (match.find()){
            String key1 = match.group(1);
            String key2 = match.group(2);
            String value = EnvAction.GetEnvValue(key1, key2);
            if(isBlank(value))value = "";
            if (ispadding || Strings.hasValue(value))oldStr = oldStr.replace("@@" + key1 + "&&" + key2 + "@@", value);
        }

        //环境变量${$env.name}
        match = Regex.match(oldStr, "\\$\\{\\$env\\.(\\w+)\\}");
        while (match.find()){
            for (int i = 1; i <= match.groupCount(); i++)
            {
                String keyword = match.group(1);
                String value = EnvAction.GetEnvValue(keyword);
                if(isBlank(value))value = "";
                if (ispadding || Strings.hasValue(value))oldStr = oldStr.replace("${$env." + keyword + "}", value);
            }
        }

        //配置信息 ${$config.name}
        match = Regex.match(oldStr,"\\$\\{\\$config\\.(\\w+)\\}");
        while (match.find()){
            String keyword = match.group(1);
            String value =  Configs.getString(keyword);
            if(isBlank(value))value = "";
            if (ispadding || Strings.hasValue(value))oldStr = oldStr.replace("${$config." + keyword + "}", value);
        }
        //处理时间 ${$date - d5 }
        match = Regex.match(oldStr, "\\$\\{\\s*\\$((date)|(now)|(datetime)|(time))((\\s*[\\+\\-=]\\s*([YyMmDdHhSs])\\s*(\\d+))*)\\}");
        while (match.find()){
            Date now = new Date();
            String head = match.group(1);
            if (match.group().contains("+") || match.group().contains("-") || match.group().contains("=")){
                String express = match.group(6);
                if(!Strings.isBlank(express)) {
                    Matcher m = Regex.match(express, "\\s*([\\+\\-=])\\s*([YyMmDdHhSs])\\s*(\\d+)");
                    while (m.find()) {
                        String op = m.group(1);
                        String unit = m.group(2);
                        String v = m.group(3);
                        int va = !Strings.isBlank(v) ? Integer.parseInt(v) : 0;
                        if (op.equals("+" ) || "=".equals(op) ) va = va * 1;
                        else va = va * -1;
                        switch (unit) {
                            case "Y":
                            case "y":
                                now = "=".equals(op) ? Times.D(Times.format(v + "-MM-dd HH:mm:ss", now)) : Times.addYear(now, va);
                                break;
                            case "M":

                                now = "=".equals(op) ? Times.D(Times.format("yyyy-" + v + "-dd HH:mm:ss", now)) : Times.addMonth(now, va);
                                break;
                            case "m":

                                now = "=".equals(op) ? Times.D(Times.format("yyyy-MM-dd HH:" + v + ":ss", now)) : Times.addMinute(now, va);
                                break;
                            case "d":
                            case "D":
                                now = "=".equals(op)? Times.D(Times.format("yyyy-MM-" + v + " HH:mm:ss", now)) : Times.addDay(now, va);
                                break;
                            case "H":
                            case "h":

                                now = "=".equals(op) ? Times.D(Times.format("yyyy-MM-dd " + v + ":mm:ss", now)) : Times.addHour(now, va);
                                break;
                            case "S":
                            case "s":
                                now = "=".equals(op)? Times.D(Times.format("yyyy-MM-dd HH:mm:" + v, now)) : Times.addSecond(now, va);
                                break;
                        }
                    }
                }
            }
            switch (head.toLowerCase())
            {
                case "date":
                    oldStr = oldStr.replace(match.group(), Times.sD(now));
                    break;
                case "now":
                case "datetime":
                    oldStr = oldStr.replace(match.group(), Times.sDT(now));
                    break;
                case "time":
                    oldStr = oldStr.replace(match.group(), Times.format("HH:mm:ss",now));
                    break;
            }
        }


        return oldStr;
    }
    public static <T> String mapping(CharSequence source, Map<String,T> dic){
        return mapping(source,dic,true);
    }
    public static <T> String mapping(CharSequence source, Map<String,T> dic,boolean ispaading) {

        if (Strings.isBlank(source)) return "";
        String oldStr = source.toString();
        oldStr = mapping(oldStr, ispaading);
        if (dic == null)
        {
            if (!ispaading) return oldStr;
            dic = new HashMap<String, T>();
        }
        //以对象型替换  @@d.id@@
        Matcher match = Regex.match(oldStr, "@@(\\w*)\\.(\\w*)@@");
        while (match.find()){
            String key1 = match.group(1);
            String key2 = match.group(2);
            T v = dic.get(key1);
            if (v == null) continue;//字典是否含有键值
            //转化成Record对象
            Record record = null;
            if (v instanceof AjaxResult){
                AjaxResult result = (AjaxResult)v;
                if (result.success) record = Record.parse(result.data);
            }
            else record = Record.parse(v);
            if (record == null) continue;
            String k = record.haveKey(key2);
            if (Strings.isBlank(k)) continue;
            String value = record.getString(k, true);
            if(value==null)value="";
            if (ispaading || Strings.hasValue(value)) oldStr = oldStr.replace("@@" + key1 + "." + key2 + "@@", value);
        }
        //${name}
        match = Regex.match(oldStr, "\\$\\{(\\w*)}");
        while (match.find())
        {
            String keyword = match.group(1);
            String value = Collections.getString(dic,keyword,true);
            if(value==null)value="";
            if (ispaading || Strings.hasValue(value))oldStr = oldStr.replace("${" + keyword + "}", value);
        }
        //以对象型替换 形式：${dd.name}
        match = Regex.match(oldStr, "\\$\\{(\\$?\\w*)\\.(\\w*)\\}");
        while (match.find()) {
            String key1 = match.group(1);
            String key2 = match.group(2);
            T v = dic.get(key1);
            if (v == null) continue;//字典是否含有键值
            //转化成Record对象
            Record record = null;
            if (v instanceof AjaxResult){
                AjaxResult result = (AjaxResult)v;
                if (result.success)
                    record = Record.parse(result.data);
            }else{
                record = Record.parse(v);
            }
            if (record == null) continue;
            String k = record.haveKey(key2);
            if (Strings.isBlank(k)) continue;
            String value = record.getString(k, true);
            if(value==null)value="";
            if (ispaading || Strings.hasValue(value)) oldStr = oldStr.replace("${" + key1 + "." + key2 + "}", value);
        }

        //${$data#&tablename#&columns#&condition#&param}模版数据
        match = Regex.match(oldStr, "\\$\\{\\$data#&([\\w\\*\\.]+?)#&([\\w]+?)#&([\\w\\W]+?)(#&([\\w\\W]*?))?#}");
        while (match.find())
        {
            String tableName = match.group(1);
            String columns = match.group(2);
            String condition = match.group(3);
            String param = match.group(5);
            AjaxResult re = QueryAction.getValue(tableName, columns, condition, param, match.group());
            if (re.success && re.data != null) oldStr = oldStr.replace(match.group(), re.data.toString());
            else
            {
                if (ispaading) oldStr = oldStr.replace(match.group(), "");
            }
        }
        //${$proxy#&servicename#&data}代理服务数据
        match = Regex.match(oldStr, "\\$\\{\\$proxy#&([\\w\\*\\.]+?)#&([\\w\\W]+?)#}");
        while (match.find()){
            String key = match.group(1);
            String data = match.group(2);
//            var re = ProxyService.execute(key, Record.Parse(data));
//            if (re.Success) oldStr = oldStr.Replace(elmatch.Groups[0].Value, re.data.ToString());
//            else
//            {
//                if (ispaading) oldStr = oldStr.Replace(elmatch.Groups[0].Value, "");
//            }
        }

        return oldStr;
    }
    /***
     * 判断是否为Json文本
     * @param str
     * @return
     */
    public static boolean isJson(String str){
        return isJson(str,0);
    }
    /***
     * 判断是否为Json文本
     * @param str
     * @param jsonType json的数据类型,0是数组或对象,1,对象,2，数组
     * @return
     */
    public static boolean isJson(String str, int jsonType) {
        if (isBlank(str)) return false;
        str = str.trim();
        if (jsonType == 0)
        {
            return (str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]"));
        }
        if (jsonType == 1)
        {
            return (str.startsWith("{") && str.endsWith("}"));
        }
        if (jsonType == 2)
        {
            return (str.startsWith("[") && str.endsWith("]"));
        }
        return false;
    }


    /***
     * 是否是手机号码
     * @param str 验证文本
     * @return
     */
    public static boolean isMobile(String str)
    {
        return !Strings.isBlank(str) && Regex.isMatch(str, "^[1][3,4,5,7,8][0-9]{9}$");
    }
    /// <summary>
    /// 是否是邮箱地址
    /// </summary>
    /// <param name="str">验证文本</param>
    /// <returns></returns>
    public static boolean isEMail(String str)
    {
        return !Strings.isBlank(str) && Regex.isMatch(str, "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    }

    /// <summary>
    /// 是否是中文字符
    /// </summary>
    /// <param name="str">验证文本</param>
    /// <returns></returns>
    public static boolean isChinese(String str)
    {
        return !Strings.isBlank(str) && Regex.isMatch(str, "^[\u4e00-\u9fa5]*$");
    }
    //region 文本的正则判断

    /// <summary>
    /// 是否是数值类型
    /// </summary>
    /// <param name="str">验证文本</param>
    /// <returns></returns>
    public static boolean isWord(String str)
    {
        return !Strings.isBlank(str) && Regex.isMatch(str, "^[\\da-z]+$");
    }



    /// <summary>
    /// 是否是英文字符
    /// </summary>
    /// <param name="str">验证文本</param>
    /// <returns></returns>
    public static boolean IsEnglish(String str)
    {
        return !Strings.isBlank(str) && Regex.isMatch(str, "^[a-zA-Z]+$");
    }
    /// <summary>
    /// 是否是日期
    /// </summary>
    /// <param name="str">验证文本</param>
    /// <returns></returns>
    public static boolean isDate(String str)
    {
        return !Strings.isBlank(str) && Regex.isMatch(str, "^(\\d{4})\\-(\\d{2})\\-(\\d{2})$");
    }
    /// <summary>
    /// 是否是日期时间
    /// </summary>
    /// <param name="str">验证文本</param>
    /// <returns></returns>
    public static boolean isDateTime(String str)
    {
        return !Strings.isBlank(str) && Regex.isMatch(str, "^(\\d{4})\\-(\\d{2})\\-(\\d{2})\\s(\\d{2}):(\\d{2}):(\\d{2})$");
    }
    /// <summary>
    /// 是否是电话号码
    /// </summary>
    /// <param name="str">验证文本</param>
    /// <returns></returns>
    public static boolean isTel(String str)
    {
        return !Strings.isBlank(str) && Regex.isMatch(str, "^((0\\d{2,3})-)(\\d{7,8})(-(\\d{3,}))?$");
    }
    /// <summary>
    /// 是否是联系方式
    /// </summary>
    /// <param name="str">验证文本</param>
    /// <returns></returns>
    public static boolean isPhone(String str)
    {
        return !Strings.isBlank(str) && (Strings.isMobile(str) || Strings.isTel(str));
    }
    /// <summary>
    /// 是否是小数
    /// </summary>
    /// <param name="str">验证文本</param>
    /// <returns></returns>
    public static boolean isDecimal(String str)
    {
        return !Strings.isBlank(str) && Regex.isMatch(str, "^-?[0-9]+([.]{1}[0-9]+){0,1}$");
    }
    /// <summary>
    /// 是否是英文字符
    /// </summary>
    /// <param name="str">验证文本</param>
    /// <returns></returns>
    public static boolean isEnglish(String str)
    {
        return !Strings.isBlank(str) && Regex.isMatch(str, "^[a-zA-Z]+$");
    }
    public static Receipt check(String str, String regText) {
        if (Regex.isMatch(regText, "(null)|(required)") && Strings.isBlank(str))
        {
            return new Receipt(false, "必填项不能为空");
        }
        if (Regex.isMatch(regText, "(mobile)") && Strings.hasValue(str) && !Strings.isMobile(str))
        {
            return new Receipt(false, "请填写正确手机号码");
        }
        if (Regex.isMatch(regText, "(email)") && Strings.hasValue(str) && !Strings.isEMail(str))
        {
            return new Receipt(false, "请填写正确E-Mail地址");
        }
        if (Regex.isMatch(regText, "(tel)") && Strings.hasValue(str) && !Strings.isTel(str))
        {
            return new Receipt(false, "请填写正确电话号码");
        }
        if (Regex.isMatch(regText, "(phone)") && Strings.hasValue(str) && !Strings.isPhone(str))
        {
            return new Receipt(false, "请填写正确联系方式");
        }
        if (Regex.isMatch(regText, "(number)") && Strings.hasValue(str) && !Strings.isNumber(str))
        {
            return new Receipt(false, "请填写正确的整数格式");
        }
        if (Regex.isMatch(regText, "(decimal)") && Strings.hasValue(str) && !Strings.isDecimal(str))
        {
            return new Receipt(false, "请填写正确的数字格式");
        }
        if (Regex.isMatch(regText, "(chinese)") && Strings.hasValue(str) && !Strings.isChinese(str))
        {
            return new Receipt(false, "请填写中文汉字");
        }
        if (Regex.isMatch(regText, "(noch)") && Strings.hasValue(str) && Strings.isChinese(str))
        {
            return new Receipt(false, "不能包含中文汉字");
        }
        if (Regex.isMatch(regText, "(chnpy)") && Strings.hasValue(str) && !(Strings.isChinese(str) || Strings.isEnglish(str)))
        {
            return new Receipt(false, "请填写中文或拼音");
        }
        if (Regex.isMatch(regText, "(date)") && Strings.hasValue(str) && !Strings.isDate(str))
        {
            return new Receipt(false, "请填写正确的时间格式");
        }
        //等于值
        if (Regex.isMatch(regText, "eq\\([\\w\\|]*?\\)"))
        {
            if (Strings.isBlank(str)) return new Receipt(false, "这是必填项");

            Matcher match = Regex.match(regText, "eq\\(([\\w\\|]*?)\\)");
            
            if (match.find() && Strings.hasValue(match.group(1)))
            {
                String[] split = match.group(1).toString().split("|");
                boolean eq = Collections.any(split, x -> x == str);
                if (!eq) return new Receipt(false, "填写值不符合数据标准");
            }
        }
        //大于等于
        if (Regex.isMatch(regText, "gte?\\(-?\\d*\\.?\\d*\\)"))
        {
            if (Strings.isBlank(str)) return new Receipt(false, "这是必填项");
            if (!Strings.isDecimal(str)) return new Receipt(false, "请填写正确的数值格式");
            Matcher match = Regex.match(regText, "(gte?)\\((-?\\d*\\.?\\d*)\\)");
            if (match.find() && Strings.hasValue(match.group(2)))
            {
                if (match.group(1).toLowerCase() == "gte")
                {
                    if (Double.parseDouble(str) < Double.parseDouble(match.group(2)))
                    {
                        return new Receipt(false, "填写值必须大于等于" + match.group(2));
                    }
                }
                else
                {
                    if (Double.parseDouble(str) <= Double.parseDouble(match.group(2)))
                        return new Receipt(false, "填写值必须大于" + match.group(2));
                }
            }
        }
        //小于等于
        if (Regex.isMatch(regText, "lte?\\(-?\\d*\\.?\\d*\\)"))
        {
            if (Strings.isBlank(str)) return new Receipt(false, "这是必填项");
            if (!Strings.isDecimal(str)) return new Receipt(false, "请填写正确的数值格式");
            Matcher match = Regex.match(regText, "(lte?)\\((-?\\d*\\.?\\d*)\\)");
            if (match.find() && Strings.hasValue(match.group(2)))
            {
                if (match.group(1).toLowerCase() == "lte")
                {
                    if (Double.parseDouble(str) > Double.parseDouble(match.group(2)))
                    {
                        return new Receipt(false, "填写值必须小于等于" + match.group(2));
                    }
                }
                else
                {
                    if (Double.parseDouble(str) >= Double.parseDouble(match.group(2)))
                        return new Receipt(false, "填写值必须小于" + match.group(2));
                }
            }
        }
        if (Regex.isMatch(regText, "not\\([\\w\\-\\.]*\\)") && Strings.hasValue(str))
        {
            Matcher match = Regex.match(regText, "not\\(([\\w\\-\\.]*)\\)");
            if (match.find() && Strings.hasValue(match.group(1)))
            {
                if (str == match.group(1))
                    return new Receipt(false, "填写值不能等于" + match.group(1));
            }
        }
        if (Regex.isMatch(regText, "len\\(?(\\d+)(-\\d*)?\\)?") && Strings.hasValue(str))
        {
            if (Strings.isBlank(str)) return new Receipt(false, "值不能为空");
            Matcher match = Regex.match(regText, "len\\(?(\\d+)(-(\\d*))?\\)?");
            if (match.find())
            {
                if (Integer.parseInt(match.group(1)) > str.length())
                    return new Receipt(false, "填写内容的长度不能小于" + match.group(1));
                if (match.groupCount() > 3 && Strings.hasValue(match.group(3)) && Integer.parseInt(match.group(3)) < str.length())
                    return new Receipt(false, "填写内容的长度不能大于" + match.group(3));

            }
        }

        return new Receipt(true);


    }
}
