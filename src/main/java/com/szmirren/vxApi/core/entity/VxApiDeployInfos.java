package com.szmirren.vxApi.core.entity;

import com.szmirren.vxApi.core.options.VxApiServerOptions;

/**
 * 存储已经部署的应用信息
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiDeployInfos {
	private String appName;// 应用的名称
	private String deployId;// 应用的部署id
	private Integer httpPort;// HTTP服务器的端口号
	private Integer httpsPort;// HTTPS服务器的端口号
	private Integer webSocketPort;// WebSocket服务器的端口号
	/**
	 * 实例化一个没有属性的部署信息
	 */
	public VxApiDeployInfos() {
		super();
	}
	/**
	 * 实例化一个部署信息
	 * 
	 * @param appName
	 *          应用的名字
	 * @param deployId
	 *          部署的id
	 * @param options
	 *          服务器配置信息
	 */
	public VxApiDeployInfos(String appName, String deployId, VxApiServerOptions options) {
		super();
		this.appName = appName;
		this.deployId = deployId;
		if (options != null) {
			if (options.isCreateHttp()) {
				this.httpPort = options.getHttpPort();
			}
			if (options.isCreateHttps()) {
				this.httpsPort = options.getHttpsPort();
			}
			if (options.isCreatewebSocket()) {
				this.webSocketPort = options.getWebSocketPort();
			}
		}
	}
	/**
	 * 实例化一个部署信息
	 * 
	 * @param appName
	 *          应用的名字
	 * @param deployId
	 *          部署的id
	 * @param httpPort
	 *          http服务器端口号
	 * @param httpsPort
	 *          https服务器端口号
	 * @param webSocketPort
	 *          webSocket端口号
	 */
	public VxApiDeployInfos(String appName, String deployId, Integer httpPort, Integer httpsPort, Integer webSocketPort) {
		super();
		this.appName = appName;
		this.deployId = deployId;
		this.httpPort = httpPort;
		this.httpsPort = httpsPort;
		this.webSocketPort = webSocketPort;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getDeployId() {
		return deployId;
	}
	public void setDeployId(String deployId) {
		this.deployId = deployId;
	}
	public Integer getHttpPort() {
		return httpPort;
	}
	public void setHttpPort(Integer httpPort) {
		this.httpPort = httpPort;
	}
	public Integer getHttpsPort() {
		return httpsPort;
	}
	public void setHttpsPort(Integer httpsPort) {
		this.httpsPort = httpsPort;
	}
	public Integer getWebSocketPort() {
		return webSocketPort;
	}
	public void setWebSocketPort(Integer webSocketPort) {
		this.webSocketPort = webSocketPort;
	}
	@Override
	public String toString() {
		return "VxApiDeployInfos [appName=" + appName + ", deployId=" + deployId + ", httpPort=" + httpPort + ", httpsPort=" + httpsPort
				+ ", webSocketPort=" + webSocketPort + "]";
	}

}
