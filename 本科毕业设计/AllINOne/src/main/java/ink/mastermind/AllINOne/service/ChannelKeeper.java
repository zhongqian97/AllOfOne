/**
 * 
 */
package ink.mastermind.AllINOne.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import ink.mastermind.AllINOne.pojo.DeviceLogin;
import ink.mastermind.AllINOne.pojo.Message;
import ink.mastermind.AllINOne.pojo.Task;
import ink.mastermind.AllINOne.pojo.User;
import ink.mastermind.AllINOne.pojo.UserGroup;
import ink.mastermind.AllINOne.pojo.UserInfo;
import ink.mastermind.AllINOne.pojo.UserRole;
import ink.mastermind.AllINOne.utils.EMail;
import ink.mastermind.AllINOne.utils.MinioClientUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author joshua
 *
 */
@Service("channelKeeper")
public class ChannelKeeper {
	
	@Autowired
    private UserGroupService userGroupService;
	@Autowired
    private UserService userService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private TaskService taskService;
	
	private final ChannelGroup group = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
	
	//用户id对应Channelid
	private final ConcurrentMap<String, ChannelId> channelMap = new ConcurrentHashMap<String, ChannelId>();
	private final ConcurrentMap<String, String> userMap = new ConcurrentHashMap<String, String>();
	
	//频道id对应User
	private final ConcurrentMap<String, String> channelUserMap = new ConcurrentHashMap<String, String>();
	
	//用户组
	private final ConcurrentMap<String, ChannelGroup> userChannelGroup = new ConcurrentHashMap<String, ChannelGroup>();
	private final ConcurrentMap<String, UserRole> userRole = new ConcurrentHashMap<String, UserRole>();

	/**
	 * @param channelId
	 * @param userId
	 * 将频道与用户id绑定
	 */
	public synchronized boolean addChannel(String channelId, String userId) {
		Channel channel = findChannelByChannelid(channelId);
		if (channel != null) {
			//绑定用户与频道
			userMap.put(userId, channelId);
			channelUserMap.put(channelId, userId);
			
			//获取信息,并加入频道
			List<UserGroup> result = this.userGroupService.getChannelMessageList(userId);
			for (UserGroup u : result) {
				UserRole role = u.getRoles().get(userId);
				setUserRole(userId, u.getId(), role);
				if (role.getReceive().equals(UserRole.REJECT) == false) 
					joinChannel(userId, u.getId());
			}

			System.out.println("ChannelKeeper addChannel");
	        return true;
		}
		return false;
	}
	
	/**
	 * @param userId
	 */
	public void removeChannel(String userId) {
		if (userMap.containsKey(userId) == false) return;
		Channel channel = findChannelByUserid(userId);
		for (String s : userChannelGroup.keySet()) {
			if (userChannelGroup.get(s).contains(channel)) {
				userChannelGroup.get(s).remove(channel);
				if (userRole.containsKey(getUserRolesKey(userId, s))) {
					userRole.remove(getUserRolesKey(userId, s));
				}
			}
		}
		if (channelUserMap.containsKey(channel.id().asShortText()))
			channelUserMap.remove(channel.id().asShortText());
		if (this.group.contains(channel))
			this.group.remove(channel);
		if (channelMap.containsKey(userId))
			channelMap.remove(userId);
		if (userMap.containsKey(userId))
			userMap.remove(userId);
	}

	/**
	 * @param channelId
	 * @return
	 */
	public Channel findChannelByChannelid(String channelId) {
		if (channelMap.containsKey(channelId))
			return this.group.find(channelMap.get(channelId));
		return null;
	}
	
	/**
	 * @param userId
	 * @return
	 */
	public Channel findChannelByUserid(String userId) {
		if (userMap.containsKey(userId))
			return this.group.find(channelMap.get(userMap.get(userId)));
		return null;
	}

	/**
	 * @param tws
	 */
	public void sendToAll(TextWebSocketFrame tws) {
		this.group.writeAndFlush(tws);
	}

	public ConcurrentMap<String, ChannelId> getChannelMap() {
		return channelMap;
	}

	public ConcurrentMap<String, String> getUserMap() {
		return userMap;
	}

	public ConcurrentMap<String, ChannelGroup> getUserChannelGroup() {
		return userChannelGroup;
	}

	public ChannelGroup getGroup() {
		return group;
	}
	
	
	/**
	 * 
	 */
	public void close() {
		this.group.close();
		this.userChannelGroup.clear();
		this.userMap.clear();
		this.channelMap.clear();
	}

//	public ConcurrentMap<String, String> getUserRole() {
//		return userRole;
//	}

	/**
	 * @param addressee
	 * @param group2
	 */
	public void addChannelGroup(String addressee, ChannelGroup group2) {
		this.userChannelGroup.put(addressee, group2);
	}

	/**
	 * @param channel
	 */
	public void removeChannel(Channel channel) {
		this.group.remove(channel);
		this.channelMap.remove(channel.id().asShortText());
		//判断用户是否注册过，没注册过就不用删除注册了
		if (this.channelUserMap.containsKey(channel.id().asShortText())) {
			//注销时间添加到用户中
			String userId = this.channelUserMap.get(channel.id().asShortText());
			User user = this.userService.findUserByUserName(userId);
			user.setTime(new Long(new Date().getTime()));
			this.userService.save(user);
			
			//删除信息
			this.userMap.remove(this.channelUserMap.get(channel.id().asShortText()));
			this.channelUserMap.remove(channel.id().asShortText());
			for (Entry<String, ChannelGroup> e : userChannelGroup.entrySet()) {
				if (e.getValue().contains(channel)) {
					e.getValue().remove(channel);
				}
			}
		}
	}

	/**
	 * @param ctx
	 * @param cause
	 * 服务器错误日志
	 */
	public void channelLog(ChannelHandlerContext ctx, Throwable cause) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param ctx
	 * @param msg
	 * 信息处理
	 */
	public void handleInformation(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
		JSONObject json = JSON.parseObject(msg.text()); 
		if (json != null) { //转换对象成功
			if (json.getString("addressee") == null || "".equals(json.getString("addressee")) ||
					json.getString("sender") == null || "".equals(json.getString("sender"))) {
				if ("DeviceRegister".equals(json.getString("type"))) {
					JSONObject login = JSON.parseObject(json.getString("data"));
					if (this.userService.checkPassword(login.getString("id"), login.getString("password"))) {
						addChannel(ctx.channel().id().asShortText(), login.getString("id"));
						json.put("data", JSONObject.toJSONString("登录成功！"));
					} else {
						json.put("data", JSONObject.toJSONString("账号或密码不正确！"));
					}
					ctx.channel().writeAndFlush(new TextWebSocketFrame(json.toJSONString()));
					return;
				} else if ("ChannelList".equals(json.getString("type"))) {
					if (!this.channelUserMap.containsKey(ctx.channel().id().asShortText())) return;
					String userId = this.channelUserMap.get(ctx.channel().id().asShortText());
					List<UserGroup> result = this.userGroupService.getChannelMessageList(userId);
					json.put("data", JSONObject.toJSONString(result));
				} else if ("ChannelMessage".equals(json.getString("type"))) {
//					TODO: 设置问题
					if (!this.channelUserMap.containsKey(ctx.channel().id().asShortText())) return;
					List<Message> result = this.messageService.getMessageByUserid(json.getString("addressee"));
					json.put("data", JSONObject.toJSONString(result));
				} else if ("loadInformation".equals(json.getString("type"))) {
					if (!this.channelUserMap.containsKey(ctx.channel().id().asShortText())) return;
					UserInfo result = this.userService.findUserInfoByUserName(json.getString("data"));
					json.put("task", (String)json.getString("data"));
					json.put("data", (result != null) ? JSONObject.toJSONString(result) : "");
				} else if ("DownloadTask".equals(json.getString("type"))) {
					//发送文件！
					if (!this.channelUserMap.containsKey(ctx.channel().id().asShortText())) return;
					Task task = this.taskService.findById(json.getString("task"));
					if (task == null) return;
					ctx.channel().writeAndFlush(new BinaryWebSocketFrame(
							Unpooled.copiedBuffer(
									MinioClientUtils.getInstance().downloadFile("task", task.getPath()))));//这个task是对象的桶名称！
					return;
				}
				ctx.channel().writeAndFlush(new TextWebSocketFrame(json.toJSONString()));
				return;
			}
			//非法闯入
			if (!this.channelUserMap.containsKey(ctx.channel().id().asShortText())) return;
			//设置时间
			json.put("time", new Long(new Date().getTime()).toString());
			json.put("sender", this.channelUserMap.get(ctx.channel().id().asShortText()));
			
			//权限管理
			String roleKey = null;
			if (!json.getString("sender").equals("") && !json.getString("sender").equals(""))
				roleKey = json.getString("sender") + ':' + json.getString("addressee");
			System.out.println(this.userRole.containsKey(roleKey));
			if (roleKey == null || this.userRole.containsKey(roleKey) == false 
					|| this.userRole.get(roleKey).getSend().equals(UserRole.PUSH)
					|| this.userRole.get(roleKey).getSend().equals(UserRole.POP)
					|| this.userRole.get(roleKey).getSend().equals(UserRole.READER)) return;
			
			//转发信息
			if (userChannelGroup.containsKey(json.getString("addressee"))) {
				//在线用户组中是否含有该值，有即为已经激活的频道，没有就重新激活
				userChannelGroup.get(json.getString("addressee")).writeAndFlush(new TextWebSocketFrame(json.toJSONString()));
			} else {
				ChannelGroup group = userGroupService.activeChannelGroup(json.getString("addressee"));
				if (group == null) return;
				group.writeAndFlush(new TextWebSocketFrame(json.toJSONString()));
			}
			//离线信息
			this.messageService.addOfflineMessage(json);
			this.sendOfflineMessage(json);
			System.out.println(json);
		}
	}

	/**
	 * @param json
	 */
	private void sendOfflineMessage(JSONObject json) {
		try {
			String id = json.getString("addressee");
			UserGroup userGroup = this.userGroupService.findChannelById(id).get();
			for (String userId : userGroup.getRoles().keySet()) {
				//在线的不管
				if (findChannelByUserid(userId) != null 
						|| UserRole.PUSH.equals(userGroup.getRoles().get(userId).getSend())) continue;
				System.out.println(userId);
				if (UserRole.URGENT.equals(
						userRole.get(getUserRolesKey(userId, id)).getReceive())) {
					EMail.sendMessage(json, this.userService.findUserInfoByUserName(userId));
				}
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	/**
	 * @param ctx
	 * @param evt
	 * 首次连接，在handler userEventTriggered 专用
	 */
	public void firstChannel(ChannelHandlerContext ctx, Object evt) {
		group.add(ctx.channel());
        Message m = new Message("ChannelRegister", null, ctx.channel().id().asShortText(), null, null);
		channelMap.put(ctx.channel().id().asShortText(), ctx.channel().id());
        ctx.channel().writeAndFlush(new TextWebSocketFrame(m.toString()));
	}

	/**
	 * @param user 用户id
	 * @param id 用户频道群组id
	 * @param role
	 */
	public void setUserRole(String user, String id, UserRole role) {
		//System.out.println(user + ':' + id + '_' + role);
		if (UserRole.POP.equals(role.getSend())) {
			if (this.userRole.containsKey(getUserRolesKey(user, id)))
				this.userRole.remove(getUserRolesKey(user, id));
			return;
		}
		this.userRole.put(getUserRolesKey(user, id), role);
	}
	
	

	/**
	 * @param user
	 * @param id
	 * @return
	 */
	private String getUserRolesKey(String user, String id) {
		return user + ':' + id;
	}

	/**
	 * @param user 用户id
	 * @param id 频道id
	 * 中途对单个频道添加用户
	 */
	public void joinChannel(String user, String id) {
		if(this.userChannelGroup.containsKey(id) && this.userMap.containsKey(user)) {
			this.userChannelGroup.get(id).add(this.findChannelByUserid(user));
		}
	}

	/**
	 * @param userId 你要发送给谁通知
	 * @param message 发送信息内容
	 * 发送通信信息
	 */
	public void sendNotice(String userId, String message) {
        Message msg = new Message("Notice", null, message, null, userId);
		this.messageService.addOfflineMessage(msg);
		if ("All".equals(userId)) { //发送给全部人
			this.sendToAll(new TextWebSocketFrame(msg.toString()));
		} else if (this.userMap.containsKey(userId)) {
			this.findChannelByUserid(userId).writeAndFlush(new TextWebSocketFrame(msg.toString()));
		}
	}
	
	/**
	 * @param userId 你要发送给谁通知
	 * @param message 发送信息内容
	 * 发送通信信息
	 */
	public void sendNotice(String userId, Message msg) {
		if (this.userMap.containsKey(userId)) {
			this.findChannelByUserid(userId).writeAndFlush(new TextWebSocketFrame(msg.toString()));
		}
		msg.setTask("");
		this.messageService.addOfflineMessage(msg);    
	}

	/**
	 * @param id 频道ID
	 * @param user 用户id
	 * @param before 之前的权限
	 * @param role 现在的权限
	 * @param channelName 频道的名字
	 * 发送频道里用户权限时踢出频道或者加入频道时需要给用户知晓
	 */
	public void sendChannelChangeNotice(String id, String user, 
				String before, String role, String channelName) {
		// TODO Auto-generated method stub
		Message msg = null;
		if (UserRole.PUSH.equals(before) && !UserRole.POP.equals(role)) {
			Optional<UserGroup> data = this.userGroupService.findChannelById(id);
			msg = new Message("ChannelAdd", data.get().toString(), channelName + "频道管理员允许您加入该频道", "", user);
		}else if (UserRole.POP.equals(role) && !UserRole.PUSH.equals(before)) {
			msg = new Message("ChannelDel",  id, channelName + "频道管理员已把您请出", "", user);
		}else if (UserRole.WRITER.equals(role) && UserRole.READER.equals(before)) {
			msg = new Message("Notice",  id, channelName + "频道管理员已把您的权限提升了", "", user);
		}else if (UserRole.WRITER.equals(before) && UserRole.READER.equals(role)) {
			msg = new Message("Notice",  id, channelName + "频道管理员已把您的权限降低了", "", user);
		}
		if (msg != null) this.sendNotice(user, msg);
	}

	/**
	 * @param userName
	 * @return
	 */
	public boolean exists(String userName) {
		// TODO Auto-generated method stub
		return this.userMap.containsKey(userName);
	}

	/**
	 * @param userGroup
	 */
	public void removeUserGroup(UserGroup userGroup) {
		if (this.userChannelGroup.containsKey(userGroup.getId())) {
			// 这里删除时必须删除权限userRole中群组里数据。不然就是一个内存泄漏 
			System.out.println("清空内存前");
			for (String s : userRole.keySet()) {
				System.out.println(s);
			}
			String pattern = ":" + userGroup.getId();
			for (String content : this.userRole.keySet()) {
				if (((content.length() - pattern.length() > 0 ) 
		    		  && content.substring(content.length() - pattern.length(), content.length()).equals(pattern)))
					this.userRole.remove(content);
			}
			System.out.println("清空内存后");
			for (String s : userRole.keySet()) {
				System.out.println(s);
			}
			this.userChannelGroup.remove(userGroup.getId());
		}
	}
	
	/**
	 * @param id 频道id
	 * @param user 用户id
	 * @param role 权限
	 * 修改现有频道中权限的功能
	 */
	public void changeSendRoles(String id, String user, UserRole role) {
		// TODO Auto-generated method stub
		if (userChannelGroup.containsKey(id)) {
			Channel channel = findChannelByUserid(user);
			ChannelGroup group = userChannelGroup.get(id);
			if (UserRole.POP.equals(role.getSend())) {
				group.remove(channel);
			} else {
				if (group.contains(channel) == false) { //中途修改权限时进来的用户
					joinChannel(user, id);
				}
			}
			setUserRole(user, id, role);
		}
	}

	/**
	 * @param id
	 * @param name
	 * @param role
	 */
	public void changReceiveRoles(String id, String user, UserRole role) {
		if (userChannelGroup.containsKey(id) == false) return;
		ChannelGroup group = userChannelGroup.get(id);
		Channel channel = findChannelByUserid(user);
		userRole.put(getUserRolesKey(user, id), role);
		if (UserRole.REJECT.equals(role.getReceive())) {
			if (group.contains(channel)) {
				group.remove(channel);
			}
			return;
		}
		if (group.contains(channel) == false) {
			group.add(channel);
		}
	}

}