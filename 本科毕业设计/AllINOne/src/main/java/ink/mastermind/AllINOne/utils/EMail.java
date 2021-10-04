/**
 * 
 */
package ink.mastermind.AllINOne.utils;

import org.apache.commons.validator.EmailValidator;

import com.alibaba.fastjson.JSONObject;

import ink.mastermind.AllINOne.pojo.Message;
import ink.mastermind.AllINOne.pojo.UserInfo;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 * @author joshua
 *
 */
public class EMail {
	/**
	 * @param args
	 */

	private static String from = "XXX@XXX.XXX";// 发件人电子邮箱
	private static Session session;
	static {
		// 获取系统属性
		Properties properties = System.getProperties();
		// 设置邮件服务器
		properties.setProperty("mail.transport.protocol", "smtp");
		properties.setProperty("mail.smtp.host", "smtp.mastermind.ink");
		properties.setProperty("mail.smtp.auth", "true");
		// 获取默认session对象
		session = Session.getDefaultInstance(properties, new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("XXX@XXX.XXX", "mima"); // 发件人邮件用户名、授权码
			}
		});
	}

	public static boolean send(String addressee, String title, String text) {
		if (EmailValidator.getInstance().isValid(addressee) == false)
			return false;
		try {
			// 创建默认的 MimeMessage 对象
			MimeMessage message = new MimeMessage(session);
			// Set From: 头部头字段
			message.setFrom(new InternetAddress(from));
			// Set To: 头部头字段
			message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(addressee));
			// Set Subject: 头部头字段
			message.setSubject(title);
			// 设置消息体
			message.setText(text);
			// 发送消息
			Transport.send(message);
			System.out.println("Sent message successfully....");
			return true;
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
		return false;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		send("root@Mastermind.ink", "咕咕咕咕咕哦", "来自无敌的Java客户端");
	}

	/**
	 * @param userInfo
	 * @param list
	 */
	public static void sendList(UserInfo userInfo, List<Message> list) {
		// TODO Auto-generated method stub
		StringBuffer msg = new StringBuffer("亲爱的" + userInfo.getName() + ":\n\n");
		for (Message m : list) {
			changeMessageToMail(m, msg);
		}
		System.out.println(msg);
		send(userInfo.getEmail(), "AllINOne离线通知信息", msg.toString());
	}

	/**
	 * @param m
	 * @param msg 
	 * @return
	 */
	private static void changeMessageToMail(Message m, StringBuffer msg) {
		//频道名称
		msg.append("频道id：" + m.getAddressee() + "\n");
		//消息类型
		msg.append("消息类型：" + m.getType() + "\n");
		//消息时间
		msg.append("消息时间：" + new Date(m.getTime()).toString() + "\n");
		//消息发送者
		msg.append("发送人员：" + m.getSender() + "\n");
		//消息内容
		msg.append("消息内容：\n" + m.getData() + "\n");
		//每一个消息后间隔符号
		msg.append("\n\n\n");
	}

	/**
	 * @param json
	 * @param userInfo 
	 */
	public static void sendMessage(JSONObject json, UserInfo userInfo) {
		StringBuffer msg = new StringBuffer("亲爱的" + userInfo.getName() + ":\n\n");
		Message m = new Message(json.getString("type"),
				json.getString("task"),
				json.getString("data"),
				json.getString("sender"),
				json.getString("addressee"));
		changeMessageToMail(m, msg);
		System.out.println(msg);
		if (userInfo != null)
			send(userInfo.getEmail(), "AllINOne紧急通知信息", msg.toString());
	}

}
