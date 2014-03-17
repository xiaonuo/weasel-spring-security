package com.weasel.security.domain.user;


/**
 * @author Dylan
 * @time 2013-8-5
 */
public interface UserRepository {

	/**
	 * 
	 * @param username
	 * @return
	 */
	User getByUsername(String username);
	
	/**
	 * 
	 * @param username
	 */
	void LockUser(User user);

}
