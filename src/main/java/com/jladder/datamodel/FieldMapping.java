package com.jladder.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FieldMapping {

    public Map<String, Map<String,Object>> fm = new HashMap<>();

   public FieldMapping(){}
   public FieldMapping(Map<String, Map<String,Object>> mapping){ this.fm = mapping;}

   public int size(){
       return fm.size();
   }

   public List<Map<String,Object>> where(Function<Map<String,Object>,Boolean> fun){
       List<Map<String,Object>> ret = new ArrayList<>();
       fm.forEach((x,y)->{
           if(fun.apply(y)){
               ret.add(y);
           }
       });
       return ret;
   }
   public boolean hasKey(String propname){
        return this.fm.containsKey(propname);
   }
   public Map<String,Object> get(String key){
       return this.fm.get(key);
   }
   public FieldMapping put(String name,Map<String,Object> config){
       this.fm.put(name,config);
       return this;
   }
}
