package com.szmirren.vxApi.spi.auth;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * 通用认证接口
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiAuth extends Handler<RoutingContext> {
	
}
