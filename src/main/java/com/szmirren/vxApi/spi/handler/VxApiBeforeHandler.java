package com.szmirren.vxApi.spi.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * API前置处理器,既API执行前做什么事情
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiBeforeHandler extends Handler<RoutingContext> {

}
