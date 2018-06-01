package com.szmirren.vxApi.core.handler.route;

import java.net.MalformedURLException;

import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.handler.route.impl.VxApiRouteHandlerHttpServiceImpl;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.RoutingContext;
/**
 * VxApiRoute HTTP/HTTPS服务类型的处理器
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiRouteHandlerHttpService extends Handler<RoutingContext> {
	/**
	 * 得到一个HTTP/HTTPS服务类型的处理器
	 * 
	 * @param appName
	 *          应用程序的名字
	 * @param isNext
	 *          是否有下一个处理器
	 * @param api
	 *          API配置文件
	 * @param httpClient
	 *          与后台连接的客户端
	 * @return
	 * @throws NullPointerException
	 * @throws MalformedURLException
	 */
	static VxApiRouteHandlerHttpService create(String appName, boolean isNext, VxApis api, HttpClient httpClient)
			throws NullPointerException, MalformedURLException {
		return new VxApiRouteHandlerHttpServiceImpl(appName, isNext, api, httpClient);
	}
}
