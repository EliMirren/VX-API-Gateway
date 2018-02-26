package com.szmirren.vxApi.core.enums;

/**
 * 返回的ContentType类型
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 */
public enum ContentTypeEnum {

	JSON("application/json"), 
	XML("application/xml"),
	HTML("text/html"), 
	TEXT("text/plain"),
	FORM("application/x-www-form-urlencoded"),
	BINARY("application/octet-stream"),
	JSON_UTF8("application/json;charset=UTF-8"), 
	XML_UTF8("application/xml;charset=UTF-8"),
	HTML_UTF8("text/html;charset=UTF-8"), 
	TEXT_UTF8("text/plain;charset=UTF-8"),
	FORM_UTF8("application/x-www-form-urlencoded;charset=UTF-8"),
	BINARY_UTF8("application/octet-stream;charset=UTF-8");

	private String type;

	private ContentTypeEnum(String type) {
		this.type = type;
	}

	public String val() {
		return type;
	}

}
