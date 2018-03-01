package com.szmirren.vxApi.core.handler.route;

import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.handler.route.impl.VxApiRouteHandlerParamCheckImpl;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * VxApiRoute参数检查处理器
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiRouteHandlerParamCheck extends Handler<RoutingContext> {
	/**
	 * 得到一个默认的参数检查处理器实现
	 * 
	 * @param api
	 * @return
	 */
	static VxApiRouteHandlerParamCheck create(VxApis api) {
		return new VxApiRouteHandlerParamCheckImpl(api);
	};
}
