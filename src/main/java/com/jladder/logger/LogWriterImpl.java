package com.jladder.logger;


import com.jladder.lang.Json;
import com.jladder.lang.Strings;
import com.jladder.lang.Times;


import com.jladder.proxy.ProxyRunning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogWriterImpl implements LogWriter{

    private static final Logger sqlLogger = LoggerFactory.getLogger("sqllogger");
    private static final Logger requestLogger = LoggerFactory.getLogger("requestlogger");
    public boolean writeRequest(LogFoRequest request){
        StringBuffer out =  new StringBuffer();
        if (Strings.hasValue(request.uuid)) out.append("请求标记:").append(request.uuid).append(System.lineSeparator());
        if (Strings.hasValue(request.method)) out.append("请求方式:").append(request.method).append(System.lineSeparator());
        if (Strings.hasValue(request.url)) out.append("请求地址:").append(request.url).append(System.lineSeparator());
        if (Strings.hasValue(request.request)) out.append("请求数据:").append(request.request).append(System.lineSeparator());
        if (request.duration>0) out.append("处理时长:").append(request.duration+"ms").append(System.lineSeparator());
//        if (request.request != null)
//            outtext.append("请求数据:").append(Json.toJson(request.request)).append(System.lineSeparator());
        if (Strings.hasValue(request.userinfo)) out.append("用户信息:").append(request.userinfo).append(System.lineSeparator());
        out.append("请求时间:").append(Times.sDT(request.starttime)).append(System.lineSeparator());
        requestLogger.info(out.toString());
        return false;
    }
    public boolean writeSql(LogForSql sqllog){
        StringBuffer outtext =  new StringBuffer();
        if (sqllog.isError)
        {
            if (Strings.hasValue(sqllog.requestmark))outtext.append("请求标识:").append(sqllog.requestmark).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.conn)) outtext.append("连 接 器:").append(sqllog.conn).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.type)) outtext.append("处理类型:").append(sqllog.type).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.tag)) outtext.append("模型标签:").append(sqllog.tag).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.sqltext)) outtext.append("执行语句:").append(sqllog.sqltext).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.data)) outtext.append("填充数据:").append(sqllog.data).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.cause)) outtext.append("引发原因:").append(sqllog.cause).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.stacktrace)) outtext.append("调用堆栈:").append(System.lineSeparator()).append(sqllog.stacktrace).append(System.lineSeparator());
            String errortext = outtext.toString();
            if (Strings.hasValue(sqllog.visitor)) errortext += "操作用户:" + sqllog.visitor + System.lineSeparator();
            errortext += "操作时间:" + Times.getNow() + System.lineSeparator();
            errortext += "--------------------------------------------------------------------------------------" + System.lineSeparator();
            requestLogger.error(errortext);
            return false;
        }
        else
        {
            if (Strings.hasValue(sqllog.requestmark))outtext.append("请求标识:").append(sqllog.requestmark).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.conn)) outtext.append("连 接 器:").append(sqllog.conn).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.type)) outtext.append("处理类型:").append(sqllog.type).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.tag)) outtext.append("模型标签:").append(sqllog.tag).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.sqltext)) outtext.append("执行语句:").append(sqllog.sqltext).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.data)) outtext.append("填充数据:").append(sqllog.data).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.duration+"ms")) outtext.append("执行时长:").append(sqllog.duration).append(System.lineSeparator());
            if (sqllog.isOverTime)
            {
                String errortext = outtext + "";
                if (Strings.hasValue(sqllog.visitor)) errortext += "操作用户:" + sqllog.visitor+ System.lineSeparator();
                errortext += "操作时间:" + Times.getNow() + System.lineSeparator();
                errortext += "--------------------------------------------------------------------------------------" + System.lineSeparator();
                sqlLogger.warn(errortext);
            }
            outtext.append(System.lineSeparator()).append("操作时间:").append(Times.getNow()).append(System.lineSeparator());
            outtext.append("--------------------------------------------------------------------------------------").append(System.lineSeparator());
            sqlLogger.info(outtext.toString());
        }
        return false;
    }

    @Override
    public boolean writeError(LogForError sqllog) {
        return false;
    }

    @Override
    public boolean writeProxy(ProxyRunning running) {
        return false;
    }

    public boolean writeLog(String log) {
        Logger logger = LoggerFactory.getLogger("comm");
        if(logger!=null){
            logger.info(log);
        }
        return false;
    }
    public boolean writeLog(Object log, String moudle) {
        StringBuffer out =  new StringBuffer();
        out.append(System.lineSeparator());
        if(log instanceof String){
            out.append(log);
        }else{
            out.append(Json.toJson(log));
        }
        out.append(System.lineSeparator()).append("操作时间:").append(Times.getNow()).append(System.lineSeparator());
        out.append("--------------------------------------------------------------------------------------").append(System.lineSeparator());
        Logger logger = LoggerFactory.getLogger(moudle);
        if(logger!=null){
            logger.info(out.toString());
        }
        return false;
    }
}
