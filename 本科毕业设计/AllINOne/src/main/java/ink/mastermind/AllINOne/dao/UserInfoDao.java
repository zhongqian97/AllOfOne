package ink.mastermind.AllINOne.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import ink.mastermind.AllINOne.pojo.UserInfo;

public interface UserInfoDao extends MongoRepository<UserInfo, String>{
	UserInfo findByUserName(String userName);
}
