package com.szmirren.vxApi.spi.customHandler;

import java.util.ArrayList;
import java.util.List;

import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.spi.customHandler.impl.GetConstantValueHandler;
import com.szmirren.vxApi.spi.customHandler.impl.GetServerUnixTimestampHandler;
import com.szmirren.vxApi.spi.customHandler.impl.SessionTokenGrantAuthHandler;

import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

/**
 * 自定义处理器工厂
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiCustomHandlerFactory {
	/**
	 * 获得VxApi当前服务的unix时间戳
	 */
	public static String GET_SERVER_UNIX_TIMESTAMP = "GET_SERVER_UNIX_TIMESTAMP";
	/**
	 * 获得自定义常量
	 */
	public static String GET_CONSTANT_VALUE = "GET_CONSTANT_VALUE";
	/**
	 * session_token权限认证的授权
	 */
	public static String SESSION_TOKEN_GRANT_AUTH = "SESSION_TOKEN_GRANT_AUTH";

	/**
	 * 获得所有实现类名字
	 * 
	 * @return
	 */
	public static List<String> getImplNames() {
		List<String> result = new ArrayList<>();
		result.add(GET_SERVER_UNIX_TIMESTAMP);
		result.add(GET_CONSTANT_VALUE);
		result.add(SESSION_TOKEN_GRANT_AUTH);
		return result;
	}

	/**
	 * 自定义处理器
	 * 
	 * @param name
	 *          处理器在工厂中的名字
	 * @param options
	 *          处理器配置文件
	 * 
	 * @param api
	 *          API 相关配置文件
	 * @param httpClient
	 *          http的客户端
	 * @return
	 * @throws NullPointerException
	 * @throws ClassNotFoundException
	 * @throws Exception
	 */
	public static VxApiCustomHandler getCustomHandler(String name, JsonObject options, VxApis api, HttpClient httpClient)
			throws NullPointerException, ClassNotFoundException, Exception {
		if (StrUtil.isNullOrEmpty(name)) {
			throw new NullPointerException("获取自定义处理器-->失败:工厂名字不能为空");
		}
		if (SESSION_TOKEN_GRANT_AUTH.equalsIgnoreCase(name)) {
			return new SessionTokenGrantAuthHandler(options, api, httpClient);
		}
		if (GET_SERVER_UNIX_TIMESTAMP.equalsIgnoreCase(name)) {
			return new GetServerUnixTimestampHandler(options);
		}
		if (GET_CONSTANT_VALUE.equalsIgnoreCase(name)) {
			return new GetConstantValueHandler(options);
		}
		throw new ClassNotFoundException("没有找到名字为 : " + name + " 的实现类");
	}

}
