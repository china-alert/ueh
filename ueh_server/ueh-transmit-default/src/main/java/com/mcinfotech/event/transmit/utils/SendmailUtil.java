package com.mcinfotech.event.transmit.utils;

import com.mcinfotech.event.exception.TransmitException;
import com.sun.mail.util.MailSSLSocketFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class SendmailUtil {
	private static Logger logger = LogManager.getLogger(SendmailUtil.class);
	/**
	 * 邮件发送
	 *
	 * @param toEmailAddress
	 *            收件箱地址
	 * @param emailTitle
	 *            邮件主题
	 * @param emailContent
	 *            邮件内容
	 * @param myEmailSMTPHost
	 *            邮箱的 SMTP 服务器地址为: smtp.qq.com
	 * @param myEmailAccount
	 *            发件人邮箱
	 * @param myEmailPassword
	 *            发件人邮箱密码（授权码）
	 * @throws Exception
	 */
	public static void sendEmail(String toEmailAddress, String emailTitle, String emailContent, String myEmailSMTPHost,String myEmailAccount, String myEmailPassword) throws Exception {
		Properties props = new Properties();
		// 开启debug调试
		// props.setProperty("mail.debug", "true");//
		props.setProperty("mail.debug", "true");
		// 发送服务器需要身份验证
		props.setProperty("mail.smtp.auth", "true");
		// 端口号
		props.put("mail.smtp.port", 25);
		// 设置邮件服务器主机名
		props.setProperty("mail.smtp.host", myEmailSMTPHost);
		// 发送邮件协议名称
		props.setProperty("mail.transport.protocol", "smtp");
		//读超时
		props.put("mail.smtp.timeout", 10000);
		//连接超时
		props.put("mail.smtp.connectiontimeout", 10000);
		//写超时
		props.put("mail.smtp.writetimeout", 10000);

		/** SSL认证，注意腾讯邮箱是基于SSL加密的，所以需要开启才可以使用 **/
		MailSSLSocketFactory sf = new MailSSLSocketFactory();
		sf.setTrustAllHosts(true);
		EmailAuthenticator mailauth=new EmailAuthenticator(myEmailAccount, myEmailPassword);
		// 设置是否使用ssl安全连接（一般都使用）
		props.put("mail.smtp.ssl.enable", "false");
		//props.put("mail.smtp.ssl.socketFactory", sf);

		// 创建会话
		Session session = Session.getInstance(props,mailauth);
		// 获取邮件对象
		// 发送的消息，基于观察者模式进行设计的
		Message msg = new MimeMessage(session);
		// 设置邮件标题
		msg.setSubject(emailTitle);
		// 设置邮件内容
		// 使用StringBuilder，因为StringBuilder加载速度会比String快，而且线程安全性也不错
		StringBuilder builder = new StringBuilder();
		// 写入内容
		builder.append("\n" + emailContent);
		// 定义要输出日期字符串的格式
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 在内容后加入邮件发送的时间
		// builder.append("\n时间：" + sdf.format(new Date()));
		// 设置显示的发件时间
		msg.setSentDate(new Date());
		// 设置邮件内容
		msg.setText(builder.toString());
		// 设置发件人邮箱
		// InternetAddress 的三个参数分别为: 发件人邮箱, 显示的昵称(只用于显示, 没有特别的要求), 昵称的字符集编码
		msg.setFrom(new InternetAddress(myEmailAccount, "事件通知程序", "UTF-8"));
		//msg.
		// 得到邮差对象
		Transport transport = session.getTransport("smtp");
		// 连接自己的邮箱账户
		// connect(host, user, password)
		transport.connect();
		// 发送邮件
		transport.sendMessage(msg, new Address[] { new InternetAddress(toEmailAddress) });
		/*
		 * //将该邮件保存到本地 OutputStream out = new FileOutputStream("MyEmail.eml");
		 * msg.writeTo(out); out.flush(); out.close();
		 */
		transport.close();
	}
	/**
	 * 邮件发送功能：可以发送，可抄送
	 * @param toEmailAddress 接收邮件地址
	 * @param ccEmailAddress 抄送邮件地址
	 * @param subject	邮件主题
	 * @param body	邮件正文
	 * @param mailServer	邮件服务器地址
	 * @param isSSL	是否加密
	 * @param mailServerPort	邮件服务端口
	 * @param fromEmailAddress	发送邮件地址
	 * @param fromNickName	发送邮件昵称
	 * @param fromEmailPassword	发送邮件地址
	 * @throws Exception
	 */
	public static void sendEmail(List<String> toEmailAddress,List<String> ccEmailAddress, String subject, String body, String mailServer,int mailServerPort,boolean isSSL,String fromEmailAddress,String fromNickName, String fromEmailPassword,int timeout) throws Exception {
		if(CollectionUtils.isEmpty(toEmailAddress)) throw new TransmitException("target's email address is empty");
		if(StringUtils.isEmpty(body)) throw new TransmitException("email's body is empty");
		if(StringUtils.isEmpty(mailServer))throw new TransmitException("email's server is empty");
		if(StringUtils.isEmpty(fromEmailAddress)) throw new TransmitException("email's from address is empty");
		Transport transport = null;
		try {
			Properties props = new Properties();
			// 开启debug调试
			// props.setProperty("mail.debug", "true");//
			props.setProperty("mail.debug", "true");
			// 发送服务器需要身份验证
			props.setProperty("mail.smtp.auth", "true");
			// 端口号
			if(mailServerPort<1){
				if(isSSL){
					props.put("mail.smtp.port", 465);
				}else{
					props.put("mail.smtp.port", 25);
				}
			}else{
				props.put("mail.smtp.port", mailServerPort);
			}
			// 设置邮件服务器主机名
			props.setProperty("mail.smtp.host", mailServer);
			// 发送邮件协议名称
			props.setProperty("mail.transport.protocol", "smtp");
			//读超时
			props.put("mail.smtp.timeout", timeout);
			//连接超时
			props.put("mail.smtp.connectiontimeout", timeout);
			//写超时
			props.put("mail.smtp.writetimeout", timeout);
			if(isSSL){
				/** SSL认证，注意腾讯邮箱是基于SSL加密的，所以需要开启才可以使用 **/
				MailSSLSocketFactory sf = new MailSSLSocketFactory();
				sf.setTrustAllHosts(true);
				// 设置是否使用ssl安全连接（一般都使用）
				props.put("mail.smtp.ssl.enable", "true");
				props.put("mail.smtp.ssl.socketFactory", sf);
			}
			// 创建会话
			Session session = Session.getInstance(props);
			// 获取邮件对象
			// 发送的消息，基于观察者模式进行设计的
			Message msg = new MimeMessage(session);
			// 设置邮件标题
			msg.setSubject(subject);
			// 设置邮件内容
			// 使用StringBuilder，因为StringBuilder加载速度会比String快，而且线程安全性也不错
			StringBuilder builder = new StringBuilder();
			// 写入内容
			builder.append("\n" + body);
			// 定义要输出日期字符串的格式
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// 在内容后加入邮件发送的时间
			// builder.append("\n时间：" + sdf.format(new Date()));
			// 设置显示的发件时间
			msg.setSentDate(new Date());
			// 设置邮件内容
			msg.setContent(builder.toString(), "text/html;charset=UTF-8");
//		msg.setText(builder.toString());
			// 设置发件人邮箱
			// InternetAddress 的三个参数分别为: 发件人邮箱, 显示的昵称(只用于显示, 没有特别的要求), 昵称的字符集编码
			msg.setFrom(new InternetAddress(fromEmailAddress, StringUtils.isEmpty(fromNickName)?"告警事件通知":fromNickName, "UTF-8"));
			Address[] toAddresses=new Address[toEmailAddress.size()];
			for(int i=0;i<toEmailAddress.size();i++){
				toAddresses[i]=new InternetAddress(toEmailAddress.get(i));
			}
			msg.setRecipients(MimeMessage.RecipientType.TO, toAddresses);
			if(CollectionUtils.isNotEmpty(ccEmailAddress)){
				Address[] ccAddresses=new Address[ccEmailAddress.size()];
				for(int i=0;i<ccEmailAddress.size();i++){
					ccAddresses[i]=new InternetAddress(ccEmailAddress.get(i));
				}
				msg.setRecipients(MimeMessage.RecipientType.CC, ccAddresses);
			}
			// 得到邮差对象
			transport = session.getTransport();
			// 连接自己的邮箱账户
			// connect(host, user, password)
			transport.connect(mailServer, fromEmailAddress, fromEmailPassword);
			// 发送邮件

			transport.sendMessage(msg, msg.getAllRecipients());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			try {
				if (transport!=null) {
					/*
					 * //将该邮件保存到本地 OutputStream out = new FileOutputStream("MyEmail.eml");
					 * msg.writeTo(out); out.flush(); out.close();
					 */
					transport.close();
				}
			} catch (MessagingException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}

	}

}
