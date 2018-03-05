package com.szmirren.vxApi.cluster;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;

/**
 * VX-APi的句群管理ClusterManager工厂类
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiClusterManagerFactory {
	/**
	 * zookeeper的实现名称
	 */
	public final static String ZOOKEEPER = "zookeeper";

	/**
	 * 获得所有实现类的名字
	 * 
	 * @return
	 */
	public static List<String> getImplNames() {
		List<String> result = new ArrayList<>();
		result.add(ZOOKEEPER);
		return result;
	}

	/**
	 * 获得集群管理
	 * 
	 * @param name
	 *            实现类在集群中的名字
	 * @param options
	 *            集群实现所需要的配置文件
	 * @return
	 */
	public static ClusterManager getClusterManager(String name, JsonObject options) {
		if (ZOOKEEPER.equals(name)) {
			return new ZookeeperClusterManager(options);
		}
		return null;
	}

}
