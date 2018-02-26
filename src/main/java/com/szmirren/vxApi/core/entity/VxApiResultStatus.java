package com.szmirren.vxApi.core.entity;

import io.vertx.core.json.JsonObject;

/**
 * API返回结果状态码
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiResultStatus {
	private String code;// 状态码
	private String msg;// 错误信息
	private String describe;// 描述

	/**
	 * 将对象装换为JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		if (code != null) {
			json.put("code", this.code);
		}
		if (msg != null) {
			json.put("msg", this.msg);
		}
		if (describe != null) {
			json.put("describe", this.describe);
		}
		return json;
	}

	/**
	 * 通过JsonObject实例化一个对象
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiResultStatus fromJson(JsonObject obj) {
		if (obj == null) {
			return null;
		}
		VxApiResultStatus option = new VxApiResultStatus();
		if (obj.getValue("code") instanceof String) {
			option.setCode(obj.getString("code"));
		}
		if (obj.getValue("msg") instanceof String) {
			option.setMsg(obj.getString("msg"));
		}
		if (obj.getValue("describe") instanceof String) {
			option.setDescribe(obj.getString("describe"));
		}
		return option;
	}

	public VxApiResultStatus() {
		super();
	}

	public VxApiResultStatus(String code, String msg, String describe) {
		super();
		this.code = code;
		this.msg = msg;
		this.describe = describe;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	@Override
	public String toString() {
		return "VxApiResultStatus [code=" + code + ", msg=" + msg + ", describe=" + describe + "]";
	}

}
