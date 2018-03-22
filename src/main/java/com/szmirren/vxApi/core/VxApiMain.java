package com.szmirren.vxApi.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.szmirren.vxApi.cluster.VxApiCluster;
import com.szmirren.vxApi.cluster.VxApiClusterFactory;
import com.szmirren.vxApi.core.common.PathUtil;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.verticle.CLIVerticle;
import com.szmirren.vxApi.core.verticle.ClientVerticle;
import com.szmirren.vxApi.core.verticle.DATAVerticle;
import com.szmirren.vxApi.core.verticle.DeploymentVerticle;
import com.szmirren.vxApi.core.verticle.SysVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
/**
 * VX-API的程序入口
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiMain extends AbstractVerticle {
	private static final Logger LOG = LogManager.getLogger(VxApiMain.class);
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
					// verticle的配置信息
					JsonObject option = conf.result().getJsonObject("verticleConfig", new JsonObject());
					// 数据的配置文件
					JsonObject dataConfig = conf.result().getJsonObject("dataConfig", getDefaultDataConfig());
					// 客户端的配置文件
					JsonObject clientConfig = conf.result().getJsonObject("clientConfig", getDefaultClientConfig());
					// CLI配置文件
					JsonObject cliConfig = conf.result().getJsonObject("cliConfig", getDefauluCLIConfig());
					@SuppressWarnings("rawtypes")
					List<Future> futures = new ArrayList<>();
					// 启动系统服务Verticle
					futures.add(Future.<String>future(sysInfo -> {
						vertx.deployVerticle(SysVerticle.class.getName(), new DeploymentOptions(option), sysInfo);
					}));
					// 启动数据服务Verticle
					futures.add(Future.<String>future(data -> {
						vertx.deployVerticle(DATAVerticle.class.getName(), new DeploymentOptions(option).setConfig(dataConfig), data);
					}));
					// 启动应用服务Verticle
					futures.add(Future.<String>future(deploy -> {
						vertx.deployVerticle(DeploymentVerticle.class.getName(), new DeploymentOptions(option), deploy);
					}));
					// 启动CLI服务Verticle
					futures.add(Future.<String>future(cli -> {
						vertx.deployVerticle(CLIVerticle.class.getName(), new DeploymentOptions(option), cli);
					}));
					// 启动CLI服务Verticle
					futures.add(Future.<String>future(client -> {
						vertx.deployVerticle(ClientVerticle.class.getName(), new DeploymentOptions(option).setConfig(clientConfig), client);
					}));
					CompositeFuture.all(futures).setHandler(res -> {
						if (res.succeeded()) {
							runCLICommand(cliConfig, cliRes -> {
								if (cliRes.succeeded()) {
									System.out.println("start VX-API successful");
									System.out.println("The Clinet running on port " + clientConfig.getInteger("clientPort", 5256));
									fut.complete();
								} else {
									System.out.println("start VX-API unsuccessful");
									System.out.println(cliRes.cause());
									fut.fail(cliRes.cause());
								}
							});
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
	 * 执行CLI的相应指令
	 * 
	 * @param config
	 * @param handler
	 */
	public void runCLICommand(JsonObject config, Handler<AsyncResult<Void>> handler) {
		// 执行客户端
		if (config.getBoolean("startEverything", false) != false) {
			vertx.eventBus().send(VxApiEventBusAddressConstant.CLI_START_EVERYTHING, null, res -> {
				if (res.failed()) {
					handler.handle(Future.failedFuture(res.cause()));
				}
			});
		} else if (config.getBoolean("startAllAPP", false) != false) {
			vertx.eventBus().send(VxApiEventBusAddressConstant.CLI_START_ALL_APP, null, res -> {
				if (res.failed()) {
					handler.handle(Future.failedFuture(res.cause()));
				}
			});
		} else if (config.getJsonArray("startAPPEverything") != null) {
			vertx.eventBus().send(VxApiEventBusAddressConstant.CLI_START_APP_EVERYTHING, config.getJsonArray("startAPPEverything"), res -> {
				if (res.failed()) {
					handler.handle(Future.failedFuture(res.cause()));
				}
			});
		} else if (config.getJsonArray("startAPP") != null) {
			vertx.eventBus().send(VxApiEventBusAddressConstant.CLI_START_APP, config.getJsonArray("startAPP"), res -> {
				if (res.failed()) {
					handler.handle(Future.failedFuture(res.cause()));
				}
			});
		}
		handler.handle(Future.succeededFuture());
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
						if (LOG.isDebugEnabled()) {
							LOG.debug("执行加载基本配置文件-->结果:" + config);
						}
						// 集群配置文件
						JsonObject clusterc = config.getJsonObject("cluster", new JsonObject().put("clusterType", CLUSTER_TYPE));
						String clusterType = clusterc.getString("clusterType");
						boolean confFromCluster = clusterc.getBoolean("getVxApiConfFromCluster", false);
						// 从集群环境中获取应用配置文件
						if (!CLUSTER_TYPE.equals(clusterType) && confFromCluster) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("执行从集群环境中加载配置文件...");
							}
							// 获取集群环境中的配置文件
							VxApiCluster clusterConfig = VxApiClusterFactory.getClusterConfig(clusterType,
									clusterc.getJsonObject("clusterConf", getDefaultClusterConfig()), vertx);
							clusterConfig.getConfig(handler -> {
								if (handler.succeeded()) {
									JsonObject result = handler.result();
									if (result != null && !result.isEmpty()) {
										if (LOG.isDebugEnabled()) {
											LOG.debug("执行从集群环境中加载配置文件-->结果:" + result);
										}
										conf.handle(Future.<JsonObject>succeededFuture(result));
									}
								} else {
									LOG.error("执行从集群环境中加载配置文件-->失败:" + handler.cause());
									conf.handle(Future.failedFuture(handler.cause()));
								}
							});
						} else {
							// 从配置中获取应用配置文件
							if (LOG.isDebugEnabled()) {
								LOG.debug("执行从基本配置中加载配置文件...");
							}
							JsonObject nextConf = new JsonObject();
							nextConf.put("verticleConfig", config.getJsonObject("verticleConfig", new JsonObject()));
							nextConf.put("dataConfig", config.getJsonObject("dataConfig", getDefaultDataConfig()));
							nextConf.put("clientConfig", config.getJsonObject("clientConfig", getDefaultClientConfig()));
							nextConf.put("cliConfig", config.getJsonObject("cliConfig", getDefaultClientConfig()));
							if (LOG.isDebugEnabled()) {
								LOG.debug("执行从基本配置中加载配置文件-->结果:" + nextConf);
							}
							conf.handle(Future.<JsonObject>succeededFuture(nextConf));
						}
					} catch (Exception e) {
						LOG.error("获取配置文件-->失败:", e);
						conf.handle(Future.<JsonObject>failedFuture(e));
					}
				} else {
					LOG.error("获取配置文件-->失败:", res.cause());
					conf.handle(Future.<JsonObject>failedFuture(res.cause()));
				}
			});
		} else {
			conf.handle(Future.<JsonObject>succeededFuture(config()));
		}
	}
	/**
	 * 获得CLI的默认配置
	 * 
	 * @return
	 */
	public JsonObject getDefauluCLIConfig() {
		JsonObject json = new JsonObject();
		json.put("startEverything", false);
		json.put("startAllAPP", false);
		return json;
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
