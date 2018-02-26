package com.szmirren.vxApi.spi.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * API后置处理器,既API逻辑执行后完毕后做什么事情,<b>
 * 逻辑处理做完后会在RoutingContext中put一个名为prev_is_success类型为{@AsyncResult<Boolean>}结果<b>
 * 可以通过RoutingContext.get得到,key值为当前接口的PREV_IS_SUCCESS_KEY值
 * <b>做完后需要进行响应,比如router.next()或者end()
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiAfterHandler extends Handler<RoutingContext> {
	/**
	 * 逻辑处理器操作结果的key值,用于通过该key值在route上下文中获取逻辑处理器的操作结果
	 */
	static String PREV_IS_SUCCESS_KEY = "prev_is_success";
}
