package ink.mastermind.AllINOne.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.alibaba.fastjson.JSON;

import ink.mastermind.AllINOne.security.MyPasswordEncoder;

@Document(collection = "user")
public class User {
	@Id
	private String id;
	private String userName;//用户名
	private String password;//密码
	private String role;//权限
	private Long time;//注销时间
	private String ownedUser;//所属用户
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = MyPasswordEncoder.m.encode(password);
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getOwnedUser() {
		return ownedUser;
	}
	public void setOwnedUser(String ownedUser) {
		this.ownedUser = ownedUser;
	}
	public User() {
		super();
	}
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public User(String userName, String password, String role, Long time, String ownedUser) {
		super();
		this.userName = userName;
		this.password = MyPasswordEncoder.m.encode(password);
		this.role = role;
		this.time = time;
		this.ownedUser = ownedUser;
	}
	public User(String id, String userName, String password, String role, Long time, String ownedUser) {
		super();
		this.id = id;
		this.userName = userName;
		this.password = MyPasswordEncoder.m.encode(password);
		this.role = role;
		this.time = time;
		this.ownedUser = ownedUser;
	}
}
