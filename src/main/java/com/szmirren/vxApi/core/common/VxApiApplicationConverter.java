package com.szmirren.vxApi.core.common;

import com.szmirren.vxApi.core.options.VxApiApplicationOptions;
import com.szmirren.vxApi.core.options.VxApiCorsOptions;
import com.szmirren.vxApi.core.options.VxApiServerOptions;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * 该类主要用于处理VxAPIApplication的json装换
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiApplicationConverter {

	public static void fromJson(JsonObject json, VxApiApplicationOptions obj) {
		if (json.getValue("appName") instanceof String) {
			obj.setAppName((String) json.getValue("appName"));
		}
		if (json.getValue("describe") instanceof String) {
			obj.setDescribe((String) json.getValue("describe"));
		}
		if (json.getValue("contentLength") instanceof Number) {
			obj.setContentLength(((Number) json.getValue("contentLength")).longValue());
		}
		if (json.getValue("scope") instanceof Number) {
			obj.setScope(((Number) json.getValue("scope")).intValue());
		}
		if (json.getValue("sessionTimeOut") instanceof Number) {
			obj.setSessionTimeOut(((Number) json.getValue("sessionTimeOut")).longValue());
		}
		if (json.getValue("sessionCookieName") instanceof String) {
			obj.setSessionCookieName((String) json.getValue("sessionCookieName"));
		}
		if (json.getValue("portOptions") instanceof JsonObject) {
			obj.setServerOptions(
					Json.decodeValue(json.getJsonObject("portOptions").toString(), VxApiServerOptions.class));
		}
		if (json.getValue("corsOptions") instanceof JsonObject) {
			obj.setCorsOptions(Json.decodeValue(json.getJsonObject("corsOptions").toString(), VxApiCorsOptions.class));
		}
	}

	public static void toJson(VxApiApplicationOptions obj, JsonObject json) {
		json.put("scope", obj.getScope());
		json.put("sessionTimeOut", obj.getSessionTimeOut());
		json.put("sessionCookieName", obj.getSessionCookieName());
		json.put("portOptions", Json.encode(obj.getServerOptions()));
		json.put("contentLength", obj.getContentLength());
		if (obj.getAppName() != null) {
			json.put("appName", obj.getAppName());
		}
		if (obj.getDescribe() != null) {
			json.put("describe", obj.getDescribe());
		}
		if (obj.getCorsOptions() != null) {
			json.put("corsOptions", Json.encode(obj.getCorsOptions()));
		}

	}

}
