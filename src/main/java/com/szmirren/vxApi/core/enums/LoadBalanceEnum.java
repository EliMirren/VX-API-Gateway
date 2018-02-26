package com.szmirren.vxApi.core.enums;

/**
 * 负载均衡类型
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public enum LoadBalanceEnum {
	/**
	 * 轮询可用
	 */
	POLLING_AVAILABLE,
	/**
	 * ip哈希化
	 */
	IP_HASH,
}
