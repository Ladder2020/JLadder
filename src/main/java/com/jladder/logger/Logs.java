package com.jladder.logger;


import com.jladder.lang.Strings;
import com.jladder.lang.Times;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.appender.rolling.action.DeleteAction;
import org.apache.logging.log4j.core.appender.rolling.action.Duration;
import org.apache.logging.log4j.core.appender.rolling.action.IfFileName;
import org.apache.logging.log4j.core.appender.rolling.action.IfLastModified;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;


import java.io.File;

public class Logs {
    private static LoggerContext ctx;
    private static org.apache.logging.log4j.core.config.Configuration myconfig;
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger("sqllogger");
    private static final org.apache.logging.log4j.Logger requestlogger = LogManager.getLogger("requestlogger");
    public static void writeRequest(LogFoRequest request){
        StringBuffer outtext =  new StringBuffer();
        if (Strings.hasValue(request.uuid)) outtext.append("请求标记:").append(request.uuid).append(System.lineSeparator());
        if (Strings.hasValue(request.method)) outtext.append("请求方式:").append(request.method).append(System.lineSeparator());
        if (Strings.hasValue(request.url)) outtext.append("请求地址:").append(request.url).append(System.lineSeparator());
        if (Strings.hasValue(request.request)) outtext.append("请求数据:").append(request.request).append(System.lineSeparator());
        if (Strings.hasValue(request.duration)) outtext.append("处理时长:").append(request.duration).append(System.lineSeparator());
//        if (request.request != null)
//            outtext.append("请求数据:").append(Json.toJson(request.request)).append(System.lineSeparator());
        if (Strings.hasValue(request.userinfo)) outtext.append("用户信息:").append(request.userinfo).append(System.lineSeparator());
        outtext.append("请求时间:").append(Times.sDT(request.starttime)).append(System.lineSeparator());
        requestlogger.info(outtext.toString());
    }
    public static void writeSql(LogForSql sqllog){
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
            logger.error(errortext);
        }
        else
        {
            if (Strings.hasValue(sqllog.requestmark))outtext.append("请求标识:").append(sqllog.requestmark).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.conn)) outtext.append("连 接 器:").append(sqllog.conn).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.type)) outtext.append("处理类型:").append(sqllog.type).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.tag)) outtext.append("模型标签:").append(sqllog.tag).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.sqltext)) outtext.append("执行语句:").append(sqllog.sqltext).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.data)) outtext.append("填充数据:").append(sqllog.data).append(System.lineSeparator());
            if (Strings.hasValue(sqllog.duration)) outtext.append("执行时长:").append(sqllog.duration).append(System.lineSeparator());
            if (sqllog.isOverTime)
            {
                String errortext = outtext.toString() + "";
                if (Strings.hasValue(sqllog.visitor)) errortext += "操作用户:" + sqllog.visitor+ System.lineSeparator();
                errortext += "操作时间:" + Times.getNow() + System.lineSeparator();
                errortext += "--------------------------------------------------------------------------------------" + System.lineSeparator();
                logger.warn(errortext);
            }
            outtext.append(System.lineSeparator()).append("操作时间:").append(Times.getNow()).append(System.lineSeparator());
            outtext.append("--------------------------------------------------------------------------------------").append(System.lineSeparator());
            logger.info(outtext.toString());
        }

        //logger.info(Json.toJson(sql));
        //logger.error(sql);
    }
    public static void writeLog(String log) {
        getLogger("comm").info(log);
    }
    public static void writeLog(String log, String moudle) {
        getLogger(moudle).info(log);
    }

    private void createRollingFileAppender(String loggerName){



        //创建一个展示的样式：PatternLayout，   还有其他的日志打印样式。
        Layout layout = PatternLayout.newBuilder().withConfiguration(myconfig).withPattern("%msg%n").build();
        //单个日志文件大小
        TimeBasedTriggeringPolicy tbtp = TimeBasedTriggeringPolicy.createPolicy(null, null);
        TriggeringPolicy tp = SizeBasedTriggeringPolicy.createPolicy("10M");
        CompositeTriggeringPolicy policyComposite = CompositeTriggeringPolicy.createPolicy(tbtp, tp);

        String loggerDir = "log" + File.separator + loggerName;
        //删除日志的条件
        IfFileName ifFileName = IfFileName.createNameCondition(null, loggerName + "\\.\\d{4}-\\d{2}-\\d{2}.*");
        IfLastModified ifLastModified = IfLastModified.createAgeCondition(Duration.parse("1d"));
        DeleteAction deleteAction = DeleteAction.createDeleteAction(
                loggerDir, false, 1, false, null,
                new PathCondition[]{ifLastModified,ifFileName}, null, myconfig);
        Action[] actions = new Action[]{deleteAction};

        DefaultRolloverStrategy strategy = DefaultRolloverStrategy.createStrategy(
                "7", "1", null, null, actions, false, myconfig);



        String loggerPathPrefix = loggerDir + File.separator + loggerName;
//        RollingFileAppender appender = RollingFileAppender.newBuilder()
//                .withFileName(loggerPathPrefix + ".log")
//                .withFilePattern(loggerPathPrefix + ".%d{yyyy-MM-dd}.%i.log")
//                .withAppend(true)
//                .withStrategy(strategy)
//                .withName(loggerName)
//                .withPolicy(policyComposite)
//                .withLayout(layout)
//                .withConfiguration(config)
//                .build();
//        appender.start();
    }

    private static void start(String loggerName) {
        Layout layout = PatternLayout.newBuilder().withConfiguration(myconfig).withPattern("%msg%n").build();;
        Appender appender = FileAppender.newBuilder()
                .withBufferedIo(true).withLocking(false)
                .setName(loggerName).withFileName("logs"+File.separator+loggerName+".log")
                .withAppend(true).setConfiguration(myconfig).setLayout(layout).build();
        myconfig.addAppender(appender);
        AppenderRef[] refs = new AppenderRef[]{AppenderRef.createAppenderRef("" + loggerName, null, null)};
        //创建配置
        LoggerConfig loggerConfig  = LoggerConfig.createLogger(false,Level.ALL,loggerName, "true",refs,null,myconfig,null);
        //附加写出器
        loggerConfig.addAppender(appender, null, null);
        //添加到容器中
        myconfig.addLogger( loggerName, loggerConfig);
        ctx.updateLoggers();
    }
    /**使用完之后记得调用此方法关闭动态创建的logger，避免内存不够用或者文件打开太多*/
    public static void stop(String loggerName) {
        myconfig.getAppender("" + loggerName).stop();
        myconfig.getLoggerConfig("" + loggerName).removeAppender(loggerName);
        myconfig.removeLogger("" + loggerName);
        ctx.updateLoggers();
    }
    /**获取Logger*/
    public static synchronized Logger getLogger(String loggerName) {
        if(ctx==null){
            ctx= (LoggerContext) LogManager.getContext(false);
            myconfig = ctx.getConfiguration();
        }
        if (!myconfig.getLoggers().containsKey(loggerName)) {
            start(loggerName);
        }
        return LogManager.getLogger(loggerName);
    }
}
