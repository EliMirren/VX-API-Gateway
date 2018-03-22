package com.szmirren.vxApi.cluster;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 集群环境获取配置文件
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public interface VxApiCluster {
	/**
	 * 在集群环境中获取应用程序的配置文件
	 * 
	 * @return
	 */
	void getConfig(Handler<AsyncResult<JsonObject>> event);
	/**
	 * 添加网关应用到集群环境
	 * 
	 * @param app
	 * @param event
	 */
	void putApplication(JsonObject app, Handler<AsyncResult<Integer>> event);

	/**
	 * 在集群环境中获取所有网关应用
	 * 
	 * @param event
	 */
	void getApplication(Handler<AsyncResult<JsonArray>> event);
	/**
	 * 添加API到集群环境
	 * 
	 * @param appName
	 * @param api
	 * @param event
	 */
	void putAPI(String appName, JsonObject api, Handler<AsyncResult<JsonArray>> event);
	/**
	 * 在集群环境中获取指定网关应用的所有API
	 * 
	 * @param appName
	 * @param event
	 */
	void getAPIS(String appName, Handler<AsyncResult<JsonArray>> event);
}
