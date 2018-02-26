package com.szmirren.vxApi.core.common;

import io.vertx.core.json.Json;

/**
 * 
 * @author Mirren
 *
 */
public class ResultFormatObj {
	private int status;// 状态码
	private String msg;// 信息
	private Object data;// 数据

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String toJsonStr() {
		return Json.encode(this);
	}

	public ResultFormatObj() {
		super();
	}

	public ResultFormatObj(int status, String msg, Object data) {
		super();
		this.msg = msg;
		this.status = status;
		this.data = data;
	}

}
