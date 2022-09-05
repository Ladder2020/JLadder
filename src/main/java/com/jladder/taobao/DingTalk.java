package com.jladder.taobao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jladder.data.Receipt;
import com.jladder.data.Record;
import com.jladder.db.Rs;
import com.jladder.lang.Json;
import com.jladder.lang.Regex;
import com.jladder.lang.Strings;
import com.jladder.net.http.HttpHelper;
import com.jladder.taobao.message.ActionCard;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DingTalk {

    private final static ConcurrentHashMap<String,String>  Tokens = new ConcurrentHashMap<String,String>();
    private final static ScheduledExecutorService mScheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final static String Field_Errcode="errcode";
    /**
     * 工作通知
     */
    private final static String Api_Note = "https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2";
    private final static String Api_oToMessages_batchSend = "https://api.dingtalk.com/v1.0/robot/oToMessages/batchSend";
    private static String createUrl(String api_url,DingSecret secret){
        return api_url+"?access_token="+getAccessToken(secret).getData();
    }
    private static String createUrl(String api_url,String appKey,String appSecret){
        return api_url+"?access_token="+getAccessToken(appKey,appSecret).getData();
    }
    /**
     * 获取AccessToken
     */
    public static Receipt<String> getAccessToken(DingSecret secret){
        return getAccessToken(secret.getAppkey(),secret.getAppsecret());
    }

    /**
     * 获取AccessToken
     * @param appKey 钉钉应用AppKey
     * @param appSecret 钉钉应用Secret
     * @return
     */
    public static Receipt<String> getAccessToken(String appKey,String appSecret){
        if(Tokens.containsKey(appKey+appSecret))return new Receipt<String>().setData(Tokens.get(appKey+appSecret));
        String api_url = "https://oapi.dingtalk.com/gettoken";
        String ret = HttpHelper.get(api_url, new Record("appkey", appKey).put("appsecret", appSecret));
        if(Strings.isBlank(ret)){
            return new Receipt(false);
        }
        JSONObject json = JSON.parseObject(ret);
        if(json.getInteger(Field_Errcode)==0){
            String token = json.getString("access_token");
            Tokens.put(appKey+appSecret,token);
            mScheduledExecutorService.schedule(() -> {
                Tokens.remove(appKey+appSecret);
            },7000, TimeUnit.SECONDS);
            return new Receipt().setData(token);
        }
        return new Receipt<String>(false,json.getString("errmsg"));
    }

    /**
     * 获取授权地址
     * @param appKey 钉钉应用的AppKey
     * @param code 状态码，可在回调中取回
     * @param url 跳入路径
     * @return
     */
    public static String getAuthorizeUrl(String appKey,String code,String url){
        return "https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=" + appKey
                + "&response_type=code&scope=snsapi_auth&state=" + code + "&redirect_uri="
                + HttpHelper.encode(url);
    }

    /**
     * 发送卡片型工作通知
     * @param secret 钉钉密钥
     * @param user 用户编号
     * @param card 卡片体
     * @return
     */
    public static Receipt<String> sendActionCardNotice(DingSecret secret, String user, ActionCard card){
        JSONObject json = new JSONObject();
        json.put("msgtype","action_card");
        json.put("action_card",card);
        Receipt<String> ret = HttpHelper.request(createUrl(Api_Note,secret), new Record("agent_id", secret.getAgentid()).put("userid_list", user).put("msg", json));
        if(!ret.isSuccess())return ret;
        JSONObject result = JSON.parseObject(ret.getData());
        if(result.getInteger(Field_Errcode)==0)return new Receipt().setData(result.getString("task_id"));
        else{
            return new Receipt<String>(false,result.getString("errmsg"));
        }
    }

    /**
     * 发送文本型工作通知
     * @param secret 钉钉密钥
     * @param user 用户编号
     * @param content 文本内容
     * @return
     */
    public static Receipt<String> sendTextNotice(DingSecret secret,String user,String content){
        String api_url = Api_Note+"?access_token="+getAccessToken(secret).getData();
        JSONObject json = new JSONObject();
        json.put("msgtype","text");
        json.put("text",new JSONObject().fluentPut("content",content));
        Receipt<String> ret = HttpHelper.request(api_url, new Record("agent_id", secret.getAgentid()).put("userid_list", user).put("msg", json));
        if(!ret.isSuccess())return ret;
        JSONObject result = JSON.parseObject(ret.getData());
        if(result.getInteger(Field_Errcode)==0)return new Receipt().setData(result.getString("task_id"));
        else return new Receipt<String>(false,result.getString("errmsg"));
    }

    /**
     * 发送工作通知
     * @param secret 钉钉密钥
     * @param user 用户编号
     * @param message 消息体，JSON结构
     * @return
     */
    public static Receipt<String> sendNotice(DingSecret secret,String user,String message){
        String api_url = Api_Note+"?access_token="+getAccessToken(secret).getData();
        JSONObject data = JSON.parseObject(message);
        Receipt<String> ret = HttpHelper.request(api_url, new Record("agent_id", secret.getAgentid()).put("userid_list", user).put("msg", data));
        if(!ret.isSuccess())return ret;
        JSONObject result = JSON.parseObject(ret.getData());
        if(result.getInteger(Field_Errcode)==0)return new Receipt().setData(result.getString("task_id"));
        else return new Receipt<String>(false,result.getString("errmsg"));
    }

    /***
     * 群机器发送文本消息
     * @param token 群机器人的Token
     * @param secret 群机器人的Secret
     * @param content 文本内容
     * @return
     */
    public static Receipt sendTextMessageByRobot(String token, String secret, String content){
        try{
            Long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String sign = URLEncoder.encode(new String(Base64.getEncoder().encode(signData)),"UTF-8");
            String msg = new Record("msgtype", "text").put("text", new Record("content", content)).toString();
            //如果Token是WebHook路径
            if(Regex.isMatch(token,"http[s]?://")){
                int index = token.indexOf("?access_token=");
                token=token.substring(index+14);
            }
            Receipt<String> ret = HttpHelper.requestByJson("https://oapi.dingtalk.com/robot/send?access_token=" + token + "&timestamp=" + timestamp + "&sign=" + sign, msg, new Record("timestamp", timestamp).put("sign", sign));
            if (!ret.result) return ret;
            Record record = Record.parse(ret.data);
            return "0".equals(record.getString("errcode")) ? new Receipt() : ret;
        }catch (Exception e){
            return new Receipt(false);
        }
    }
    /***
     * 群机器发送消息
     * @param token 群机器人的Token
     * @param secret 群机器人的Secret
     * @param message JSON消息体，参考钉钉消息格式
     * @return
     */
    public static Receipt sendMessageByRobot(String token, String secret,String message){
        try{
            Long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String sign = URLEncoder.encode(new String(Base64.getEncoder().encode(signData)),"UTF-8");
            Receipt<String> ret = HttpHelper.requestByJson("https://oapi.dingtalk.com/robot/send?access_token=" + token + "&timestamp=" + timestamp + "&sign=" + sign, message, new Record("timestamp", timestamp).put("sign", sign));
            if (!ret.result) return ret;
            Record record = Record.parse(ret.data);
            return "0".equals(record.getString("errcode")) ? new Receipt() : ret;
        }catch (Exception e){
            return new Receipt(false);
        }
    }

    /**
     * 通过钉钉机器人应用发送消息
     * @param secret 钉钉密钥
     * @param userIds 用户组
     * @param msgKey 消息类型
     * @param msgParam 消息参数
     * @return
     */

    public static Receipt sendMessageByRobot(DingSecret secret, List<String> userIds,String msgKey,String msgParam){
        Receipt<String> ret = HttpHelper.requestByJson(Api_oToMessages_batchSend,
                new Record("robotCode",secret.getAppkey()).put("userIds",userIds).put("msgKey",msgKey).put("msgParam",msgParam),
                new Record("x-acs-dingtalk-access-token", getAccessToken(secret).data));
        return ret;
    }

    /**
     * 获取部门集合
     * @param secret 钉钉密钥
     * @param dept_id 部门编号，1为跟部门
     * @param dic 历史集合，可空，仅在有历史记录时使用
     * @return
     */
    public static Receipt<Map<Long,Record>> getDepartmentMap(DingSecret secret, Long dept_id, Map<Long,Record> dic){
        if(dic==null)dic=new HashMap<Long,Record>();
        Receipt<String> ret = HttpHelper.requestByJson("https://oapi.dingtalk.com/topapi/v2/department/listsub?access_token=" + getAccessToken(secret).getData() , new Record("dept_id", dept_id),null);
        if (!ret.result) return new Receipt<Map<Long,Record>>(false,ret.message);
        JSONObject result = JSON.parseObject(ret.getData());
        if(result==null|| !"0".equals(result.getString("errcode")))return new Receipt<Map<Long,Record>>(false,result==null?"请求失败[0193]":result.getString("errmsg"));
        JSONArray depts = result.getJSONArray("result");
        if(depts==null)return new Receipt<Map<Long,Record>>(false,"部门数据为空");
        for (int i = 0; i < depts.size(); i++) {
            JSONObject data = depts.getJSONObject(i);
            if(data==null)continue;
            String path=data.getString("name");
            Long parent_id = data.getLong("parent_id");
            Long this_dept_id = data.getLong("dept_id");
            if(dic.containsKey(parent_id)){
               String dept_path = dic.get(parent_id).getString("dept_path");
               if(Strings.hasValue(dept_path))path=dept_path+"/"+path;
            }
            dic.put(this_dept_id,
                new Record("auto_add_user",data.getBooleanValue("auto_add_user"))
                        .put("create_dept_group",data.getBooleanValue("create_dept_group"))
                        .put("dept_id",this_dept_id)
                        .put("name",data.getString("name"))
                        .put("parent_id",data.getLong("parent_id"))
                        .put("dept_path",path)
            );
            Receipt<Map<Long, Record>> rett = getDepartmentMap(secret, this_dept_id, dic);
            if(rett.isSuccess())dic.putAll(rett.data);
        }
        return new Receipt<Map<Long,Record>>().setData(dic);
    }


    /**
     * 获取用户列表
     * @param secret 钉钉密钥
     * @param dept_id 部门编号，跟目录：1
     * @param cursor 游标，跳过条数，默认：0
     * @return
     */
    public static Receipt<List<Record>> getDepartmentUserList(DingSecret secret,Long dept_id,int cursor){
        List<Record> ret = new ArrayList<Record>();
        Receipt<String> rett = HttpHelper.requestByJson("https://oapi.dingtalk.com/topapi/v2/user/list?access_token=" + getAccessToken(secret).getData() ,
                new Record("dept_id", dept_id).put("size",100).put("cursor",cursor),null);
        if (!rett.result) return new Receipt<List<Record>>(false,rett.message);
        JSONObject result = JSON.parseObject(rett.getData());
        if(result==null|| !"0".equals(result.getString("errcode")))return new Receipt<List<Record>>(false,result==null?"请求失败[0193]":result.getString("errmsg"));
        JSONObject ur = result.getJSONObject("result");
        JSONArray list = ur.getJSONArray("list");
        if(Rs.isBlank(list))return new Receipt<List<Record>>(false,"用户为空[0196]");
        for (int i = 0; i < list.size(); i++) {
            ret.add(toUserInfo(list.getJSONObject(i)));
        }
        if(ur.getBooleanValue("has_more")){
            Receipt<List<Record>> ret_next = getDepartmentUserList(secret, dept_id, ur.getInteger("next_cursor"));
            if(ret_next.isSuccess())ret.addAll(ret_next.getData());
        }
        return new Receipt<List<Record>>().setData(ret);
    }

    /**
     * 获取用户信息
     * @param secret 钉钉密钥
     * @param userid 用户编号
     * @return
     */
    public static Receipt<Record> getUserInfo(DingSecret secret,String userid){
        Receipt<String> ret = HttpHelper.requestByJson("https://oapi.dingtalk.com/topapi/v2/user/get?access_token=" + getAccessToken(secret).getData(), new Record("userid", userid),null);
        if (!ret.result) return new Receipt<Record>(false,ret.message);
        JSONObject result = JSON.parseObject(ret.getData());
        if(result==null|| !"0".equals(result.getString("errcode")))return new Receipt<Record>(false,result==null?"请求失败[0193]":result.getString("errmsg"));
        JSONObject data = result.getJSONObject("result");
        Record user = toUserInfo(data);
        if(user.containsKey("dept_id")){
            Receipt<Record> dept = getDepartmentInfo(secret, user.getString("dept_id"));
            if(dept.isSuccess())user.put("dept_name",dept.data.get("name"));
        }
        return new Receipt<Record>().setData(user);
    }

    /**
     * 获取部门信息
     * @param secret 钉钉密钥
     * @param dept_id 部门编码
     * @return
     */
    public static Receipt<Record> getDepartmentInfo(DingSecret secret,String dept_id){
        Receipt<String> ret = HttpHelper.requestByJson("https://oapi.dingtalk.com/topapi/v2/department/get?access_token=" + getAccessToken(secret).getData(), new Record("dept_id", dept_id),null);
        if (!ret.result) return new Receipt<Record>(false,ret.message);
        JSONObject result = JSON.parseObject(ret.getData());
        if(result==null|| !"0".equals(result.getString("errcode")))return new Receipt<Record>(false,result==null?"请求失败[0193]":result.getString("errmsg"));
        JSONObject data = result.getJSONObject("result");
        Record dept = new Record("auto_add_user",data.getBooleanValue("auto_add_user"))
                .put("create_dept_group",data.getBooleanValue("create_dept_group"))
                .put("dept_id",data.getLong("dept_id"))
                .put("name",data.getString("name"))
                .put("parent_id",data.getLong("parent_id"));
                //.put("dept_path",path);
        return new Receipt<Record>().setData(dept);
    }

    /**
     * 根据扫描码获取用户信息，用于钉钉授权链接回调时
     * @param secret 钉钉密钥
     * @param code 扫描码
     * @return
     */
    public static Receipt<Record> getUserInfoByScanCode(DingSecret secret,String code){
        try{
            Long timestamp = System.currentTimeMillis();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getAppsecret().getBytes("UTF-8"), "HmacSHA256"));
            byte[] signatureBytes  = mac.doFinal((timestamp+"").getBytes("UTF-8"));
            String signature = new String(Base64.getEncoder().encode(signatureBytes));
            if(Strings.isBlank(signature)) {
                return new Receipt<>(false,"签名计算失败[248]");
            }
            String encoded = URLEncoder.encode(signature, "UTF-8");
            String urlEncodeSignature = encoded.replace("+", "%20").replace("*", "%2A").replace("~", "%7E").replace("/", "%2F");
            Receipt<String> ret = HttpHelper.requestByJson("https://oapi.dingtalk.com/sns/getuserinfo_bycode?accessKey=" + secret.getAppkey()+"&timestamp="+timestamp+"&signature="+urlEncodeSignature, new Record("tmp_auth_code", code),null);
            if (!ret.result) return new Receipt<Record>(false,ret.message);
            JSONObject result = JSON.parseObject(ret.getData());
            if(result==null|| !"0".equals(result.getString("errcode")))return new Receipt<Record>(false,result==null?"请求失败[0193]":result.getString("errmsg"));
            String unionid = result.getJSONObject("user_info").getString("unionid");
            ret = HttpHelper.requestByJson("https://oapi.dingtalk.com/topapi/user/getbyunionid?access_token=" + getAccessToken(secret).getData(), new Record("unionid", unionid),null);
            if (!ret.result) return new Receipt<Record>(false,ret.message);
            result = JSON.parseObject(ret.getData());
            if(result==null|| !"0".equals(result.getString("errcode")))return new Receipt<Record>(false,result==null?"请求失败[0263]":result.getString("errmsg"));
            String userid = result.getJSONObject("result").getString("userid");
            return getUserInfo(secret,userid);
        }catch (Exception e){
            return new Receipt<Record>(false,e.getMessage());
        }
    }

    /**
     * 跟临时授权码获取用户信息，钉钉微应用体系
     * @param secret 钉钉密钥
     * @param code 临时码
     * @return
     */
    public static Receipt<Record> getUserInfoByAuthCode(DingSecret secret,String code){
        Receipt<String> ret = HttpHelper.requestByJson("https://oapi.dingtalk.com/user/getuserinfo?access_token=" +getAccessToken(secret).getData()+"&code="+code, new Record("tmp_auth_code", code),null,null,null,"get");
        if (!ret.result) return new Receipt<Record>(false,ret.message);
        JSONObject result = JSON.parseObject(ret.getData());
        if(result==null|| !"0".equals(result.getString("errcode")))return new Receipt<Record>(false,result==null?"请求失败[0193]":result.getString("errmsg"));
        String userid = result.getString("userid");
        return getUserInfo(secret,userid);
    }

    /**
     * 用户信息整理
     * @param data 接口返回JSON数据
     * @return
     */
    private static Record toUserInfo(JSONObject data){
        if(data==null)return null;
        String username = data.getString("userid");
        if(Strings.isBlank(username))return null;
        Record user = new Record("userid", username).put("user_id",username).put("username",username)
                .put("unionid", data.getString("unionid"))
                .put("name", data.getString("name"))
                .put("avatar", data.getString("avatar"))
                .put("state_code", data.getString("state_code"))
                .put("mobile", data.getString("mobile"))
                .put("hide_mobile", data.getBooleanValue("hide_mobile"))
                .put("telephone", data.getString("telephone"))
                .put("job_number", data.getString("job_number"))
                .put("jobnumber", data.getString("job_number"))
                .put("title", data.getString("title"))
                .put("email", data.getString("email"))
                .put("org_email", data.getString("org_email"))
                .put("work_place", data.getString("work_place"))
                .put("remark", data.getString("remark"))
                .put("dept_id_list", data.getString("dept_id_list"))
                .put("extension", Record.parse(data.getString("extension")))
                .put("hired_date", data.getString("hired_date"))
                .put("active", data.getBooleanValue("active"))
                .put("admin", data.getBooleanValue("admin"))
                .put("boss", data.getBooleanValue("boss"))
                .put("leader", data.getBooleanValue("leader"))
                .put("login_id", data.getString("login_id"))
                .put("fullname", data.getString("name"))
                .put("nickname", data.getString("nickname"));
        try{
            user.put("dept_id", data.getJSONArray("dept_id_list").get(0));
        }
        finally {
            return user;
        }
    }

    /**
     * 钉钉创建部门
     * @param access_token API授权凭证
     * @param name 部门名称
     * @param parent_id 部门父级ID
     * @param outer_dept 是否限制本部门成员查看通讯录
     * @param hide_dept 是否隐藏本部门
     * @param create_dept_group 是否创建一个关联此部门的企业群
     * @param outer_permit_users 指定本部门成员可查看的通讯录用户userId列表
     */
    public static Receipt<Record> createDeptForUpdate(String access_token,String name, String parent_id, String outer_dept, String hide_dept, String create_dept_group, String outer_permit_users) {
        Record rex  = new Record();

        if (Strings.isBlank(name)) return new Receipt<>(false,"部门名称不可为空");
        if (Strings.isBlank(parent_id)) {
            return new Receipt<>(false,"部门父级ID不可为空或0");
        }
        rex.put("name",name);
        rex.put("parent_id",Long.parseLong(parent_id));
        if (!Strings.isBlank(outer_dept)) rex.put("outer_dept",outer_dept);
        if (!Strings.isBlank(hide_dept)) rex.put("hide_dept",hide_dept);
        if (!Strings.isBlank(create_dept_group)) rex.put("create_dept_group",create_dept_group);
        if (!Strings.isBlank(outer_permit_users)) rex.put("outer_permit_users",outer_permit_users);

        return request("https://oapi.dingtalk.com/topapi/v2/department/create",access_token,rex,"post",Record.class);
    }

    /**
     * 获取部门
     * @param access_token API授权凭证
     * @param parent_id 部门父级ID
     */
    public static Receipt<Record> getDeptByParentID(String access_token, String parent_id){

        Record rex  = new Record();

        if (!Strings.isBlank(parent_id)) {
            if (!Strings.isNumber(parent_id)) return new Receipt<>(false,"部门父级ID错误");
            rex.put("dept_id",Long.parseLong(parent_id));
        }
        return request("https://oapi.dingtalk.com/topapi/v2/department/listsub",access_token,rex,"post",Record.class);
    }

    /**
     * 更新用户信息
     * @param access_token API授权凭证
     * @param dept_id_list 部门父级ID
     * @param userid 部门父级ID
     */
    public static Receipt<Record> updateUserByDeptLiist(String access_token,String userid,String dept_id_list){
        Record rex  = new Record();

        if (Strings.isBlank(userid)) return new Receipt<>(false,"用户id不可为空");
        if (Strings.isBlank(dept_id_list)) return new Receipt<>(false,"部门id不可为空");
        rex.put("userid",userid);
        rex.put("dept_id_list",dept_id_list);

        return request("https://oapi.dingtalk.com/topapi/v2/user/update",access_token,rex,"post",Record.class);
    }

    /**
     * 删除钉钉部门
     * @param access_token API授权凭证
     * @param dept_id 部门ID
     */
    public static Receipt<Record> deleteDept(String access_token,String dept_id){
        Record rex  = new Record();
        if (Strings.isBlank(dept_id)) return new Receipt<>(false,"部门ID不可为空");
        rex.put("dept_id",dept_id);
        return request("https://oapi.dingtalk.com/topapi/v2/user/update",access_token,rex,"post",Record.class);
    }//https://oapi.dingtalk.com/topapi/v2/user/get

    /**
     * 获取用户信息
     * @param access_token API授权凭证
     * @param userid 用户id
     */
    public static Receipt<Record> getUser(String access_token,String userid){

        Record rex  = new Record();

        if (Strings.isBlank(userid)) return new Receipt<>(false,"用户id不可为空");
        rex.put("userid",userid);

        return request("https://oapi.dingtalk.com/topapi/v2/user/get",access_token,rex,"post",Record.class);
    }

    /**
     * jsapi鉴权
     * @param access_token API授权凭证
     */
    public static Receipt<Record> JsApiAuth(String access_token){
        Record rex  = new Record();
        return request("https://oapi.dingtalk.com/get_jsapi_ticket",access_token,rex,"get",Record.class);
    }

    /**
     * 创建会话群
     * @param access_token API授权凭证
     * @param title 群名称
     * @param owner 群主的userid
     * @param users 群成员的userid
     * @param config 配置项
     * showHistoryType 新成员是否可查看聊天历史消息 0（默认）：不可以查看历史记录  1：可以查看历史记录
     * searchable 群是否可搜索 0（默认）：不可搜索  1：可搜索
     * validation_type 入群是否需要验证 0（默认）：不验证入群  1：入群验证
     * mention_all_authority @all 权限：0（默认）：所有人都可以@all   1：仅群主可@all
     * management_type 管理类型 0（默认）：所有人可管理  1：仅群主可管理
     * chat_banned_type 是否开启群禁言：0（默认）：不禁言  1：全员禁言
     */
    public static Receipt<Record> chat_create(String access_token,String title,String owner,List<String> users,Record config){
        if (Strings.isBlank(title)) return new Receipt<>(false,"群名称不可为空");
        if (Strings.isBlank(owner)) return new Receipt<>(false,"群主不可为空");
        if (Rs.isBlank(users)) return new Receipt<>(false,"群成员不可为空");
        Record rex  = new Record();
        rex.put("name",title);
        rex.put("owner",owner);
        rex.put("useridlist", Json.toJson(users));
        if(config==null)config=new Record();
        String config_value = config.getString("showHistoryType",true);
        if (!Strings.isBlank(config_value)) rex.put("showHistoryType",config_value);
        config_value = config.getString("searchable",true);
        if (!Strings.isBlank(config_value)) rex.put("searchable",config_value);
        config_value = config.getString("validation_type,validationType",true);
        if (!Strings.isBlank(config_value)) rex.put("validationType",config_value);
        config_value = config.getString("mention_all_authority,mentionAllAuthority",true);
        if (!Strings.isBlank(config_value)) rex.put("mentionAllAuthority",config_value);
        config_value = config.getString("management_type,managementType",true);
        if (!Strings.isBlank(config_value)) rex.put("managementType",config_value);
        config_value = config.getString("chat_banned_type,chatBannedType",true);
        if (!Strings.isBlank(config_value)) rex.put("chatBannedType",config_value);
        return request("https://oapi.dingtalk.com/chat/create",access_token,rex,"post",Record.class);//https://oapi.dingtalk.com/topapi/im/chat/scenegroup/create
    }

    /**
     * 发送会话群消息
     * @param access_token API授权凭证
     * @param chatid 群会话编号。
     * @param content 消息内容，如果是文本，以及MarkDown发送
     */
    public static Receipt chat_send(String access_token,String chatid,String content){
        Record req = new Record("chatid",chatid);
        if(Strings.isJson(content)){
            req.put("msg",content);
        }else{
            req.put("msg",new Record("msgtype","markdown").put("markdown",new Record("text",content).put("title","提示消息")));
        }
        return request("https://oapi.dingtalk.com/chat/send",access_token,req,"post",null);
    }

    /**
     * 新增会话群成员
     * @param access_token 调用服务端API的应用凭证
     * @param chatid 接收消息的群的openConversationId，可通过创建场景群接口获取。
     * @param user_ids 批量增加的成员userid，多个userid之间使用英文逗号分隔。
     */
    public static Receipt chat_addMember(String access_token,String chatid,String user_ids){
        Record req = new Record("chatid",chatid);
        req.put("add_useridlist",user_ids.split(","));
        return request("https://oapi.dingtalk.com/chat/update",access_token,req,"post",null);
    }

    /**
     * 请求API
     * @param access_token API授权凭证
     * @param url API链接
     * @param rex 参数
     * @param method 请求方式
     * @return com.jladder.data.AjaxResult
     */
    private static <T> Receipt<T> request(String url,String access_token,Record rex,String method,Class<T> clazz){
        if (Strings.isBlank(access_token)) return new Receipt<>(false,"API授权凭证不可为空");
        if(Strings.isBlank(method)) method = "post";
        try {
            Receipt<String> ret = HttpHelper.requestByJson (url+"?access_token=" + access_token, rex,null,null,null,method);
            if (!ret.result) return new Receipt<T>(false,ret.message);
            JSONObject result = JSON.parseObject(ret.getData());
            if(result==null|| !"0".equals(result.getString("errcode")))return new Receipt<T>(false,result==null?"请求失败[0588]":result.getString("errmsg"));
            if(clazz==null || clazz.equals(JSONObject.class))return new Receipt<T>().setData((T)result);
            if(clazz.equals(Record.class)) return new Receipt<T>().setData((T)Record.parse(result.getInnerMap()));
            return new Receipt<T>().setData(result.toJavaObject(clazz));
        }
        catch(Exception ex){
            ex.printStackTrace();
            return new Receipt<T>(false, "请求出现异常，"+ex.getMessage());
        }
    }

}
