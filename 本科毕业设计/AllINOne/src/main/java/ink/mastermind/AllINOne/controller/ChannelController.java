/**
 * 
 */
package ink.mastermind.AllINOne.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ink.mastermind.AllINOne.pojo.Json;
import ink.mastermind.AllINOne.pojo.User;
import ink.mastermind.AllINOne.pojo.UserGroup;
import ink.mastermind.AllINOne.pojo.UserInfo;
import ink.mastermind.AllINOne.pojo.UserRole;
import ink.mastermind.AllINOne.service.ChannelKeeper;
import ink.mastermind.AllINOne.service.TaskService;
import ink.mastermind.AllINOne.service.UserGroupService;

/**
 * @author joshua
 *
 */
@Controller
public class ChannelController {
	
	@Autowired
	private TaskService taskService;
	@Autowired
	private UserGroupService userGroupService;
	@Autowired
	private ChannelKeeper channelKeeper;
	/**
	 * @param channelId
	 * @param principal
	 * @return
	 * 登录进websocket时注册使用
	 */
	@RequestMapping("/channel/register")
	@ResponseBody
	public Json register(String channelId, Principal principal) {
		System.out.println(principal.getName());
		if (channelKeeper.addChannel(channelId, principal.getName())) 
			return Json.getJson().setAndPush(200, "你已成功上线！", null);
		else 
			return Json.getJson().setAndPush(500, "你无法上线，请重启页面！", null);
	}
	
	/**
	 * @param userGroup
	 * @param principal
	 * @return
	 * 频道基本信息添加或修改
	 */
	@RequestMapping("/channel/updateChannel")
	@ResponseBody
	public Json updateChannel(@RequestBody UserGroup userGroup, Principal principal) {
		if (userGroup == null)
			return Json.getJson().setAndPush(500, "上传频道信息为空！", null);
		String taskName = this.taskService.getTaskName(userGroup.getTask());
		if (taskName == null)
			return Json.getJson().setAndPush(500, "任务id不存在！", null);
		try {
			if (userGroup.getId() == null || userGroup.getId().equals("")) {
				this.userGroupService.addChannel(userGroup, principal.getName());
				this.channelKeeper.sendChannelChangeNotice(userGroup.getId(), principal.getName(), 
						UserRole.PUSH, UserRole.WRITER, userGroup.getName());
				return Json.getJson().setAndPush(200, "添加成功，任务名称为" + taskName, null);
			} else {
				userGroup.setBuilder(principal.getName());
				if (this.userGroupService.changeChannel(userGroup))
					return Json.getJson().setAndPush(200, "更新成功，任务名称为" + taskName, null);
				return Json.getJson().setAndPush(500, "更新失败", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param principal
	 * @return
	 * 获取用户的频道列表
	 */
	@RequestMapping("/channel/getChannel")
	@ResponseBody
	public List<UserGroup> getChannel(Principal principal) {
		try {
			return this.userGroupService.getChannel(principal.getName());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param userGroup
	 * @param principal
	 * @return
	 * 删除频道功能
	 */
	@RequestMapping("/channel/deleteChannel")
	@ResponseBody
	public Json deleteChannel(String id, Principal principal) {
		try {
			if (this.userGroupService.deleteChannel(id, principal.getName()))
				return Json.getJson().setAndPush(200, "删除成功", null);
			return Json.getJson().setAndPush(500, "删除失败", null);
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param userGroup
	 * @param principal
	 * @return
	 * 加入频道功能
	 */
	@RequestMapping("/channel/joinChannel")
	@ResponseBody
	public Json joinChannel(@RequestBody UserGroup userGroup, Principal principal) {
		try {
			userGroup = this.userGroupService.findChannelByIdAndName(userGroup);
			if (userGroup != null) {
				if (userGroup.getRoles().containsKey(principal.getName()))
					return Json.getJson().setAndPush(500, "该用户已存在", null);
				this.userGroupService.joinChannel(userGroup, principal.getName());
				return Json.getJson().setAndPush(200, "加入成功", null);
			}
			return Json.getJson().setAndPush(500, "未找到该频道", null);
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param userGroup
	 * @param principal
	 * @return
	 * 修改频道用户权限的功能
	 */
	@RequestMapping("/channel/changeChannelRole")
	@ResponseBody
	public Json changeChannelRole(String id, String user, String role, Principal principal) {
		try {
			UserGroup userGroup = this.userGroupService.findByIdAndBuilder(id, principal.getName());
			UserRole userRole = null;
			if (userGroup != null) {
				String before = userGroup.getRoles().get(user).getSend();
				if (UserRole.POP.equals(role)) {
					userGroup.getRoles().remove(user);
					userRole = new UserRole(UserRole.POP, UserRole.RECEIVE);
				} else {
					if (UserRole.PUSH.equals(role)) {
						userRole = new UserRole(UserRole.PUSH, UserRole.RECEIVE);
					} else {
						userRole = userGroup.getRoles().get(user);
					}
					userRole.setSend(role);
					userGroup.getRoles().put(user, userRole);
				}
				this.userGroupService.updateAll(userGroup);
				this.channelKeeper.changeSendRoles(id, user, userRole);
				this.channelKeeper.sendChannelChangeNotice(id, user, before, role, userGroup.getName());
				return Json.getJson().setAndPush(200, "修改成功", null);
			}
			return Json.getJson().setAndPush(500, "修改失败", null);
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param id
	 * @param principal
	 * @return
	 * 用户退出频道
	 */
	@RequestMapping("/channel/exitChannel")
	@ResponseBody
	public Json exitChannel(String id, Principal principal) {
		try {
			Optional<UserGroup> userGroups = this.userGroupService.findChannelById(id);
			if (userGroups != null && userGroups.get() != null) {
				UserGroup userGroup = userGroups.get();
				userGroup.getRoles().remove(principal.getName());
				this.userGroupService.updateAll(userGroup);
				this.channelKeeper.changeSendRoles(id, principal.getName(), 
						new UserRole(UserRole.POP, UserRole.RECEIVE));
				this.channelKeeper.sendNotice(userGroup.getBuilder(),
						"用户" + principal.getName() + "已退出您的频道" + userGroup.getName());
				return Json.getJson().setAndPush(200, "退出成功", null);
			}
			return Json.getJson().setAndPush(500, "退出失败", null);
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	@RequestMapping("/channel/setUserReceiveRole")
	@ResponseBody
	public Json setUserReceiveRole(String id, String role, Principal principal) {
		try {
			UserGroup userGroup = this.userGroupService.findChannelById(id).get();
			UserRole userRole = userGroup.getRoles().get(principal.getName());
			if (userRole.getReceive().equals(role))
				return Json.getJson().setAndPush(500, "修改前后无差异", null);
			userRole.setReceive(role);
			this.userGroupService.updateAll(userGroup);
			this.channelKeeper.changReceiveRoles(id, principal.getName(), 
					userRole);
			return Json.getJson().setAndPush(200, "修改成功", null);
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, "修改失败", null);		}
	}
	
}
