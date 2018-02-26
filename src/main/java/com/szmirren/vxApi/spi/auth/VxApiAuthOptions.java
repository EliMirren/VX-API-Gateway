package com.szmirren.vxApi.spi.auth;

import io.vertx.core.json.JsonObject;

/**
 * 权限认证的配置信息
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiAuthOptions {
	private String inFactoryName;// 实现类的名字
	private JsonObject option;// 配置信息

	/**
	 * 将对象转为JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("inFactoryName", this.inFactoryName);
		json.put("option", this.option);
		return json;
	}

	/**
	 * 通过Json对象获得一个实例
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiAuthOptions fromJson(JsonObject obj) {
		if (obj == null) {
			return null;
		}
		VxApiAuthOptions option = new VxApiAuthOptions();
		if (obj.getValue("inFactoryName") instanceof String) {
			option.setInFactoryName(obj.getString("inFactoryName"));
		}

		if (obj.getValue("option") instanceof JsonObject) {
			option.setOption(obj.getJsonObject("option"));
		}
		return option;
	}

	private VxApiAuthOptions() {
		super();
	}

	public VxApiAuthOptions(String inFactoryName, JsonObject option) {
		super();
		this.inFactoryName = inFactoryName;
		this.option = option;
	}

	public String getInFactoryName() {
		return inFactoryName;
	}

	public void setInFactoryName(String inFactoryName) {
		this.inFactoryName = inFactoryName;
	}

	public JsonObject getOption() {
		return option;
	}

	public void setOption(JsonObject option) {
		this.option = option;
	}

}
