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
	 *          API配置文件
	 * @param maxContentLength
	 *          请求体的长度限制参数的长度
	 * @return
	 */
	static VxApiRouteHandlerParamCheck create(VxApis api, long maxContentLength) {
		return new VxApiRouteHandlerParamCheckImpl(api, maxContentLength);
	};
}
