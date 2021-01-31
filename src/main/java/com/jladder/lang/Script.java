package com.jladder.lang;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Script {



    public static <T> T eval(String express,Class<T> glass){
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        try{

            Object ret = engine.eval(express);
            return Convert.convert(glass,ret);
            // engine.eval("alert(\"js alert\");");    // 不能调用浏览器中定义的js函数 // 错误，会抛出alert引用不存在的异常
        }catch(ScriptException e){

            e.printStackTrace();
        }
        return null;
    }
}
