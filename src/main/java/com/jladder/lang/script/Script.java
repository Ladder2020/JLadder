package com.jladder.lang.script;

import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.lang.Convert;
import com.jladder.lang.Strings;
import javax.script.*;
import java.util.List;
import java.util.Map;

public class Script {

    /**
     * 执行js表达式,未注入Ladder库
     * @param express 表达式代码
     * @param glass 泛型类
     * @param <T> 泛型
     * @return
     */
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

    /**
     * 执行脚本,注入Ladder库
     * @param express
     * @return
     */
    public static Object execute(String express){
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        try{
            engine.put("Ladder", new ScriptLadderFuntion());
            Object ret = engine.eval(express);
            return ret;
        }catch(ScriptException e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 执行脚本,注入Ladder库
     * @param express js脚本
     * @param param 作用域变量
     * @return
     */
    public static Object execute(String express,Record param){
        try{
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
            Compilable compilable = (Compilable) engine;
            Bindings bindings = engine.createBindings(); //Local级别的Binding
            CompiledScript JSFunction = compilable.compile(express); //解析编译脚本函数
            for(Map.Entry<String,Object> entry:param.entrySet()){
                bindings.put(entry.getKey(),entry.getValue());
            }
            Object result=JSFunction.eval(bindings);
            return result;
        }catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 执行脚本,注入Ladder库
     * @param code 代码内容
     * @param fun 调用函数名
     * @param arg1 参数1
     * @param arg2 参数2
     * @param arg3 参数3
     * @return
     */
    public static Receipt<Object> invoke(String code,String fun,Object arg1,Object arg2,Object arg3){
        Object[] args = {arg1,arg2,arg3};
        return invoke(code,fun,args);
    }
    /**
     * 执行脚本,注入Ladder库
     * @param code 代码内容
     * @param fun 调用函数名
     * @param arg1 参数1
     * @param arg2 参数2
     * @return
     */
    public static Receipt<Object> invoke(String code,String fun,Object arg1,Object arg2){
        Object[] args = {arg1,arg2};
        return invoke(code,fun,args);
    }
    /**
     * 执行脚本,注入Ladder库
     * @param code 代码内容
     * @param fun 调用函数名
     * @param arg1 参数1
     * @return
     */
    public static Receipt<Object> invoke(String code,String fun,Object arg1){
        Object[] args = {arg1};
        return invoke(code,fun,args);
    }
    /**
     * 执行脚本,注入Ladder库
     * @param code 代码内容
     * @param fun 调用函数名
     * @param args 参数列表
     * @return
     */
    public static Receipt<Object> invoke(String code, String fun, Object[] args){
        try{
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("javascript");
            engine.put("Ladder", new ScriptLadderFuntion());
            Object rett = engine.eval(code);
            if(Strings.isBlank(fun))return new Receipt<Object>(false,"函数未指定");
            Invocable jsInvoke = (Invocable) engine;
            Object ret=null;
            if(args==null){
                ret = jsInvoke.invokeFunction(fun);
                return new Receipt<Object>().setData(ret);
            }
            switch (args.length){
                case 0:
                    ret=jsInvoke.invokeFunction(fun);
                    break;
                case 1:
                    ret=jsInvoke.invokeFunction(fun,args[0]);
                    break;
                case 2:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1]);
                    break;
                case 3:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1],args[2]);
                    break;
                case 4:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1],args[2],args[3]);
                    break;
                case 5:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1],args[2],args[3],args[4]);
                    break;
                case 6:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1],args[2],args[3],args[4],args[5]);
                    break;
                case 7:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1],args[2],args[3],args[4],args[5],args[6]);
                    break;
                case 8:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7]);
                    break;
            }
            return new Receipt<Object>().setData(ret);
        }catch (Exception e) {
            e.printStackTrace();
            return new Receipt<Object>(false,e.getMessage());
        }
    }

    /**
     * 执行脚本库，并执行main方法,注入Ladder库
     * @param code 代码内容
     * @param args 参数列表
     * @return
     */
    public static Receipt<Object> main(String code, Object... args){
        if(args!=null&args.length==1){
            Object arg = args[0];
            if(arg instanceof List)return invoke(code,"main",((List)arg).toArray());
            if(arg.getClass().isArray())return invoke(code,"main",((Object[])arg));
            return invoke(code,"main",args);
        }else{
            return invoke(code,"main",args);
        }

    }
    /**
     * 执行脚本库，并执行main方法,注入Ladder库
     * @param code 代码内容
     * @return
     */
    public static Receipt<Object> main(String code){
        return invoke(code,"main",null);
    }

    /**
     * JS引擎
     */
    private ScriptEngine engine=new ScriptEngineManager().getEngineByName("JavaScript");

    private String error=null;
    /**
     * 初始化
     */
    public Script(){
        engine.put("Ladder", new ScriptLadderFuntion());
    }
    /**
     * 初始化
     * @param code 脚本代码
     */
    public Script(String code){
        try {
            engine.put("Ladder", new ScriptLadderFuntion());
            engine.eval(code);
        } catch (ScriptException e) {
            e.printStackTrace();
            error=e.getMessage();
        }
    }
    public String getError(){
        return error;
    }
    /**
     * 设置JS代码
     * @param code
     */
    public void setCode(String code){
        try {
            engine.eval(code);
        } catch (ScriptException e) {
            e.printStackTrace();
            error=e.getMessage();
        }
    }

    /**
     * 调用Main方法
     * @return
     */
    public Receipt<Object> main(){
        Invocable jsInvoke = (Invocable) engine;
        try {
            Object ret = jsInvoke.invokeFunction("main");
            return new Receipt<Object>().setData(ret);
        } catch (Exception e) {
            e.printStackTrace();
            return new Receipt<Object>(false,e.getMessage());
        }
    }

    /**
     * 调用Main方法
     * @param args 不定参数
     * @return
     */
    public Receipt<Object> main(Object... args){
        Invocable jsInvoke = (Invocable) engine;
        try {
            Object ret=null;
            if(args==null){
                ret =  jsInvoke.invokeFunction("main");
                return new Receipt<Object>().setData(ret);
            }
            switch (args.length){
                case 0:
                    ret=jsInvoke.invokeFunction("main");
                    break;
                case 1:
                    ret=jsInvoke.invokeFunction("main",args[0]);
                    break;
                case 2:
                    ret=jsInvoke.invokeFunction("main",args[0],args[1]);
                    break;
                case 3:
                    ret=jsInvoke.invokeFunction("main",args[0],args[1],args[2]);
                    break;
                case 4:
                    ret=jsInvoke.invokeFunction("main",args[0],args[1],args[2],args[3]);
                    break;
                case 5:
                    ret=jsInvoke.invokeFunction("main",args[0],args[1],args[2],args[3],args[4]);
                    break;
                case 6:
                    ret=jsInvoke.invokeFunction("main",args[0],args[1],args[2],args[3],args[4],args[5]);
                    break;
                case 7:
                    ret=jsInvoke.invokeFunction("main",args[0],args[1],args[2],args[3],args[4],args[5],args[6]);
                    break;
                case 8:
                    ret=jsInvoke.invokeFunction("main",args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7]);
                    break;
            }
            return new Receipt<Object>().setData(ret);
        } catch (Exception e) {
            e.printStackTrace();
            return new Receipt<Object>(false,e.getMessage());
        }
    }

    /**
     * 调用函数
     * @param fun 函数名
     * @param args 参数列表
     * @return
     */
    public Receipt<Object> invoke(String fun,Object[] args){
        try{
            if(Strings.isBlank(fun))return new Receipt<Object>(false,"函数未指定");
            Invocable jsInvoke = (Invocable) engine;
            Object ret=null;
            if(args==null){
                ret=jsInvoke.invokeFunction(fun);
                return new Receipt<Object>().setData(ret);
            }
            switch (args.length){
                case 0:
                    ret=jsInvoke.invokeFunction(fun);
                    break;
                case 1:
                    ret=jsInvoke.invokeFunction(fun,args[0]);
                    break;
                case 2:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1]);
                    break;
                case 3:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1],args[2]);
                    break;
                case 4:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1],args[2],args[3]);
                    break;
                case 5:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1],args[2],args[3],args[4]);
                    break;
                case 6:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1],args[2],args[3],args[4],args[5]);
                    break;
                case 7:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1],args[2],args[3],args[4],args[5],args[6]);
                    break;
                case 8:
                    ret=jsInvoke.invokeFunction(fun,args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7]);
                    break;
            }
            return new Receipt<Object>().setData(ret);
        }catch (Exception e) {
            e.printStackTrace();
            return new Receipt<Object>(false,e.getMessage());
        }
    }

    /**
     * 调用函数
     * @param fun 函数名
     * @param arg1 参数1
     * @param arg2 参数2
     * @param arg3 参数3
     * @param arg4 参数4
     * @param arg5 参数5
     * @return
     */
    public Receipt<Object> invoke(String fun,Object arg1,Object arg2,Object arg3,String arg4,String arg5){
        Object[] args = {arg1,arg2,arg3,arg4,arg5};
        return invoke(fun,args);
    }
    /**
     * 调用函数
     * @param fun 函数名
     * @param arg1 参数1
     * @param arg2 参数2
     * @param arg3 参数3
     * @param arg4 参数4
     * @return
     */
    public Receipt<Object> invoke(String fun,Object arg1,Object arg2,Object arg3,String arg4){
        Object[] args = {arg1,arg2,arg3,arg4};
        return invoke(fun,args);
    }
    /**
     * 调用函数
     * @param fun 函数名
     * @param arg1 参数1
     * @param arg2 参数2
     * @param arg3 参数3
     * @return
     */
    public Receipt<Object> invoke(String fun,Object arg1,Object arg2,Object arg3){
        Object[] args = {arg1,arg2,arg3};
        return invoke(fun,args);
    }
    /**
     * 调用函数
     * @param fun 函数名
     * @param arg1 参数1
     * @param arg2 参数2
     * @return
     */
    public Receipt<Object> invoke(String fun,Object arg1,Object arg2){
        Object[] args = {arg1,arg2};
        return invoke(fun,args);
    }
    /**
     * 调用函数
     * @param fun 函数名
     * @param arg1 参数1
     * @return
     */
    public Receipt<Object> invoke(String fun,Object arg1){
        Object[] args = {arg1};
        return invoke(fun,args);
    }
    /**
     * 调用函数
     * @param fun 函数名
     * @return
     */
    public Receipt<Object> invoke(String fun){
        Object[] args=new Object[0];
        return invoke(fun,args);
    }
}
