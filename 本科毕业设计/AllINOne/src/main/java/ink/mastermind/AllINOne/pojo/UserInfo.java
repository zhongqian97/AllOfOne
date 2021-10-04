package ink.mastermind.AllINOne.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author joshua
 * 用户信息类
 */

@Document(collection = "userinfo")
public class UserInfo {
	@Id
	private String id;
	private String userName;//用户名
	private String name;//个人昵称
	private String sex;//性别
	private String email;//邮箱
	private String phone;//手机号码
	private String evaluate;//个性签名
	private String site;//个人网址
	private String picture;//照片，1MB
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEvaluate() {
		return evaluate;
	}
	public void setEvaluate(String evaluate) {
		this.evaluate = evaluate;
	}
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	@Override
	public String toString() {
		return "UserInfo [id=" + id + ", userName=" + userName + ", name=" + name + ", sex=" + sex + ", email=" + email
				+ ", phone=" + phone + ", evaluate=" + evaluate + ", site=" + site + ", picture=" + (picture != null) + "]";
	}
	public UserInfo() {
		super();
	}
	public UserInfo(String userName, String name, String sex, String email, String phone, String evaluate, String site,
			String picture) {
		super();
		this.userName = userName;
		this.name = name;
		this.sex = sex;
		this.email = email;
		this.phone = phone;
		this.evaluate = evaluate;
		this.site = site;
		this.picture = picture;
	}
	public UserInfo(UserInfo info) {
		super();
		this.userName = info.getUserName();
		this.name = info.getName();
		this.sex = info.getSex();
		this.email = info.getEmail();
		this.phone = info.getPhone();
		this.evaluate = info.getEvaluate();
		this.site = info.getSite();
		this.picture = info.getPicture();
	}
	
	
	
	public void update(UserInfo info) {
		this.userName = info.getUserName();
		this.name = info.getName();
		this.sex = info.getSex();
		this.email = info.getEmail();
		this.phone = info.getPhone();
		this.evaluate = info.getEvaluate();
		this.site = info.getSite();
		this.picture = info.getPicture();
	}
	
}
