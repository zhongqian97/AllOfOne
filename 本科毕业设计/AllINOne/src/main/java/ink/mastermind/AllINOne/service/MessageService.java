/**
 * 
 */
package ink.mastermind.AllINOne.service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import ink.mastermind.AllINOne.dao.MessageDao;
import ink.mastermind.AllINOne.dao.UserDao;
import ink.mastermind.AllINOne.dao.UserGroupDao;
import ink.mastermind.AllINOne.dao.UserInfoDao;
import ink.mastermind.AllINOne.pojo.Message;
import ink.mastermind.AllINOne.pojo.User;
import ink.mastermind.AllINOne.pojo.UserGroup;
import ink.mastermind.AllINOne.pojo.UserInfo;
import ink.mastermind.AllINOne.pojo.UserRole;
import ink.mastermind.AllINOne.utils.EMail;

/**
 * @author joshua
 *
 */
@Service("messageService")
public class MessageService {
	
	@Autowired
	private MessageDao messageDao;
	@Autowired
	private UserGroupDao userGroupDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private UserInfoDao userInfoDao;
	@Autowired
	private ChannelKeeper channelKeeper;
	
	/**
	 * 定时任务，离线发送信息
	 */
	@Scheduled(cron = "0 05 19 ? * *")
	private void OfflineTaskToSendMessage() {
		//查找全部用户与用户组
		List<User> users = this.userDao.findAll();
		List<UserGroup> userGroups = this.userGroupDao.findAll();
		UserInfo userInfo = null;
		for (User user : users) {
			//如果用户时间不存在或者还在线你就别发离线通知了！
			
			if (user.getTime() == null || channelKeeper.exists(user.getUserName())) continue;
			//邮箱不存在，发个锤子
			//System.out.println(user);
			userInfo = this.userInfoDao.findByUserName(user.getUserName());
			if(userInfo == null || userInfo.getEmail() == null 
					|| "".equals(userInfo.getEmail())) continue;
			for (UserGroup userGroup : userGroups) {
				//System.out.println("进入用户组");
				//该频道是否有该用户
				if (userGroup.getRoles().containsKey(user.getUserName()) 
						&& userGroup.getRoles().get(user.getUserName()).getSend().equals(UserRole.PUSH) == false
						&& userGroup.getRoles().get(user.getUserName()).getReceive().equals(UserRole.IMPORTANT) == true) {
					//System.out.println("收集资料");
					List<Message> list = this.messageDao.findByAddresseeAndTimeGreaterThan(
							userGroup.getId(), new Long(user.getTime()));
					//System.out.println(list.size());
					list.addAll(this.messageDao.findByAddresseeAndTimeGreaterThan(
							"All", new Long(user.getTime())));
					//System.out.println(list.size());
					if (list == null || list.isEmpty()) continue;
					//System.out.println("开始发送");
					//发送离线通知！
					EMail.sendList(userInfo, list);
				}
			}
		}
	}
	
	/**
	 * @param id
	 * @param name
	 * @return
	 */
	public List<Message> getMessageByChannel(String id, String name) {
		Optional<UserGroup> l = this.userGroupDao.findById(id);
		List<Message> m = null;
		if (l != null && l.get() != null) {
			if (l.get().getRoles().containsKey(name) && !l.get().getRoles().get(name).getSend().equals(UserRole.PUSH)) {
				m = this.messageDao.findByAddressee(id);
			}
		}
		return m;
	}

	/**
	 * @param userId
	 * @param message
	 * 保存离线信息
	 */
	public void addOfflineMessage(Message message) {
		ObjectId id = new ObjectId();
		message.setId(id.toString());
		this.messageDao.save(message);
	}

	/**
	 * @param addressee
	 * @return
	 * 获取信息通过频道地址
	 */
	public List<Message> getMessageByUserid(String addressee) {
		return this.messageDao.findByAddressee(addressee);
	}

	/**
	 * @param json
	 */
	public void addOfflineMessage(JSONObject json) {
		Message m = new Message((String)json.getString("type"), 
				(String)json.getString("task"),
				(String)json.getString("data"),
				(String)json.getString("sender"),
				(String)json.getString("addressee"));
		this.addOfflineMessage(m);
	}

	/**
	 * @param name
	 * @return
	 */
	public List<Message> getNotice(String name) {
		List<Message> list = new LinkedList<Message>();
		list.addAll(this.getMessageByUserid(name));
		list.addAll(this.getMessageByUserid("All"));
		return list;
	}

	/**
	 * @param m
	 */
	public void deleteMessage(Message m) {
		// TODO Auto-generated method stub
		this.messageDao.delete(m);
	}
	
}
