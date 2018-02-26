package com.szmirren.vxApi.core.options;

import java.util.ArrayList;
import java.util.List;

import com.szmirren.vxApi.core.entity.VxApiServerURL;
import com.szmirren.vxApi.core.enums.LoadBalanceEnum;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Api服务端处理类型为HTTP或者HTTPS
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiServerEntranceHttpOptions {
	/**
	 * 默认的请求超时时间
	 */
	private final long DEFAULT_TIME_OUT = 6000L;
	/**
	 * 默认的请求超时时间
	 */
	private final long DEFAULT_RETRY_TIME = 30000L;

	private LoadBalanceEnum balanceType;// 负载均衡类型
	private List<VxApiServerURL> serverUrls;// 服务器的服务路径
	private HttpMethod method;// 请求的方式
	private long timeOut = DEFAULT_TIME_OUT;// 连接超时时间默认6000ms
	private long retryTime = DEFAULT_RETRY_TIME;// 当服务连接不可用时,重试连接服务的间隔时间,默认30000ms
	private List<VxApiParamOptions> params;// 参数集

	/**
	 * 将对象转换为JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		if (balanceType != null) {
			json.put("balanceType", this.balanceType);
		}
		if (this.serverUrls != null) {
			JsonArray array = new JsonArray();
			this.serverUrls.forEach(va -> {
				array.add(va.toJson());
			});
			json.put("serverUrls", array);
		}
		if (method != null) {
			json.put("method", this.method);
		}
		json.put("timeOut", this.timeOut);
		json.put("retryTime", this.retryTime);
		if (params != null) {
			JsonArray array = new JsonArray();
			params.forEach(va -> {
				array.add(va.toJson());
			});
			json.put("params", array);
		}
		return json;
	}

	/**
	 * 通过一个JsonObject获得一个实例
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiServerEntranceHttpOptions fromJson(JsonObject obj) {
		if (obj == null) {
			return null;
		}
		VxApiServerEntranceHttpOptions option = new VxApiServerEntranceHttpOptions();
		if (obj.getValue("balanceType") instanceof String) {
			option.setBalanceType(LoadBalanceEnum.valueOf(obj.getString("balanceType")));
		}
		if (obj.getValue("serverUrls") instanceof JsonArray) {
			List<VxApiServerURL> list = new ArrayList<>();
			obj.getJsonArray("serverUrls").forEach(va -> {
				if (va instanceof JsonObject) {
					list.add(VxApiServerURL.fromJson((JsonObject) va));
				} else if (va instanceof String) {
					list.add(VxApiServerURL.fromJson(new JsonObject(va.toString())));
				}
			});
			option.setServerUrls(list);
		}
		if (obj.getValue("method") instanceof String) {
			option.setMethod(HttpMethod.valueOf(obj.getString("method")));
		}
		if (obj.getValue("timeOut") instanceof Number) {
			option.setTimeOut(((Number) obj.getValue("timeOut")).longValue());
		}
		if (obj.getValue("retryTime") instanceof Number) {
			option.setRetryTime(((Number) obj.getValue("retryTime")).longValue());
		}
		if (obj.getValue("params") instanceof JsonArray) {
			List<VxApiParamOptions> params = new ArrayList<>();
			obj.getJsonArray("params").forEach(va -> {
				params.add(VxApiParamOptions.fromJson((JsonObject) va));
			});
			option.setParams(params);
		}

		return option;
	}

	private VxApiServerEntranceHttpOptions() {
		super();
	}

	/**
	 * 获得负责均衡类型
	 * 
	 * @return
	 */
	public LoadBalanceEnum getBalanceType() {
		return balanceType;
	}

	/**
	 * 设置负载均衡类型
	 * 
	 * @param balanceType
	 */
	public void setBalanceType(LoadBalanceEnum balanceType) {
		this.balanceType = balanceType;
	}

	/**
	 * 获得服务器的服务路径
	 * 
	 * @return
	 */
	public List<VxApiServerURL> getServerUrls() {
		return serverUrls;
	}

	/**
	 * 设置服务器的服务路径
	 * 
	 * @param serverUrls
	 */
	public void setServerUrls(List<VxApiServerURL> serverUrls) {
		this.serverUrls = serverUrls;
	}

	/**
	 * 获得请求方式
	 * 
	 * @return
	 */
	public HttpMethod getMethod() {
		return method;
	}

	/**
	 * 设置请求方式
	 * 
	 * @param method
	 */
	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	/**
	 * 获取请求超时时间
	 * 
	 * @return
	 */
	public long getTimeOut() {
		return timeOut;
	}

	/**
	 * 设置请求超时时间(ms),如果时间小于0则使用默认时间,默认6000ms
	 * 
	 * @param timeOut
	 */
	public void setTimeOut(long timeOut) {
		if (timeOut < 0) {
			this.timeOut = DEFAULT_TIME_OUT;
		} else {
			this.timeOut = timeOut;
		}
	}

	/**
	 * 获得重试连接失效服务的时间
	 * 
	 * @return
	 */
	public long getRetryTime() {
		return retryTime;
	}

	/**
	 * 设置重试连接失效服务的时间(ms),如果时间小于0则使用默认时间,默认30000ms
	 * 
	 * @param retryTime
	 */
	public void setRetryTime(long retryTime) {
		if (retryTime < 0) {
			this.retryTime = DEFAULT_RETRY_TIME;
		} else {
			this.retryTime = retryTime;
		}
	}

	/**
	 * 获得参数集
	 * 
	 * @return
	 */
	public List<VxApiParamOptions> getParams() {
		return params;
	}

	/**
	 * 设置参数集
	 * 
	 * @param params
	 */
	public void setParams(List<VxApiParamOptions> params) {
		this.params = params;
	}

}
