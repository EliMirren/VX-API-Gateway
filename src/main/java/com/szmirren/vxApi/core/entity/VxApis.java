package com.szmirren.vxApi.core.entity;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.szmirren.vxApi.core.enums.HttpMethodEnum;
import com.szmirren.vxApi.core.enums.TimeUnitEnum;
import com.szmirren.vxApi.core.options.VxApisDTO;
import com.szmirren.vxApi.spi.auth.VxApiAuthOptions;
import com.szmirren.vxApi.spi.handler.VxApiAfterHandlerOptions;
import com.szmirren.vxApi.spi.handler.VxApiBeforeHandlerOptions;

/**
 * API类
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApis {
	private String appName;// 应用网关的名字
	private String apiName;// API的名字
	private String apiDescribe;// API的描述
	private Instant apiCreateTime;// API的创建时间
	private TimeUnitEnum limitUnit;// 访问限制单位
	private long apiLimit;// API访问限制次数
	private long ipLimit;// IP访问限制次数
	private String path;// 接口路径
	private VxApiAuthOptions authOptions;// 权限配置信息
	private VxApiBeforeHandlerOptions beforeHandlerOptions;// 前置处理器配置信息
	private VxApiAfterHandlerOptions afterHandlerOptions;// 后置处理器配置信息
	private String contentType;// 返回的Content-Type类型
	private Set<String> consumes;// 接口consumes
	private HttpMethodEnum method;// 请求方法类型
	private List<VxApiEntranceParam> enterParam;// 参数的配置
	private VxApiServerEntrance serverEntrance;// API 服务端入口
	private VxApiResult result;// API返回结果

	public VxApis(VxApisDTO option) {
		super();
		if (option == null) {
			return;
		}
		this.appName = option.getAppName();
		this.apiName = option.getApiName();
		this.apiDescribe = option.getApiDescribe();
		this.apiCreateTime = option.getApiCreateTime();
		this.limitUnit = option.getLimitUnit();
		this.apiLimit = option.getApiLimit();
		this.ipLimit = option.getIpLimit();
		this.authOptions = option.getAuthOptions();
		this.beforeHandlerOptions = option.getBeforeHandlerOptions();
		this.afterHandlerOptions = option.getAfterHandlerOptions();
		this.contentType = option.getContentType();
		this.consumes = option.getConsumes();
		this.method = option.getMethod();
		this.path = option.getPath();
		this.enterParam = option.getEnterParam();
		this.serverEntrance = option.getServerEntrance();
		this.result = option.getResult();
		// 判断是否带有/杠,如果没有就加上
		if (this.path.charAt(0) != '/') {
			this.path = "/" + this.path;
		}
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

	public Instant getApiCreateTime() {
		return apiCreateTime;
	}

	public void setApiCreateTime(Instant apiCreateTime) {
		this.apiCreateTime = apiCreateTime;
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

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Set<String> getConsumes() {
		return consumes;
	}

	public void setConsumes(Set<String> consumes) {
		this.consumes = consumes;
	}

	public HttpMethodEnum getMethod() {
		return method;
	}

	public void setMethod(HttpMethodEnum method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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
		return "VxApis [apiName=" + apiName + ", apiDescribe=" + apiDescribe + ", apiCreateTime=" + apiCreateTime
				+ ", limitUnit=" + limitUnit + ", apiLimit=" + apiLimit + ", ipLimit=" + ipLimit + ", path=" + path
				+ ", authOptions=" + authOptions + ", beforeHandlerOptions=" + beforeHandlerOptions
				+ ", afterHandlerOptions=" + afterHandlerOptions + ", contentType=" + contentType + ", consumes="
				+ consumes + ", method=" + method + ", enterParam=" + enterParam + ", serverEntrance=" + serverEntrance
				+ ", result=" + result + "]";
	}

}
