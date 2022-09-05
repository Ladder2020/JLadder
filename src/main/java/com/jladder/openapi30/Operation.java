package com.jladder.openapi30;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.JsonObject;
import com.jladder.data.Record;
import com.jladder.lang.Core;
import com.jladder.lang.Json;
import com.jladder.proxy.ProxyMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Operation {
    private List<String> tags;
    private String summary="";
    private String description="";
    private String operationId= Core.genNuid();
    private List<Parameter> parameters;
    private RequestBody requestBody;
    private Record responses=new Record("200",new Record("description","Success"));
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setTag(String tag){
        if(tags==null)tags=new ArrayList();
        tags.add(tag);
    }
    public void setParametersByGet(List<ProxyMapping> params){
        if(params==null)return;
        if(parameters==null)parameters=new ArrayList();
        params.forEach(x->{
            parameters.add(new Parameter(x));
        });
    }
    public void setParametersByPost(List<ProxyMapping> params){
        requestBody = new RequestBody(params);
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
    public Record getResponses() {
        return responses;
    }
    public void setResponses(Record responses) {
        this.responses = responses;
    }
}
