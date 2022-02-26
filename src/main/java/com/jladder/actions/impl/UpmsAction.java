package com.jladder.actions.impl;


import com.jladder.Ladder;
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

/**
 * 用户权限类
 */
public class UpmsAction {

    public static String DataModelForUser = "sys_user";
    public static String DataModelForGroup= "sys_usergroup";
    public static String Key_UserName= "username";
    public static String Key_UUID= "logininfo";

    private static Record Cache=new Record();

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

    public static <T extends BaseUserInfo> AjaxResult active(String userinfo, String token, Class<T> clazz){
        if (WebHub.IsDebug){
            if (Strings.hasValue(userinfo) && userinfo != "{}"){
                return new AjaxResult(Json.toObject(userinfo,clazz)).setStatusCode(111);
            }
            BaseUserInfo info = UpmsAction.getUserInfo(clazz);
            if (info == null){
                //var cookies = WebContext.Current.Request.Cookies;
            }
            return new AjaxResult(UpmsAction.getUserInfo(clazz)).setStatusCode(111);
        }
        if (Strings.isBlank(userinfo) || Regex.isMatch(userinfo, "\\s*\\{\\s*\\}\\s*")){
            T info = Strings.isBlank(token)?getUserInfo(clazz):getUserInfo(token,clazz);
            if (info == null){
                //Logs.WriteLog($"uuid:{GetUuid()};UserSessionUUID:{WebContext.Current.Session.GetString("UserSessionUUID")};GetCookies:{HttpHelper.GetCookies("UserSessionUUID")};SessionID{WebContext.Current.Session.Id}", "UpmsService");
            }
            if (Regex.isMatch(HttpHelper.getIp(), "(127.0.0.1)|(localhost)")) return new AjaxResult(200).setData(info);
            //if (Strings.isBlank(info.uuid)) info.uuid = GetUuid();
            return new AjaxResult(info == null ? -401 : 200).setData(info);
        }
        else{
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

    /**
     * 获取登录的用户名
     * @return
     */
    public static String getUserName(){
        try{
            if (WebContext.getSession()!= null){
                BaseUserInfo userinfo = getUserInfo(BaseUserInfo.class);
                return userinfo==null?"":userinfo.getUsername();
            }
        }
        catch (Exception e) {
            //Logs.Write(new LogForError(e.Message,"用户测试"),LogOption.Error);
            return "";
        }
        return "";
    }

    /**
     * 是否存在客户端
     * @param uuid 客户端编码
     * @return
     */
    public static boolean exist(String uuid){
        if(Strings.isBlank(uuid))return false;
        BaseUserInfo user = (BaseUserInfo) WebContext.getSession().getAttribute("_userinfo_");
        if(user!=null&&uuid.equals(user.uuid))return true;
        boolean has = DataHub.WorkCache.hasModuleCache(uuid,"_user_");
        if(has)return true;
        return false;
    }

    public static <T extends BaseUserInfo> T  getUserInfo(Class<T> clazz){
        return getUserInfo(getUuid(),clazz);
    }

    /**
     * 获取登录的用户信息
     * @param uuid
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends BaseUserInfo> T  getUserInfo(String uuid,Class<T> clazz){
        try{
            if (WebContext.getSession() != null){
                T userInfo = (T)Cache.get(uuid);
                if(userInfo!=null)return userInfo;
                userInfo = DataHub.WorkCache.getModuleCache(uuid, "_user_");
                // if (userInfo == null && HttpContext.Current.Session["AgainLogin"] != null) return null;
                if (userInfo == null){
                    //userInfo = RedisHelper.Instance.getCache(uuid,clazz).data;
                    if (userInfo == null && WebContext.getSession().getAttribute("AgainLogin") != null) return null;
                    if (userInfo == null && WebContext.getSession().getAttribute("AgainLogin") == null){
                        WebContext.getSession().setAttribute("AgainLogin","1");
                        String username="";
                        //String username = QueryAction.getValue("sys_user", "username", new Cnd("logininfo",uuid),null,String.class);
                        if(Strings.hasValue(username))userInfo = getUserInfo(username,"username",clazz);
                    }
                    if (userInfo != null){
                        DataHub.WorkCache.addModuleCache(uuid, userInfo,  "_user_",20);
                        Cache.put(uuid,userInfo);
                        WebContext.getSession().removeAttribute("AgainLogin");
                    }
                }
                else{
                    WebContext.getSession().removeAttribute("AgainLogin");
                }
                return userInfo;
            }
        }
        catch (Exception e){
            return null;
        }
        return null;
    }

    /**
     * 设置登录用户信息
     * @param userinfo 用户信息
     * @param firstLogin 首次登录
     * @param <T>
     */
    public static <T extends BaseUserInfo> void setUserInfo(T userinfo,boolean firstLogin){
        if(userinfo==null)return;
        userinfo.uuid= getUuid();
        userinfo.setSessionid(WebContext.getSession().getId());
        if (firstLogin){
            WebContext.getSession().removeAttribute("AgainLogin");
            //Logs.WriteLog($"写入->Uuid:{userinfo.Uuid}  UserName:{userinfo.UserName}  Sesssion:{WebContext.Current.Session.Id}\nRequest:{ArgumentMapping.GetRequestParams()}\nStack:{RunTime.GetStackTraces()}", "UpmsService");
            String old = QueryAction.getValue("sys_user", "logininfo", new Cnd("username", userinfo.getUsername()), null, String.class);
            if (!userinfo.uuid.equals(old)){
                DataHub.WorkCache.removeModuleCache(userinfo.uuid,"_user_");
                SaveAction.update(DataModelForUser,new Record("userinfo",userinfo.uuid), new Cnd("username", userinfo.getUsername()));
            }
            try{
                List<String> keys=new ArrayList<>();
                Cache.forEach((k,v)->{
                    if(userinfo.getUsername().equals(((BaseUserInfo)v).getUsername())){
                        keys.add(k);
                    }
                });
                if(keys.size()>0){
                    keys.forEach(x->Cache.remove(x));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            Cache.put(userinfo.uuid,userinfo);
        }
        DataHub.WorkCache.addModuleCache(userinfo.uuid, userinfo,  "_user_",20*60);
    }
    /**
     * 获取用户信息
     * @param cnd 用户条件
     * @param glass 泛型类型
     * @param <T> 泛型
     * @return
     */
    public static <T extends BaseUserInfo> T getUserInfo(Cnd cnd,Class<T> glass){
        return repair(QueryAction.getObject(DataModelForUser, cnd,null,null,glass));
    }

    /**
     * 获取用户信息
     * @param username 用户名
     * @param propname 属性名称
     * @param clazz 类型
     * @param <T> 泛型
     * @return
     */
    public static <T extends BaseUserInfo> T getUserInfo(String username,String propname,Class<T> clazz){
        if (Strings.isBlank(propname)) propname = Key_UserName;
        return repair(QueryAction.getObject(DataModelForUser, new Cnd(propname, username), null, null, clazz));
    }

    /**
     * 修复用户信息
     * @param userinfo 用户信息
     * @param <T> 泛型
     * @return
     */
    private static <T extends BaseUserInfo> T repair(T userinfo){
        if(userinfo==null)return null;
        if (Strings.hasValue(userinfo.getGroupid())){
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

    /**
     * 退出
     */
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
    /**
     * 获取账户中心token
     * @param username 用户名
     * @return
     */
    public static ReStruct<String, String> genUserToken(String username)
    {
        String ssoserver = Ladder.Settings().getBusiness().getSso();
        String site = Ladder.Settings().getSite();
        if(site==null)site = Ladder.Settings().getSite();
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
    public static void setUuid(String uuid){
        try {
            WebContext.setAttribute("_user_uuid_",uuid);
            WebContext.getSession().setAttribute("UserSessionUUID", uuid.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 获取客户端的UUID
     * @return
     */
    public static String getUuid(){
        int step = 0;
        String uuid=WebContext.getAttributeString("_user_uuid_");
        if(Strings.hasValue(uuid)){
            return uuid;
        }
        uuid = Core.toString(WebContext.getSession().getAttribute("UserSessionUUID"));
        if (Strings.isBlank(uuid)){
            step = 1;
            uuid = WebContext.getCookie("UserSessionUUID");
        }
        else{
            Cookie[] cookies = WebContext.getRequest().getCookies();
            if (cookies!=null && cookies.length < 1) step = 2;
        }
        if (Strings.isBlank(uuid)){
            step = 2;
            try{
                uuid = WebContext.getSession().getId();
            }
            catch (Exception e){
                uuid = "";
            }
        }
        if (Strings.isBlank(uuid)){
            step = 3;
            uuid = Core.genUuid();
        }
        try{
            if (step > 0) WebContext.getSession().setAttribute("UserSessionUUID", uuid);
            if (step > 1) WebContext.getResponse().addCookie(new Cookie("UserSessionUUID", uuid));
        }
        catch (Exception e){
            // ignored
        }
        return uuid;
    }



}
