package com.szmirren.vxApi.spi.auth;

import java.util.ArrayList;
import java.util.List;

import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.spi.auth.impl.VxApiAuthJwtTokenImpl;
import com.szmirren.vxApi.spi.auth.impl.VxApiAuthSessionTokenImpl;

import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

/**
 * VxApiAuth 实现工厂
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiAuthFactory {
	/**
	 * sessionToken的实现类名称
	 */
	public final static String SESSION_TOKEN_AUTH = "sessionTokenAuth";
	
	public final static String JWT_TOKEN_AUTH = "jwtTokenAuth";

	/**
	 * 获得所有实现类的名字
	 * 
	 * @return
	 */
	public static List<String> getImplNames() {
		List<String> result = new ArrayList<>();
		result.add(SESSION_TOKEN_AUTH);
		return result;
	}

	/**
	 * 通过名字获得相应的权限实现类,可采用本类自带的静态字符串,如果配置信息类为空则采用默认的实现类
	 * 
	 * @param name
	 *          实现类的在工厂中的名字,可以使用本类静态属性
	 * @param options
	 *          配置信息
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static VxApiAuth getVxApiAuth(String name, JsonObject options, VxApis api, HttpClient httpClient)
			throws NullPointerException, ClassNotFoundException {
		if (StrUtil.isNullOrEmpty(name)) {
			throw new NullPointerException("获取API权限验证实现-->失败:工厂名字不能为空");
		}
		if (SESSION_TOKEN_AUTH.equalsIgnoreCase(name)) {
			return new VxApiAuthSessionTokenImpl(options);
		}
		if (JWT_TOKEN_AUTH.equalsIgnoreCase(name)) {
			return new VxApiAuthJwtTokenImpl(options);
		}
		throw new ClassNotFoundException("没有找到名字为 : " + name + " 的API权限验证实现类");
	}
}
