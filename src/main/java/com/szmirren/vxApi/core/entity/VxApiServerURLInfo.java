package com.szmirren.vxApi.core.entity;

/**
 * 服务地址的信息,getUrl得到服务地址,getIndex得到这个地址对应的下标,下标用于报告地址可用或地址不可用
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiServerURLInfo {
	private String url;// 路径
	private int index;// 路径的下标

	public VxApiServerURLInfo(String url, int index) {
		super();
		this.url = url;
		this.index = index;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "ServerUrlInfo [url=" + url + ", index=" + index + "]";
	}

}
