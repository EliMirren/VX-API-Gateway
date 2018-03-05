package com.szmirren.vxApi.core.common;

/**
 * 该类主要用于存放应用网关与API数据存储于传输的关键字,主要用于方便自己存储网关数据到自己的数据库中
 * <p>
 * 应用网关存储的表结构:
 * <ul>
 * <li>表名 = APPLICATION_TABLE_NAME 存放网关应用的表名</li>
 * <li>主键列 = APPLICATION_ID_COLUMN 字符串用于存放应用网关的名字</li>
 * <li>内容列 = APPLICATION_ID_COLUMN json字符串用于存放应用网关的名字</li>
 * </ul>
 * <p>
 * 应用网关属性在传输中的名字:
 * <ul>
 * <li>主键 = APPLICATION_ID_NAME 主键在json中的名字</li>
 * <li>内容 = APPLICATION_CONTENT_COLUMN 内容在json中的名字</li>
 * </ul>
 * <p>
 * API存储的表结构:
 * <ul>
 * <li>表名 = API_TABLE_NAME 存放API的表名</li>
 * <li>主键列 = API_ID_COLUMN 字符串用于存放API的名字</li>
 * <li>应用网关主键列 = API_APP_ID_COLUMN 字符串用于存放API的名字</li>
 * <li>内容列 = API_CONTENT_COLUMN json字符串用于存放API的名字</li>
 * </ul>
 * <p>
 * API属性在传输中的名字:
 * <ul>
 * <li>主键 = API_ID_NAME 主键在json中的名字</li>
 * <li>主键 = API_APP_ID_NAME 应用网关主键在json中的名字</li>
 * <li>内容 = API_CONTENT_NAME 内容在json中的名字</li>
 * </ul>
 * <p>
 * 黑名单列表的表结构:(系统默认取黑名单列表第一行数据,)
 * <ul>
 * <li>表名 = BLACKLIST_TABLE_NAME 存放网关应用的黑名单的表名</li>
 * <li>主键列 = BLACKLIST_ID_COLUMN 字符串用于存放应用网关黑名单主键的名字</li>
 * <li>内容列 = BLACKLIST_ID_COLUMN json数组字符串用于存放应用网关黑名单内容的名字</li>
 * </ul>
 * <p>
 * 黑名单列表在传输中的名字:
 * <ul>
 * <li>主键 = BLACKLIST_ID_NAME 黑名单主键在json中的名字</li>
 * <li>内容 = BLACKLIST_CONTENT_COLUMN 黑名单内容在json中的名字</li>
 * </ul>
 * <p>
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiDATAStoreConstant {
	// ==============================================
	// ==================应用网关表结构=================
	// ==============================================
	/**
	 * 网关应用的表名
	 */
	static final String APPLICATION_TABLE_NAME = "vx_api_application";
	/**
	 * 网关应用的主键列
	 */
	static final String APPLICATION_ID_COLUMN = "name";
	/**
	 * 网关应用json字符串内容的列名
	 */
	static final String APPLICATION_CONTENT_COLUMN = "content";

	// ==================应用网关传输中获取内容的名字=================
	/**
	 * 网关应用通讯传输中获取json中存放主键的属性名
	 */
	static final String APPLICATION_ID_NAME = "name";
	/**
	 * 网关应用通讯传输中获取json中存放json字符串对象的属性名
	 */
	static final String APPLICATION_CONTENT_NAME = "content";

	// ==============================================
	// ==================API表结构====================
	// ==============================================

	/**
	 * 存放API的表名
	 */
	static final String API_TABLE_NAME = "vx_api_apis";
	/**
	 * 存放API的主键列
	 */
	static final String API_ID_COLUMN = "name";
	/**
	 * 存放API表中网关应用主键的列
	 */
	static final String API_APP_ID_COLUMN = "app_name";
	/**
	 * 存放API,json字符串内容的列名
	 */
	static final String API_CONTENT_COLUMN = "content";

	// ==================API传输中获取内容的名字=================

	/**
	 * API通讯传输中获取json中存放主键的属性名
	 */
	static final String API_ID_NAME = "name";
	/**
	 * API通讯传输中获取json中存放应用网关主键的属性名
	 */
	static final String API_APP_ID_NAME = "appName";
	/**
	 * API通讯传输中获取json中存放json字符串对象的属性名
	 */
	static final String API_CONTENT_NAME = "content";

	// ==============================================
	// ==================黑名单列表表结构================
	// ==============================================
	/**
	 * 黑名单列表的表名
	 */
	static final String BLACKLIST_TABLE_NAME = "vx_api_blacklist";
	/**
	 * 黑名单列表的主键列
	 */
	static final String BLACKLIST_ID_COLUMN = "name";
	/**
	 * 黑名单列表jsonArray字符串内容的列名
	 */
	static final String BLACKLIST_CONTENT_COLUMN = "content";

	// ==================应用网关传输中获取内容的名字=================
	/**
	 * 黑名单列表通讯传输中获取json中存放主键的属性名
	 */
	static final String BLACKLIST_ID_NAME = "blacklist";
	/**
	 * 黑名单列表通讯传输中获取json中存放json字符串对象的属性名
	 */
	static final String BLACKLIST_CONTENT_NAME = "content";

}
