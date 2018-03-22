package com.szmirren.vxApi.cluster;

import java.util.ArrayList;
import java.util.List;

import com.szmirren.vxApi.cluster.impl.VxApiClusterZookeeperImpl;
import com.szmirren.vxApi.core.common.StrUtil;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 配置中心工厂
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiClusterFactory {
	/**
	 * sessionToken的实现类名称
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
	 * 通过名字获得配置中心的实现类
	 * 
	 * @param name
	 * @param options
	 * @return
	 * @throws NullPointerException
	 * @throws ClassNotFoundException
	 */
	public static VxApiClusterZookeeperImpl getClusterConfig(String name, JsonObject options, Vertx vertx)
			throws NullPointerException, ClassNotFoundException {
		if (StrUtil.isNullOrEmpty(name)) {
			throw new NullPointerException("获取集群配置中心实现类-->失败:工厂名字不能为空");
		}
		if (ZOOKEEPER.equalsIgnoreCase(name)) {
			return new VxApiClusterZookeeperImpl(options, vertx);
		}
		throw new ClassNotFoundException("没有找到名字为 : " + name + " 的集群配置中心实现类");
	}
}
