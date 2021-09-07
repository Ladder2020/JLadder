package com.jladder.actions.impl;


import com.jladder.configs.Configs;
import com.jladder.data.*;
import com.jladder.datamodel.IDataModel;
import com.jladder.db.Cnd;
import com.jladder.db.DaoSeesion;
import com.jladder.hub.DataHub;
import com.jladder.hub.WebHub;
import com.jladder.lang.*;
import com.jladder.net.http.HttpHelper;
import com.jladder.utils.RedisHelper;
import com.jladder.utils.VCodeGenerator;
import com.jladder.web.WebContext;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class UpmsAction {

    public static String DataModelForUser = "sys_user";
    public static String DataModelForGroup= "sys_usergroup";
    public static String Key_UserName= "username";
    public static String Key_UUID= "logininfo";


    public static ReStruct<String,String> getCheckCode(){
        VCodeGenerator vcode = new VCodeGenerator();
        String code = vcode.generatorVCode();
        BufferedImage bImage = vcode.generatorRotateVCodeImage(code, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bImage, "png", out);
        } catch (IOException e) {
            return new ReStruct<>(e.getMessage());
        }
        byte[] data = out.toByteArray();
        return new ReStruct<>(true,code ,Security.encryptByBase64(data));
    }

    /***
     *
     * @param userinfo
     * @param token
     * @param clazz
     * @param <T>
     * @return
     */

    public static <T extends BaseUserInfo> AjaxResult active(String userinfo, String token, Class<T> clazz)
    {
        if (WebHub.IsDebug)
        {
            if (Strings.hasValue(userinfo) && userinfo != "{}"){
                return new AjaxResult(Json.toObject(userinfo,clazz)).setStatusCode(111);
            }
            BaseUserInfo info = UpmsAction.getUserInfo(clazz);
            if (info == null)
            {
                //var cookies = WebContext.Current.Request.Cookies;
            }
            return new AjaxResult(UpmsAction.getUserInfo(clazz)).setStatusCode(111);
        }
        if (Strings.isBlank(userinfo) || Regex.isMatch(userinfo, "\\s*\\{\\s*\\}\\s*"))
        {
            T info = Strings.isBlank(token)?getUserInfo(clazz):getUserInfo(token,clazz);
            if (info == null)
            {
                //Logs.WriteLog($"uuid:{GetUuid()};UserSessionUUID:{WebContext.Current.Session.GetString("UserSessionUUID")};GetCookies:{HttpHelper.GetCookies("UserSessionUUID")};SessionID{WebContext.Current.Session.Id}", "UpmsService");
            }
            if (Regex.isMatch(HttpHelper.getIp(), "(127.0.0.1)|(localhost)")) return new AjaxResult(200).setData(info);
            //if (Strings.isBlank(info.uuid)) info.uuid = GetUuid();
            return new AjaxResult(info == null ? -401 : 200).setData(info);
        }
        else
        {
            T info = Json.toObject(userinfo,clazz);
            if (info == null || Strings.isBlank(info.getUsername())) return new AjaxResult(-401);
            Record record = QueryAction.getRecord("sys_user", new Cnd("username", info.getUsername()),null,null);

            if (record.getString("logininfo") != info.uuid && !Regex.isMatch(HttpHelper.getIp(), "(127.0.0.1)|(localhost)"))
            {
                return new AjaxResult(333, "用户在他处登录").setData(record.getString("ip"));
            }
            setUserInfo(info,false);
            return new AjaxResult().setData(info);
        }
    }
    /// <summary>
    /// 获取登录的用户名
    /// </summary>
    /// <returns></returns>
    public static String getUserName()
    {
        try
        {
            if (WebContext.getSession()!= null)
            {
                BaseUserInfo userinfo = getUserInfo(BaseUserInfo.class);
                return userinfo==null?"":userinfo.getUsername();
            }
        }
        catch (Exception e)
        {
            //Logs.Write(new LogForError(e.Message,"用户测试"),LogOption.Error);
            return "";
        }
        return "";
    }

    /**
     *
     * @param uuid
     * @return
     */
    public static boolean exist(String uuid){
        boolean has = DataHub.WorkCache.hasModuleCache(uuid,"_user_");
        if(has)return true;
        has = RedisHelper.Instance.hasKey(uuid);
        if(has){
            Receipt<BaseUserInfo> user = RedisHelper.Instance.getCache(uuid, BaseUserInfo.class);
            if(user.isSuccess())DataHub.WorkCache.addModuleCache(uuid, user.data,  "_user_",20*60);
            return true;
        }
        return false;
    }

    public static <T extends BaseUserInfo> T  getUserInfo(Class<T> clazz){
        return getUserInfo(getUuid(),clazz);
    }
    /// <summary>
    /// 获取登录的用户名
    /// </summary>
    /// <returns></returns>
    public static <T extends BaseUserInfo> T  getUserInfo(String uuid,Class<T> clazz){
        try
        {
            if (WebContext.getSession() != null)
            {
                T userInfo = DataHub.WorkCache.getModuleCache(uuid, "_user_");
                // if (userInfo == null && HttpContext.Current.Session["AgainLogin"] != null) return null;
                if (userInfo == null)
                {

                    userInfo = RedisHelper.Instance.getCache(uuid,clazz).data;
                    if (userInfo == null && WebContext.getSession().getAttribute("AgainLogin") != null) return null;
                    if (userInfo == null && WebContext.getSession().getAttribute("AgainLogin") == null)
                    {
                        WebContext.getSession().setAttribute("AgainLogin","1");
                        String username = QueryAction.getValue("sys_user", "username", new Cnd("logininfo",uuid),null,String.class);
                        if(Strings.hasValue(username))userInfo = getUserInfo(username,"username",clazz);
                    }
                    if (userInfo != null)
                    {
                        RedisHelper.Instance.addCache(uuid,userInfo,20);
                        DataHub.WorkCache.addModuleCache(uuid, userInfo,  "_user_",20);
                        WebContext.getSession().removeAttribute("AgainLogin");
                    }
                }
                else
                {
                    WebContext.getSession().removeAttribute("AgainLogin");
                }
                return userInfo;
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return null;
    }
    /// <summary>
    ///
    /// </summary>
    /// <param name="userinfo">用户名</param>
    /// <param name="firstLogin">是否登录</param>
    public static <T extends BaseUserInfo> void setUserInfo(T userinfo,boolean firstLogin)
    {
        if(userinfo==null)return;
        userinfo.uuid= getUuid();
        userinfo.setSessionid(WebContext.getSession().getId());
        if (firstLogin)
        {
            WebContext.getSession().removeAttribute("AgainLogin");
//            PluginHub.Redis.AddCache(userinfo.uuid, userinfo,TimeSpan.FromSeconds(20));
            //Logs.WriteLog($"写入->Uuid:{userinfo.Uuid}  UserName:{userinfo.UserName}  Sesssion:{WebContext.Current.Session.Id}\nRequest:{ArgumentMapping.GetRequestParams()}\nStack:{RunTime.GetStackTraces()}", "UpmsService");
            String old = QueryAction.getValue("sys_user", "logininfo", new Cnd("username", userinfo.getUsername()), null, String.class);
            if (old != userinfo.uuid)
            {
                //PluginHub.Redis.Delete(old);
            }
        }
        RedisHelper.Instance.addCache(userinfo.uuid,userinfo);
        DataHub.WorkCache.addModuleCache(userinfo.uuid, userinfo,  "_user_",20*60);
    }

    /// <summary>
    /// 获取用户信息
    /// </summary>
    /// <param name="cnd">用户条件</param>
    /// <returns></returns>
    public static <T extends BaseUserInfo> T getUserInfo(Cnd cnd,Class<T> glass)
    {
        return repair(QueryAction.getObject(DataModelForUser, cnd,null,null,glass));
    }

    /// <summary>
    /// 获取用户信息
    /// </summary>
    /// <param name="username">用户名</param>
    /// <param name="propname">属性名称</param>
    /// <returns></returns>
    public static <T extends BaseUserInfo> T getUserInfo(String username,String propname,Class<T> clazz)
    {
        if (Strings.isBlank(propname)) propname = Key_UserName;
        return repair(QueryAction.getObject(DataModelForUser, new Cnd(propname, username), null, null, clazz));
    }
    private static <T extends BaseUserInfo> T repair(T userinfo){
        if(userinfo==null)return null;
        if (Strings.hasValue(userinfo.getGroupid()))
        {
            TreeModel tm = new TreeModel();
            IDataModel dm = DaoSeesion.getDataModel(DataModelForGroup);
            String tableName = dm.getTableName();
            tm.RecurFromTable(userinfo.getGroupid(), tableName);
            List<String> cids = tm.GetIds();
            cids.add(userinfo.getGroupid());
            userinfo.setGroups(cids);
        }else{
            userinfo.setGroupid("_default_");
            List<String> groups = new ArrayList<String>();
            groups.add("_default_");
            userinfo.setGroups(groups);
        }
        return userinfo;
    }
    public static void loginout(){
        loginout(null);
    }
    /***
     * 退出登录
     */
    public static void loginout(String uuid)
    {
        if(Strings.isBlank(uuid))uuid=getUuid();
        DataHub.WorkCache.removeModuleCache(uuid, "_user_");
        WebContext.getSession().removeAttribute("UserSessionUUID");
        WebContext.getSession().setAttribute("AgainLogin", 11);
        RedisHelper.Instance.delete(uuid);
        SaveAction.update(DataModelForUser, new Record(Key_UUID, ""), new Cnd(Key_UUID, uuid));
    }
    /// <summary>
    ///获取用户标题
    /// </summary>
    /// <returns></returns>
    public static String getUserTitle()
    {
        try
        {
/*            if (RunTime.IsWeb && WebContext.Current?.Session != null)
            {
                var userinfo = GetUserInfo();
                if (userinfo == null) return "";
                return userinfo.username+"{}[{userinfo.FullName}]";
            }*/
        }
        catch (Exception e)
        {
            return "";
        }
        return "";
    }
    /// <summary>
    /// 获取的用户的Token
    /// </summary>
    /// <returns></returns>
    public static String getUserToken()
    {
        try
        {
/*            if (RunTime.IsWeb && WebContext.Current?.Session != null)
            {
                var userinfo = GetUserInfo();
                if (userinfo == null) return "";
                return userinfo.UserToken;
            }*/
        }
        catch (Exception e)
        {
            return "";
        }
        return "";
    }

    /// <summary>
    /// 获取账户中心token
    /// </summary>
    /// <param name="username">用户名</param>
    /// <returns></returns>
    public static ReStruct<String, String> genUserToken(String username)
    {
        String ssoserver = Configs.getString("sso");
        String site = Configs.getString("app") ;
        if(site==null)site = WebHub.SiteName;
        String resultInfo = HttpHelper.request(ssoserver + "/LoginBySub.aspx?systemcode=" + site + "&username=" + username, null, "GET",null).data;
        if (!Strings.isBlank(resultInfo))
        {
            Record ret = Record.parse(resultInfo);
            if (ret != null &&   "200".equals(ret.getString("Code", true)))
            {
                String token = ret.getString("Result", true);
                return new ReStruct<String, String>(true, token, ret.getString("Rel"));
            }
        }
        return new ReStruct<String, String>(false);
    }

    /// <summary>
    /// 获取客户端的UUID
    /// </summary>
    /// <returns></returns>
    public static String getUuid()
    {
        int step = 0;
        String uuid = Core.toString(WebContext.getSession().getAttribute("UserSessionUUID"));
        if (Strings.isBlank(uuid))
        {
            step = 1;
            uuid = WebContext.getCookie("UserSessionUUID");
        }
        else
        {
            Cookie[] cookies = WebContext.getRequest().getCookies();
            if (cookies!=null && cookies.length < 1) step = 2;
        }
        if (Strings.isBlank(uuid))
        {
            step = 2;
            try
            {
                uuid = WebContext.getSession().getId();
            }
            catch (Exception e)
            {
                uuid = "";
            }
        }
        if (Strings.isBlank(uuid))
        {
            step = 3;
            uuid = Core.genUuid();
        }
        try
        {
            if (step > 0) WebContext.getSession().setAttribute("UserSessionUUID", uuid);
            if (step > 1) WebContext.getResponse().addCookie(new Cookie("UserSessionUUID", uuid));
        }
        catch (Exception e)
        {
            // ignored
        }
        return uuid;
    }



}
