package com.szmirren.vxApi.core.enums;

/**
 * 返回的ContentType类型
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 */
public enum ContentTypeEnum {
	/** 内容: application/json */
	JSON("application/json"),
	/** 内容: application/xml */
	XML("application/xml"),
	/** 内容: text/html */
	HTML("text/html"),
	/** 内容: text/plain */
	TEXT("text/plain"),
	/** 内容: application/x-www-form-urlencoded */
	FORM("application/x-www-form-urlencoded"),
	/** 内容: application/x-www-form-urlencoded */
	APPLICATION_X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded"),
	/** 内容: multipart/form-data */
	MULTIPART_FORM_DATA("multipart/form-data"),
	/** 内容: application/octet-stream */
	BINARY("application/octet-stream"),
	/** 内容: application/json;charset=UTF-8 */
	JSON_UTF8("application/json;charset=UTF-8"),
	/** 内容: application/xml;charset=UTF-8 */
	XML_UTF8("application/xml;charset=UTF-8"),
	/** 内容: text/html;charset=UTF-8 */
	HTML_UTF8("text/html;charset=UTF-8"),
	/** 内容: text/plain;charset=UTF-8 */
	TEXT_UTF8("text/plain;charset=UTF-8"),
	/** 内容: application/x-www-form-urlencoded;charset=UTF-8 */
	FORM_UTF8("application/x-www-form-urlencoded;charset=UTF-8"),
	/** 内容: application/octet-stream;charset=UTF-8 */
	BINARY_UTF8("application/octet-stream;charset=UTF-8"),
	/** 内容: Content-Type */
	CONTENT_TYPE("Content-Type");

	private String type;

	private ContentTypeEnum(String type) {
		this.type = type;
	}

	public String val() {
		return type;
	}

}
