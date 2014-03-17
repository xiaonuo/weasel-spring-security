package com.weasel.security.interfaces;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.ServletRequestUtils;

import com.weasel.core.helper.DemonPredict;
import com.weasel.core.helper.TimeHelper;
import com.weasel.security.application.cache.CurrentUserCacheService;
import com.weasel.security.application.cache.LoginErrorCacheService;
import com.weasel.security.application.user.UserService;
import com.weasel.security.domain.cache.LoginError;
import com.weasel.security.domain.user.User;
import com.weasel.security.infrastructure.exception.LockAccountException;
import com.weasel.security.infrastructure.exception.UnAllowLoginException;
import com.weasel.security.infrastructure.exception.UserHasOnlineException;
import com.weasel.security.infrastructure.helper.ShiroAuthorizationHelper;
import com.weasel.security.infrastructure.helper.ShiroSecurityHelper;

/**
 * 
 * @author Dylan
 * @time 2013-8-27
 */
public class SecurityServer {

	private int lockUserNumber;
	private int lockUserTime;
	
	private final static Logger LOG = LoggerFactory.getLogger(SecurityServer.class);

	@Autowired
	private UserService userService;
	
	@Autowired
	private CurrentUserCacheService currentUserMemcacheService;

	@Autowired
	private LoginErrorCacheService loginErrorMemcacheService;

	/**
	 * 
	 * @param user
	 * @param request
	 * @param response
	 */
	public void login(User user, HttpServletRequest request, HttpServletResponse response) {
		user.encodePassword();
		baseLogin(user, request, response);
	}

	/**
	 * 自动登录
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public Map<String, Object> autoLogin(HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> responseMsg = new HashMap<String, Object>();
		Subject currentUser = SecurityUtils.getSubject();
		if(currentUser.isRemembered()){
			String username = ShiroSecurityHelper.getCurrentUsername();
			LOG.info("用户【{}】自动登录----{}", username,TimeHelper.getCurrentTime());
			User user = userService.getByUsername(username);
			baseLogin(user, request, response);
			ShiroAuthorizationHelper.clearAuthorizationInfo(username); // 用户是自动登录，首先清一下用户权限缓存，让重新加载
			responseMsg.put("username", username);
		}
		return responseMsg;
	}

	/**
	 * 伪登录
	 */
	@Deprecated
	public void fakeLogin(String username, HttpServletRequest request, HttpServletResponse response) {

		throw new UnsupportedOperationException("can not call this method!");
		
	}

	/**
	 * 退出登录
	 * 
	 * @return
	 */
	public void logout() {
		Subject subject = SecurityUtils.getSubject();
		if (subject.isAuthenticated()) {
			String username = ShiroSecurityHelper.getCurrentUsername();
			subject.logout(); // session 会销毁，在SessionListener监听session销毁，清理权限缓存
			currentUserMemcacheService.remove(username);
			if (LOG.isDebugEnabled()) {
				LOG.debug("用户" + username + "退出登录");
			}
		}
	}

	/**
	 * 
	 * @param user
	 * @param request
	 * @param response
	 */
	public void baseLogin(User user, HttpServletRequest request, HttpServletResponse response) {
		
		DemonPredict.notNull(user, "用户密码不能为空");
		User sessionUser = userService.getByUsername(user.getUsername());
		if(null == sessionUser){
			request.setAttribute("error", "您输入的用户或者密码不正确");
			throw new UnAllowLoginException();
		}
		try {
			Subject currentUser = SecurityUtils.getSubject();
			if (currentUser.isAuthenticated()) {
				return;
			}
			//如果用户已登录，先踢出
			ShiroSecurityHelper.kickOutUser(user.getUsername());
			
			boolean rememberMe = ServletRequestUtils.getBooleanParameter(request, "rememberMe", false);
			UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), user.getPassword(), rememberMe);
			currentUser.login(token); // 登录

			ShiroSecurityHelper.setUser(sessionUser); // 把user放到cache中

			request.setAttribute("username", sessionUser.getUsername());
		} catch (Exception e) {
			request.setAttribute("error", translateException(e, sessionUser));
			throw new UnAllowLoginException(e);
		}finally{
			ShiroAuthorizationHelper.clearAuthorizationInfo(sessionUser.getUsername());
		}
	}

	/**
	 * 
	 * @param e
	 * @param user
	 * @return
	 */
	private String translateException(Exception e, User user) {
		if (e instanceof IncorrectCredentialsException || e instanceof UnknownAccountException) { // 密码不正确异常
			String username = user.getUsername();
			LoginError error = loginErrorMemcacheService.get(username);
			if (error.getErrorNumber() >= (lockUserNumber-1)) { // 从memcache取数据，如果错误登录指定次数，进行帐号锁定，锁定时间通过配置得到
				if (null != user) {
					user.setLockedTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(TimeHelper.getAfterMinuteTime(lockUserTime)));
					userService.lockUser(user);
					return "该账户登录出错已达上限，请"+lockUserTime+"小时后重试";
				}
			}
			error.increaseErrorNumber().setValidTime(TimeHelper.getAfterHourTime(lockUserTime));
			loginErrorMemcacheService.save(error);
			return "您输入的用户名或密码不正确，还有"+(lockUserNumber-error.getErrorNumber())+"次机会";
		}
		if (e instanceof LockAccountException) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
			Date date = null;
			try {
				date = sdf.parse(user.getLockedTime());
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			String time = sdf2.format(date);
			return "您的帐号已被锁定,请在" + time + "后再登录";
		}
		if (e instanceof UserHasOnlineException) {
			return "帐号已处于登录状态";
		}
		if(e instanceof UnknownAccountException){
			return "该用户类型不能登录该站点";
		}
		e.printStackTrace();
		return "未知异常，请联系管理员";
	}

	public int getLockUserNumber() {
		return lockUserNumber;
	}

	public void setLockUserNumber(int lockUserNumber) {
		this.lockUserNumber = lockUserNumber;
	}

	public int getLockUserTime() {
		return lockUserTime;
	}

	public void setLockUserTime(int lockUserTime) {
		this.lockUserTime = lockUserTime;
	}
	
}
