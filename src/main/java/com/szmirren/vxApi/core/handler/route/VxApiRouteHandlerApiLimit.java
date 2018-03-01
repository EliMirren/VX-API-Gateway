package com.szmirren.vxApi.core.handler.route;

import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.handler.route.impl.VxApiRouteApiLimitImpl;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * VxApiRoute访问限制处理器
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiRouteHandlerApiLimit extends Handler<RoutingContext> {
	/**
	 * 得到一个默认的访问限制实现
	 * 
	 * @param api
	 * @return
	 */
	static VxApiRouteHandlerApiLimit create(VxApis api) {
		return new VxApiRouteApiLimitImpl(api);
	};
}
