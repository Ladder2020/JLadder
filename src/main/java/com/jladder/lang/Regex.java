package com.jladder.lang;

import com.jladder.db.annotation.Table;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Table("")
public class Regex {


    public static String[] split(String source,String reg){
        if(Strings.isBlank(source))return null;
        return source.split(reg);
    }


    public static boolean isMatch(String value,String regex) {
        return getPattern(regex).matcher(value).find();
    }
    public static String replace(String old,String regex){
        return old.replaceAll(regex,"");
    }
    public static String replace(String old,String regex,String newstr){


        return old.replaceAll(regex,newstr);
         //old;
    }

    public static Pattern getPattern(String regex) {
        return Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
//        Pattern pattern = cache.get(regex);
//        if (pattern == null) {
//            pattern = Pattern.compile(regex);
//            cache.put(regex, pattern);
//        }
//        return pattern;
    }

    public static Matcher match(String str, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }
}
