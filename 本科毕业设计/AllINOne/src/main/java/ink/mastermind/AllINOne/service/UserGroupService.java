/**
 * 
 */
package ink.mastermind.AllINOne.service;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import ink.mastermind.AllINOne.dao.UserGroupDao;
import ink.mastermind.AllINOne.pojo.Message;
import ink.mastermind.AllINOne.pojo.UserGroup;
import ink.mastermind.AllINOne.pojo.UserRole;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.ImmediateEventExecutor;

/**
 * @author joshua
 *
 */
@Service("userGroupService")
public class UserGroupService {
	
	@Autowired
	private ChannelKeeper channelKeeper;
	@Autowired
	private UserGroupDao userGroupDao;
	@Autowired
	private MessageService messageService;
	/**
	 * @param userGroup
	 * @return
	 * 修改频道的任务名称与频道名称
	 */
	public boolean changeChannel(UserGroup userGroup) {
		// TODO Auto-generated method stub
		UserGroup u = this.userGroupDao.findByIdAndBuilder(userGroup.getId(), userGroup.getBuilder());
		if (u != null) {
			u.setName(userGroup.getName());
			u.setTask(userGroup.getTask());
			u.setPicture(userGroup.getPicture());
			this.userGroupDao.save(u);
			return true;
		}
		return false;
	}

	/**
	 * @param name
	 * @return 
	 * 
	 */
	public List<UserGroup> getChannel(String name) {
		// TODO Auto-generated method stub
		return this.userGroupDao.findByBuilder(name);
	}

	/**
	 * @param userGroup
	 * @return
	 * 找频道，通过id name
	 */
	public UserGroup findChannelByIdAndName(UserGroup userGroup) {
		// TODO Auto-generated method stub
		return this.userGroupDao.findByIdAndName(userGroup.getId(), userGroup.getName());
	}

	/**
	 * @param userGroup
	 * @param name
	 * 加入频道
	 */
	public void joinChannel(UserGroup userGroup, String name) {
		userGroup.getRoles().put(name, new UserRole(UserRole.PUSH, UserRole.RECEIVE));
		channelKeeper.sendNotice(userGroup.getBuilder(), 
				"您管理的频道" + userGroup.getName() + "，有用户" + name + "加入群组，请您处理一下");
		this.userGroupDao.save(userGroup);
	}

	/**
	 * @param userGroup
	 * @param name
	 * 添加频道
	 */
	public void addChannel(UserGroup userGroup, String name) {
		// TODO Auto-generated method stub
		userGroup.setBuilder(name);
		userGroup.setRoles(new ConcurrentHashMap<String, UserRole>());
		userGroup.getRoles().put(name, new UserRole(UserRole.WRITER, UserRole.RECEIVE));
		this.userGroupDao.save(userGroup);
	}

	/**
	 * @param id
	 * @return
	 */
	public Optional<UserGroup> findChannelById(String id) {
		// TODO Auto-generated method stub
		return this.userGroupDao.findById(id);
	}

	/**
	 * @param id
	 * @param name
	 * @return
	 */
	public UserGroup findByIdAndBuilder(String id, String builder) {
		// TODO Auto-generated method stub
		return this.userGroupDao.findByIdAndBuilder(id, builder);
	}

	/**
	 * @param userGroup
	 */
	public void updateAll(UserGroup userGroup) {
		// TODO Auto-generated method stub
		this.userGroupDao.save(userGroup);
	}

	/**
	 * @param id
	 * @param name
	 * @return
	 * 删除频道
	 */
	public synchronized boolean deleteChannel(String id, String name) {
		// TODO Auto-generated method stub
		UserGroup userGroup = this.userGroupDao.findByIdAndBuilder(id, name);
		if (userGroup != null) {
			List<Message> list = this.messageService.getMessageByUserid(userGroup.getId());
			for (Message m : list) {
				this.messageService.deleteMessage(m);
			}
			this.userGroupDao.delete(userGroup);
			this.channelKeeper.removeUserGroup(userGroup);
			return true;
		}
		return false;
	}

	/**
	 * @param name
	 * @return
	 */
	public List<UserGroup> getChannelMessageList(String name) {
		// TODO Auto-generated method stub
		List<UserGroup> result = new LinkedList<UserGroup>();
		List<UserGroup> l = this.userGroupDao.findAll();
		for (UserGroup u : l) {
			// push人员无法搞事
			if (u.getRoles().containsKey(name) && u.getRoles().get(name).getSend().equals(UserRole.PUSH) == false) {
				result.add(u);
			}
		}
		return result;
	}

	/**
	 * @param addressee
	 * @return
	 * 激活频道
	 */
	public ChannelGroup activeChannelGroup(String addressee) {
		// TODO Auto-generated method stub
		ChannelGroup group = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
		Optional<UserGroup> list = this.userGroupDao.findById(addressee);
		if (list == null || list.get() == null) return null;
		UserGroup userGroup = list.get();
		for (String s : userGroup.getRoles().keySet()) {
			if (userGroup.getRoles().get(s).getSend().equals(UserRole.PUSH) == false //设置发送权限
					&& userGroup.getRoles().get(s).getReceive().equals(UserRole.REJECT) == false
					&& channelKeeper.findChannelByUserid(s) != null) {
				group.add(channelKeeper.findChannelByUserid(s));
			}
			if (userGroup.getRoles().get(s).getSend().equals(UserRole.PUSH) == false)
				this.channelKeeper.setUserRole(s, userGroup.getId(), userGroup.getRoles().get(s));
		}
		channelKeeper.addChannelGroup(addressee, group);
		return group;
	}

	/**
	 * @param userGroup
	 */
	public void save(UserGroup userGroup) {
		// TODO Auto-generated method stub
		this.userGroupDao.save(userGroup);
		System.out.println("保存的内容了！");
	}

	/**
	 * @param id 用户id
	 */
	public synchronized void deleteUser(String id) {
		List<UserGroup> groups = this.userGroupDao.findAll();
		for (UserGroup u : groups) {
			if (u.getRoles().containsKey(id) == false) continue;
			u.getRoles().remove(id);
			this.channelKeeper.removeChannel(id);
			save(u);
		}
	}
}
