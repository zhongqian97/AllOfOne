/**
 * 
 */
package ink.mastermind.AllINOne.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ink.mastermind.AllINOne.pojo.Message;

/**
 * @author joshua
 *
 */
public interface MessageDao extends MongoRepository<Message, String>{

	/**
	 * @param id
	 * @return
	 */
	List<Message> findByAddressee(String addressee);

	/**
	 * @param addressee
	 * @param time
	 * @return
	 */
	List<Message> findByAddresseeAndTimeGreaterThan(String addressee, Long time);

	/**
	 * @param addressee
	 * @param integer
	 * @return
	 */
	List<Message> findByAddresseeAndTimeGreaterThan(String addressee, Integer time);

	/**
	 * @param id
	 * @param long1
	 * @return
	 */
	List<Message> findByTimeGreaterThanAndAddressee(String addressee, Long time);

}
