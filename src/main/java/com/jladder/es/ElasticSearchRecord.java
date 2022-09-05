package com.jladder.es;

import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElasticSearchRecord {
    private String index;
    private Map<String,Object> data;
    private Map<String, HighlightField> highlight;
    private List<String> keywords = new ArrayList<String>();
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
                        ret.keywords.add(fragment.string());
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


    public ElasticSearchRecord addKeyWord(String word) {
        this.keywords.add(word);
        return this;
    }
    public ElasticSearchRecord setKeysWords(List<String> words) {
        this.keywords=words;
        return this;
    }
    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
