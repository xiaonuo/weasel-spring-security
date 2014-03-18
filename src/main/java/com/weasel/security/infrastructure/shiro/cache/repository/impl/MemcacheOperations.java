package com.weasel.security.infrastructure.shiro.cache.repository.impl;

import java.util.Set;

import com.weasel.memcached.MemcacheRepository;
import com.weasel.security.infrastructure.shiro.cache.repository.CacheRepository;

/**
 * @author Dylan
 * @time 2014年3月14日
 */
public class MemcacheOperations implements CacheRepository{
	
	private MemcacheRepository repository = new com.weasel.memcached.MemcacheOperations();
	private static MemcacheOperations self;

	@Override
	public <T> T get(String key) {
		return repository.get(key);
	}

	@Override
	public <T> void save(String key, T entity) {
		repository.save(key, entity);
	}

	@Override
	public void remove(String key) {
		repository.remove(key);
	}

	@Override
	public Set<String> keys() {
		return repository.keys();
	}
	
	public static MemcacheOperations newSingleton(){
		synchronized (MemcacheOperations.class) {
			if(null == self)
				self = new MemcacheOperations();
		}
		return self;
	}

}
