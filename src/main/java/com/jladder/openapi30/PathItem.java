package com.jladder.openapi30;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jladder.data.Record;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PathItem {
    private String summary;
    private String description;
    private Operation get;
    private Operation post;

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

    public Operation getGet() {
        return get;
    }

    public void setGet(Operation get) {
        this.get = get;
    }

    public Operation getPost() {
        return post;
    }

    public void setPost(Operation post) {
        this.post = post;
    }


}
