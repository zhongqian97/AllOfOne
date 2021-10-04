/**
 * 
 */
package ink.mastermind.AllINOne.pojo;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.jaxrs.FastJsonAutoDiscoverable;

/**
 * @author joshua
 *
 */
@Document(collection = "message")

public class Message {
	@Id
	private String id;
	private String type;
	private String task;
	private String data;
	private Long time;
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
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
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
		this.time = new Long(new Date().getTime());
		this.sender = sender;
		this.addressee = addressee;
	}
	public Message(String id, String type, String task, String data, String sender, String addressee) {
		super();
		this.id = id;
		this.type = type;
		this.task = task;
		this.data = data;
		this.time = new Long(new Date().getTime());
		this.sender = sender;
		this.addressee = addressee;
	}
	
	public Message(String id, String type, String task, String data, Long time, String sender, String addressee) {
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
		// TODO Auto-generated constructor stub
	}
	
	
}
