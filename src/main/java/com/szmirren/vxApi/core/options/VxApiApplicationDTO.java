package com.szmirren.vxApi.core.options;

import java.time.Instant;

import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;

import io.vertx.core.json.JsonObject;

/**
 * 应用网关的基本数据,用于做Application装换与校验,用于作为Application的传输,Application中的ServerOption中3个最大只依依赖于fromJson方法
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiApplicationDTO {
	public final long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000;
	public final int DEFAULT_DECODER_INITIAL_BUFFER_SIZE = 128;
	public final int DEFAULT_MAX_POOL_SIZE = 5;
	public final int DEFAULT_MAX_INITIAL_LINE_LENGTH = 4096;
	public final int DEFAULT_MAX_HEADER_SIZE = 8192;
	public final boolean DEFAULT_KEEP_ALIVE = true;
	public final String DEFAULT_NOT_FOUND_RESULT = "not found resource";

	private String appName;// 网关应用的名称
	private String describe;// 网关应用的描述
	private long contentLength = -1;// 请求主体的最大长度-1无限制长度,默认-1
	private int scope;// 网关应用的作用域0=测试版,1=预览版,2=正式版
	private long sessionTimeOut = DEFAULT_SESSION_TIMEOUT;// 会话超时时间
	private String sessionCookieName = VxApiGatewayAttribute.SESSION_COOKIE_NAME;// 会话的cookie名称
	private int decoderInitialBufferSize = DEFAULT_DECODER_INITIAL_BUFFER_SIZE;// 设置解码缓存内容大小
	private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;// 设置交互线程数量
	private int maxInitialLineLength = DEFAULT_MAX_INITIAL_LINE_LENGTH;// 请求大小
	private int maxHeaderSize = DEFAULT_MAX_HEADER_SIZE;// 设置header大小
	private boolean keepAlive = DEFAULT_KEEP_ALIVE;// 设置是否keepalive
	private String notFoundContentType;// 找不到路径(404)返回什么Content-Type类型
	private String notFoundResult = DEFAULT_NOT_FOUND_RESULT;// 找不到路径(404)状态码返回什么内容,默认not found resource
	private VxApiServerOptions serverOptions = new VxApiServerOptions();// 网关应用的端口集合
	private VxApiCorsOptions corsOptions;// 跨域处理
	private Instant time;

	private VxApiApplicationDTO() {
		super();
	}

	public VxApiApplicationDTO(String appName, String describe) {
		super();
		this.appName = appName;
		this.describe = describe;
	}

	public VxApiApplicationDTO(VxApiApplicationOptions options) {
		super();
		this.appName = options.getAppName();
		this.describe = options.getDescribe();
		this.contentLength = options.getContentLength();
		this.scope = options.getScope();
		this.sessionTimeOut = options.getSessionTimeOut();
		this.sessionCookieName = options.getSessionCookieName();
		this.decoderInitialBufferSize = options.getDecoderInitialBufferSize();
		this.maxPoolSize = options.getMaxPoolSize();
		this.maxInitialLineLength = options.getMaxInitialLineLength();
		this.maxHeaderSize = options.getMaxHeaderSize();
		this.keepAlive = options.isKeepAlive();
		this.serverOptions = options.getServerOptions();
		this.corsOptions = options.getCorsOptions();
	}

	/**
	 * 将对象装换为jsonObject对象
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("appName", this.appName);
		json.put("describe", this.describe);
		json.put("contentLength", this.contentLength);
		json.put("scope", this.scope);
		json.put("sessionTimeOut", this.sessionTimeOut);
		json.put("sessionCookieName", this.sessionCookieName);
		json.put("decoderInitialBufferSize", this.decoderInitialBufferSize);
		json.put("maxPoolSize", this.maxPoolSize);
		json.put("maxInitialLineLength", this.maxInitialLineLength);
		json.put("maxHeaderSize", this.maxHeaderSize);
		json.put("keepAlive", this.keepAlive);
		if (this.notFoundContentType != null) {
			json.put("notFoundContentType", this.notFoundContentType);
		}
		if (this.notFoundResult != null) {
			json.put("notFoundResult", this.notFoundResult);
		}
		json.put("serverOptions", this.serverOptions.toJson());
		if (this.corsOptions != null) {
			json.put("corsOptions", this.corsOptions.toJson());
		}
		if (time != null) {
			json.put("time", time);
		}
		return json;
	}

	/**
	 * 将对象装换为json字符串
	 * 
	 * @return
	 */
	public String toJsonString() {
		return toJson().toString();
	}

	/**
	 * 通过json获得一个APP配置对象,如果对象错误或者为空返回null对象
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiApplicationDTO fromJson(JsonObject obj) {
		if (obj != null && obj.getValue("appName") instanceof String) {
			VxApiApplicationDTO options = new VxApiApplicationDTO();
			options.setAppName(obj.getString("appName"));
			if (obj.getValue("describe") instanceof String) {
				options.setDescribe(obj.getString("describe"));
			}
			if (obj.getValue("sessionCookieName") instanceof String) {
				options.setSessionCookieName(obj.getString("sessionCookieName"));
			}
			if (obj.getValue("keepAlive") instanceof Boolean) {
				options.setKeepAlive(obj.getBoolean("keepAlive"));
			}
			if (obj.getValue("contentLength") instanceof Number) {
				options.setContentLength(((Number) obj.getValue("contentLength")).longValue());
			}
			if (obj.getValue("sessionTimeOut") instanceof Number) {
				options.setSessionTimeOut(((Number) obj.getValue("sessionTimeOut")).longValue());
			}
			if (obj.getValue("scope") instanceof Number) {
				options.setScope(((Number) obj.getValue("scope")).intValue());
			}
			if (obj.getValue("decoderInitialBufferSize") instanceof Number) {
				options.setDecoderInitialBufferSize(((Number) obj.getValue("decoderInitialBufferSize")).intValue());
			}
			if (obj.getValue("maxPoolSize") instanceof Number) {
				options.setMaxHeaderSize(((Number) obj.getValue("maxPoolSize")).intValue());
			}
			if (obj.getValue("maxInitialLineLength") instanceof Number) {
				options.setMaxInitialLineLength(((Number) obj.getValue("maxInitialLineLength")).intValue());
			}
			if (obj.getValue("maxHeaderSize") instanceof Number) {
				options.setMaxHeaderSize(((Number) obj.getValue("maxHeaderSize")).intValue());
			}
			if (obj.getValue("notFoundContentType") instanceof String) {
				options.setNotFoundContentType(obj.getString("notFoundContentType"));
			}
			if (obj.getValue("notFoundResult") instanceof String) {
				options.setNotFoundResult(obj.getString("notFoundResult"));
			}
			if (obj.getValue("serverOptions") instanceof JsonObject) {
				// TODO 这里设置了ServerOption最大值与应用的一样
				VxApiServerOptions ser = VxApiServerOptions.fromJson(obj.getJsonObject("serverOptions"));
				ser.setMaxHeaderSize(options.getMaxHeaderSize());
				ser.setMaxInitialLineLength(options.getMaxInitialLineLength());
				ser.setDecoderInitialBufferSize(options.getDecoderInitialBufferSize());
				options.setServerOptions(ser);
			}
			if (obj.getValue("corsOptions") instanceof JsonObject) {
				options.setCorsOptions(VxApiCorsOptions.fromJson(obj.getJsonObject("corsOptions")));
			}
			if (obj.getValue("time") instanceof Instant) {
				options.setTime(obj.getInstant("time"));
			} else if (obj.getValue("time") instanceof String) {
				options.setTime(Instant.parse(obj.getString("time")));
			}

			return options;
		} else {
			return null;
		}

	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public VxApiServerOptions getServerOptions() {
		return serverOptions;
	}

	public void setServerOptions(VxApiServerOptions serverOptions) {
		this.serverOptions = serverOptions;
	}

	public VxApiCorsOptions getCorsOptions() {
		return corsOptions;
	}

	public void setCorsOptions(VxApiCorsOptions corsOptions) {
		this.corsOptions = corsOptions;
	}

	public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public int getScope() {
		return scope;
	}

	public void setScope(int scope) {
		this.scope = scope;
	}

	public long getSessionTimeOut() {
		return sessionTimeOut;
	}

	public void setSessionTimeOut(long sessionTimeOut) {
		this.sessionTimeOut = sessionTimeOut;
	}

	public String getSessionCookieName() {
		return sessionCookieName;
	}

	public void setSessionCookieName(String sessionCookieName) {
		this.sessionCookieName = sessionCookieName;
	}

	public int getDecoderInitialBufferSize() {
		return decoderInitialBufferSize;
	}

	public void setDecoderInitialBufferSize(int decoderInitialBufferSize) {
		this.decoderInitialBufferSize = decoderInitialBufferSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getMaxInitialLineLength() {
		return maxInitialLineLength;
	}

	public void setMaxInitialLineLength(int maxInitialLineLength) {
		this.maxInitialLineLength = maxInitialLineLength;
	}

	public int getMaxHeaderSize() {
		return maxHeaderSize;
	}

	public void setMaxHeaderSize(int maxHeaderSize) {
		this.maxHeaderSize = maxHeaderSize;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public String getNotFoundContentType() {
		return notFoundContentType;
	}

	public void setNotFoundContentType(String notFoundContentType) {
		this.notFoundContentType = notFoundContentType;
	}

	public String getNotFoundResult() {
		return notFoundResult;
	}

	public void setNotFoundResult(String notFoundResult) {
		this.notFoundResult = notFoundResult;
	}

	public Instant getTime() {
		return time;
	}

	public void setTime(Instant time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "VxApiApplicationDTO [appName=" + appName + ", describe=" + describe + ", contentLength=" + contentLength
				+ ", scope=" + scope + ", sessionTimeOut=" + sessionTimeOut + ", sessionCookieName=" + sessionCookieName
				+ ", decoderInitialBufferSize=" + decoderInitialBufferSize + ", maxPoolSize=" + maxPoolSize
				+ ", maxInitialLineLength=" + maxInitialLineLength + ", maxHeaderSize=" + maxHeaderSize + ", keepAlive="
				+ keepAlive + ", notFoundContentType=" + notFoundContentType + ", notFoundResult=" + notFoundResult
				+ ", serverOptions=" + serverOptions + ", corsOptions=" + corsOptions + ", time=" + time + "]";
	}

}
