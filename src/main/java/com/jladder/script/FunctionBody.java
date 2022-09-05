package com.jladder.script;

import com.jladder.lang.Collections;

import java.io.Serializable;
import java.util.List;

/**
 * 函数体
 */
public class FunctionBody implements Serializable {

    public String name;
    public String project;
    public String title;
    public String type;
    public String path;
    public String functionname;
    public String code;
    public String descr;
    public String writer;
    public String id;
    private Script script;
    private List<FunctionParam> params;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFunctionname() {
        return functionname;
    }

    public void setFunctionname(String functionname) {
        this.functionname = functionname;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<FunctionParam> getParams() {
        return params;
    }

    public void setParams(List<FunctionParam> params) {
        this.params = null;
        if(params!=null){
            this.params = Collections.sort(params,"level",false);
        }
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
}
