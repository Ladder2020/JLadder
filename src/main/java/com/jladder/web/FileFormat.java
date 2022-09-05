package com.jladder.web;

public enum FileFormat {

    /// <summary>
    /// 未有文件
    /// </summary>
    None,
    /// <summary>
    /// 转为二进制
    /// </summary>
    Binary,
    /// <summary>
    /// 转为PostFile
    /// </summary>
    HttpPostFile,
    /// <summary>
    /// 转为流文件
    /// </summary>
    Stream,

    /// <summary>
    /// 自行组装的上传文件结构
    /// </summary>
    UploadFile


}
