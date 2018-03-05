package com.szmirren.vxApi.spi.customHandler;

import io.vertx.core.json.JsonObject;

/**
 * 自定义处理器配置信息
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiCustomHandlerOptions {
	private String inFactoryName;// 实现类的名字
	private boolean isNext = false;// 是否有后置处理器,用于判断是响应请求还是将请求传送到下一个处理器
	private JsonObject option;// 配置信息

	/**
	 * 将对象转为JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("inFactoryName", this.inFactoryName);
		json.put("isNext", this.isNext);
		json.put("option", this.option);
		return json;
	}

	/**
	 * 通过Json对象获得一个实例
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiCustomHandlerOptions fromJson(JsonObject obj) {
		if (obj == null) {
			return null;
		}
		VxApiCustomHandlerOptions option = new VxApiCustomHandlerOptions();
		if (obj.getValue("inFactoryName") instanceof String) {
			option.setInFactoryName(obj.getString("inFactoryName"));
		}
		if (obj.getValue("isNext") instanceof Boolean) {
			option.setNext(obj.getBoolean("isNext"));
		}
		if (obj.getValue("option") instanceof JsonObject) {
			option.setOption(obj.getJsonObject("option"));
		}
		return option;
	}

	public VxApiCustomHandlerOptions() {
		super();
	}

	public VxApiCustomHandlerOptions(String inFactoryName, JsonObject option) {
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

	public boolean isNext() {
		return isNext;
	}

	public void setNext(boolean isNext) {
		this.isNext = isNext;
	}

	@Override
	public String toString() {
		return "VxApiCustomHandlerOptions [inFactoryName=" + inFactoryName + ", isNext=" + isNext + ", option=" + option + "]";
	}

}
