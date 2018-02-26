package com.szmirren.vxApi.core.cluster.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.szmirren.vxApi.core.cluster.VxApiClusterConfig;

import io.vertx.core.json.JsonObject;

/**
 * 用于集群时获取网关所需要配置信息
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiClusterConfigZookeeperImpl implements VxApiClusterConfig {
	private JsonObject zkConf = new JsonObject();// 配置信息

	/**
	 * 初始化一个Zookeeper的配置文件
	 * 
	 * @param zkConf
	 */
	public VxApiClusterConfigZookeeperImpl(JsonObject zkConf) {
		this.zkConf = zkConf;
	}

	@Override
	public JsonObject getConfig() throws Exception {
		JsonObject retry = zkConf.getJsonObject("retry", new JsonObject());
		String hosts = zkConf.getString("zookeeperHosts", "127.0.0.1");
		int baseSleepTimeMs = retry.getInteger("initialSleepTime", 1000);
		int maxRetries = retry.getInteger("maxTimes", 5);
		int maxSleepMs = retry.getInteger("intervalTimes", 10000);
		int sessionTimeoutMs = zkConf.getInteger("sessionTimeout", 20000);
		int connectionTimeoutMs = zkConf.getInteger("connectTimeout", 3000);
		ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries, maxSleepMs);
		String vxApiConfPath = zkConf.getString("vxApiConfPath", "/io.vertx/vx.api.gateway/conf");
		CuratorFramework client = CuratorFrameworkFactory.newClient(hosts, sessionTimeoutMs, connectionTimeoutMs,
				retryPolicy);
		client.start();
		byte[] data = client.getData().forPath(vxApiConfPath);
		client.close();
		String jsons = new String(data);
		return new JsonObject(jsons);
	}
}
