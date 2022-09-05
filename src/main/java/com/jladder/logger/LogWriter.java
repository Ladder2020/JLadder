package com.jladder.logger;

import com.jladder.proxy.ProxyRunning;
public interface LogWriter {
    public boolean writeRequest(LogFoRequest request);
    public boolean writeSql(LogForSql sqllog);
    public boolean writeError(LogForError sqllog);
    public boolean writeProxy(ProxyRunning running);
    public boolean writeLog(String log);
    public boolean writeLog(Object log, String module);
}
