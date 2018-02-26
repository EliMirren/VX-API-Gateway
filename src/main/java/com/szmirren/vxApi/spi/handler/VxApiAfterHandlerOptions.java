package com.szmirren.vxApi.spi.handler;

import io.vertx.core.json.JsonObject;

/**
 * API后置处理器共产,通过该名字获得相应的实现类
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiAfterHandlerOptions {
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
	public static VxApiAfterHandlerOptions fromJson(JsonObject obj) {
		if (obj == null) {
			return null;
		}
		VxApiAfterHandlerOptions option = new VxApiAfterHandlerOptions();
		if (obj.getValue("inFactoryName") instanceof String) {
			option.setInFactoryName(obj.getString("inFactoryName"));
		}

		if (obj.getValue("option") instanceof JsonObject) {
			option.setOption(obj.getJsonObject("option"));
		}
		return option;
	}

	public VxApiAfterHandlerOptions() {
		super();
	}

	public VxApiAfterHandlerOptions(String inFactoryName, JsonObject option) {
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

	@Override
	public String toString() {
		return "BeforeHandlerOptions [inFactoryName=" + inFactoryName + ", option=" + option + "]";
	}

}
