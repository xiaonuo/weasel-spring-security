package com.weasel.security.domain.user;

import com.weasel.mybatis.MybatisRepository;

/**
 * @author Dylan
 * @time 2013-8-5
 */
public interface UserRepository extends MybatisRepository<Integer, User> {

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
