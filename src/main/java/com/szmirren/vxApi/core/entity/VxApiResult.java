package com.szmirren.vxApi.core.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * API返回结果
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiResult {
	private Set<String> tranHeaders;// 要透传的header
	private String apiEnterCheckFailureExample;// API网关入口参数检查失败返回结果
	private String limitExample;// 访问限制返回结果
	private String successExample;// API请求服务器成功示例
	private String failureExample;// API请求服务器失败示例
	private String cantConnServerExample;// API无法连接上后台服务器失败示例
	private List<VxApiResultStatus> status;// 状态码集合

	/**
	 * 将对象转换为JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		if (tranHeaders != null) {
			JsonArray array = new JsonArray();
			this.tranHeaders.forEach(va -> {
				array.add(va);
			});
			json.put("tranHeaders", array);
		}
		if (limitExample != null) {
			json.put("limitExample", this.limitExample);
		}
		if (apiEnterCheckFailureExample != null) {
			json.put("apiEnterCheckFailureExample", this.apiEnterCheckFailureExample);
		}
		if (successExample != null) {
			json.put("successExample", this.successExample);
		}
		if (failureExample != null) {
			json.put("failureExample", this.failureExample);
		}
		if (cantConnServerExample != null) {
			json.put("cantConnServerExample", this.cantConnServerExample);
		}
		if (status != null) {
			JsonArray array = new JsonArray();
			status.forEach(va -> {
				array.add(va.toJson());
			});
			json.put("status", array);
		}
		return json;
	}

	/**
	 * 通过json实例化一个对象
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiResult fromJson(JsonObject obj) {
		if (obj == null) {
			return null;
		}
		VxApiResult option = new VxApiResult();
		if (obj.getValue("tranHeaders") instanceof JsonArray) {
			Set<String> set = new HashSet<>();
			obj.getJsonArray("tranHeaders").forEach(va -> {
				set.add(va.toString());
			});
			option.setTranHeaders(set);
		}
		if (obj.getValue("apiEnterCheckFailureExample") instanceof String) {
			option.setApiEnterCheckFailureExample(obj.getString("apiEnterCheckFailureExample"));
		}
		if (obj.getValue("limitExample") instanceof String) {
			option.setLimitExample(obj.getString("limitExample"));
		}
		if (obj.getValue("successExample") instanceof String) {
			option.setSuccessExample(obj.getString("successExample"));
		}
		if (obj.getValue("failureExample") instanceof String) {
			option.setFailureExample(obj.getString("failureExample"));
		}
		if (obj.getValue("cantConnServerExample") instanceof String) {
			option.setCantConnServerExample(obj.getString("cantConnServerExample"));
		}
		if (obj.getValue("status") instanceof JsonArray) {
			List<VxApiResultStatus> list = new ArrayList<>();
			obj.getJsonArray("status").forEach(va -> {
				if (va instanceof JsonObject) {
					list.add(VxApiResultStatus.fromJson((JsonObject) va));
				}
			});
			option.setStatus(list);
		}
		return option;
	}

	public VxApiResult() {
		super();
	}

	/**
	 * 获得需要透传的header值
	 * 
	 * @return
	 */
	public Set<String> getTranHeaders() {
		return tranHeaders;
	}

	/**
	 * 设置需要透传的header值
	 * 
	 * @param tranHeaders
	 */
	public void setTranHeaders(Set<String> tranHeaders) {
		this.tranHeaders = tranHeaders;
	}

	/**
	 * 获得API网关入口参数检查失败返回结果
	 * 
	 * @return
	 */
	public String getApiEnterCheckFailureExample() {
		return apiEnterCheckFailureExample;
	}

	/**
	 * 设置API网关入口参数检查失败返回结果
	 * 
	 * @param apiEnterCheckFailureExample
	 */
	public void setApiEnterCheckFailureExample(String apiEnterCheckFailureExample) {
		this.apiEnterCheckFailureExample = apiEnterCheckFailureExample;
	}

	/**
	 * 获得API访问限制返回结果
	 * 
	 * @return
	 */
	public String getLimitExample() {
		return limitExample;
	}

	/**
	 * 设置API访问限制返回结果
	 * 
	 * @param limitExample
	 */
	public void setLimitExample(String limitExample) {
		this.limitExample = limitExample;
	}

	/**
	 * 获得接口调用成功返回示例
	 * 
	 * @return
	 */
	public String getSuccessExample() {
		return successExample;
	}

	/**
	 * 设置接口调用成功返回示例
	 * 
	 * @param successExample
	 */
	public void setSuccessExample(String successExample) {
		this.successExample = successExample;
	}

	/**
	 * 得到接口调用失败返回示例
	 * 
	 * @return
	 */
	public String getFailureExample() {
		return failureExample;
	}

	/**
	 * 设置接口调用失败示例
	 * 
	 * @param failureExample
	 */
	public void setFailureExample(String failureExample) {
		this.failureExample = failureExample;
	}

	/**
	 * 获得API无法连接上后台服务器失败示例
	 * 
	 * @return
	 */
	public String getCantConnServerExample() {
		return cantConnServerExample;
	}

	/**
	 * 设置API无法连接上后台服务器失败示例
	 * 
	 * @param cantConnServerExample
	 */
	public void setCantConnServerExample(String cantConnServerExample) {
		this.cantConnServerExample = cantConnServerExample;
	}

	/**
	 * 获得返回状态码集合
	 * 
	 * @return
	 */
	public List<VxApiResultStatus> getStatus() {
		return status;
	}

	/**
	 * 设置返回状态码集合
	 * 
	 * @param status
	 */
	public void setStatus(List<VxApiResultStatus> status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "VxApiResult [tranHeaders=" + tranHeaders + ", apiEnterCheckFailureExample="
				+ apiEnterCheckFailureExample + ", limitExample=" + limitExample + ", successExample=" + successExample
				+ ", failureExample=" + failureExample + ", cantConnServerExample=" + cantConnServerExample
				+ ", status=" + status + "]";
	}

}
