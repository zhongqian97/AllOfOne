/**
 * 
 */
package ink.mastermind.AllINOne.controller;

import java.security.Principal;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ink.mastermind.AllINOne.pojo.Json;
import ink.mastermind.AllINOne.pojo.User;
import ink.mastermind.AllINOne.pojo.UserInfo;
import ink.mastermind.AllINOne.service.UserService;

/**
 * @author joshua
 * 设备控制器
 */
@Controller
public class DeviceController {
	@Autowired
	private UserService userService;
	
	/**
	 * @param principal
	 * @return
	 * 获取用户的全部设备
	 */
	@RequestMapping("/device/getDevice")
	@ResponseBody
	public List<User> getDevice(Principal principal) {
		try {
			return this.userService.getDevice(principal.getName());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param principal
	 * @return
	 * 添加设备
	 */
	@RequestMapping("/device/addDevice")
	@ResponseBody
	public Json addDevice(Principal principal) {
		try {
			ObjectId id = new ObjectId();
			User user = new User(id.toString(), id.toString(), "123456", "ROLE_DEVICE", new Long(new Date().getTime()), principal.getName());
			this.userService.save(user);
			return Json.getJson().setAndPush(200, "创建设备成功！初始密码为123456", null);
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param id
	 * @param principal
	 * @return
	 * 删除设备
	 */
	@RequestMapping("/device/deleteDevice")
	@ResponseBody
	public Json deleteDevice(String id, Principal principal) {
		try {
			if (this.userService.deleteDevice(id, principal.getName())) 
				return Json.getJson().setAndPush(200, "删除成功", null);
			return Json.getJson().setAndPush(500, "删除失败", null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param userName
	 * @param name
	 * @param password
	 * @param picture
	 * @param principal
	 * @return
	 * 修改设备信息
	 */
	@RequestMapping("/device/changeDevice")
	@ResponseBody
	public Json changeDevice(String userName, String name, String password, String picture, Principal principal) {
		try {
			User user = this.userService.findUserByUserNameAndOwnedUser(userName, principal.getName());
			if (user == null) 
				return Json.getJson().setAndPush(500, "未找到您的设备！", null);
			if (password != null && "".equals(password) == false) user.setPassword(password);
			UserInfo userInfo = this.userService.findUserInfoByUserName(userName);
			if (userInfo == null) userInfo = new UserInfo(userName, name, "", "", "", "", "", picture);
			if (name != null && "".equals(name) == false) userInfo.setName(name);
			if (picture != null && "".equals(picture) == false) userInfo.setPicture(picture);
			if (this.userService.changeDevice(user, userInfo)) 
				return Json.getJson().setAndPush(200, "更新成功", null);
			return Json.getJson().setAndPush(500, "更新失败", null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param userName
	 * @param principal
	 * @return
	 * 获取设备个人信息
	 */
	@RequestMapping("/device/getDeviceInfo")
	@ResponseBody
	public UserInfo getDeviceInfo(String userName, Principal principal) {
		UserInfo userInfo = null;
		try {
			userInfo = this.userService.findUserInfoByUserName(userName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(userInfo == null) {
			userInfo = new UserInfo();
			userInfo.setUserName(userName);
		}
		return userInfo;
	}
}
