package ink.mastermind.AllINOne.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ink.mastermind.AllINOne.pojo.Json;
import ink.mastermind.AllINOne.pojo.User;
import ink.mastermind.AllINOne.pojo.UserGroup;
import ink.mastermind.AllINOne.service.ChannelKeeper;
import ink.mastermind.AllINOne.service.UserService;

@Controller
public class AdminController {
	
	@Autowired
	private UserService userService;
	@Autowired
	private ChannelKeeper channelKeeper;
	
	/**
	 * @param userName
	 * @param command
	 * @param principal
	 * @return
	 * 封禁用户
	 */
	@RequestMapping("/admin/banUser")
	@ResponseBody
	public Json banUser(String userName, String command, Principal principal) {
		User user = this.userService.findUserByUserName(userName);
		if ("ban".equals(command)) {
			user.setRole("ROLE_BAN");
			this.userService.save(user);
			return Json.getJson().setAndPush(200, "封禁用户成功", null);
		} else {
			if (user.getUserName().equals(user.getOwnedUser())) {
				user.setRole("ROLE_USER");
			} else {
				user.setRole("ROLE_DEVICE");
			}
			this.userService.save(user);
			return Json.getJson().setAndPush(200, "解封用户成功", null);
		}
	}
	
	/**
	 * @param principal
	 * @return
	 * 获取全部用户
	 */
	@RequestMapping("/admin/getUser")
	@ResponseBody
	public List<User> getUser(Principal principal) {
		try {
			return this.userService.getUser();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param title
	 * @param tips
	 * @param principal
	 * @return
	 * 系统公告
	 */
	@RequestMapping("/admin/systemTips")
	@ResponseBody
	public Json systemTips(String title, String tips, Principal principal) {
		this.channelKeeper.sendNotice("All", "标题：" + title + "<br>" + tips);
		return Json.getJson().setAndPush(200, "发送信息成功", null);
	}
	
}
