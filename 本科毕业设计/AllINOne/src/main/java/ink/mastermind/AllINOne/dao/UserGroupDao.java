/**
 * 
 */
package ink.mastermind.AllINOne.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ink.mastermind.AllINOne.pojo.UserGroup;

/**
 * @author joshua
 *
 */
public interface UserGroupDao extends MongoRepository<UserGroup, String>{

	/**
	 * @param name
	 */
	List<UserGroup> findByBuilder(String builder);

	/**
	 * @param id
	 * @param name
	 * @return
	 */
	UserGroup findByIdAndName(String id, String name);

	/**
	 * @param id
	 * @param builder
	 * @return
	 */
	UserGroup findByIdAndBuilder(String id, String builder);
}
