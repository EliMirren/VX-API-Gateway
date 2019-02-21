package com.szmirren.vxApi.core.entity;

import java.nio.charset.Charset;

public class VxApiContentType {

	private String contentType;
	private String boundary;
	private Charset charset;

	/**
	 * 通过Content-Type字符串初始一个ContentType对象
	 * 
	 * @param contentType
	 */
	public VxApiContentType(String contentType) {
		super();
		if (contentType != null) {
			init(contentType);
		}
	}

	/**
	 * 初始化
	 * 
	 * @param contentType
	 */
	public void init(String contentType) {
		String[] item = contentType.split(";");
		this.contentType = item[0];
		if (item.length > 1) {
			if (item[1].indexOf("=") != -1) {
				String[] split = item[1].split("=");
				initVar(split[0].trim(), split[1].trim());
			}
		}
		if (item.length > 2) {
			if (item[2].indexOf("=") != -1) {
				String[] split = item[1].split("=");
				initVar(split[0].trim(), split[1].trim());
			}
		}
	}

	/**
	 * 根据类型进行初始化数据
	 * 
	 * @param type
	 * @param value
	 */
	public void initVar(String type, String value) {
		if ("charset".equalsIgnoreCase(type)) {
			try {
				this.charset = Charset.forName(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("boundary".equalsIgnoreCase(type)) {
			this.boundary = value;
		}
	}

	/**
	 * 判断Content-Type类型是否支持解析
	 * 
	 * @return
	 */
	public boolean isDecodedSupport() {
		return (isUrlencoded() || isApplicationJson());
	}

	/**
	 * Content-Type是否为:null或者application/x-www-form-urlencoded
	 * 
	 * @return
	 */
	public boolean isNullOrUrlencoded() {
		return contentType == null || "application/x-www-form-urlencoded".equalsIgnoreCase(contentType);
	}

	/**
	 * Content-Type是否为:application/x-www-form-urlencoded
	 * 
	 * @return
	 */
	public boolean isUrlencoded() {
		return "application/x-www-form-urlencoded".equalsIgnoreCase(contentType);
	}

	/**
	 * Content-Type是否为:multipart/form-data
	 * 
	 * @return
	 */
	public boolean isFormData() {
		return "multipart/form-data".equalsIgnoreCase(contentType);
	}

	/**
	 * Content-Type是否为:application/json
	 * 
	 * @return
	 */
	public boolean isApplicationJson() {
		return "application/json".equalsIgnoreCase(contentType);
	}

	/**
	 * 获得content类型
	 * 
	 * @return
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * 获得Boundary
	 * 
	 * @return
	 */
	public String getBoundary() {
		return boundary;
	}

	/**
	 * 获得字符编码
	 * 
	 * @return
	 */
	public Charset getCharset() {
		return charset;
	}

	/**
	 * 设置content类型
	 * 
	 * @param contentType
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	/**
	 * 设置Boundary
	 * 
	 * @param boundary
	 */
	public void setBoundary(String boundary) {
		this.boundary = boundary;
	}
	/**
	 * 设置字符编码
	 * 
	 * @param charset
	 */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	@Override
	public String toString() {
		return "VxApiContentType [contentType=" + contentType + ", boundary=" + boundary + ", charset=" + charset + "]";
	}

}
