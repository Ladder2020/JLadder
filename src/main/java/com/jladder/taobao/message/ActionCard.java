package com.jladder.taobao.message;

import com.jladder.data.Record;
import com.jladder.lang.R;
import com.jladder.net.http.HttpHelper;

public class ActionCard {


//    private Record btn_json_list;
    /**
     * 消息点击链接地址
     */
    private String single_url;


    /**
     * 按钮排列方式 0：竖直排列,1：横向排列
     */
    private String btnOrientation="0";
    /**
     *整体跳转ActionCard样式时的标题
     */
    private String single_title;
    private String markdown;
    /**
     * 透出到会话列表和通知的文案
     */
    private String title;

    public ActionCard(){}
    public ActionCard(String title,String single_title) {
        this.title = title;
        this.single_title = single_title;
    }

    public String getSingle_url() {
        return single_url;
    }
    public ActionCard setUrl(String url){
        this.single_url=url;
        return this;
    }
    public void setUrl(String appKey,String code,String url) {
        this.single_url = "https://oapi.dingtalk.com/connect/oauth2/sns_authorize?appid=" + appKey
                + "&response_type=code&scope=snsapi_auth&state=" + code + "&redirect_uri="
                + HttpHelper.encode(url);
    }

    public void setSingle_url(String single_url) {
        this.single_url = single_url;
    }

    public String getBtnOrientation() {
        return btnOrientation;
    }

    public void setBtnOrientation(String btnOrientation) {
        this.btnOrientation = btnOrientation;
    }

    public String getSingle_title() {
        return single_title;
    }

    public void setSingle_title(String single_title) {
        this.single_title = single_title;
    }

    public String getMarkdown() {
        return markdown;
    }

    public ActionCard setMarkdown(String markdown) {
        this.markdown = markdown;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ActionCard setTitle(String title) {
        this.title = title;
        return this;
    }


}
