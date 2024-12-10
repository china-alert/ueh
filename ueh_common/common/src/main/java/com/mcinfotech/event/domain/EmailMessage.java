package com.mcinfotech.event.domain;    /**  
    * @Description: 邮件消息
    * @date 2021/5/13 15:17
    */
  
  
public class EmailMessage {

    //用户名
    private String user;
    //密码
    private String password;
    //邮件服务器地址
    private String serverHost;
    //邮件文本内容类型
    private String contextType;
    //发件人
    private String mailFrom;
    //收件人
    private String mailTo;
    //抄送人
    private String mailCc;
    //密送人
    private String mailBcc;
    //邮件主题
    private String title;
    //邮件正文
    private String text;
    //附件
    private String attachPart;


    public EmailMessage(String user, String password, String serverHost, String contextType, String mailFrom, String mailTo, String mailCc, String mailBcc, String title, String text, String attachPart) {
        this.user = user;
        this.password = password;
        this.serverHost = serverHost;
        this.contextType = contextType;
        this.mailFrom = mailFrom;
        this.mailTo = mailTo;
        this.mailCc = mailCc;
        this.mailBcc = mailBcc;
        this.title = title;
        this.text = text;
        this.attachPart = attachPart;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public String getContextType() {
        return contextType;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getMailCc() {
        return mailCc;
    }

    public void setMailCc(String mailCc) {
        this.mailCc = mailCc;
    }

    public String getMailBcc() {
        return mailBcc;
    }

    public void setMailBcc(String mailBcc) {
        this.mailBcc = mailBcc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAttachPart() {
        return attachPart;
    }

    public void setAttachPart(String attachPart) {
        this.attachPart = attachPart;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
