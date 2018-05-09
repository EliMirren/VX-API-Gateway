package com.szmirren.vxApi.core.common;

/**
 * event bus 通讯地址
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiEventBusAddressConstant {
	/**
	 * 客户端启动所有应用于API,无参数要求<br>
	 * 结果:成功ok,500失败
	 */
	final static String CLI_START_EVERYTHING = "cli:startEverything";
	/**
	 * 客户端启动所有APP,无参数要求<br>
	 */
	final static String CLI_START_ALL_APP = "cli:startAllAPP";
	/**
	 * 客户端启动指定APP并同时启动所有的API,参数要求:JsonArray<br>
	 * ["网关应用的名字",...]<br>
	 * 结果:ok等于成功,其他为失败1400缺少参数,500错误
	 */
	final static String CLI_START_APP_EVERYTHING = "cli:startAPPEverything";
	/**
	 * 客户端启动指定APP,参数要求:JsonArray<br>
	 * ["网关应用的名字",...]<br>
	 * 结果:ok等于成功,其他为失败1400缺少参数,500错误
	 */
	final static String CLI_START_APP = "cli:startAPP";

	/**
	 * 查看系统运行情况,无需参数<br>
	 * 返回结果:500失败,其他成功
	 */
	final static String SYSTEM_GET_INFO = "sys:getSysInfos";
	/**
	 * 添加异常信息,参数要求:JsonObject<br>
	 * [{@link com.szmirren.vxApi.core.entity.VxApiTrackInfos} to JsonObject
	 * 选填]<br>
	 * 没有返回结果
	 */
	final static String SYSTEM_PLUS_ERROR = "sys:plusError";
	/**
	 * 添加API追踪信息记录,参数要求:<br>
	 * [{@link com.szmirren.vxApi.core.entity.VxApiTrackInfos} to JsonObject
	 * 选填]<br>
	 * 没有返回结果
	 */
	final static String SYSTEM_PLUS_TRACK_INFO = "sys:plusTrackInfos";
	/**
	 * 查看API追踪信息记录,参数要求:JsonObject<br>
	 * [String 必填 ] appName 网关名字 , [String 必填 ] apiName API的名
	 */
	final static String SYSTEM_GET_TRACK_INFO = "sys:getTrackInfos";
	/**
	 * 更新IP地址,无需参数<br>
	 * 没有返回结果
	 */
	final static String SYSTEM_PUBLISH_BLACK_IP_LIST = "publish:updateBlackIpList";

	/**
	 * 查看黑名单ip地址,与SysVerticle交互,无需参数<br>
	 * 返回结果:失败500,其他成功
	 */
	final static String SYSTEM_BLACK_IP_FIND = "sys:findBlackIp";
	/**
	 * 更新黑名单ip地址,与SysVerticle交互,参数要求:JsonObject<br>
	 * [JsonArray 必填 ]ipList<br>
	 * 返回结果:失败:500,1400,1405,其他为成功
	 */
	final static String SYSTEM_BLACK_IP_REPLACE = "sys:replaceBlackIP";
	/**
	 * 添加请求到达VX的数量,无需参数,无返回
	 */
	final static String SYSTEM_PLUS_VX_REQUEST = "sys:requestVxApiCountPlus";
	/**
	 * 添加请求到达到达核心处理器(HTTP/HTTPS)的数量,无需参数,无返回
	 */
	final static String SYSTEM_PLUS_HTTP_API_REQUEST = "sys:requestHttpApiCountAndProcessingPlus";
	/**
	 * 减少核心处理器(HTTP/HTTPS)当前正在处理的数量,无需参数,无返回
	 */
	final static String SYSTEM_MINUS_CURRENT_PROCESSING = "sys:currentHttpApiProcessingCountMinus";

	/**
	 * 获取应用网关的数量,无需参数<br>
	 * 返回结果:JsonObject:app:网关应用的数量,api:API的数量
	 */
	final static String DEPLOY_APP_COUNT = "deploy:applicationCount";
	/**
	 * 部署应用程序,参数要求:JsonObject<br>
	 * [String 必填 ]appName网关名字,[JsonObject 必填 ]app网关配置信息<br>
	 * 结果:成功reply=ok,其他为失败
	 */
	final static String DEPLOY_APP_DEPLOY = "deploy:deployApplication";
	/**
	 * 卸载应用程序应用程序,参数要求:JsonObject<br>
	 * [String 必填 ]appName网关名字 , [String 选填 ]thisVertxName 集群环境时当前应用名字<br>
	 * 结果:ok成功,其他失败
	 */
	final static String DEPLOY_APP_UNDEPLOY = "deploy:undeployApplication";

	/**
	 * 启动一个应用中的所有API,需要参数:JsonObject<br>
	 * [String 必填 ]appName , [JsonArray 必填 ] apis<br>
	 * 结果:JsonObject
	 */
	final static String DEPLOY_API_START_ALL = "deploy:startAllAPI";

	/**
	 * 启动API,需要参数:JsonObject<br>
	 * [String 必填] appName 网关名字, <br>
	 * [String 必填] apiName API名字, <br>
	 * [String 必填] thisVertxName 集群环境时当前应用名字, <br>
	 * [JsonObject 必填] api API信息, <br>
	 * 结果:1成功,其他失败:0,500
	 */
	final static String DEPLOY_API_START = "deploy:startAPI";
	/**
	 * 暂停API,需要参数:JsonObject<br>
	 * [String 必填] appName 网关名字, <br>
	 * [String 必填] apiName API名字, <br>
	 * [String 必填] thisVertxName 集群环境时当前应用名字, <br>
	 * 结果:1成功,其他失败:500
	 */
	final static String DEPLOY_API_STOP = "deploy:stopAPI";

	/**
	 * 获得已部署的应用程序,无需参数<br>
	 * 结果:JsonArray
	 */
	final static String DEPLOY_FIND_ONLINE_APP = "deploy:findOnlineAPP";
	/**
	 * 查看APP是否已经部署,参数要求:String<br>
	 * 结果:Boolean
	 */
	final static String DEPLOY_APP_IS_ONLINE = "deploy:getAppIsOnline";
	/**
	 * 获得已部署的API,参数要求:JsonObject<br>
	 * [String 必填] appName 网关名字, <br>
	 * [String 必填] apiName API名字, <br>
	 * 结果:JsonArray
	 */
	final static String DEPLOY_FIND_ONLINE_API = "deploy:findOnlineAPI";
	/**
	 * 查看API是否已经启动,参数要求:JsonObject<br>
	 * [String 必填] appName 网关名字, <br>
	 * [String 必填] apiName API名字, <br>
	 * 结果:Boolean
	 */
	final static String DEPLOY_API_IS_ONLINE = "deploy:getApiIsOnline";

	/**
	 * 应用程序添加API的后缀,参数要求,JsonObject<br>
	 * [JsonObject 必填] api API的配置信息<br>
	 * [Boolean 选填] elseRouteToThis 是否代理启动API, <br>
	 * [Integer 选填] serverType 服务的类型1=HTTP,2=HTTPS,3=webSocket <br>
	 * 结果:成功=1,其他失败404,500,1400
	 */
	final static String APPLICATION_ADD_API_SUFFIX = "/:addRoute";

	/**
	 * 应用程序删除API的后缀,参数要求,String 必填 API的名字<br>
	 * 结果:成功=1,其他失败1400
	 */
	final static String APPLICATION_DEL_API_SUFFIX = "/:delRoute";

	/**
	 * 查看所有APP,无需参数
	 */
	final static String FIND_APP = "data:findApplication";
	/**
	 * 查看单个APP,参数要求:String
	 */
	final static String GET_APP = "data:getApplication";
	/**
	 * 添加APP,参数要求:JsonObject<br>
	 * [String 必填 ]appName 应用的名称, [JsonObject 必填] app 应用的属性<br>
	 * 返回结果:成功受影响行数,失败返回411,500
	 */
	final static String ADD_APP = "data:addApplication";
	/**
	 * 删除APP,参数要求:String<br>
	 * 结果:成功受影响行数,失败返回411,500
	 */
	final static String DEL_APP = "data:delApplication";
	/**
	 * 更新APP,参数要求:JsonObject<br>
	 * [String 必填 ]appName 应用的名称, [JsonObject 必填] app 应用的属性<br>
	 * 返回结果:成功受影响行数,失败返回411,500
	 */
	final static String UPDT_APP = "data:updtApplication";

	/**
	 * 查询所有API,需要参数:JsonObject<br>
	 * [String 必填 ] appName<br>
	 * 结果:失败返回411,500
	 */
	final static String FIND_API_ALL = "data:findAllAPI";

	/**
	 * 查询API通过分页的方式,参数要求:JsonObject<br>
	 * [String 必填 ]appName 应用的名称, <br>
	 * [Integer 选填 ]limit 分页limit, <br>
	 * [Integer 选填 ]offset 分页offset<br>
	 * 结果:失败返回411,500
	 */
	final static String FIND_API_BY_PAGE = "data:findApiByPage";
	/**
	 * 查询单个API,参数要求:String<br>
	 * 结果:成功受影响行数,失败返回411,500
	 */
	final static String GET_API = "data:getAPI";
	/**
	 * 添加API,参数要求:JsonObject<br>
	 * [String 必填] appName 网关名字, <br>
	 * [String 必填] apiName API名字, <br>
	 * [JsonObject 必填] api API信息, <br>
	 * 结果:成功受影响行数,失败返回411,500
	 */
	final static String ADD_API = "data:addAPI";
	/**
	 * 删除API,参数要求:JsonObject<br>
	 * [String 必填] appName 网关名字, <br>
	 * [String 必填] apiName API名字, <br>
	 * 结果:成功受影响行数,失败返回411,500
	 */
	final static String DEL_API = "data:delAPI";
	/**
	 * 更新API,参数要求:JsonObject<br>
	 * [String 必填] apiName API名字, <br>
	 * [JsonObject 必填] api API信息, <br>
	 * 结果:成功受影响行数,失败返回411,500
	 */
	final static String UPDT_API = "data:updtAPI";
	/**
	 * 更新黑名单列表,与DATAVerticle交互,无需参数
	 */
	final static String FIND_BLACKLIST = "data:findBlacklist";
	/**
	 * 更新黑名单列表,与DATAVerticle交互,参数要求:JsonObject<br>
	 * [String 必填] blacklistName 黑名单的名字, <br>
	 * [JsonArray 必填] blacklistBody 黑名单的内容, <br>
	 * 结果:成功受影响行数,失败返回500
	 */
	final static String REPLACE_BLACKLIST = "data:updtBlacklist";

}
