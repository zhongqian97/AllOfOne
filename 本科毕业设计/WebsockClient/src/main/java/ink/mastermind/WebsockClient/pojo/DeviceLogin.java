package ink.mastermind.WebsockClient.pojo;

import com.alibaba.fastjson.JSON;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class DeviceLogin {
	private String id;
	private String password;
	private boolean first;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	
	public DeviceLogin(String id, String password, boolean first) {
		super();
		this.id = id;
		this.password = password;
		this.first = first;
	}
	public DeviceLogin() {
		super();
		// TODO Auto-generated constructor stub
	}
	public boolean isFirst() {
		return first;
	}
	public void setFirst(boolean first) {
		this.first = first;
	}
	public void register(ChannelHandlerContext ctx) {
		// TODO Auto-generated method stub
		if (first) {
			first = false;
			Message m = new Message("DeviceRegister", "", this.toString(), "", "");
			ctx.channel().writeAndFlush(new TextWebSocketFrame(m.toString()));
		}
	}
}
