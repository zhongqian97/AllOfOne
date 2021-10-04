/**
 * 
 */
package ink.mastermind.AllINOne.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ink.mastermind.AllINOne.pojo.Task;

/**
 * @author joshua
 *
 */
public interface TaskDao extends MongoRepository<Task, String>{

	/**
	 * @param user
	 * @return
	 */
	List<Task> findByUser(String user);

	/**
	 * @param id
	 * @param user
	 * @return
	 */
	Task findByIdAndUser(String id, String user);

	/**
	 * @param show
	 * @return
	 */
	List<Task> findByShow(String show);

	/**
	 * @param path
	 * @return
	 */
	List<Task> findByPath(String path);

	/**
	 * @param jspath
	 * @return
	 */
	List<Task> findByJspath(String jspath);

}
