package com.szmirren.vxApi.core.auth;

/**
 * 用户的角色
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiRolesConstant {
	// =================================================
	// ===================权限能力========================
	// =================================================
	/**
	 * 读写权限
	 */
	final String WRITE = "write";
	/**
	 * 读权限
	 */
	final String READ = "read";

	// =================================================
	// ==============用户在传输协议中的名字===================
	// =================================================
	/**
	 * 用户名字的key
	 */
	final String USER_NAME_KEY = "user";
	/**
	 * 用户密码的key
	 */
	final String USER_PWD_KEY = "pwd";
	/**
	 * 用户角色的key
	 */
	final String USER_ROLE_KEY = "role";
}
