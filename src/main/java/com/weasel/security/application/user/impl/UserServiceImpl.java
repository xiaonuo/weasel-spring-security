package com.weasel.security.application.user.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.weasel.security.application.user.UserService;
import com.weasel.security.domain.user.User;
import com.weasel.security.domain.user.UserRepository;

/**
 * @author Dylan
 * @time 2013-8-5
 */
@Service(value="userService")
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository repository;

	@Override
	public User getByUsername(String username) {
		return repository.getByUsername(username);
	}


	@Override
	public void lockUser(User user) {
		repository.LockUser(user);
	}

}
