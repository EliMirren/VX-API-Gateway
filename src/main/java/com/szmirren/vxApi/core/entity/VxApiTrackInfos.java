package com.szmirren.vxApi.core.entity;

import java.time.Instant;

import io.vertx.core.json.JsonObject;

/**
 * 该类用于做记录信息
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiTrackInfos {
	private String appName;// 应用程序的名字
	private String apiName;// api的名字
	private Instant startTime = Instant.now();// 主业务处理开始时间
	private Instant endTime;// 主业务处理结束时间
	private Instant requestTime;// 与后端服务器交互开始时间
	private Instant responseTime;// 与后端服务器交互相应时间
	private int requestBufferLen;// 用户请求的的主体buffer长度
	private int responseBufferLen;// 服务端响应的主体buffer长度
	private boolean successful = true;// 是否成功
	private String errMsg;// 异常信息
	private String errStackTrace;// 异常信息

	/**
	 * 将当前对象装换为JSON
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("appName", this.appName);
		json.put("apiName", this.apiName);
		json.put("startTime", this.startTime);
		json.put("requestTime", this.requestTime);
		json.put("responseTime", this.responseTime);
		json.put("endTime", this.endTime);
		json.put("requestBufferLen", this.requestBufferLen);
		json.put("responseBufferLen", this.responseBufferLen);
		json.put("successful", this.successful);
		if (errMsg != null) {
			json.put("errMsg", this.errMsg);
		}
		if (errStackTrace != null) {
			json.put("errStackTrace", this.errStackTrace);
		}
		return json;
	}

	/**
	 * 通过JsonObject实例化一个对象
	 * 
	 * @param json
	 * @return
	 */
	public static VxApiTrackInfos fromJson(JsonObject json) {
		if (json == null) {
			return null;
		}
		VxApiTrackInfos option = new VxApiTrackInfos();
		if (json.getValue("appName") instanceof String) {
			option.setAppName(json.getString("appName"));
		}
		if (json.getValue("apiName") instanceof String) {
			option.setApiName(json.getString("apiName"));
		}

		if (json.getValue("startTime") instanceof String || json.getValue("startTime") instanceof Instant) {
			option.setStartTime(json.getInstant("startTime"));
		}
		if (json.getValue("requestTime") instanceof String || json.getValue("requestTime") instanceof Instant) {
			option.setRequestTime(json.getInstant("requestTime"));
		}
		if (json.getValue("responseTime") instanceof String || json.getValue("responseTime") instanceof Instant) {
			option.setResponseTime(json.getInstant("responseTime"));
		}
		if (json.getValue("endTime") instanceof String || json.getValue("endTime") instanceof Instant) {
			option.setEndTime(json.getInstant("endTime"));
		}
		if (json.getValue("requestBufferLen") instanceof Number) {
			option.setRequestBufferLen(((Number) json.getValue("requestBufferLen")).intValue());
		}
		if (json.getValue("responseBufferLen") instanceof Number) {
			option.setResponseBufferLen(((Number) json.getValue("responseBufferLen")).intValue());
		}
		if (json.getValue("successful") instanceof Boolean) {
			option.setSuccessful(Boolean.valueOf(json.getValue("successful").toString()));
		}
		if (json.getValue("errMsg") instanceof String) {
			option.setErrMsg(json.getString("errMsg"));
		}
		if (json.getValue("errStackTrace") instanceof String) {
			option.setErrStackTrace(json.getString("errStackTrace"));
		}

		return option;
	}

	public VxApiTrackInfos() {
		super();
	}

	/**
	 * 通过应用名称与API名称实例化一个追踪信息对象
	 * 
	 * @param appName
	 * @param apiName
	 */
	public VxApiTrackInfos(String appName, String apiName) {
		super();
		this.appName = appName;
		this.apiName = apiName;
	}

	/**
	 * 获得网关应用的名字
	 * 
	 * @return
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * 设置网关应用的名字
	 * 
	 * @param appName
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}

	/**
	 * 获得API的名字
	 * 
	 * @return
	 */
	public String getApiName() {
		return apiName;
	}

	/**
	 * 设置API的名字
	 * 
	 * @param apiName
	 */
	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	/**
	 * 获得业务处理的开始时间
	 * 
	 * @return
	 */
	public Instant getStartTime() {
		return startTime;
	}

	/**
	 * 设置业务处理的开始时间
	 * 
	 * @param startTime
	 */
	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	/**
	 * 获得与后台服务器交互请求的时间
	 * 
	 * @return
	 */
	public Instant getRequestTime() {
		return requestTime;
	}

	/**
	 * 设置与后台服务器交互的请求时间
	 * 
	 * @param requestTime
	 */
	public void setRequestTime(Instant requestTime) {
		this.requestTime = requestTime;
	}

	/**
	 * 获得与后台服务器交互的相应时间
	 * 
	 * @return
	 */
	public Instant getResponseTime() {
		return responseTime;
	}

	/**
	 * 设置与后台服务器交互的相应时间
	 * 
	 * @param responseTime
	 */
	public void setResponseTime(Instant responseTime) {
		this.responseTime = responseTime;
	}

	/**
	 * 获得用户请求API的主体长度
	 * 
	 * @return
	 */
	public int getRequestBufferLen() {
		return requestBufferLen;
	}

	/**
	 * 设置用户请求API的主体长度
	 * 
	 * @param requestBufferLen
	 */
	public void setRequestBufferLen(int requestBufferLen) {
		this.requestBufferLen = requestBufferLen;
	}

	/**
	 * 获得服务端返回给API的主体长度
	 * 
	 * @return
	 */
	public int getResponseBufferLen() {
		return responseBufferLen;
	}

	/**
	 * 设置服务端返回给API的主体长度
	 * 
	 * @param responseBufferLen
	 */
	public void setResponseBufferLen(int responseBufferLen) {
		this.responseBufferLen = responseBufferLen;
	}

	/**
	 * 获得业务处理的结束时间
	 * 
	 * @return
	 */
	public Instant getEndTime() {
		return endTime;
	}

	/**
	 * 设置业务处理的结束时间
	 * 
	 * @param endTime
	 */
	public void setEndTime(Instant endTime) {
		this.endTime = endTime;
	}

	/**
	 * 获得是否发生了异常
	 * 
	 * @return
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * 设置是否成功了,默认true
	 * 
	 * @param succeeded
	 */
	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	/**
	 * 获得异常的信息
	 * 
	 * @return
	 */
	public String getErrMsg() {
		return errMsg;
	}

	/**
	 * 设置异常的信息
	 * 
	 * @param errMsg
	 */
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	/**
	 * 获得错误的堆栈信息
	 * 
	 * @return
	 */
	public String getErrStackTrace() {
		return errStackTrace;
	}

	/**
	 * 设置错误堆栈信息
	 * 
	 * @param errStackTrace
	 */
	private void setErrStackTrace(String errStackTrace) {
		this.errStackTrace = errStackTrace;
	}

	/**
	 * 设置异常的堆栈信息
	 * 
	 * @param stackTrace
	 *          报错的堆栈信息
	 */
	public void setErrStackTrace(StackTraceElement[] stackTrace) {

		if (stackTrace != null && stackTrace.length > 0) {
			StringBuilder sub = new StringBuilder();
			for (StackTraceElement element : stackTrace) {
				sub.append(element.toString() + "\r\n");
			}
			errStackTrace = sub.toString();
		}
	}

	@Override
	public String toString() {
		return "VxApiTrackInfos [startTime=" + startTime + ", endTime=" + endTime
				+ ", requestTime=" + requestTime + ", responseTime=" + responseTime + ", requestBufferLen=" + requestBufferLen
				+ ", responseBufferLen=" + responseBufferLen + ", successful=" + successful + ", errMsg=" + errMsg + ", errStackTrace="
				+ errStackTrace + "]";
	}

}
