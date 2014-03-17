package com.weasel.security.domain.user;

import com.weasel.core.BaseObject;

/**
 * @author Dylan
 * @time 2013-8-5
 */
public class Auth extends BaseObject<Integer>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1554690122999348374L;
	/**
	 * 权限代码
	 */
	private String code;
	/**
	 * 权限名称
	 */
	private String name;
	
	private String fieldName;
	
	public Auth(){}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
}
