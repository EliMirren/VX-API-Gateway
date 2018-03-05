package com.szmirren.vxApi.core;

import com.szmirren.vxApi.cluster.VxApiClusterConfig;
import com.szmirren.vxApi.cluster.VxApiClusterConfigFactory;
import com.szmirren.vxApi.core.common.PathUtil;
import com.szmirren.vxApi.core.verticle.ClientVerticle;
import com.szmirren.vxApi.core.verticle.DATAVerticle;
import com.szmirren.vxApi.core.verticle.DeploymentVerticle;
import com.szmirren.vxApi.core.verticle.SysVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class VxApiMain extends AbstractVerticle {
	/**
	 * 使用集群的模式的默认值
	 */
	private final String CLUSTER_TYPE = "NONE";

	@Override
	public void start(Future<Void> fut) throws Exception {
		System.out.println("start VX-API...");
		getConfig(conf -> {
			if (conf.succeeded()) {
				try {
					JsonObject option = conf.result().getJsonObject("verticleConfig", new JsonObject());
					// 数据的配置文件
					JsonObject datac = conf.result().getJsonObject("dataConfig", getDefaultDataConfig());
					// 客户端的配置文件
					JsonObject clinetc = conf.result().getJsonObject("clientConfig", getDefaultClientConfig());
					Future.<String>future(sysInfo -> vertx.deployVerticle(SysVerticle.class.getName(), new DeploymentOptions(option), sysInfo))
							.compose(res -> Future.<String>future(
									data -> vertx.deployVerticle(DATAVerticle.class.getName(), new DeploymentOptions(option).setConfig(datac), data)))
							.compose(res -> Future.<String>future(
									client -> vertx.deployVerticle(ClientVerticle.class.getName(), new DeploymentOptions(option).setConfig(clinetc), client)))
							.compose(res -> Future.<String>future(
									deploy -> vertx.deployVerticle(DeploymentVerticle.class.getName(), new DeploymentOptions(option), deploy)))
							.setHandler(res -> {
								if (res.succeeded()) {
									System.out.println("start VX-API successful");
									fut.complete();
								} else {
									System.out.println("start VX-API unsuccessful");
									System.out.println(res.cause());
									fut.fail(res.cause());
								}
							});
				} catch (Exception e) {
					System.out.println("start VX-API unsuccessful");
					System.out.println(e);
					fut.fail(e);
				}
			} else {
				System.out.println("start VX-API unsuccessful");
				System.out.println(conf.cause());
				fut.fail(conf.cause());
			}
		});
	}

	/**
	 * 获得配置文件
	 */
	public void getConfig(Handler<AsyncResult<JsonObject>> conf) {
		if (config() == null || config().isEmpty()) {
			// 获得系统配置文件
			vertx.fileSystem().readFile(PathUtil.getPathString("conf.json"), res -> {
				if (res.succeeded()) {
					try {
						JsonObject config = res.result().toJsonObject();
						// 集群配置文件
						JsonObject clusterc = config.getJsonObject("cluster", new JsonObject().put("clusterType", CLUSTER_TYPE));
						String clusterType = clusterc.getString("clusterType");
						// 从集群环境中获取应用配置文件
						if (!CLUSTER_TYPE.equals(clusterType)) {
							// 获取集群环境中的配置文件
							VxApiClusterConfig clusterConfig = VxApiClusterConfigFactory.getClusterConfig(clusterType,
									clusterc.getJsonObject("clusterConf", getDefaultClusterConfig()), vertx);
							clusterConfig.getConfig(handler -> {
								if (handler.succeeded()) {
									JsonObject result = handler.result();
									if (result != null && !result.isEmpty()) {
										conf.handle(Future.<JsonObject>succeededFuture(result));
									}
								} else {
									conf.handle(Future.failedFuture(handler.cause()));
								}
							});
						} else {
							// 从配置中获取应用配置文件
							JsonObject nextConf = new JsonObject();
							nextConf.put("verticleConfig", config.getJsonObject("verticleConfig", new JsonObject()));
							nextConf.put("dataConfig", config.getJsonObject("dataConfig", getDefaultDataConfig()));
							nextConf.put("clientConfig", config.getJsonObject("clientConfig", getDefaultClientConfig()));
							conf.handle(Future.<JsonObject>succeededFuture(nextConf));
						}
					} catch (Exception e) {
						System.out.println("获取配置文件-->失败:" + e);
						conf.handle(Future.<JsonObject>failedFuture(e));
					}
				} else {
					conf.handle(Future.<JsonObject>failedFuture(res.cause()));
				}
			});
		} else {
			conf.handle(Future.<JsonObject>succeededFuture(config()));
		}
	}

	/**
	 * 获得默认的集群配置文件
	 * 
	 * @return
	 */
	public JsonObject getDefaultClusterConfig() {
		JsonObject json = new JsonObject();
		json.put("zookeeperHosts", "127.0.0.1");
		json.put("sessionTimeout", 20000);
		json.put("connectTimeout", 3000);
		json.put("rootPath", "io.vertx");
		json.put("vxApiConfPath", "/io.vertx/vx.api.gateway/conf");
		JsonObject retry = new JsonObject();
		retry.put("initialSleepTime", 100);
		retry.put("intervalTimes", 10000);
		retry.put("maxTimes", 5);
		json.put("retry", retry);
		return json;
	}

	/**
	 * 获得默认的数据库配置
	 * 
	 * @return
	 */
	public JsonObject getDefaultDataConfig() {
		return new JsonObject().put("url", "jdbc:sqlite:configDB.db").put("driver_class", "org.sqlite.JDBC");
	}

	/**
	 * 获得默认的客户端配置
	 * 
	 * @return
	 */
	public JsonObject getDefaultClientConfig() {
		return new JsonObject().put("clientPort", 5256);
	}

}
