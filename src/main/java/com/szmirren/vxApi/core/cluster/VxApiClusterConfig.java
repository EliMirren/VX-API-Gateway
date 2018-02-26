package com.szmirren.vxApi.core.cluster;

import io.vertx.core.json.JsonObject;

/**
 * 集群环境获取配置文件
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiClusterConfig {
	/**
	 * 在集群配置中获得应用程序的配置文件
	 * 
	 * @return
	 */
	JsonObject getConfig() throws Exception;
}
