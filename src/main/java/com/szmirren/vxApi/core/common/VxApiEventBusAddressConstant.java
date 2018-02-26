package com.szmirren.vxApi.core.common;

/**
 * event bus 通讯地址
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiEventBusAddressConstant {
	/**
	 * 查看系统运行情况
	 */
	final static String SYSTEM_GET_INFO = "sys:getSysInfos";
	/**
	 * 添加部署app的数量
	 */
	final static String SYSTEM_PLUS_APP = "sys:plusAPP";
	/**
	 * 减去部署app的数量
	 */
	final static String SYSTEM_MINUS_APP = "sys:minusAPP";
	/**
	 * 添加异常信息
	 */
	final static String SYSTEM_PLUS_ERROR = "sys:plusError";
	/**
	 * 添加API追踪信息记录
	 */
	final static String SYSTEM_PLUS_TRACK_INFO = "sys:plusTrackInfos";
	/**
	 * 查看API追踪信息记录
	 */
	final static String SYSTEM_GET_TRACK_INFO = "sys:getTrackInfos";
	/**
	 * 更新IP地址
	 */
	final static String SYSTEM_PUBLISH_BLACK_IP_LIST = "publish:updateBlackIpList";

	/**
	 * 查看黑名单ip地址,与SysVerticle交互
	 */
	final static String SYSTEM_BLACK_IP_FIND = "sys:findBlackIp";
	/**
	 * 更新黑名单ip地址,与SysVerticle交互
	 */
	final static String SYSTEM_BLACK_IP_REPLACE = "sys:replaceBlackIP";
	/**
	 * 部署应用程序
	 */
	final static String DEPLOY_APP_DEPLOY = "deploy:deployApplication";
	/**
	 * 卸载应用程序应用程序
	 */
	final static String DEPLOY_APP_UNDEPLOY = "deploy:undeployApplication";

	/**
	 * 启动一个应用中的所有API
	 */
	final static String DEPLOY_API_START_ALL = "deploy:startAllAPI";

	/**
	 * 启动API
	 */
	final static String DEPLOY_API_START = "deploy:startAPI";
	/**
	 * 暂停API
	 */
	final static String DEPLOY_API_STOP = "deploy:stopAPI";
	/**
	 * 获得已部署的应用程序
	 */
	final static String DEPLOY_FIND_ONLINE_APP = "deploy:findOnlineAPP";
	/**
	 * 查看APP是否已经部署
	 */
	final static String DEPLOY_APP_IS_ONLINE = "deploy:getAppIsOnline";
	/**
	 * 获得已部署的API
	 */
	final static String DEPLOY_FIND_ONLINE_API = "deploy:findOnlineAPI";
	/**
	 * 查看API是否已经部署
	 */
	final static String DEPLOY_API_IS_ONLINE = "deploy:getApiIsOnline";
	/**
	 * 应用程序添加API的后缀
	 */
	final static String APPLICATION_ADD_API_SUFFIX = "/:addRoute";
	/**
	 * 应用程序修改API的后缀
	 */
	final static String APPLICATION_UPDT_API_SUFFIX = "/:updtRoute";
	/**
	 * 应用程序删除API的后缀
	 */
	final static String APPLICATION_DEL_API_SUFFIX = "/:delRoute";

	/**
	 * 查看所有APP
	 */
	final static String FIND_APP = "data:findApplication";
	/**
	 * 查看单个APP
	 */
	final static String GET_APP = "data:getApplication";
	/**
	 * 添加APP
	 */
	final static String ADD_APP = "data:addApplication";
	/**
	 * 删除APP
	 */
	final static String DEL_APP = "data:delApplication";
	/**
	 * 更新APP
	 */
	final static String UPDT_APP = "data:updtApplication";

	/**
	 * 查询所有API
	 */
	final static String FIND_API_ALL = "data:findAllAPI";

	/**
	 * 查询API通过分页的方式
	 */
	final static String FIND_API_BY_PAGE = "data:findApiByPage";
	/**
	 * 查询单个API
	 */
	final static String GET_API = "data:getAPI";
	/**
	 * 添加API
	 */
	final static String ADD_API = "data:addAPI";
	/**
	 * 删除API
	 */
	final static String DEL_API = "data:delAPI";
	/**
	 * 更新API
	 */
	final static String UPDT_API = "data:updtAPI";
	/**
	 * 更新黑名单列表,与DATAVerticle交互
	 */
	final static String FIND_BLACKLIST = "data:findBlacklist";
	/**
	 * 更新黑名单列表,与DATAVerticle交互
	 */
	final static String REPLACE_BLACKLIST = "data:updtBlacklist";

}
