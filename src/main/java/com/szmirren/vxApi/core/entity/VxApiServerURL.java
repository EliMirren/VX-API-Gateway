package com.szmirren.vxApi.core.entity;

import io.vertx.core.json.JsonObject;

/**
 * 服务器地址与权重
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiServerURL {
	private String url;// 路径
	private int weight = 0;// 访问权重

	/**
	 * 将当前对象装换为JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("url", url);
		json.put("weight", weight);
		return json;
	}

	/**
	 * 通过Json配置文件得到一个服务地址对象,如果配置文件为空或者,key:url非String类型报错NullPointerException
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiServerURL fromJson(JsonObject obj) {
		if (obj == null) {
			throw new NullPointerException("服务地址的JSON配置文件不能是null");
		}
		VxApiServerURL option = new VxApiServerURL();
		if (obj.getValue("url") instanceof String) {
			option.setUrl(obj.getString("url"));
		} else {
			throw new NullPointerException("url必须为字符串类型");
		}
		if (obj.getValue("weight") instanceof Number) {
			option.setWeight(((Number) obj.getValue("weight")).intValue());
		}
		return option;
	}

	private VxApiServerURL() {
		super();
	}

	public VxApiServerURL(String url) {
		super();
		this.url = url;
		this.weight = 0;
	}

	public VxApiServerURL(String url, int weight) {
		super();
		this.url = url;
		this.weight = weight;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

}
