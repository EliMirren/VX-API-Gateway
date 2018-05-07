package com.szmirren.vxApi.core.options;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.szmirren.vxApi.core.entity.VxApiEntranceParam;
import com.szmirren.vxApi.core.entity.VxApiResult;
import com.szmirren.vxApi.core.entity.VxApiServerEntrance;
import com.szmirren.vxApi.core.enums.HttpMethodEnum;
import com.szmirren.vxApi.core.enums.TimeUnitEnum;
import com.szmirren.vxApi.spi.auth.VxApiAuthOptions;
import com.szmirren.vxApi.spi.handler.VxApiAfterHandlerOptions;
import com.szmirren.vxApi.spi.handler.VxApiBeforeHandlerOptions;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * API类
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApisDTO {
	/** 网关应用的名字 */
	private String appName;
	/** API的名字 */
	private String apiName;
	/** API的描述 */
	private String apiDescribe;
	/** 接口路径 */
	private String path;
	/** 返回contentType值 */
	private String contentType;
	/** API的创建时间 */
	private Instant apiCreateTime;
	/** 请求采用的方式 */
	private HttpMethodEnum method;
	/** 流量限制的策略 */
	private TimeUnitEnum limitUnit;
	/** API访问限制次数 */
	private long apiLimit = -1;
	/** IP访问限制次数 */
	private long ipLimit = -1;
	/** 是否透传body */
	private boolean passBody;
	/** 是否将body的参数映射到query允许被query访问 */
	private boolean bodyAsQuery = true;
	/** 处理的请求类型consumes */
	private Set<String> consumes;
	/** 权限认证配置信息 */
	private VxApiAuthOptions authOptions;
	/** 前置处理器配置信息 */
	private VxApiBeforeHandlerOptions beforeHandlerOptions;
	/** 后置处理器配置信息 */
	private VxApiAfterHandlerOptions afterHandlerOptions;
	/** 参数的配置 */
	private List<VxApiEntranceParam> enterParam;
	/** API 服务端入口 */
	private VxApiServerEntrance serverEntrance;
	/** API返回结果 */
	private VxApiResult result;

	/**
	 * 将对象转换为JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("appName", this.appName);
		json.put("apiName", this.apiName);
		json.put("apiDescribe", this.apiDescribe);
		json.put("apiCreateTime", this.apiCreateTime);
		json.put("path", this.path);
		if (method != null) {
			json.put("method", this.method);
		}
		if (contentType != null) {
			json.put("contentType", this.contentType);
		}
		if (consumes != null) {
			JsonArray array = new JsonArray();
			this.consumes.forEach(va -> {
				array.add(va);
			});
			json.put("consumes", array);
		}
		if (limitUnit != null) {
			json.put("limitUnit", this.limitUnit);
		}
		json.put("apiLimit", this.apiLimit);
		json.put("ipLimit", this.ipLimit);
		json.put("passBody", passBody);
		json.put("bodyAsQuery", bodyAsQuery);
		if (authOptions != null) {
			json.put("authOptions", authOptions.toJson());
		}
		if (beforeHandlerOptions != null) {
			json.put("beforeHandlerOptions", beforeHandlerOptions.toJson());
		}
		if (afterHandlerOptions != null) {
			json.put("afterHandlerOptions", afterHandlerOptions.toJson());
		}
		if (enterParam != null) {
			JsonArray array = new JsonArray();
			this.enterParam.forEach(va -> {
				array.add(va.toJson());
			});
			json.put("enterParam", array);
		}
		if (serverEntrance != null) {
			json.put("serverEntrance", serverEntrance.toJson());
		}
		if (result != null) {
			json.put("result", result.toJson());
		}
		return json;
	}

	/**
	 * 通过JsonObject实例化一个对象
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApisDTO fromJson(JsonObject obj) {
		if (obj == null) {
			return null;
		}
		VxApisDTO option = new VxApisDTO();
		if (obj.getValue("appName") instanceof String) {
			option.setAppName(obj.getString("appName").trim());
		}
		if (obj.getValue("apiName") instanceof String) {
			option.setApiName(obj.getString("apiName").trim());
		}
		if (obj.getValue("apiDescribe") instanceof String) {
			option.setApiDescribe(obj.getString("apiDescribe").trim());
		}
		if (obj.getValue("path") instanceof String) {
			option.setPath(obj.getString("path").trim());
		}
		if (obj.getValue("contentType") instanceof String) {
			option.setContentType(obj.getString("contentType").trim());
		}
		if (obj.getValue("apiCreateTime") instanceof Instant) {
			option.setApiCreateTime(obj.getInstant("apiCreateTime"));
		} else if (obj.getValue("apiCreateTime") instanceof String) {
			option.setApiCreateTime(Instant.parse(obj.getString("apiCreateTime").trim()));
		}
		if (obj.getValue("limitUnit") instanceof String) {
			option.setLimitUnit(TimeUnitEnum.valueOf(obj.getString("limitUnit").trim()));
		}
		if (obj.getValue("apiLimit") instanceof Number) {
			option.setApiLimit(((Number) obj.getValue("apiLimit")).longValue());
		}
		if (obj.getValue("ipLimit") instanceof Number) {
			option.setIpLimit(((Number) obj.getValue("ipLimit")).longValue());
		}
		if (obj.getValue("passBody") instanceof Boolean) {
			option.setPassBody(obj.getBoolean("passBody"));
		}
		if (obj.getValue("bodyAsQuery") instanceof Boolean) {
			option.setBodyAsQuery(obj.getBoolean("bodyAsQuery"));
		}
		if (obj.getValue("method") instanceof String) {
			option.setMethod(HttpMethodEnum.valueOf(obj.getString("method").trim()));
		}
		if (obj.getValue("consumes") instanceof JsonArray) {
			Set<String> set = new HashSet<>();
			obj.getJsonArray("consumes").forEach(va -> {
				if (va instanceof String) {
					set.add(va.toString().trim());
				}
			});
			option.setConsumes(set);
		}
		if (obj.getValue("authOptions") instanceof JsonObject) {
			option.setAuthOptions(VxApiAuthOptions.fromJson(obj.getJsonObject("authOptions")));
		}
		if (obj.getValue("beforeHandlerOptions") instanceof JsonObject) {
			option.setBeforeHandlerOptions(VxApiBeforeHandlerOptions.fromJson(obj.getJsonObject("beforeHandlerOptions")));
		}
		if (obj.getValue("afterHandlerOptions") instanceof JsonObject) {
			option.setAfterHandlerOptions(VxApiAfterHandlerOptions.fromJson(obj.getJsonObject("afterHandlerOptions")));
		}
		if (obj.getValue("enterParam") instanceof JsonArray) {
			List<VxApiEntranceParam> list = new ArrayList<>();
			obj.getJsonArray("enterParam").forEach(va -> {
				if (va instanceof JsonObject) {
					list.add(VxApiEntranceParam.fromJson((JsonObject) va));
				}
			});
			option.setEnterParam(list);
		}
		if (obj.getValue("serverEntrance") instanceof JsonObject) {
			option.setServerEntrance(VxApiServerEntrance.fromJson(obj.getJsonObject("serverEntrance")));
		}
		if (obj.getValue("result") instanceof JsonObject) {
			option.setResult(VxApiResult.fromJson(obj.getJsonObject("result")));
		}
		return option;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getApiDescribe() {
		return apiDescribe;
	}

	public void setApiDescribe(String apiDescribe) {
		this.apiDescribe = apiDescribe;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Instant getApiCreateTime() {
		return apiCreateTime;
	}

	public void setApiCreateTime(Instant apiCreateTime) {
		this.apiCreateTime = apiCreateTime;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public TimeUnitEnum getLimitUnit() {
		return limitUnit;
	}

	public void setLimitUnit(TimeUnitEnum limitUnit) {
		this.limitUnit = limitUnit;
	}

	public long getApiLimit() {
		return apiLimit;
	}

	public long getIpLimit() {
		return ipLimit;
	}

	public boolean isPassBody() {
		return passBody;
	}

	public void setPassBody(boolean passBody) {
		this.passBody = passBody;
	}

	public boolean isBodyAsQuery() {
		return bodyAsQuery;
	}

	public void setBodyAsQuery(boolean bodyAsQuery) {
		this.bodyAsQuery = bodyAsQuery;
	}

	public HttpMethodEnum getMethod() {
		return method;
	}

	public void setMethod(HttpMethodEnum method) {
		this.method = method;
	}

	public Set<String> getConsumes() {
		return consumes;
	}

	public void setConsumes(Set<String> consumes) {
		this.consumes = consumes;
	}

	public VxApiAuthOptions getAuthOptions() {
		return authOptions;
	}

	public void setAuthOptions(VxApiAuthOptions authOptions) {
		this.authOptions = authOptions;
	}

	public VxApiBeforeHandlerOptions getBeforeHandlerOptions() {
		return beforeHandlerOptions;
	}

	public void setBeforeHandlerOptions(VxApiBeforeHandlerOptions beforeHandlerOptions) {
		this.beforeHandlerOptions = beforeHandlerOptions;
	}

	public VxApiAfterHandlerOptions getAfterHandlerOptions() {
		return afterHandlerOptions;
	}

	public void setAfterHandlerOptions(VxApiAfterHandlerOptions afterHandlerOptions) {
		this.afterHandlerOptions = afterHandlerOptions;
	}

	public void setApiLimit(long apiLimit) {
		this.apiLimit = apiLimit;
	}

	public void setIpLimit(long ipLimit) {
		this.ipLimit = ipLimit;
	}

	public List<VxApiEntranceParam> getEnterParam() {
		return enterParam;
	}

	public void setEnterParam(List<VxApiEntranceParam> enterParam) {
		this.enterParam = enterParam;
	}

	public VxApiServerEntrance getServerEntrance() {
		return serverEntrance;
	}

	public void setServerEntrance(VxApiServerEntrance serverEntrance) {
		this.serverEntrance = serverEntrance;
	}

	public VxApiResult getResult() {
		return result;
	}

	public void setResult(VxApiResult result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "VxApisDTO [appName=" + appName + ", apiName=" + apiName + ", apiDescribe=" + apiDescribe + ", path=" + path + ", contentType="
				+ contentType + ", apiCreateTime=" + apiCreateTime + ", method=" + method + ", limitUnit=" + limitUnit + ", apiLimit=" + apiLimit
				+ ", ipLimit=" + ipLimit + ", passBody=" + passBody + ", bodyAsQuery=" + bodyAsQuery + ", consumes=" + consumes + ", authOptions="
				+ authOptions + ", beforeHandlerOptions=" + beforeHandlerOptions + ", afterHandlerOptions=" + afterHandlerOptions + ", enterParam="
				+ enterParam + ", serverEntrance=" + serverEntrance + ", result=" + result + "]";
	}

}
