package com.jladder.datalink;

public class FileDataSource {

    /// <summary>
    /// 工作薄
    /// </summary>
    private String sheet;
    /// <summary>
    /// 文件版本类型
    /// </summary>
    private String filetype;
    /// <summary>
    /// 文件地址
    /// </summary>
    private String file;
    /// <summary>
    /// 分割符号
    /// </summary>
    private String split;
    /// <summary>
    /// 源数据
    /// </summary>
    private String data;
    public String getSheet() {
        return sheet;
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getSplit() {
        return split;
    }

    public void setSplit(String split) {
        this.split = split;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
