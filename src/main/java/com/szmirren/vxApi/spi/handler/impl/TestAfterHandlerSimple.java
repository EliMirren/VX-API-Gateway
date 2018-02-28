package com.szmirren.vxApi.spi.handler.impl;

import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.spi.handler.VxApiAfterHandler;

import io.vertx.core.AsyncResult;
import io.vertx.ext.web.RoutingContext;

/**
 * 后置处理器处理示例
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class TestAfterHandlerSimple implements VxApiAfterHandler {
	private VxApis apis;

	@Override
	public void handle(RoutingContext event) {
		// 这里得到主处理器的执行结果后可以做一些相应的处理,处理完毕后响应请求
		AsyncResult<Boolean> result = event.<AsyncResult<Boolean>>get(PREV_IS_SUCCESS_KEY);
		if (result.succeeded()) {
			System.out.println("AfterHandler :　" + apis);
		} else {
			System.out.println("AfterHandler error :　" + result.cause());
		}
		event.response().end();
	}

	public TestAfterHandlerSimple(VxApis api) {
		super();
		this.apis = api;
	}

}
