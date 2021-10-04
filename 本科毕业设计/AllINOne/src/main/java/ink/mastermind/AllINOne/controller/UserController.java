package ink.mastermind.AllINOne.controller;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ink.mastermind.AllINOne.pojo.Json;
import ink.mastermind.AllINOne.pojo.User;
import ink.mastermind.AllINOne.pojo.UserInfo;
import ink.mastermind.AllINOne.security.MyPasswordEncoder;
import ink.mastermind.AllINOne.service.UserService;
import net.jodah.expiringmap.ExpiringMap;


/**
 * @author joshua
 * 用户控制器类
 */
@Controller
public class UserController {
	
	@Autowired
	private UserService userService;
	
	/**
	 * 重定向登录
	 * @return
	 */
	@RequestMapping("/")
	public String index() {
		return "forward:index.html";
	}
	
	/**
	 * @param user
	 * @return
	 * 此方法为注册用户
	 */
	@RequestMapping("/register")
	@ResponseBody
	public Json register(@RequestBody User user) {
		
		try {
			User u = this.userService.findUserByUserName(user.getUserName());
			if (u != null) {
				return Json.getJson().setAndPush(500, "用户名已存在！", null);
			} else {
				user.setRole("ROLE_USER");//设置权限
				user.setOwnedUser(user.getUserName());//设置所属用户，一般用户都是自己，子设备是用户
				this.userService.save(user);
				return Json.getJson().setAndPush(200, "注册成功，请登录！", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}

	/**
	 * @param userInfo
	 * @param principal
	 * @return 
	 * 上传信息
	 */
	@RequestMapping("/user/upLoadInfo")
	@ResponseBody
	public Json upLoadInfo(@RequestBody UserInfo userInfo, Principal principal) {
		try {
			userInfo.setUserName(principal.getName());
			this.userService.upLoadInfo(userInfo);
			return Json.getJson().setAndPush(200, "更新成功", null);
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param principal
	 * @return 
	 * 下载信息
	 */
	@RequestMapping("/user/downLoadInfo")
	@ResponseBody
	public UserInfo downLoadInfo(Principal principal) {
		UserInfo userInfo = null;
		try {
			userInfo = this.userService.findUserInfoByUserName(principal.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(userInfo == null) {
			userInfo = new UserInfo();
			userInfo.setUserName(principal.getName());
		}
		return userInfo;
	}
	
	/**
	 * @param principal
	 * @return 
	 * 加载头像
	 */
	@RequestMapping("/user/userFace")
	@ResponseBody
	public Json userFace(Principal principal) {
		try {
			UserInfo userInfo = this.userService.findUserInfoByUserName(principal.getName());
			if(userInfo == null) {
				return Json.getJson().setAndPush(500, "新用户请上传头像", null);
			}else {
				return Json.getJson().setAndPush(200, "头像上传成功", userInfo.getPicture());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param principal
	 * @return 
	 * 空白请求
	 */
	@RequestMapping("/404")
	@ResponseBody
	public Json user404(Principal principal) {
		return Json.getJson().setAndPush(404, "", null);
	}
	
	/**
	 * @param oldPwd
	 * @param newPwd
	 * @param principal
	 * @return 
	 * 修改密码模块
	 */
	@RequestMapping("/user/changePassword")
	@ResponseBody
	public Json changePassword(String oldPwd, String newPwd, Principal principal) {
		try {
			if(this.userService.changePassword(principal.getName(), oldPwd, newPwd)) {
				return Json.getJson().setAndPush(200, "修改密码成功！", null);
			} else {
				return Json.getJson().setAndPush(200, "修改密码失败！", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param oldPwd
	 * @param principal
	 * @return
	 * 检查密码是否一致，页面解锁时候使用
	 */
	@RequestMapping("/user/checkPassword")
	@ResponseBody
	public Json checkPassword(String oldPwd, Principal principal) {
		try {
			if (this.userService.checkPassword(principal.getName(), oldPwd)) {
				return Json.getJson().setAndPush(200, "解锁成功！", null);
			} else {
				return Json.getJson().setAndPush(500, "解锁失败！", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param userName
	 * @param email
	 * @return
	 * 检查邮箱信息
	 */
	@RequestMapping("/checkEmail")
	@ResponseBody
	public Json checkEmail(String userName, String email) {
		try {
			UserInfo userinfo = this.userService.findUserInfoByUserName(userName);
			if (userinfo == null) {
				return Json.getJson().setAndPush(500, "用户信息不存在，你号没了。", null);
			}
			if (email.equals(userinfo.getEmail()) == false) {
				return Json.getJson().setAndPush(500, "你的账号邮箱与你当前邮箱错误，请检查输入账号和邮箱！", null);
			}
			this.userService.newCodeAndSend(userName, email);
			return Json.getJson().setAndPush(200, "发送成功，请检查邮箱的发件箱与垃圾箱！", null);
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param userName
	 * @param password
	 * @param code
	 * @return
	 * 找回密码
	 */
	@RequestMapping("/findPassword")
	@ResponseBody
	public Json findPassword(String userName, String password, String code) {
		try {
			if (this.userService.findPassword(userName, code, password)) {
				return Json.getJson().setAndPush(200, "修改密码成功！", null);
			} else {
				return Json.getJson().setAndPush(500, "修改密码失败！", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param request
	 * @param response
	 * @param session
	 * 退出登录
	 */
	@RequestMapping("/user/exit")
	public void logoutPage(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		// Authentication是一个接口，表示用户认证信息
		try {
			Authentication auth = null;
			if (SecurityContextHolder.getContext() != null 
					&& SecurityContextHolder.getContext().getAuthentication() != null)
					auth = SecurityContextHolder.getContext().getAuthentication();
			// 如果用户认知信息不为空，注销
			if (session != null) 
				session.removeAttribute("USER_SESSION");
			if (auth != null) {
				new SecurityContextLogoutHandler().logout(request, response, auth);
			}
		} catch (Exception e) {
		}
	}
}
