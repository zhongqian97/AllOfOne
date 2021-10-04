package ink.mastermind.WebsockClient.pojo;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.jaxrs.FastJsonAutoDiscoverable;

/**
 * @author joshua
 *
 */

public class Message {
	private String id;
	private String type;
	private String task;
	private String data;
	private String time;
	private String sender;
	private String addressee;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getAddressee() {
		return addressee;
	}
	public void setAddressee(String addressee) {
		this.addressee = addressee;
	}
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	public Message(String type, String task, String data, String sender, String addressee) {
		super();
		this.type = type;
		this.task = task;
		this.data = data;
		this.time = new Long(new Date().getTime()).toString();
		this.sender = sender;
		this.addressee = addressee;
	}
	public Message(String id, String type, String task, String data, String sender, String addressee) {
		super();
		this.id = id;
		this.type = type;
		this.task = task;
		this.data = data;
		this.time = new Long(new Date().getTime()).toString();
		this.sender = sender;
		this.addressee = addressee;
	}
	
	public Message(String id, String type, String task, String data, String time, String sender, String addressee) {
		super();
		this.id = id;
		this.type = type;
		this.task = task;
		this.data = data;
		this.time = time;
		this.sender = sender;
		this.addressee = addressee;
	}
	public Message() {
		super();
	}
	
	public Message(Message message) {
		this.id = message.getId();
		this.type = message.getType();
		this.task = message.getTask();
		this.data = message.getData();
		this.time = message.getTime();
		this.sender = message.getSender();
		this.addressee = message.getAddressee();
	}
}
