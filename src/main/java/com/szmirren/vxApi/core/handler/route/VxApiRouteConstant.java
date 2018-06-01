package com.szmirren.vxApi.core.handler.route;

/**
 * VxApiRoute需要用到的常量
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiRouteConstant {
	/** 返回类型 Content-Type */
	public final static String CONTENT_TYPE = "Content-Type";
	/** 返回类型小写 content-type */
	public final static String CONTENT_TYPE_LOWER_CASE = "content-type";
	/** 时间 Content-Length */
	public final static String CONTENT_LENGTH = "Content-Length";
	/** 时间 content-length */
	public final static String CONTENT_LENGTH_LOWER_CASE = "content-length";
	/** 服务器类型 Server */
	public final static String SERVER = "Server";
	/** 服务器类型 User-Agent */
	public final static String USER_AGENT = "User-Agent";
	/** 时间 Date */
	public final static String DATE = "Date";
	/** 从RoutingContext获取用户请求长度的key */
	public final static String BODY_KEY_CONTENT_LENGTH = "VxApiBodyContentLength";
	/** 从RoutingContext获取用户请求PATH参数的key */
	public final static String BODY_KEY_PATH_TYPE_MultiMap = "VxApiBodyPathParams";
	/** 从RoutingContext获取用户请求HEADER参数的key */
	public final static String BODY_KEY_HEADER_TYPE_MultiMap = "VxApiBodyHeaderParams";
	/** 从RoutingContext获取用户请求QUERY参数的key */
	public final static String BODY_KEY_QUERY_TYPE_QueryStringEncoder = "VxApiBodyQueryPathParams";
	/** 从RoutingContext获取用户请求BODY参数的key */
	public final static String BODY_KEY_BODY_TYPE_QueryStringEncoder = "VxApiBodyBodyPathParams";

}
