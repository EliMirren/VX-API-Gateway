package com.szmirren.vxApi.spi.customHandler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * 拓展处理器,用于做某些自定义的业务
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiCustomHandler extends Handler<RoutingContext> {
}
