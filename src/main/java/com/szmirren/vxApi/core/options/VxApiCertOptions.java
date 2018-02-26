package com.szmirren.vxApi.core.options;

import io.vertx.core.json.JsonObject;

/**
 * 证书类
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiCertOptions {
	private String certType;// 证书的类型支持pem与pfx
	private String certKey;// 证书的key,pem模式为证书key的路径,pfx模式证书的密码
	private String certPath;// 证书的路径

	/**
	 * 将对象转换为json
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("certType", this.certType);
		json.put("certKey", this.certKey);
		json.put("certPath", this.certPath);
		return json;
	}

	/**
	 * 将一个json对象装换为VxApiCertOptions,如果JsonObject为null或者或者JsonObject没有数据返回null
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiCertOptions fromJson(JsonObject obj) {
		if (obj == null) {
			return null;
		} else {
			VxApiCertOptions options = new VxApiCertOptions();
			boolean isEmpty = true;
			if (obj.getValue("certType") instanceof String) {
				isEmpty = false;
				options.setCertType(obj.getString("certType"));
			}
			if (obj.getValue("certKey") instanceof String) {
				isEmpty = false;
				options.setCertKey(obj.getString("certKey"));
			}
			if (obj.getValue("certPath") instanceof String) {
				isEmpty = false;
				options.setCertPath(obj.getString("certPath"));
			}
			if (!isEmpty) {
				return options;
			}
			return null;
			
		}
	}

	/**
	 * 获得证书的类型
	 * 
	 * @return
	 */
	public String getCertType() {
		return certType;
	}

	/**
	 * 设置证书的类型支持pem与pfx
	 * 
	 * @param certType
	 */
	public void setCertType(String certType) {
		this.certType = certType;
	}

	/**
	 * 获得证书key的路径或者密码
	 * 
	 * @return
	 */
	public String getCertKey() {
		return certKey;
	}

	/**
	 * 设置证书的key,pem模式为证书key的路径,pfx模式证书的密码
	 * 
	 * @param certKey
	 */
	public void setCertKey(String certKey) {
		this.certKey = certKey;
	}

	/**
	 * 获得证书的路径
	 * 
	 * @return
	 */
	public String getCertPath() {
		return certPath;
	}

	/**
	 * 设置证书的路径
	 * 
	 * @param certPath
	 */
	public void setCertPath(String certPath) {
		this.certPath = certPath;
	}

}
