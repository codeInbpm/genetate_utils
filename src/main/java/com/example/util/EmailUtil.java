package com.example.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class EmailUtil {


    public static void sendEmail(String host, int port, String username, String password, String to,
                                 String subject, String content, boolean isHtml) throws MessagingException {
        // 创建邮件会话
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");

        // 开启SSL连接
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", host);

        // 创建邮件会话
        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        // 创建邮件对象
        MimeMessage message = new MimeMessage(session);

        // 设置发件人
        message.setFrom(new InternetAddress(username));

        // 设置收件人
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

        // 设置邮件主题
        message.setSubject(subject);

        // 设置邮件正文
        if (isHtml) {
            message.setContent(content, "text/html;charset=UTF-8");
        } else {
            message.setText(content, "UTF-8");
        }

        // 发送邮件
        Transport.send(message);
    }

    @Test
    public void sendddd() throws MessagingException {
        sendEmail("smtp.qq.com", 465, "196322385@qq.com", "pyxafypgzufabicg", "wwb1126925@126.com", "测试邮件", "这是一封测试邮件。", true);
    }
}