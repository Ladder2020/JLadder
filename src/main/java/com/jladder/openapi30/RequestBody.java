package com.jladder.openapi30;

import com.google.gson.JsonObject;
import com.jladder.data.Record;
import com.jladder.proxy.ProxyMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestBody {
    private String description="请求数据";
    private Map<String,Object> content=new HashMap<String,Object>();
    private boolean required=false;
    public RequestBody(){
        //content.put("application/json",new Record("schema",new Record("type","object").put("properties",_properties)));
    }
    public RequestBody(List<ProxyMapping> params){
        Record properties=new Record();
        content.put("application/json",new Record("schema",new Record("type","object").put("properties",properties)));
        params.forEach(x->{
            Record p = new Record();
            p.put("type",x.datatype);
            p.put("example",x.dvalue);
            p.put("description",x.express);
//            p.put("required","required".equals(x.valid));
            properties.put(x.paramname,p);
        });
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String,Object> getContent() {
        return content;
    }

    public void setContent(Map<String,Object> content) {
        this.content = content;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }


}
