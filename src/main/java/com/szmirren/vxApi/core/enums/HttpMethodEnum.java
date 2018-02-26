package com.szmirren.vxApi.core.enums;

/**
 * http的请求方式
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public enum HttpMethodEnum {
	ALL("ALL"), OPTIONS("OPTIONS"), GET("GET"),  HEAD("HEAD"), POST("POST"), 
	PUT("PUT"), DELETE("DELETE"), TRACE("TRACE"), CONNECT("CONNECT"), PATCH("PATCH"), OTHER("OTHER");
	private String val;
	private HttpMethodEnum(String val) {
		this.val = val;
	}
	public String getVal() {
		return val;
	}

}
