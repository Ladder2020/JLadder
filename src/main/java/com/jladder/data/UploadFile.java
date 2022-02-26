package com.jladder.data;

import com.jladder.lang.Core;
import com.jladder.lang.Files;
import com.jladder.lang.Security;

import java.io.IOException;
import java.nio.file.Paths;

/// <summary>
/// 上传文件结构
/// </summary>
public class UploadFile
{
    /// <summary>
    /// 文件名
    /// </summary>
    private String filename;
    /// <summary>
    /// 表单名
    /// </summary>
    private String formname;
    /// <summary>
    /// 数据体
    /// </summary>
    private byte[] data;
    /// <summary>
    /// 格式
    /// </summary>
    private String format;
    /// <summary>
    /// 文件前二位代码
    /// </summary>
    private String filecode;
    /// <summary>
    /// md5值
    /// </summary>
    private String md5;

    /// <summary>
    /// 文件长度
    /// </summary>
    private long length;

    /// <summary>
    ///
    /// </summary>
    public UploadFile()
    {
    }

    /// <summary>
    /// 初始化
    /// </summary>
    /// <param name="filename">文件名</param>
    /// <param name="data">数据</param>
    /// <param name="formname">表单名称</param>
    public UploadFile(String filename, byte[] data,String formname)
    {
        this.filename = filename;
        this.formname = formname;
        this.data = data;
        this.format = Files.getExt(filename);
        if (data.length > 3)
        {
            this.filecode = data[0]+ "," + data[1]+ "," + data[2];
            this.md5 = Security.md5(this.data);
        }

    }
    /// <summary>
    /// 设置上传数据
    /// </summary>
    /// <param name="data">数据</param>
    /// <returns></returns>
    public UploadFile SetData(byte[] data)
    {
        this.data = data;
        if (data!=null && data.length > 3)
        {
            this.filecode = data[0]+ "," + data[1]+ "," + data[2];
            this.md5 = Security.md5(this.data);
        }
        else
        {
            this.filecode = "";
            this.md5  = "";
        }
        return this;
    }
    /// <summary>
    /// 设置长度
    /// </summary>
    /// <param name="length">长度</param>
    /// <returns></returns>
    public UploadFile SetLength(long length)
    {
        this.length = length;
        return this;
    }
    /// <summary>
    /// 另存
    /// </summary>
    /// <returns></returns>
    public boolean SaveAs(String filename)
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

    public String getFilename() {
        return filename;
    }

    public String getFormname() {
        return formname;
    }

    public String getMd5() {
        return md5;
    }

    public String getFilecode() {
        return filecode;
    }

    public String getFormat() {
        return format;
    }

    public long getLength() {
        return length;
    }
}