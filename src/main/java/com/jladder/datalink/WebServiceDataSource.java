package com.jladder.datalink;

import com.jladder.data.Record;

public class WebServiceDataSource {
    public String calltype;
    public String url;
    public Record header;
    public String methodname;
    public Record data;
    public String org;

    public String getCalltype() {
        return calltype;
    }

    public void setCalltype(String calltype) {
        this.calltype = calltype;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Record getHeader() {
        return header;
    }

    public void setHeader(Record header) {
        this.header = header;
    }

    public String getMethodname() {
        return methodname;
    }

    public void setMethodname(String methodname) {
        this.methodname = methodname;
    }

    public Record getData() {
        return data;
    }

    public void setData(Record data) {
        this.data = data;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String template;
}
