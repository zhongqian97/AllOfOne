package ink.mastermind.AllINOne.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ink.mastermind.AllINOne.dao.UserDao;
import ink.mastermind.AllINOne.dao.UserInfoDao;
import ink.mastermind.AllINOne.pojo.User;
import ink.mastermind.AllINOne.pojo.UserInfo;
import ink.mastermind.AllINOne.security.MyPasswordEncoder;
import ink.mastermind.AllINOne.utils.EMail;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

@Service("userService")
public class UserService implements UserDetailsService {

	@Resource
	private UserDao userDao;
	@Resource
	private UserInfoDao userInfoDao;
	@Autowired
	private UserGroupService userGroupService;
	
	private ExpiringMap<String, String> findPaswordCode =  ExpiringMap.builder().expiration(15, TimeUnit.MINUTES)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();
	
	/**
	 * @param user 保存用户，无ID时可新增用户
	 */
	public void save(User user) {
		this.userDao.save(user);
	}

	/**
	 * @param string
	 * @return 通过用户名查找用户并返回
	 */
	public User findUserByUserName(String string) {
		return this.userDao.findByUserName(string);
	}

	/**
	 * security模块
	 */
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		// 调用持久层接口方法查找用户
		User user = this.userDao.findByUserName(userName);
		if (user == null || user.getRole().equals("ROLE_BAN")) {
			throw new UsernameNotFoundException("用户名不存在");
		}
		// 创建List集合，用来保存用户权限，GrantedAuthority对象代表赋予给当前用户的权限
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority(user.getRole()));
		return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword().trim(),
				authorities);
	}

	public void upLoadInfo(UserInfo userInfo) {
		UserInfo u = this.userInfoDao.findByUserName(userInfo.getUserName());
		if(u != null) {
			u.update(userInfo);
			this.userInfoDao.save(u);
		}else 
			this.userInfoDao.save(userInfo);
	}

	/**
	 * @param name
	 * @return
	 */
	public UserInfo findUserInfoByUserName(String name) {
		return this.userInfoDao.findByUserName(name);
	}

	/**
	 * @param userName
	 * @param email
	 * 生成验证码并发送验证码
	 */
	public void newCodeAndSend(String userName, String email) {
		 String code = new Integer((int)(Math.random() * 1000000)).toString();
		 System.out.println(code);
		 EMail.send(email, "找回密码验证码，有效期仅15分钟", "亲爱的用户：\n您的验证码为：" + code);
		 findPaswordCode.put(userName, code);
	}

	/**
	 * @param userName
	 * @param password
	 * @return
	 * 检查用户名是否存在并检查密码是否一致
	 */
	public boolean checkPassword(String userName, String password) {
		User user = this.findUserByUserName(userName);
		if (user != null && MyPasswordEncoder.m.matches(password, user.getPassword())) {
			return true;
		}
		return false;
	}

	/**
	 * @param name
	 * @param oldPwd
	 * @param newPwd
	 * @return
	 */
	public boolean changePassword(String name, String oldPwd, String newPwd) {
		User user = this.findUserByUserName(name);
		if (user != null && MyPasswordEncoder.m.matches(oldPwd, user.getPassword())) {
			user.setPassword(newPwd);
			this.save(user);
			return true;
		}
		return false;
	}

	/**
	 * @param userName
	 * @param code
	 * @param password
	 * @return
	 * 找回密码
	 */
	public boolean findPassword(String userName, String code, String password) {
		if ((findPaswordCode.containsKey(userName) && findPaswordCode.get(userName).equals(code))) {
			User user = this.findUserByUserName(userName);
			user.setPassword(password);
			this.save(user);
			return true;
		}
		return false;
	}

	/**
	 * @param name
	 * @return
	 */
	public List<User> getDevice(String name) {
		// TODO Auto-generated method stub
		List<User> list = this.userDao.findByOwnedUser(name);
		List<User> result = new LinkedList<User>();
		for (User u : list) {
			if (u.getOwnedUser().equals(u.getUserName())) {
				continue;
			}
			result.add(u);
		}
		return result;
	}

	/**
	 * @param id
	 * @param name
	 * @return
	 * 删除设备
	 */
	public boolean deleteDevice(String id, String name) {
		List<User> list = this.userDao.findByOwnedUser(name);
		for (User u : list) {
			if (u.getUserName().equals(id)) {
				this.userDao.deleteById(id);
				//删除附带的东西，比如频道，频道权限，设备个人信息
				UserInfo userInfo = this.userInfoDao.findByUserName(id);
				if (userInfo != null) this.userInfoDao.delete(userInfo);
				this.userGroupService.deleteUser(id);
				return true;
			}
		}
		return false;
	}

	/**
	 * @param userName
	 * @param ownedUser
	 * @return
	 */
	public boolean isOwned(String userName, String ownedUser) {
		return (this.userDao.findByUserNameAndOwnedUser(userName, ownedUser) != null);
	}

	/**
	 * @param userName
	 * @param ownedUser
	 * @return
	 */
	public User findUserByUserNameAndOwnedUser(String userName, String ownedUser) {
		return this.userDao.findByUserNameAndOwnedUser(userName, ownedUser);
	}

	/**
	 * @param user
	 * @param userInfo
	 * @return
	 * 修改设备信息
	 */
	public boolean changeDevice(User user, UserInfo userInfo) {
		this.userDao.save(user);
		this.userInfoDao.save(userInfo);
		return true;
	}

	/**
	 * @param name
	 */
	public User getUserByUsername(String userName) {
		// TODO Auto-generated method stub
		return this.userDao.findByUserName(userName);
	}

	/**
	 * @return
	 */
	public List<User> getUser() {
		// TODO Auto-generated method stub
		return this.userDao.findAll();
	}

}
