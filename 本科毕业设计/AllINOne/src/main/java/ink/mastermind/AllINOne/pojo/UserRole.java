/**
 * 
 */
package ink.mastermind.AllINOne.pojo;

import java.lang.*;

import com.alibaba.fastjson.JSON;

/**
 * @author joshua
 *
 */
public class UserRole {
	//发送专用权限
	public static final String PUSH = "push"; //等待加入频道
	public static final String READER = "reader"; //读者权限，无写入权限
	public static final String WRITER = "writer"; //可读可写权限
	public static final String POP = "pop";  //已踢出频道
	//接收专用权限
	public static final String RECEIVE = "receive"; //可以接收信息
	public static final String REJECT = "reject"; //拒绝接收信息
	public static final String IMPORTANT = "important"; //每天离线发送信息
	public static final String URGENT = "urgent"; //紧急信息，立刻发送
	
	private String send;//发送权限
	private String receive;//接收权限
	
	
	public String getReceive() {
		return receive;
	}
	public void setReceive(String receive) {
		this.receive = receive;
	}
	public String getSend() {
		return send;
	}
	public void setSend(String send) {
		this.send = send;
	}
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	
	public UserRole(String send, String receive) {
		super();
		this.send = send;
		this.receive = receive;
	}
	public UserRole() {
		super();
	}
	
}
