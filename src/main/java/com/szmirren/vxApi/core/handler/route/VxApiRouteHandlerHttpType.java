package com.szmirren.vxApi.core.handler.route;

import java.net.MalformedURLException;

import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.handler.route.impl.VxApiRouteHandlerHttpTypeImpl;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;

/**
 * VxApiRoute HTTP/HTTPS服务类型的处理器
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiRouteHandlerHttpType extends Handler<RoutingContext> {
	/**
	 * 得到一个默认的访问限制实现
	 * 
	 * @param api
	 * @return
	 * @throws MalformedURLException
	 * @throws NullPointerException
	 */
	static VxApiRouteHandlerHttpType create(boolean isNext, VxApis api, String appName, WebClient webClient)
			throws NullPointerException, MalformedURLException {
		return new VxApiRouteHandlerHttpTypeImpl(isNext, api, appName, webClient);
	};
}
