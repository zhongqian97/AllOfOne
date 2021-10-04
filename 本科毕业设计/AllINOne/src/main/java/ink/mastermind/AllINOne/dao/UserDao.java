package ink.mastermind.AllINOne.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ink.mastermind.AllINOne.pojo.User;

public interface UserDao extends MongoRepository<User, String>{
	User findByUserName(String userName);

	/**
	 * @param name
	 * @return
	 */
	List<User> findByOwnedUser(String ownedUser);

	/**
	 * @param userName
	 * @param ownedUser
	 * @return
	 */
	User findByUserNameAndOwnedUser(String userName, String ownedUser);
}
