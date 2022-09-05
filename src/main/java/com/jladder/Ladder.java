package com.jladder;

import com.jladder.configs.Configure;
import com.jladder.configs.LadderSettings;
import com.jladder.hub.DataHub;
import com.jladder.hub.IWorkCache;
import com.jladder.hub.WebHub;
import com.jladder.logger.LogWriter;
import com.jladder.proxy.ICrossAccess;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Ladder {
    public static final String Version="3.4.1";
    private static LadderSettings mSettings;

    static {
        mSettings=new LadderSettings();
        LoggerFactory.getLogger(Ladder.class).info("Ladder "+Version);
    }
    public static LadderSettings Settings(){
        return mSettings ==null? new LadderSettings():mSettings;
    }

    /**
     * 设置Ladder的设置
     * @param settings 设置
     */
    @Autowired(required = false)
    public void setSettings(LadderSettings settings){
        if(settings!=null){
            mSettings = settings;
            if(mSettings.getSettings()!=null){
                mSettings.getSettings().forEach((k,v)->Configure.put(k,v));
            }
        }
    }

    /**
     * 设置工作缓存
     * @param workcache 工作缓存对象
     */
    @Autowired(required = false)
    public void setWorkCache(IWorkCache workcache){
        DataHub.WorkCache=workcache;
    }

    /**
     * 设置交叉权限
     * @param access
     */
    @Autowired(required = false)
    public void setCrossAccess(ICrossAccess access){
        WebHub.CrossAccess=access;
    }
    /**
     * 设置写出日志
     * @param writer
     */
    @Autowired(required = false)
    public void setLogWrite(LogWriter writer){
        if(writer!=null)DataHub.LogWriter=writer;
    }

    /**
     * 输出设置
     * @param settings
     */
    public static void init(LadderSettings settings){
        if(settings!=null){
            mSettings = settings;
            if(mSettings.getSettings()!=null){
                mSettings.getSettings().forEach((k,v)->Configure.put(k,v));
            }
        }
    }
}
