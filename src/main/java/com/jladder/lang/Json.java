package com.jladder.lang;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jladder.data.Record;

import java.io.File;

public class Json {
    private Json(){}
    private static final Gson GSON;
    private static final Gson GSON_SUPPORT_EXPOSE ;
    private static final Gson GSON_DISABLE_HTML;
    private static final Gson GSON_PRETTY_PRINT ;

    static {
        GsonBuilder GSON_BUILDER = new GsonBuilder();
        GSON = GSON_BUILDER.setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        GSON_SUPPORT_EXPOSE = GSON_BUILDER.excludeFieldsWithoutExposeAnnotation().create();
        GSON_DISABLE_HTML = GSON_BUILDER.disableHtmlEscaping().create();
        GSON_PRETTY_PRINT = GSON_BUILDER.setPrettyPrinting().create();
    }
    public static String toJson(Object obj){
        if(obj==null)return "";
        if(obj instanceof CharSequence)return obj.toString();
        Gson gson = GSON;
        String json = gson.toJson(obj);
        return json;
    }
    public static Object toObject(String json){
        if(Strings.isBlank(json))return null;
        Object jsonObject = JSONObject.parseObject(json,Object.class);
        return jsonObject;
    }
    public static <T> T toObject(String json,Class<T> clazz){
        if(Strings.isBlank(json))return null;
        try{
            T jsonObject = JSONObject.parseObject(json,clazz);
            return jsonObject;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }


    }
    public static <T> T toObject(CharSequence json, TypeReference<T> type){
        if(Strings.isBlank(json))return null;
        try{
            T jsonObject = JSONObject.parseObject(json.toString(),type.getType());
            return jsonObject;
        }catch (Exception e){
            throw e;
        }
    }

//    public static <T> T toObject(CharSequence json, Type type){
//        if(Strings.isBlank(json))return null;
//        T jsonObject = JSONObject.parseObject(json.toString(),type);
//        return jsonObject;
//    }
//    public static <T> T toObject(CharSequence json){
//        tt = new ObjectMapper().readValue(json, new TypeReference<List<Record>>(){})
//        if(Strings.isBlank(json))return null;
//        T jsonObject = JSONObject.parseObject(json.toString(),type);
//        return jsonObject;
//    }



    public static String FromFile(String path) {
        return removeComments(Files.read(path));
    }


    /// <summary>
    /// 快速把键值对象转换成json文本
    /// </summary>
    /// <param name="name"></param>
    /// <param name="value"></param>
    /// <returns></returns>
    public static String JsonText(String name, Object ... value)
    {
        String[] nameArray = name.split(",");
        int objSize = value.length;
        int count = Maths.min(nameArray.length, objSize);
        if (count < 1) return "";
        Record record=new Record();
        for (int i = 0; i < count; i++)
        {
            record.put(nameArray[i],value[i]);
        }
        return toJson(record);
    }

    public static String FromFile(File file) {
        return removeComments(Files.read(file));
    }


    public static String removeComments(String json){
        if(Strings.isBlank(json))return "";
        json = json.trim();
        String pattern = "^\\s*(var)\\s*\\w*\\s*=\\s*(\\{|\\[)";
        if (Regex.isMatch(json, pattern))
        {
            json = Regex.replace(json, "^\\s*(var)\\s*\\w*\\s*=", "");
        }
        json = json.trim();
//            content = Regex.Replace(content, "^\\s*", "");//删除前置空格
        json = Regex.replace(json, "\\s//.+\\r\\n", "\r\n");//删除行注释
        json = json.replace("\n", "");
        json = Regex.replace(json, "/\\*[\\s\\S]*?\\*/", "");//删除块注释
        return json.trim();
    }
}
