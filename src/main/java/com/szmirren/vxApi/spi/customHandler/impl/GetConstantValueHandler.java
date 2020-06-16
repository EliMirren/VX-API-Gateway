package com.szmirren.vxApi.spi.customHandler.impl;

import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.enums.ContentTypeEnum;
import com.szmirren.vxApi.spi.common.HttpHeaderConstant;
import com.szmirren.vxApi.spi.customHandler.VxApiCustomHandler;
import com.szmirren.vxApi.spi.handler.VxApiAfterHandler;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * 返回常量值处理器
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class GetConstantValueHandler implements VxApiCustomHandler {
	/**
	 * 返回值默认等于null
	 */
	private Object value = "null";

	/**
	 * 返回结果定义,默认占位符为$(val)
	 */
	private String resultFormat = "$(val)";
	/**
	 * 路由器要结束了还是讲任务传到下一个处理器,默认为结束
	 */
	private boolean isNext = false;
	/**
	 * 默认的返回类型为json_utf8
	 */
	private String contentType = ContentTypeEnum.JSON_UTF8.val();

	@Override
	public void handle(RoutingContext rct) {
		String result = resultFormat.replace("$(val)", value.toString());
		if (isNext) {
			rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>succeededFuture(true));// 告诉后置处理器当前操作成功执行
			rct.next();
		} else {
			if (!rct.response().ended()) {
				rct.response().putHeader(HttpHeaderConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
						.putHeader(HttpHeaderConstant.CONTENT_TYPE, contentType).end(result);
			}
		}

	}

	/**
	 * 初始化一个常量返回值处理器<br>
	 * option.value 要返回的值,默认值字符串null返回时以该值的toString方法,所以特殊返回值需要实现toString方法<br>
	 * option.resultFormat 格式化返回值,$(val)为值占位,默认值$(val)<br>
	 * option.isNext 是否还有后置处理器,默认false, true处理请求后next,false响应请求<br>
	 * option.contentType 返回的content-type 类型 <@ ContentTypeEnum.JSON_UTF8><br>
	 * 
	 * @param option
	 */
	public GetConstantValueHandler(JsonObject option) {
		if (option.getValue("value") != null) {
			this.value = option.getValue("value");
		}
		if (option.getValue("resultFormat") instanceof String) {
			this.resultFormat = option.getString("resultFormat");
		}
		if (option.getValue("isNext") instanceof Boolean) {
			this.isNext = option.getBoolean("isNext");
		}
		if (option.getValue("contentType") instanceof String) {
			this.contentType = option.getString("contentType");
		}
	}

}
