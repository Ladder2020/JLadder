package com.jladder.data;

public class MappingInfo {
    private String sourcepath;
    private String destpath;
    private String title;
    private String ext;
    private int enable;
    private int ignore;
    private String value;
    private String valid;
    private String cmdtype;
    private String cmdtext;
    private String descr;

    public String getSourcepath() {
        return sourcepath;
    }

    public void setSourcepath(String sourcepath) {
        this.sourcepath = sourcepath;
    }

    public String getDestpath() {
        return destpath;
    }

    public void setDestpath(String destpath) {
        this.destpath = destpath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValid() {
        return valid;
    }

    public void setValid(String valid) {
        this.valid = valid;
    }

    public String getCmdtype() {
        return cmdtype;
    }

    public void setCmdtype(String cmdtype) {
        this.cmdtype = cmdtype;
    }

    public String getCmdtext() {
        return cmdtext;
    }

    public void setCmdtext(String cmdtext) {
        this.cmdtext = cmdtext;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }


    public int getIgnore() {
        return ignore;
    }

    public void setIgnore(int ignore) {
        this.ignore = ignore;
    }
}
