package com.szmirren.vxApi.core.entity;

import java.util.List;

import com.szmirren.vxApi.core.enums.HttpMethodEnum;

/**
 * VxApi的入口
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiApiEntrance {
	private String path;// 请求路径
	private List<HttpMethodEnum> methods;// 请求的方法
	private List<VxApiEntranceParam> paramOptions;// 参数的配置

	public VxApiApiEntrance() {
		super();
	}

	public VxApiApiEntrance(String path, List<HttpMethodEnum> methods, List<VxApiEntranceParam> paramOptions) {
		super();
		this.path = path;
		this.methods = methods;
		this.paramOptions = paramOptions;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<HttpMethodEnum> getMethods() {
		return methods;
	}

	public void setMethods(List<HttpMethodEnum> methods) {
		this.methods = methods;
	}

	public List<VxApiEntranceParam> getParamOptions() {
		return paramOptions;
	}

	public void setParamOptions(List<VxApiEntranceParam> paramOptions) {
		this.paramOptions = paramOptions;
	}

}
