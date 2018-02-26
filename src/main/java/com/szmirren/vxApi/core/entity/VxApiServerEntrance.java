package com.szmirren.vxApi.core.entity;

import com.szmirren.vxApi.core.enums.ApiServerTypeEnum;

import io.vertx.core.json.JsonObject;

/**
 * Api服务端入口
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiServerEntrance {

	private ApiServerTypeEnum serverType;// api的服务类型
	private JsonObject body;// 存储后端服务的json,根据serverType而实例化相应的服务操作对象

	/**
	 * 将对象转换为JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("serverType", this.serverType);
		json.put("body", this.body);
		return json;
	}

	/**
	 * 通过一个JsonObject获得一个实例
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiServerEntrance fromJson(JsonObject obj) {
		if (obj == null) {
			return null;
		}
		VxApiServerEntrance option = new VxApiServerEntrance();
		if (obj.getValue("serverType") instanceof String) {
			option.setServerType(ApiServerTypeEnum.valueOf(obj.getString("serverType")));
		}
		if (obj.getValue("body") instanceof JsonObject) {
			option.setBody(obj.getJsonObject("body"));
		}
		return option;
	}

	public ApiServerTypeEnum getServerType() {
		return serverType;
	}

	public void setServerType(ApiServerTypeEnum serverType) {
		this.serverType = serverType;
	}

	public JsonObject getBody() {
		return body;
	}

	public void setBody(JsonObject body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "VxApiServerEntrance [serverType=" + serverType + ", body=" + body + "]";
	}

}
