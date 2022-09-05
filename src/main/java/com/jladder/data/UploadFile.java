package com.jladder.data;

import com.jladder.lang.Core;
import com.jladder.lang.Files;
import com.jladder.lang.Security;
import com.jladder.lang.Strings;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * 上传文件结构
 */
public class UploadFile {
    /**
     * 文件名
     */
    private String filename;
    /**
     * 表单名
     */
    private String formname;
    /**
     * 数据体
     */
    private byte[] data;
    /**
     * 格式
     */
    private String format;
    /**
     * 文件前二位代码
     */
    private String filecode;
    /**
     * md5值
     */
    private String md5;

    /**
     * 文件长度
     */
    private long length;


    public UploadFile()
    {
    }
    /**
     * 初始化
     * @param filename 文件名
     * @param data 数据
     * @param formname 表单名称
     */
    public UploadFile(String filename, byte[] data,String formname)
    {
        this.filename = filename;
        this.formname = formname;
        this.data = data;
        this.format = Files.getExt(filename);
        if (data.length > 3){
            this.filecode = data[0]+ "," + data[1]+ "," + data[2];
            this.md5 = Security.md5(this.data);
        }
        if(data!=null)this.length=data.length;
    }
    /**
     * 设置上传数据
     * @param data 数据
     * @return
     */
    public UploadFile setData(byte[] data)
    {
        this.data = data;
        if (data!=null && data.length > 3){
            this.filecode = data[0]+ "," + data[1]+ "," + data[2];
            this.md5 = Security.md5(this.data);
        }
        else{
            this.filecode = "";
            this.md5  = "";
        }
        if(data!=null)this.length=data.length;
        return this;
    }
    /**
     * 设置长度
     * @param length 长度
     * @return
     */
    public UploadFile setLength(long length)
    {
        this.length = length;
        return this;
    }

    /**
     * 另存
     * @param filename 文件名称
     * @return
     */
    public boolean saveAs(String filename)
    {
        if (Core.isEmpty(this.data)) return false;
        try {
            java.nio.file.Files.write(Paths.get(filename),this.data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public byte[] getData() {
        return data;
    }

    public String getFileName() {
        return filename;
    }

    public String getFormName() {
        return formname;
    }

    public String getMd5() {
        return md5;
    }

    public String getFileCode() {
        return filecode;
    }

    public String getFormat() {
        if(Strings.isBlank(this.format)){
            this.format = Files.getExt(this.filename);
        }
        return Strings.isBlank(this.format)?"":this.format;
    }

    public long getLength() {
        return length;
    }
}