package com.szmirren.vxApi.core.options;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 应用网关跨域设置
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiCorsOptions {

	private String allowedOrigin;
	private boolean allowCredentials;
	private int maxAgeSeconds;
	private Set<HttpMethod> allowedMethods;// HttpMethod依赖io.vertx.core.http.HttpMethod
	private Set<String> allowedHeaders;
	private Set<String> exposedHeaders;

	private VxApiCorsOptions() {
		super();
	}

	public VxApiCorsOptions(String allowedOrigin) {
		super();
	}

	/**
	 * 将对象装换为JsonString
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("allowedOrigin", this.allowedOrigin);
		json.put("allowCredentials", this.allowCredentials);
		json.put("maxAgeSeconds", this.maxAgeSeconds);
		if (allowedMethods != null) {
			JsonArray ar = new JsonArray();
			this.allowedMethods.forEach(va -> {
				ar.add(va.toString());
			});
			json.put("allowedMethods", ar);
		}
		if (allowedHeaders != null) {
			json.put("allowedHeaders", new ArrayList<>(this.allowedHeaders));
		}
		if (exposedHeaders != null) {
			json.put("exposedHeaders", new ArrayList<>(this.exposedHeaders));
		}
		return json;
	}

	/**
	 * 通过Json得到一个对象,如果Json对象为空或者是错误的格式放回一个null对象
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiCorsOptions fromJson(JsonObject obj) {
		if (obj != null && obj.getValue("allowedOrigin") instanceof String) {
			VxApiCorsOptions options = new VxApiCorsOptions();
			options.setAllowedOrigin(obj.getString("allowedOrigin"));
			if (obj.getValue("allowCredentials") instanceof Boolean) {
				options.setAllowCredentials(obj.getBoolean("allowCredentials"));
			} else if (obj.getValue("allowCredentials") instanceof String) {
				if ("true".equals(obj.getString("allowCredentials"))
						|| "false".equals(obj.getString("allowCredentials"))) {
					options.setAllowCredentials(Boolean.valueOf(obj.getString("allowCredentials")));
				}
			}
			if (obj.getValue("maxAgeSeconds") instanceof Number) {
				options.setMaxAgeSeconds(((Number) obj.getValue("maxAgeSeconds")).intValue());
			}
			if (obj.getValue("allowedHeaders") instanceof JsonArray) {
				Set<String> linkedHashSet = new LinkedHashSet<>();
				obj.getJsonArray("allowedHeaders").forEach(item -> {
					if (item instanceof String) {
						linkedHashSet.add(item.toString());
					}
				});
				options.setAllowedHeaders(linkedHashSet);
			}
			if (obj.getValue("exposedHeaders") instanceof JsonArray) {
				Set<String> linkedHashSet = new LinkedHashSet<>();
				obj.getJsonArray("exposedHeaders").forEach(item -> {
					if (item instanceof String) {
						linkedHashSet.add(item.toString());
					}
				});
				options.setExposedHeaders(linkedHashSet);
			}
			if (obj.getValue("allowedMethods") instanceof JsonArray) {
				Set<HttpMethod> linkedHashSet = new LinkedHashSet<>();
				obj.getJsonArray("allowedMethods").forEach(item -> {
					if (item instanceof String) {
						linkedHashSet.add(HttpMethod.valueOf(item.toString()));
					}
				});
				options.setAllowedMethods(linkedHashSet);
			}
			return options;
		} else {
			return null;
		}
	}

	public String getAllowedOrigin() {
		return allowedOrigin;
	}

	public void setAllowedOrigin(String allowedOrigin) {
		this.allowedOrigin = allowedOrigin;
	}

	public boolean isAllowCredentials() {
		return allowCredentials;
	}

	public void setAllowCredentials(boolean allowCredentials) {
		this.allowCredentials = allowCredentials;
	}

	public int getMaxAgeSeconds() {
		return maxAgeSeconds;
	}

	public void setMaxAgeSeconds(int maxAgeSeconds) {
		this.maxAgeSeconds = maxAgeSeconds;
	}

	public Set<HttpMethod> getAllowedMethods() {
		return allowedMethods;
	}

	public void setAllowedMethods(Set<HttpMethod> allowedMethods) {
		this.allowedMethods = allowedMethods;
	}

	public Set<String> getAllowedHeaders() {
		return allowedHeaders;
	}

	public void setAllowedHeaders(Set<String> allowedHeaders) {
		this.allowedHeaders = allowedHeaders;
	}

	public Set<String> getExposedHeaders() {
		return exposedHeaders;
	}

	public void setExposedHeaders(Set<String> exposedHeaders) {
		this.exposedHeaders = exposedHeaders;
	}

	@Override
	public String toString() {
		return "VxApiCorsOptions [allowedOrigin=" + allowedOrigin + ", allowCredentials=" + allowCredentials
				+ ", maxAgeSeconds=" + maxAgeSeconds + ", allowedMethods=" + allowedMethods + ", allowedHeaders="
				+ allowedHeaders + ", exposedHeaders=" + exposedHeaders + "]";
	}

}
