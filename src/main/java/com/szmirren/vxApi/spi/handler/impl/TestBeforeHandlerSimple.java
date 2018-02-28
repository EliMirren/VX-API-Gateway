package com.szmirren.vxApi.spi.handler.impl;

import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.spi.handler.VxApiBeforeHandler;

import io.vertx.ext.web.RoutingContext;

/**
 * 前置处理器处理示例
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class TestBeforeHandlerSimple implements VxApiBeforeHandler {
	private VxApis apis;

	@Override
	public void handle(RoutingContext event) {
		// 这里做些什么事情后将请求放行到下一个处理器,或者在这里响应请求
		System.out.println("beforeHandler : " + apis);
		event.next();
	}

	public TestBeforeHandlerSimple(VxApis api) {
		super();
		this.apis = api;
	}

}
