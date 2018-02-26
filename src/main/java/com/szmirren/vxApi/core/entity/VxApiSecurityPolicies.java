package com.szmirren.vxApi.core.entity;

import com.szmirren.vxApi.core.enums.TimeUnitEnum;

import io.vertx.core.json.JsonObject;

/**
 * 安全策略
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiSecurityPolicies {
	private String policyName;// 安全策略的名字
	private String policyDescribe;// 安全策略的描述
	private TimeUnitEnum timeUnit;// 安全策略的限制单位
	private long apiLimit;// api的总限制访问数,0等于无限制,默认=0
	private long ipLimit;// ip的限制访问数,必须小于apiLimint,默认与apiLimit一致

	/**
	 * 将对象装换为json
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("policyName", this.policyName);
		json.put("policyDescribe", this.policyDescribe);
		json.put("timeUnit", this.timeUnit);
		json.put("apiLimit", this.apiLimit);
		json.put("ipLimit", this.ipLimit);
		return json;
	}

	/**
	 * 通过json实例化一个对象
	 * 
	 * @param json
	 * @return
	 */
	public static VxApiSecurityPolicies fromJson(JsonObject json) {
		if (json == null) {
			return null;
		}
		VxApiSecurityPolicies option = new VxApiSecurityPolicies();
		if (json.getValue("policyName") instanceof String) {
			option.setPolicyName(json.getString("policyName"));
		}
		if (json.getValue("policyDescribe") instanceof String) {
			option.setPolicyDescribe(json.getString("policyDescribe"));
		}
		if (json.getValue("timeUnit") instanceof String) {
			option.setTimeUnit(TimeUnitEnum.valueOf(json.getString("timeUnit")));
		}
		if (json.getValue("apiLimit") instanceof Number) {
			option.setApiLimit(((Number) json.getValue("apiLimit")).longValue());
		}
		if (json.getValue("ipLimit") instanceof Number) {
			option.setApiLimit(((Number) json.getValue("ipLimit")).longValue());
		}
		return option;
	}

	public VxApiSecurityPolicies() {
		super();
	}

	public VxApiSecurityPolicies(String policyName, String policyDescribe, TimeUnitEnum timeUnit, long apiLimit,
			long ipLimit) {
		super();
		this.policyName = policyName;
		this.policyDescribe = policyDescribe;
		this.timeUnit = timeUnit;
		this.apiLimit = apiLimit;
		this.ipLimit = ipLimit;
	}

	/**
	 * 获得安全策略的名字
	 * 
	 * @return
	 */
	public String getPolicyName() {
		return policyName;
	}

	/**
	 * 设置安全策略的名字
	 * 
	 * @param policyName
	 */
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	/**
	 * 获得安全策略的描述
	 * 
	 * @return
	 */
	public String getPolicyDescribe() {
		return policyDescribe;
	}

	/**
	 * 安全策略的描述
	 * 
	 * @param policyDescribe
	 */
	public void setPolicyDescribe(String policyDescribe) {
		this.policyDescribe = policyDescribe;
	}

	/**
	 * 获得安全策略的限制单位
	 * 
	 * @return
	 */
	public TimeUnitEnum getTimeUnit() {
		return timeUnit;
	}

	/**
	 * 设置安全策略的限制单位
	 * 
	 * @param timeUnit
	 */
	public void setTimeUnit(TimeUnitEnum timeUnit) {
		this.timeUnit = timeUnit;
	}

	/**
	 * 获得api的总限制访问数
	 * 
	 * @return
	 */
	public long getApiLimit() {
		return apiLimit;
	}

	/**
	 * 设置api的总限制访问数,0等于无限制,默认=0
	 * 
	 * @param apiLimit
	 */
	public void setApiLimit(long apiLimit) {
		this.apiLimit = apiLimit;
	}

	/**
	 * 获得ip的限制访问数
	 * 
	 * @return
	 */
	public long getIpLimit() {
		return ipLimit;
	}

	/**
	 * 设置ip的限制访问数,必须小于apiLimint,默认与apiLimit一致
	 * 
	 * @param ipLimit
	 */
	public void setIpLimit(long ipLimit) {
		this.ipLimit = ipLimit;
	}

	@Override
	public String toString() {
		return "VxSecurityPolicies [policyName=" + policyName + ", policyDescribe=" + policyDescribe + ", timeUnit="
				+ timeUnit + ", apiLimit=" + apiLimit + ", ipLimit=" + ipLimit + "]";
	}

}
