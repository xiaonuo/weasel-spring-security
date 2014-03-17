package com.weasel.security.application.user;

import com.weasel.security.domain.user.User;

/**
 * @author Dylan
 * @time 2013-8-5
 */
public interface UserService {

	/**
	 * @param username
	 * @return
	 */
	User getByUsername(String username);
	
	/**
	 * 
	 * @param username
	 */
	void lockUser(User user);

}
