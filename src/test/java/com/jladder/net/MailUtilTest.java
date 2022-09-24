package com.jladder.net;

import com.jladder.net.mail.MailUtil;
import junit.framework.TestCase;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

public class MailUtilTest extends TestCase {

    public void testSendMail() {
        String sendMail="";
        String receiveMail="";
        String authUserName="";
        String authPassword="";
        try{
            // 创建复杂邮件的正文
            // 1.创建图片，正文中引用
            String imagePath = "C:\\Users\\base\\Desktop\\测试图片.jpg";
            String only_image_ID = "add_image_id";
            MimeBodyPart image = MailUtil.getMailContentImage(imagePath, only_image_ID);
            // 2.创建普通文本，添加引用图片id
            String content = "这是验证邮件，您的图片为<img src='cid:add_image_id'/>";
            MimeBodyPart text = MailUtil.getMailContentText(content);
            // 3.创建一个附件
            String attachmentPath = "C:\\Users\\base\\Desktop\\mail.rar";
            MimeBodyPart attachment = MailUtil.getMailContentAttachment(attachmentPath);
            // 4.创建一个混合节点，添加以上普通节点
            MimeBodyPart[] mimeBodyPart = { image, text, attachment };
            MimeMultipart mimeMultipart = MailUtil.getMailContentMultipart(mimeBodyPart);

            MailUtil.sendMail("smtp.qq.com", sendMail, null, receiveMail, null, "用户账户激活", mimeMultipart, null,
                    authUserName, authPassword);
        }catch (Exception e){

        }
    }

    public void testTestSendMail() {



    }
}