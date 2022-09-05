package com.jladder.actions.impl;

import com.jladder.configs.Configure;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.db.Cnd;
import com.jladder.hub.DataHub;
import com.jladder.lang.*;
import com.jladder.lang.func.Action2;

import java.util.regex.Matcher;

/**
 * @author YiFeng
 * @date 2022年06月16日 20:24
 */
public class ViewAction {
    /// <summary>
    ///
    /// </summary>
    /// <param name="view"></param>
    /// <param name="config"></param>
    /// <returns></returns>
    public static Receipt<String> GetViewCode(String view, String config){
        String code = "";
        Record tt =Record.parse(config);
        if(tt==null)tt=new Record();
        Receipt<String> ret = GetView(view, 1, tt,null);
        if (ret.result){
            code = ret.data;
            code = Strings.findBlock(code,"//基本代码start", "//基本代码end");
            code = Security.encryptByBase64(code);
            ret.setData(code);
        }
        return ret;
    }
    /// <summary>
    ///
    /// </summary>
    /// <param name="uuid"></param>
    /// <param name="info"></param>
    /// <returns></returns>
    public static Receipt saveTempView(String uuid, String info){
        if (Strings.isBlank(uuid)) uuid = Core.genUuid();
        DataHub.WorkCache.addModuleCache(uuid, info, "_SaveTempPage_",5*60);
        return new Receipt().setData(uuid);
    }
    /// <summary>
    ///
    /// </summary>
    /// <param name="view"></param>
    /// <param name="uuid"></param>
    /// <returns></returns>
    public static Receipt<String> previewView(String view, String uuid){
        if (Strings.isBlank(uuid)) return new Receipt<String>(false, "预览ID未传递[052]");
        String info = DataHub.WorkCache.getModuleCache(uuid,"_SaveTempPage_");
        if (info==null) return new Receipt<String>(false, "预览信息未找到[055]");
        Record bean = Record.parse(info);
        Record config = Record.parse(bean.getString("configs")) ;
        if(config==null)config = new Record();
        if (Strings.isBlank(view)) view = bean.getString("viewname,view");
        Receipt<String> ret = GetView(view, bean.getInt("way"), config, bean.getString("code"));
        return ret;
    }
    /// <summary>
    ///
    /// </summary>
    /// <param name="cnd"></param>
    /// <param name="settings"></param>
    /// <returns></returns>
    public static Receipt<String> BuildView(Object cnd, Object settings){
        Receipt<String> ret = null;
        Record ext = Record.parse(settings);
        if(ext==null)ext=new Record();
        Record bean = QueryAction.getRecord("sys_pages", cnd);
        if(bean==null)return new Receipt<String>(false,"页面配置不存在[071]");
        //var ip = HttpHelper.GetIp();
        String trust = bean.getString("trust");
        /* 安全策略
        if (!Strings.isBlank(trust) && trust.trim() != "*" && HttpHelper.getIp()!="127.0.0.1")
        {
            var refurl = WebContext.Current?.Request.Host.Host+":"+ WebContext.Current?.Request.Host.Port;
            if (refurl.IsBlank()) return new Receipt<string>(false, "违反安全策略,禁止使用[080]");
            var ip = Dns.GetHostAddresses(refurl)?.FirstOrDefault()?.ToString();
            if (ip.HasValue() && !Regex.IsMatch(ip, "^\\s*(127\\.0\\.0\\.1)|(\\*)\\s*$"))
            {
                trust = trust.Replace("\n", "|").Replace(Environment.NewLine, "|").Replace(";", "|");
                if (("|" + trust + "|").IndexOf("|" + ip + "|", StringComparison.Ordinal) < 0) return new Receipt<string>(false, "违反安全策略,禁止使用[080]");
            }
        }
         */
        Record config = Record.parse(bean.getString("configs"));
        if(config==null)config = new Record();
        String view = ext.getString("viewname,view", true);
        if (Strings.isBlank(view)) view = bean.getString("viewname");
        String way = ext.getString("way");
        if (Strings.isBlank(way)) way = bean.getString("way");
        config.merge(ext);
        String code = ext.getString("code", true);
        if (Strings.isBlank(code)) code = bean.getString("code");
        ret = GetView(view, bean.getInt("way"), config, code);
        return ret;
    }

    /// <summary>
    /// 生成Form表单的Html代码页
    /// </summary>
    /// <param name="view">视图名称或配置</param>
    /// <param name="name">键名</param>
    /// <returns></returns>
    public static Receipt<String> BuildViewByName(String name)
    {
        return BuildView(new Cnd("name", name),null);
    }
    /// <summary>
    ///
    /// </summary>
    /// <param name="view"></param>
    /// <param name="type"></param>
    /// <param name="config"></param>
    /// <param name="code"></param>
    /// <returns></returns>
    public static Receipt<String> GetView(String view,int type, Record config, String code){
        String content = "";
        switch (view)
        {
            case "SimpleGrid":

                return GetSimpleGridView(type, config, code);
            default:

                //content = LocalCache.GetCache(view, "_PageAction_")?.ToString();
                content = "";
                if (Strings.isBlank(content))
                {
                    content = Files.read(Configure.getBasicPath() + "\\template\\"+ view + ".html");
                    DataHub.WorkCache.addModuleCache(view,content,"_PageAction_",20*60);
                }
                if (Strings.isBlank(content)) return new Receipt<String>(false, "模版内容为空");
                break;
        }
        if(config==null)config=new Record();
        //包含选项处理
        Matcher inMatchs = Regex.match(content, "//#in_([\\w]+)_start[\\w\\W]+//#in_\\1_end");
        while (inMatchs.find()){
            String label = inMatchs.group(1);
            String smallContent = inMatchs.group(0);
            String old = inMatchs.group(0);
            String value = config.getString(label);
            if (Strings.isBlank(value)){
                content = content.replace(smallContent, "");
            }
            else{
                Matcher ma = Regex.match(smallContent, "//([\\w]+)_start[\\w\\W]+//\\1_end");
                while (ma.find()){
                    String action = ma.group(1);
                    if (("," + value + ",").indexOf(action) < 0){
                        String rt = ma.group(0);
                        smallContent = smallContent.replace(rt, "");
                    }
                }
                content = content.replace(old, smallContent);
            }
        }
        Matcher ifMatchs = Regex.match(content, "/*#if\\(([\\w]+)(={1,3}[\'\"]?[\\w]+[\'\"]?)?\\)##([\\w\\W]*?)##([\\w\\W]*?)#endif");
        while (ifMatchs.find()) {

            String allText = ifMatchs.group(0);
            String keyText = ifMatchs.group(1);
            String expressText = ifMatchs.group(2);
            String yesText = ifMatchs.group(3);
            String noText = ifMatchs.group(4);
            String value = config.getString(keyText);
            if (Strings.isBlank(expressText)){
                content = ((Strings.isBlank(value) || "0".equals(value) || "false".equals(value)) ? content.replace(allText, yesText) : content.replace(allText, noText));
            }
            else{
                String checkValue = Regex.replace(Regex.replace(expressText, "^={1,3}[\'\"]?", ""), "[\'\"]?$", "");
                content = ((checkValue.equals(value)) ? content.replace(allText, yesText) : content.replace(allText, noText));
            }
        }
        switch (type){
            case 1:
                content = Strings.mapping(content,config);
                break;
            case 2:
                content = Strings.mapping(content,config);
                String old = Strings.findBlock(content,"//基本代码start", "//基本代码end");
                content = content.replace(old, Security.decryptByBase64(code));
                break;
        }
        return Strings.isBlank(content) ? new Receipt<String>(false, "未处理[0112]") : new Receipt<String>().setData(content);

    }
    /// <summary>
    ///
    /// </summary>
    /// <param name="type"></param>
    /// <param name="config"></param>
    /// <param name="code"></param>
    /// <returns></returns>
    public static Receipt<String> GetSimpleGridView(int type, Record config, String code){
        final String[] content = {DataHub.WorkCache.getModuleCache("GetSimpleGridView", "_PageAction_")};
        if (Strings.isBlank(content[0]))
        {
            content[0] = Files.read(Configure.getBasicPath() + "\\template\\SimpleGrid.html");
            DataHub.WorkCache.addModuleCache("GetSimpleGridView", content[0],  "_PageAction_",20*60);
        }
        try{
            if (Strings.isBlank(content[0])) return new Receipt<String>(false, "模版内容为空");
            switch (type){
                case 1:{
                    String query = config.getString("query");
                    String libpath = config.getString("libpath");
                    libpath = Regex.replace(libpath, "/$", "");
                    if (Strings.isBlank(libpath))
                    {
                        libpath = "../..";
                        config.put("libpath", libpath);
                    }
                    else config.put("libpath", libpath);
                    String wh = config.getString("wh");
                    if (Strings.isBlank(wh)) config.put("width", 600 + "px").put("height", "auto");
                    else
                    {
                        String[] ws = wh.split(",");
                        config.put("width", ws[0] == "auto" ? "auto" : ws[0].replace("px", "") + "px");
                        if (ws.length > 1) config.put("height", ws[1] == "auto" ? "auto" : ws[1].replace("px", "") + "px");
                    }
                    String pnames = config.getString("pnames");
                    if (Strings.isBlank(pnames)) config.put("queryid", "id").put("saveid", "id");
                    else
                    {
                        String[] ps = pnames.split(",");
                        config.put("saveid", ps[0]).put("queryid", ps[0]);
                        if (ps.length > 1) config.put("queryid", ps[1]);
                    }
                    content[0] = Strings.mapping(content[0],config);
                    Action2<String, String> removeTextBlock = (x, y) ->{
                        try{
                            if (Strings.isBlank(y)) y = x;
                            int start = content[0].indexOf(x);
                            int end = content[0].indexOf(y, start);
                            content[0] = content[0].substring(0, start) + content[0].substring(end + y.length());
                        }
                        catch (Exception e)
                        {
                            // ignored
                        }
                    };
                    //选择一个
                    Action2<String, Boolean> SelectOne = (x, y) ->{
                        Matcher match = Regex.match(content[0], "#if\\(" + x + "\\)##([\\w\\W]*?)##([\\w\\W]*?)#endif");
                        while (match.find()){
                            String alltext = match.group(0);
                            String yestext = match.group(1);
                            String notext = match.group(2);
                            content[0] = (y ? content[0].replace(alltext, yestext) : content[0].replace(alltext, notext));
                        }
                    };

                    String btnpos = config.getString("btnpos");

                    if (!Regex.isMatch(btnpos, "head")) removeTextBlock.invoke("//btnpos-head-start", "//btnpos-head-end");
                    if (!Regex.isMatch(btnpos, "grid")) removeTextBlock.invoke("//btnpos-grid-start", "//btnpos-grid-end");

                    String act = "," + config.getString("action") + ",";
                    if (Strings.hasValue(config.getString("action")))
                    {
                        if (!Regex.isMatch(act, ",insert,")) removeTextBlock.invoke("//新增start", "//新增end");
                        if (!Regex.isMatch(act, ",update,")) removeTextBlock.invoke("//修改start", "//修改end");
                        if (!Regex.isMatch(act, ",delete,")) removeTextBlock.invoke("//删除start", "//删除end");
                        if (!Regex.isMatch(act, ",deletes,")) removeTextBlock.invoke("//批量删除start", "//批量删除end");
                        if (!Regex.isMatch(act, ",export,")) removeTextBlock.invoke("//导出start", "//导出end");
                        if (Regex.isMatch(act, ",!edit,")) removeTextBlock.invoke("//修改start", "//修改end");
                        if (!Regex.isMatch(act, ",look,")) removeTextBlock.invoke("//查看start", "//查看end");
                    }
                    SelectOne.invoke("permission","1".equals(config.getString("permission")));
                    return new Receipt<String>(true).setData(content[0]);
                }
                case 2:
                    if (config == null) config = new Record();
                    config.put("libpath", "https://cdn.jsdelivr.net/npm/pladder@latest");
                    content[0] = Strings.mapping(content[0],config);
                    String old = Strings.findBlock(content[0],"//基本代码start", "//基本代码end");
                    content[0] = content[0].replace(old, Security.decryptByBase64(code));
                    break;
            }

        }
        catch (Exception e)
        {
            return new Receipt<String>(false, e.getMessage());
        }
        return new Receipt<String>(true).setData(content[0]);
    }
}
