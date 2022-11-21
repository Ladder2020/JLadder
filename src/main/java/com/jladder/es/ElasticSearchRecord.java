package com.jladder.es;

import com.jladder.lang.Strings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ElasticSearchRecord {
    private String index;
    private Map<String,Object> data;
    private Map<String, HighlightField> highlight;
    private Map<String,List<String>> keywords = new LinkedHashMap<String,List<String>>();
    public ElasticSearchRecord(String index, Map<String, Object> data) {
        this.index = index;
        this.data = data;
    }
    public static ElasticSearchRecord from(SearchHit hit){
        if(hit==null)return null;
        ElasticSearchRecord ret = new ElasticSearchRecord(hit.getIndex(),hit.getSourceAsMap());
        ret.highlight = hit.getHighlightFields();
        if(ret.highlight!=null){
            ret.highlight.forEach((k,v)->{
                if(v!=null){
                    for (Text fragment : v.getFragments()) {
                        String word = fragment.string();
                        if(ret.keywords.containsKey(k)){
                            List<String> old = ret.keywords.get(k);
                            old.add(word);
                        }
                        else{
                            ret.keywords.put(k,new ArrayList<String>(){
                                {add(word);}
                            });
                        }

                    }
                }
            });
        }
        return ret;
    }

    public String getIndex() {
        return index;
    }

    public ElasticSearchRecord setIndex(String index) {
        this.index = index;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public ElasticSearchRecord setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }


    public ElasticSearchRecord addKeyWord(String key,String word) {
        if(Strings.isBlank(key))key="_default_";
        List<String> old = this.keywords.get(key);
        if(old!=null)old.add(word);
        else {
            this.keywords.put(key,new ArrayList<String>(){
                {
                    add(word);
                }
            });
        }
        return this;
    }
    public ElasticSearchRecord addKeysWords(String key,List<String> words) {
        this.keywords.put(key,words);
        return this;
    }
    public Map<String,List<String>> getKeywords() {
        return keywords;
    }

    public void setKeywords(Map<String,List<String>> keywords) {
        this.keywords = keywords;
    }


}
