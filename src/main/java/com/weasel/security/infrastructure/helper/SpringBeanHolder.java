package com.weasel.security.infrastructure.helper;

import org.apache.commons.lang.Validate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @author Dylan
 * @time 2013-7-25
 */
public class SpringBeanHolder implements ApplicationContextAware {
	
	private static ApplicationContext context = null;

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		Validate.notNull(context);
		SpringBeanHolder.context = context;
	}

	public static ApplicationContext getContext() {
		if(null == context)
			throw new RuntimeException("please register the SpringBeanHolder bean to spring...");
		return context;
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public static <T> T getBean(Class<T> clazz) {
		return getContext().getBean(clazz);
	}

	/**
	 * 
	 * @param beanName
	 * @return
	 */
	public static Object getBean(String beanName) {
		return getContext().getBean(beanName);
	}

}
