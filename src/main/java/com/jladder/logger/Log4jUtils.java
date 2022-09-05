package com.jladder.logger;



import java.io.File;

public class Log4jUtils {
    /*
    private static LoggerContext ctx;
    private static org.apache.logging.log4j.core.config.Configuration myconfig;
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
        LoggerConfig loggerConfig  = LoggerConfig.createLogger(false, Level.ALL,loggerName, "true",refs,null,myconfig,null);
        //附加写出器
        loggerConfig.addAppender(appender, null, null);
        //添加到容器中
        myconfig.addLogger( loggerName, loggerConfig);
        ctx.updateLoggers();
    }
    //使用完之后记得调用此方法关闭动态创建的logger，避免内存不够用或者文件打开太多
    public static void stop(String loggerName) {
        myconfig.getAppender("" + loggerName).stop();
        myconfig.getLoggerConfig("" + loggerName).removeAppender(loggerName);
        myconfig.removeLogger("" + loggerName);
        ctx.updateLoggers();
    }
    //获取Logger
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

    */
}
