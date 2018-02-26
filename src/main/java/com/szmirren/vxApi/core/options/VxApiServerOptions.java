package com.szmirren.vxApi.core.options;

import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;

/**
 * 应用程序的端口号配置默认http=8330,https=8430,websocket=8530,
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 * 
 */
public class VxApiServerOptions extends HttpServerOptions {
	/**
	 * 默认HTTP端口号
	 */
	public final static int HTTP_DEFAULT_PORT = 8330;
	/**
	 * 默认的HTTPS端口号
	 */
	public final static int HTTPS_DEFAULT_PORT = 8430;
	/**
	 * 默认的webSocket端口号
	 */
	public final static int WEB_SOCKET_DEFAULT_PORT = 8530;
	private boolean createHttp = true;// 是否开启http服务
	private boolean createHttps = false;// 是否开启https服务
	private boolean createwebSocket = false;// 是否开启WebSocket服务
	private int httpPort = HTTP_DEFAULT_PORT;// HTTP服务器的端口号
	private int httpsPort = HTTPS_DEFAULT_PORT;// HTTPS服务器的端口号
	private int webSocketPort = WEB_SOCKET_DEFAULT_PORT;// WebSocket服务器的端口号
	private VxApiCertOptions certOptions;// 服务器证书
	private String custom;// 拓展配置

	/**
	 * 创建一个默认端口号的端口配置
	 */
	public VxApiServerOptions() {
		super();
	}

	/**
	 * 通过json实例化一个对象
	 * 
	 * @param json
	 */
	private VxApiServerOptions(JsonObject json) {
		super(json);
	}

	/**
	 * 将对象转换为json
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("createHttp", this.createHttp);
		json.put("createHttps", this.createHttps);
		json.put("createwebSocket", this.createwebSocket);
		json.put("httpPort", this.httpPort);
		json.put("httpsPort", this.httpsPort);
		json.put("webSocketPort", this.webSocketPort);
		if (certOptions != null) {
			if (!certOptions.toJson().isEmpty()) {
				json.put("certOptions", certOptions.toJson());
			}
		}
		if (custom != null) {
			json.put("custom", this.custom);
		}
		return json;
	}

	/**
	 * 将一个json对象装换为VxApiCertOptions,如果JsonObject为null或者或者JsonObject没有数据返回null
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiServerOptions fromJson(JsonObject obj) {
		VxApiServerOptions options;
		if (obj.getValue("custom") instanceof String) {
			try {
				options = new VxApiServerOptions(new JsonObject(obj.getString("custom")));
			} catch (Exception e) {
				options = new VxApiServerOptions();
			}
			options.setCustom(obj.getString("custom"));
		} else {
			options = new VxApiServerOptions();
		}
		if (obj.getValue("httpPort") instanceof Number) {
			options.setHttpPort(((Number) obj.getValue("httpPort")).intValue());
		}
		if (obj.getValue("httpsPort") instanceof Number) {
			options.setHttpsPort(((Number) obj.getValue("httpsPort")).intValue());
		}
		if (obj.getValue("webSocketPort") instanceof Number) {
			options.setWebSocketPort(((Number) obj.getValue("webSocketPort")).intValue());
		}
		if (obj.getValue("createHttp") instanceof Boolean) {
			options.setCreateHttp((Boolean) obj.getValue("createHttp"));
		}
		if (obj.getValue("createHttps") instanceof Boolean) {
			options.setCreateHttps((Boolean) obj.getValue("createHttps"));
		}
		if (obj.getValue("createwebSocket") instanceof Boolean) {
			options.setCreatewebSocket((Boolean) obj.getValue("createwebSocket"));
		}
		if (obj.getValue("certOptions") instanceof JsonObject) {
			options.setCertOptions(VxApiCertOptions.fromJson(obj.getJsonObject("certOptions")));
		}
		return options;
	}

	/**
	 * 获得是否开启http服务器
	 * 
	 * @return
	 */
	public boolean isCreateHttp() {
		return createHttp;
	}

	/**
	 * 设置是否开启http服务器
	 * 
	 * @param createHttp
	 */
	public void setCreateHttp(boolean createHttp) {
		this.createHttp = createHttp;
	}

	/**
	 * 获得是否开启http服务器
	 * 
	 * @return
	 */
	public boolean isCreateHttps() {
		return createHttps;
	}

	/**
	 * 设置是否开启https服务器
	 * 
	 * @param createHttps
	 */
	public void setCreateHttps(boolean createHttps) {
		this.createHttps = createHttps;
	}

	/**
	 * 获得是否开启webSocket服务器
	 * 
	 * @return
	 */
	public boolean isCreatewebSocket() {
		return createwebSocket;
	}

	/**
	 * 设置是否开启webSocket服务器
	 * 
	 * @param createwebSocket
	 */
	public void setCreatewebSocket(boolean createwebSocket) {
		this.createwebSocket = createwebSocket;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public VxApiServerOptions setHttpPort(int httpPort) {
		this.httpPort = httpPort;
		return this;
	}

	public int getHttpsPort() {
		return httpsPort;
	}

	public VxApiServerOptions setHttpsPort(int httpsPort) {
		this.httpsPort = httpsPort;
		return this;
	}

	public int getWebSocketPort() {
		return webSocketPort;
	}

	public VxApiServerOptions setWebSocketPort(int webSocketPort) {
		this.webSocketPort = webSocketPort;
		return this;
	}

	/**
	 * 获得服务器证书
	 * 
	 * @return
	 */
	public VxApiCertOptions getCertOptions() {
		return certOptions;
	}

	/**
	 * 设置服务器证书
	 * 
	 * @param certOptions
	 */
	public void setCertOptions(VxApiCertOptions certOptions) {
		this.certOptions = certOptions;
	}

	/**
	 * 获得拓展配置信息
	 * 
	 * @return
	 */
	public String getCustom() {
		return custom;
	}

	/**
	 * 设置拓展配置信息
	 * 
	 * @param custom
	 */
	public void setCustom(String custom) {
		this.custom = custom;
	}

	@Override
	public String toString() {
		return "VxApiServerOptions [createHttp=" + createHttp + ", createHttps=" + createHttps + ", createwebSocket="
				+ createwebSocket + ", httpPort=" + httpPort + ", httpsPort=" + httpsPort + ", webSocketPort="
				+ webSocketPort + ", certOptions=" + certOptions + ", custom=" + custom + "]";
	}

}
