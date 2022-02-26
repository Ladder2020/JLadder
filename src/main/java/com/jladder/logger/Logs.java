package com.jladder.logger;

import com.jladder.Ladder;
import com.jladder.actions.impl.EnvAction;
import com.jladder.configs.Configure;
import com.jladder.data.Record;
import com.jladder.hub.DataHub;
import com.jladder.hub.WebHub;
import com.jladder.lang.*;
import com.jladder.net.http.HttpHelper;
import com.jladder.proxy.ProxyRunning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;

public class Logs {

    /**
     * 文件种子
     */
    public static String FileSeed;
    /**
     * logArray缓存数据
     */
    private static final ConcurrentLinkedQueue<LogStruct> logQueue = new ConcurrentLinkedQueue<>();
    /**
     * 信号灯变量
     */
    private static final AutoResetEvent resetEvent = new AutoResetEvent(false);
    static {
        Task.startNew(Logs::writeForLoop);
    }
    /**
     * 写出数据
     * @param text 日志文本
     */
    public static void write(String text){
        LogOption option = Ladder.Settings().getLogLevel();
        logQueue.add(new LogStruct(option + ":" + text + "\n", option));
        resetEvent.set();
    }


    /**
     * 写出数据
     * @param text 日志文本
     * @param option 选项
     */
    public static void write(String text, LogOption option){
        logQueue.add(new LogStruct(option + ":" + text + "\n", option));
        resetEvent.set();
    }
    private static void write(String text, String path){
        write(text,path, LogOption.Debug);
    }
    /**
     * 写出数据
     * @param text 日志文本
     * @param path 选项
     * @param option 文件路径,注意是全路径
     */
    public static void write(String text, String path, LogOption option){
        logQueue.add(new LogStruct(text, option).setFilename(path).setUser(EnvAction.getEnvValue("username")));
        resetEvent.set();
    }
    public static void write(Exception e){
        write(e,"Exception");
    }

    /**
     * 写出异常日志
     * @param e 异常对象
     * @param module 模块
     */
    public static void write(Exception e,String module)
    {
        if (e == null) return;
        logQueue.add(new LogStruct(new LogForError(e.getMessage()).setStacktrace(Arrays.toString(e.getStackTrace())).setModule(module), LogOption.Error).setUser(EnvAction.getEnvValue("username")));
        resetEvent.set();
    }
    public static void write(LogFoRequest request){
        write(request,LogOption.Request);
    }

    /**
     * 写出请求日志
     * @param request 请求对象
     * @param option 日志等级
     */
    public static void write(LogFoRequest request, LogOption option)
    {

        logQueue.add(new LogStruct(request, option).setUser(EnvAction.getEnvValue("username")));
        resetEvent.set();
    }
    public static void write(LogForSql sql){
        write(sql,LogOption.Sql);
    }
    public static void write(LogForError error){
        write(error,LogOption.Error);
    }
    public static void write(ProxyRunning running){
        write(running,LogOption.Proxy);
    }

    /**
     * 写出日志
     * @param log 数据对象
     * @param option 日志选项
     * @param <T> 泛型
     */
    public static <T> void write(T log, LogOption option){
        logQueue.add(new LogStruct(log, option).setUser(EnvAction.getEnvValue("username")));
        resetEvent.set();
    }
    /**
     * 循环写操作
     */
    private static void writeForLoop(){
        while (true) {
            try{
                resetEvent.waitOne();
                List<LogStruct> logs = new ArrayList<LogStruct>();
                while (logQueue.size() > 0) {
                    LogStruct logStruct = logQueue.poll();
                    if(logStruct==null)continue;
                    logs.add(logStruct);
                    StringBuilder builder = new StringBuilder();
                    if (logStruct.getOption().getIndex() < Ladder.Settings().getLogLevel().getIndex())continue;
                    String module="";
                    if (logStruct.getLog() instanceof String){
                        if(DataHub.LogWriter!=null && !DataHub.LogWriter.writeLog((String) logStruct.getLog())){
                            continue;
                        }
                        builder.append((String) logStruct.getLog());
                    }
                    else if (logStruct.getLog() instanceof LogFoRequest){
                        LogFoRequest requestLog = (LogFoRequest) logStruct.getLog();
                        if(DataHub.LogWriter!=null && !DataHub.LogWriter.writeRequest(requestLog)){
                            continue;
                        }
                        append(builder,requestLog.requestmark ,"请求标识");
                        append(builder,requestLog.url ,"请求地址");
                        append(builder,requestLog.method ,"请求方式");
                        append(builder,requestLog.referer ,"请求来源");
                        append(builder,Json.toJson(requestLog.request) ,"请求数据");
                        append(builder,requestLog.userinfo ,"用户信息");
                        module="request";
                    }
                    else if (logStruct.getLog() instanceof LogForSql){
                        module="sql";
                        LogForSql sqllog = (LogForSql) logStruct.getLog();
                        if(DataHub.LogWriter!=null && !DataHub.LogWriter.writeSql(sqllog)){
                            continue;
                        }
                        append(builder,sqllog.requestmark,"请求标识");
                        if (sqllog.isError){
                            append(builder,sqllog.conn,"连 接 器");
                            append(builder,sqllog.type,"处理类型");
                            append(builder,sqllog.tag,"模型标签");
                            append(builder,sqllog.sqltext,"执行语句");
                            append(builder,sqllog.data,"填充数据");
                            append(builder,sqllog.cause,"引发原因");
                            append(builder,sqllog.stacktrace,"调用堆栈");
                            append(builder,logStruct.getUser() ,"操作用户");
                            String errorText=builder.toString();
                            if (Strings.hasValue(logStruct.getUser())) errorText += "操作用户:" + logStruct.getUser() +System.lineSeparator();
                            errorText+="操作时间:" + Times.sDT(logStruct.getCreatetime()) + System.lineSeparator();
                            errorText+="--------------------------------------------------------------------------------------" +System.lineSeparator();
                            out(errorText, getDir() + "/sql_error_log.log");
                        }
                        else{
                            append(builder,sqllog.conn,"连 接 器");
                            append(builder,sqllog.type,"处理类型");
                            append(builder,sqllog.tag,"模型标签");
                            append(builder,sqllog.sqltext,"执行语句");
                            append(builder,sqllog.data,"填充数据");
                            append(builder,sqllog.duration+"ms","执行时长");
                            if (sqllog.isOverTime)
                            {
                                String warnText = builder.toString();
                                if (Strings.hasValue(logStruct.getUser())) warnText += "操作用户:" + logStruct.getUser() +System.lineSeparator();
                                warnText += "操作时间:" + Times.sDT(logStruct.getCreatetime()) +System.lineSeparator();
                                warnText += "--------------------------------------------------------------------------------------" +System.lineSeparator();
                                out(warnText, getDir() + "/sql_warn_log.log");
                            }
                        }
                    }
                    else if (logStruct.getLog() instanceof ProxyRunning){
                        ProxyRunning running = (ProxyRunning)logStruct.getLog();
                        if(DataHub.LogWriter!=null && !DataHub.LogWriter.writeProxy(running)){
                            continue;
                        }
                        String _logpath=Ladder.Settings().getLogPath();
                        String dir = _logpath.substring(0,_logpath.lastIndexOf("/"));
                        String filename = Files.getFullPath(dir+File.separator+"proxy"+File.separator+ running.config.name+File.separator+Times.getDate()+".log");
                        logStruct.setFilename(filename);
                        running.trace.forEach((k,v) ->{
                            builder.append("--" + k+ "_start--" + System.lineSeparator() + v + System.lineSeparator() + "--" + k + "_end--" + System.lineSeparator());
                        });
                    }
                    else if (logStruct.getLog() instanceof LogForError){
                        LogForError errorlog = (LogForError) logStruct.getLog();
                        if(DataHub.LogWriter!=null && !DataHub.LogWriter.writeError(errorlog)){
                            continue;
                        }
                        append(builder,errorlog.getModule(),"应用模块");
                        append(builder,errorlog.getType(),"方法类型");
                        append(builder,errorlog.getMessage(),"消息正文");
                        append(builder,errorlog.getStacktrace(),"调用堆栈");
                        append(builder,logStruct.getUser(),"操作用户");
                        append(builder,Times.sDT(logStruct.getCreatetime()),"操作时间");
                        builder.append("--------------------------------------------------------------------------------------"+System.lineSeparator());
                        out(builder.toString(), getDir() + "/app_error_log.log");
                    }

                    else if(logStruct.getLog() instanceof LogForDataModelByVisit){
                        LogForDataModelByVisit v = (LogForDataModelByVisit) logStruct.getLog();
                        if(v==null)continue;
                        append(builder,"访问统计","分析项目");
                        append(builder,v.tablename,"模版名称");
                        append(builder,Times.sDT(v.starttime),"访问时间");
                        append(builder,v.duration+"ms","持续时长");
                        append(builder,Json.toJson(v.request),"入口参数");
                    }
                    else if(logStruct.getLog() instanceof LogForDataModelByKeep){
                        LogForDataModelByKeep v = (LogForDataModelByKeep) logStruct.getLog();
                        if(v==null)continue;
                        append(builder,"增减统计","分析项目");
                        append(builder,v.tablename,"模版名称");
                        append(builder, (v.isAdd ? "新增(insert)" : "删除(delete|truncate)"),"操作类型");
                        append(builder,Json.toJson(v.data),"保留数据");
                    }
                    else if(logStruct.getLog() instanceof LogForDataModelByCompare){
                        LogForDataModelByCompare v = (LogForDataModelByCompare) logStruct.getLog();
                        if(v==null)continue;
                        append(builder,"修改对照","分析项目");
                        append(builder, v.tablename,"模版名称");
                        append(builder, Json.toJson(v.oldrawdata),"操作之前");
                        append(builder, Json.toJson(v.data),"操作之后");
                    }
                    else if(logStruct.getLog() instanceof LogForDataModelByRate){
                        LogForDataModelByRate v = (LogForDataModelByRate) logStruct.getLog();
                        if(v==null)continue;
                        append(builder,"修改对照","数据吞吐");
                        append(builder, v.tablename,"模版名称");
                        append(builder, Times.sDT(v.starttime),"访问时间");
                        append(builder, v.recordcount + "条","获取数据");
                        append(builder, v.duration+"ms","持续时长");
                        append(builder,  v.rate,"记录占比");
                    }

                    append(builder, logStruct.getUser(),"操作用户");
                    builder.append("操作时间:" +Times.sDT(logStruct.getCreatetime()) +System.lineSeparator());
                    builder.append("--------------------------------------------------------------------------------------" +System.lineSeparator());
                    if (Strings.isBlank(logStruct.getFilename())) out(builder.toString(), getDir(),module, true);
                    else out(builder.toString(), logStruct.getFilename());
                }
                //批量上传
                if (Strings.hasValue(Ladder.Settings().getLogServer()) && !Core.isEmpty(logs)){
                    //sql日志只上传操作sql
                    List<LogStruct> filters = Collections.where(logs, x -> {
                        if (!x.isUpload() || x.getLog() == null) return false;
                        if (!(x.getLog() instanceof LogForSql))return true;
                        LogForSql sqllog = (LogForSql)x.getLog();
                        if (sqllog != null && sqllog.isError) return true;
                        //Matcher match = Regex.match(sqllog.sqltext, "^\\s*([\\w]*)");
                        return ("," + Ladder.Settings().getSqlDebugItem()+ ",").contains(","+sqllog.type+",");
                    });
                    if (filters.size()>0){
                        String url=Ladder.Settings().getLogServer();
                        //url="http://10.7.75.28:9903";
                        HttpHelper.request(url,new Record("site", Ladder.Settings().getSite()).put("data",Json.toJson(filters)),"POST");
                    }
                }
            }
            catch (Exception e){
                System.out.println(e.getMessage()+System.lineSeparator()+ Arrays.toString(e.getStackTrace()));
            }
            finally{
                resetEvent.reset();
            }
        }
    }

    private static StringBuilder append(StringBuilder builder,String text,String title){
        if(Strings.hasValue(text))builder.append(title+":"+text+System.lineSeparator());
        return builder;
    }
    /**
     * 获取日志的目录
     * @return
     */
    private static String getDir(){
        String dir = Files.getFullPath(Ladder.Settings().getLogPath());
        Date date = new Date();
        //替换dir中{yyyy-MM-dd}
        Matcher ms = Regex.match(dir, "\\{([\\w\\W]*?)\\}");
        while (ms.find()){
            String keyword = ms.group(1);
            dir = dir.replace("{" + keyword + "}", Times.format(keyword,date));
        }
        if (!Files.exist(dir)){
            Files.createDirectory(dir);
            SimpleDateFormat sdf = new SimpleDateFormat("HH_mm_ss_SSSS");
            FileSeed =sdf.format(new Date());
        }
        return dir+ File.separator;
    }
    private static void out(String text, String path){
        out(text,path,"",false);
    }

    /**
     * 写出文件
     * @param text 文本数据
     * @param path 文件名或者文件夹
     * @param judge 判定文件的大小
     */
    private static void out(String text, String path,String module, boolean judge) {
        FileWriter fileWriter = null;
        try{
            if (judge){
                if (Strings.isBlank(FileSeed)) FileSeed = Times.format("HH_mm_ss_SSSS");
                String ext = ".log";
                String filename = Files.getFullPath(path +module+ FileSeed + ext);
                if (!Files.exist(filename)){
                    Files.createFile(filename);
                }
                else{
                    while (Files.isUse(filename) || Files.getSize(filename) > 1024 * 1024 * 10){
                        FileSeed = Times.format("HH_mm_ss_SSSS");
                        filename = Files.getFullPath(path +module+ FileSeed + ext);
                        if (!Files.exist(filename)){
                            Files.createFile(filename);
                            break;
                        }
                    }
                }
                filename = Files.getFullPath(path +module+ FileSeed + ext);
                fileWriter = new FileWriter(filename,true);
                BufferedWriter bw  = new BufferedWriter(fileWriter);
                bw.write(text);
                bw.close();
            }
            else{
                String filename=Files.getFullPath(path);
                if (!Files.exist(filename)){
                    Files.createFile(filename);
                }
                fileWriter = new FileWriter(filename,true);
                fileWriter.write(text);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally{
            if(fileWriter!=null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /***
     * 写出通用日志
     * @param data
     */
    public static void writeLog(Object data){
        writeLog(data,"comm");
    }
    /**
     * 写日志
     * @param data 日志数据
     * @param catalog 日志类别
     */
    public static void writeLog(Object data, String catalog) {
        if(DataHub.LogWriter!=null && !DataHub.LogWriter.writeLog(data,catalog))return;
        String dir = Configure.getBasicPath() + "/log/" + catalog;
        dir = Files.getFullPath(dir);
        Files.createDirectory(dir);
        //var qian = $"--------------------------------------------------------------------------------------\r\n{DateTime.Now:yyyy-MM-dd HH:mm:ss}\r\n";
        if (data instanceof String){
            write(data + "\n", Files.getFullPath(dir + "/"+Times.getDate()+".log"));
        }else{
            write(Json.toJson(data) + "\n", Files.getFullPath(dir + "/"+Times.getDate()+".log"));
        }
    }
    /**
     * 写日志
     * @param catalog 日志数据
     * @param data 日志类别
     */
    public static void writeLine(String catalog,Object... data){
        if (Core.isEmpty(data) || Strings.isBlank(catalog)) return;
        StringBuilder builder = new StringBuilder();
        Collections.forEach(data,x -> builder.append(Json.toJson(data) + System.lineSeparator()));
        if(DataHub.LogWriter!=null && !DataHub.LogWriter.writeLog(builder.toString(),catalog))return;
        String dir = Configure.getBasicPath() + "/log/" + catalog;
        Files.createDirectory(dir);
        write(builder.toString(), dir + Times.getDate()+".log");
    }
}
