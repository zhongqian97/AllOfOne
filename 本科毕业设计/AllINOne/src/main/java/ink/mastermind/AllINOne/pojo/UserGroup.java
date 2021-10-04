/**
 * 
 */
package ink.mastermind.AllINOne.pojo;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.alibaba.fastjson.JSON;

/**
 * @author joshua
 *
 */
@Document(collection = "channel")
public class UserGroup {
	@Id
	private String id;
	private String builder;
	private String name;
	private String task;
	private String channel;
	private String picture;
	@Field
	private ConcurrentHashMap<String, UserRole> roles;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBuilder() {
		return builder;
	}
	public void setBuilder(String builder) {
		this.builder = builder;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public ConcurrentHashMap<String, UserRole> getRoles() {
		return roles;
	}
	public void setRoles(ConcurrentHashMap<String, UserRole> roles) {
		this.roles = roles;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	public UserGroup() {
		super();
	}
	public UserGroup(String id, String builder, String name, String task, String channel, String picture,
			ConcurrentHashMap<String, UserRole> roles) {
		super();
		this.id = id;
		this.builder = builder;
		this.name = name;
		this.task = task;
		this.channel = channel;
		this.picture = picture;
		this.roles = roles;
	}
	public UserGroup(String builder, String name, String task, String channel, String picture,
			ConcurrentHashMap<String, UserRole> roles) {
		super();
		this.builder = builder;
		this.name = name;
		this.task = task;
		this.channel = channel;
		this.picture = picture;
		this.roles = roles;
	}
	
}
