package com.jladder.convert;

import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.lang.Times;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ConverterRegistry {

    private static final ConverterRegistry instance=new ConverterRegistry();

    final Map<Type,IConvert> registry= new HashMap();
    public static ConverterRegistry getInstance() {
        return instance;
    }

    public ConverterRegistry(){
        defaultConverter();
    }

    public <T> T convert(Type type, Object value, T defaultValue) {
        if(registry.containsKey(type)){
            return (T)registry.get(type).convert(value,defaultValue);
        }else{
            return null;
        }
    }
    public void defaultConverter(){

        registry.put(String.class, new IConvert<String>() {
            @Override
            public String convert(Object value, String defaultValue) {
                if(value==null)return defaultValue;
                String ret = value.toString();
                if("".equals(ret))return "";
                if(ret==null)return defaultValue;
                return ret;
            }
        });
        registry.put(int.class, new IConvert<Integer>() {
            @Override
            public Integer convert(Object value, Integer defaultValue) {
                if(value==null)return defaultValue==null?0:defaultValue;
                String ret = value.toString();
                return Strings.toInt(ret);
            }
        });
        IConvert long_convert=new IConvert<Long>() {
            @Override
            public Long convert(Object value, Long defaultValue) {
                if(value==null)return defaultValue==null?0L:defaultValue;
                String ret = value.toString();
                if(Strings.isNumber(ret)){
                    return Long.parseLong(ret);
                }else{
                    return 0L;
                }
            }
        };

        registry.put(long.class,long_convert);
        registry.put(Long.class,long_convert);
        registry.put(Integer.class, new IConvert<Integer>() {
            @Override
            public Integer convert(Object value, Integer defaultValue) {
                if(value==null)return defaultValue==null?0:defaultValue;
                String ret = value.toString();
                return Strings.toInt(ret);
            }
        });
        registry.put(Boolean.class, new IConvert<Boolean>() {
            @Override
            public Boolean convert(Object value, Boolean defaultValue) {
                if(value==null){
                    if(defaultValue==null)return false;
                    return defaultValue;
                }
                String v = value.toString().trim();
                if(Strings.isBlank(v))return false;
                if(Strings.isNumber(v)){
                    double d = Double.parseDouble(v);
                    return d!=0;
                }
                if(Regex.isMatch(v,"(false)|(False)"))return false;
                return true;
            }
        });
        registry.put(Double.class, new IConvert<Double>() {
            @Override
            public Double convert(Object value, Double defaultValue) {
                if(value==null){
                    if(defaultValue==null)return new Double(0);
                    return defaultValue;
                }
                String v = value.toString().trim();
                if(Strings.isBlank(v))return new Double(0);
                if(Strings.isNumber(v)){
                    double d = Double.parseDouble(v);
                    return d;
                }
                if(Regex.isMatch(v,"(true)|(True)"))return new Double(1);
                return new Double(1);
            }
        });
        registry.put(Float.class, new IConvert<Float>() {
            @Override
            public Float convert(Object value, Float defaultValue) {

                if(value==null){
                    if(defaultValue==null)return new Float(0);
                    return defaultValue;
                }
                if(value instanceof Float){
                    if(value.equals(0)&&defaultValue!=null)return defaultValue;
                    return (Float)value;
                }
                String v = value.toString().trim();
                if(Strings.isBlank(v))return new Float(0);
                if(Strings.isNumber(v)){
                    Float d = Float.parseFloat(v);
                    return d;
                }
                if(Regex.isMatch(v,"(true)|(True)"))return new Float(1);
                return new Float(1);
            }
        });
        registry.put(Date.class, new IConvert<Date>() {
            @Override
            public Date convert(Object value, Date defaultValue) {
                if(value==null){
                    if(defaultValue==null)return null;
                    return defaultValue;
                }
                if(value instanceof Date){
                    return (Date)value;
                }
                String v = value.toString().trim();
                if(Strings.isBlank(v))return null;
                if(Strings.isNumber(v)){
                    return Times.D(Integer.parseInt(v));
                }
                return  Times.convert(v);
            }
        });
    }
}
