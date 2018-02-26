package com.szmirren.vxApi.core.enums;

/**
 * HTTP状态码与状态信息的枚举类
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public enum HTTPStatusCodeMsgEnum {
	/**
	 * ok
	 */
	C200(200, "ok"),
	/**
	 * Bad Request
	 */
	C400(400, "Bad Request"),
	/**
	 * Unauthorized
	 */
	C401(401, "Unauthorized"),
	/**
	 * Forbidden
	 */
	C403(403, "Forbidden"),
	/**
	 * Not Found
	 */
	C404(404, "Not Found"),
	/**
	 * Internal Server Error
	 */
	C500(500, "Internal Server Error"),
	/**
	 * Server Unavailable
	 */
	C503(503, "Server Unavailable"),
	/**
	 * 空指针异常
	 */
	C1000(1000, "NullPointerException"),
	/**
	 * 文件中缺少比用参数或者参数不正确
	 */
	C1400(1400, "Lack of request parameters or parameters is invalid"),
	/**
	 * 没有找到指定文件
	 */
	C1404(1404, "No file in path can be found"),
	/**
	 * 错误的json格式文件
	 */
	C1405(1405, "The specified JSON file is wrong"),
	/**
	 * 文件已经存在或者数据已经存在
	 */
	C1444(1444, "The data has already existed"),

	/**
	 * 当前端口被占用
	 */
	C1111(1111, "Address already in use: bind"),
	/**
	 * 未知错误
	 */
	C1999(1999, "Unknown error"),;
	// 状态码
	private int code;
	private String msg;

	HTTPStatusCodeMsgEnum(int code, String msg) {
		this.msg = msg;
		this.code = code;
	}

	/**
	 * 得到状态码
	 * 
	 * @return
	 */
	public int getCode() {
		return code;
	}

	/**
	 * 获得状态码相应的信息
	 * 
	 * @return
	 */
	public String getMsg() {
		return msg;
	}

}
