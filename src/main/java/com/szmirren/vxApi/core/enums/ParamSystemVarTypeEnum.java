package com.szmirren.vxApi.core.enums;

/**
 * API请求后台时的产量常量
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public enum ParamSystemVarTypeEnum {
	/**
	 * 请求客服端的ip地址
	 */
	CLIENT_HOST,
	/**
	 * 请求客户端的端口
	 */
	CLIENT_PORT,
	/**
	 * 请求客户端的PATH
	 */
	CLIENT_PATH,
	/**
	 * 请求客户端的sessionId
	 */
	CLIENT_SESSION_ID,

	/**
	 * 用户请求的完整路径
	 */
	CLIENT_ABSOLUTE_URI,
	/**
	 * 用户请求的模式
	 */
	CLIENT_REQUEST_SCHEMA,
	/**
	 * 获得api的名字
	 */
	SERVER_API_NAME,
	/**
	 * 获得API服务器的unix时间戳
	 */
	SERVER_UNIX_TIME,
	/**
	 * 获得网关USER_AGENT
	 */
	SERVER_USER_AGENT;
}
