package com.szmirren.vxApi.core.handler.route;

import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.handler.route.impl.VxApiRouteHandlerRedirectTypeImpl;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * VxApiRoute页面跳转处理器
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiRouteHandlerRedirectType extends Handler<RoutingContext> {
	/**
	 * 得到一个默认的页面跳转处理器实现
	 * 
	 * @param api
	 * @return
	 */
	static VxApiRouteHandlerRedirectType create(boolean isNext,VxApis api) {
		return new VxApiRouteHandlerRedirectTypeImpl(isNext,api);
	};
}
