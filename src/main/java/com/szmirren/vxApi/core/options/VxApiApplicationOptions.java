package com.szmirren.vxApi.core.options;

import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;

/**
 * 应用程序配置信息
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiApplicationOptions extends WebClientOptions {
	/**
	 * session默认的过期时间
	 */
	public final long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000;

	private String appName;// 网关应用的名称
	private String describe;// 网关应用的描述
	private VxApiServerOptions serverOptions = new VxApiServerOptions();// 网关应用的端口集合
	private VxApiCorsOptions corsOptions;// 跨域处理
	private long contentLength = -1;// 请求主体的最大长度-1无限制长度,默认-1
	private int scope;// 网关应用的作用域0=测试版,1=预览版,2=正式版
	private long sessionTimeOut = DEFAULT_SESSION_TIMEOUT;// 会话超时时间
	private String sessionCookieName = VxApiGatewayAttribute.SESSION_COOKIE_NAME;// 会话的cookie名称

	/**
	 * 通过VxApiApplicationDTO实例化一个网关应用配置
	 */
	public VxApiApplicationOptions(VxApiApplicationDTO option) {
		super();
		super.setUserAgent(VxApiGatewayAttribute.VX_API_USER_AGENT);
		this.appName = option.getAppName();
		this.describe = option.getDescribe();
		this.serverOptions = option.getServerOptions();
		this.corsOptions = option.getCorsOptions();
		this.contentLength = option.getContentLength();
		this.scope = option.getScope();
		this.sessionTimeOut = option.getSessionTimeOut();
		this.sessionCookieName = option.getSessionCookieName();
		super.setDecoderInitialBufferSize(option.getDecoderInitialBufferSize());
		super.setMaxHeaderSize(option.getMaxHeaderSize());
		super.setMaxPoolSize(option.getMaxPoolSize());
		super.setMaxInitialLineLength(option.getMaxInitialLineLength());
		super.setKeepAlive(option.isKeepAlive());
	}

	/**
	 * 将对象装换为传输对象
	 * 
	 * @return
	 */
	public VxApiApplicationDTO toVxApiApplicationDTO() {
		VxApiApplicationDTO obj = new VxApiApplicationDTO(this);
		return obj;
	}

	/**
	 * 获得传输对象的JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		return toVxApiApplicationDTO().toJson();
	}

	/**
	 * 获得传输对象的JsonObjectString
	 * 
	 * @return
	 */
	public String toJsonString() {
		return toVxApiApplicationDTO().toJson().toString();
	}

	/**
	 * 获得应用程序的名字
	 * 
	 * @return
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * 设置应用程序的名字
	 * 
	 * @return
	 * 
	 * @return
	 */
	public VxApiApplicationOptions setAppName(String appName) {
		this.appName = appName;
		return this;
	}

	/**
	 * 获得描述
	 * 
	 * @return
	 */
	public String getDescribe() {
		return describe;
	}

	/**
	 * 设置描述
	 * 
	 * @param describe
	 * @return
	 */
	public VxApiApplicationOptions setDescribe(String describe) {
		this.describe = describe;
		return this;
	}

	/**
	 * 获得端口配置
	 * 
	 * @return
	 */
	public VxApiServerOptions getServerOptions() {
		return serverOptions;
	}

	/**
	 * 设置端口配置
	 * 
	 * @param portOptions
	 * @return
	 */
	public VxApiApplicationOptions setServerOptions(VxApiServerOptions serverOptions) {
		this.serverOptions = serverOptions;
		return this;
	}

	/**
	 * 获得跨域处理
	 * 
	 * @return
	 */
	public VxApiCorsOptions getCorsOptions() {
		return corsOptions;
	}

	/**
	 * 设置跨域处理
	 * 
	 * @param corsOptions
	 */
	public void setCorsOptions(VxApiCorsOptions corsOptions) {
		this.corsOptions = corsOptions;
	}

	/**
	 * 获得请求主体的长度
	 * 
	 * @return
	 */
	public long getContentLength() {
		return contentLength;
	}

	/**
	 * 获得请求主体的长度,-1无限制长度
	 * 
	 * @param contentLength
	 */
	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * 获得网关应用的作用域0=测试版,1=预览版,2=正式版
	 * 
	 * @return
	 */
	public int getScope() {
		return scope;
	}

	/**
	 * 设置网关应用的作用域0=测试版,1=预览版,2=正式版
	 * 
	 * @param scope
	 * @return
	 */
	public VxApiApplicationOptions setScope(int scope) {
		this.scope = scope;
		return this;
	}

	/**
	 * 获得会话超时时间 ms
	 * 
	 * @return
	 */
	public long getSessionTimeOut() {
		return sessionTimeOut;
	}

	/**
	 * 设置会话超时时间 ms
	 * 
	 * @param sessionTimeOut
	 * @return
	 */
	public VxApiApplicationOptions setSessionTimeOut(long sessionTimeOut) {
		this.sessionTimeOut = sessionTimeOut;
		return this;
	}

	/**
	 * 获得会话cookie名字
	 * 
	 * @return
	 */
	public String getSessionCookieName() {
		return sessionCookieName;
	}

	/**
	 * 设置会话cookie名字
	 * 
	 * @param sessionCookieName
	 * @return
	 */
	public VxApiApplicationOptions setSessionCookieName(String sessionCookieName) {
		this.sessionCookieName = sessionCookieName;
		return this;
	}

}
