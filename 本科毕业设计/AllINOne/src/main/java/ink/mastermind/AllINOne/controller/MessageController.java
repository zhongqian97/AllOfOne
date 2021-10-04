/**
 * 
 */
package ink.mastermind.AllINOne.controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ink.mastermind.AllINOne.pojo.Json;
import ink.mastermind.AllINOne.pojo.Message;
import ink.mastermind.AllINOne.pojo.User;
import ink.mastermind.AllINOne.pojo.UserGroup;
import ink.mastermind.AllINOne.pojo.UserRole;
import ink.mastermind.AllINOne.service.ChannelKeeper;
import ink.mastermind.AllINOne.service.MessageService;
import ink.mastermind.AllINOne.service.UserGroupService;
import ink.mastermind.AllINOne.service.UserService;

/**
 * @author joshua
 *
 */
@Controller
public class MessageController {
	
	@Autowired
	private MessageService messageService;
	
	@Autowired
	private ChannelKeeper channelKeeper;	
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserGroupService userGroupService;
	
	/**
	 * @param principal
	 * @return
	 * 获取通知信息
	 */
	@RequestMapping("/message/getNotice")
	@ResponseBody
	public List<Message> getNotice(Principal principal) {
		try {
			return this.messageService.getNotice(principal.getName());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param id
	 * @param name
	 * @param user
	 * @param principal
	 * @return
	 * 发送邀请频道信息
	 */
	@RequestMapping("/message/sendInviteChannel")
	@ResponseBody
	public Json sendInviteChannel(String id, String name, String user, Principal principal) {
		User u = this.userService.getUserByUsername(user);
		if (u == null) return Json.getJson().setAndPush(500, "邀请用户失败，用户不存在", null);
		if (principal.getName().equals(u.getOwnedUser())) {
			UserGroup userGroup = this.userGroupService.findByIdAndBuilder(id, principal.getName());
			if (userGroup != null) {
				UserRole userRole = new UserRole(UserRole.WRITER, UserRole.RECEIVE);
				this.channelKeeper.changeSendRoles(id, user, userRole);
				userGroup.getRoles().put(user, userRole);
				this.userGroupService.save(userGroup);
				return Json.getJson().setAndPush(200, "你已成功邀请用户" + user, null);
			}
			return Json.getJson().setAndPush(500, "邀请用户失败", null);
		} else {
			this.channelKeeper.sendNotice(user, "用户" + principal.getName()
			+ "邀请您加入频道，id为：" + id + ",频道名称为：" + name);
			return Json.getJson().setAndPush(200, "你已成功邀请用户" + user, null);
		}
	}
	
}
