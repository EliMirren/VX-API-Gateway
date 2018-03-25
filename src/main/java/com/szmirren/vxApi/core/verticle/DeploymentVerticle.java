package com.szmirren.vxApi.core.verticle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.szmirren.vxApi.core.common.ResultFormat;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.entity.VxApiDeployInfos;
import com.szmirren.vxApi.core.enums.HTTPStatusCodeMsgEnum;
import com.szmirren.vxApi.core.options.VxApiServerOptions;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 用于部署应用与部署API
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class DeploymentVerticle extends AbstractVerticle {
	private static final Logger LOG = LogManager.getLogger(DeploymentVerticle.class);
	/**
	 * 存储已经在运行了的项目
	 */
	private Map<String, VxApiDeployInfos> applicationMaps = new HashMap<>();
	/**
	 * 存储已经在运行的应用API集合
	 */
	private Map<String, Set<String>> applicationApiMaps = new HashMap<>();
	/**
	 * 端口服务代理者
	 */
	private Map<Integer, VxApiDeployInfos> portProxyMap = new HashMap<>();
	/**
	 * 备用端口服务代理者
	 */
	private Map<Integer, List<VxApiDeployInfos>> portStandbyProxyMap = new HashMap<>();
	/**
	 * 被端口服务代理的应用,set值为端口号+appName
	 */
	private Map<Integer, Set<String>> portProxyApplicationMap = new HashMap<>();
	/**
	 * 被端口服务代理应用的API,key值为端口号+appName
	 */
	private Map<String, JsonArray> portProxyApplicationApiMap = new HashMap<>();

	/**
	 * 当前Vertx的唯一标识
	 */
	private String thisVertxName;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOG.info("start Deployment Verticle ...");
		thisVertxName = System.getProperty("thisVertxName", "VX-API");
		if (vertx.isClustered()) {
			// 如果vert.x是以集群的方式运行添加接受广播的部署相关
			vertx.eventBus().consumer(VxApiEventBusAddressConstant.DEPLOY_APP_DEPLOY, this::deploymentAPP);
			vertx.eventBus().consumer(VxApiEventBusAddressConstant.DEPLOY_APP_UNDEPLOY, this::unDeploymentAPP);
			vertx.eventBus().consumer(VxApiEventBusAddressConstant.DEPLOY_API_START_ALL, this::startAllAPI);
			vertx.eventBus().consumer(VxApiEventBusAddressConstant.DEPLOY_API_START, this::startAPI);
			vertx.eventBus().consumer(VxApiEventBusAddressConstant.DEPLOY_API_STOP, this::stopAPI);
		}

		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.DEPLOY_APP_COUNT,
				this::applicationCount);

		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.DEPLOY_APP_DEPLOY, this::deploymentAPP);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.DEPLOY_APP_UNDEPLOY,
				this::unDeploymentAPP);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.DEPLOY_API_START_ALL, this::startAllAPI);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.DEPLOY_API_START, this::startAPI);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.DEPLOY_API_STOP, this::stopAPI);

		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.DEPLOY_FIND_ONLINE_APP,
				this::findOnlineAPP);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.DEPLOY_APP_IS_ONLINE,
				this::getAppIsOnline);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.DEPLOY_FIND_ONLINE_API,
				this::findOnlineAPI);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.DEPLOY_API_IS_ONLINE,
				this::getApiIsOnline);
		LOG.info("start Deployment Verticle successful");
		super.start(startFuture);
	}

	/**
	 * 部署应用程序
	 * 
	 * @param msg
	 */
	public void deploymentAPP(Message<JsonObject> msg) {
		JsonObject body = new JsonObject();
		if (vertx.isClustered()) {
			if (thisVertxName.equals(body.getString("thisVertxName"))) {
				return;
			}
		}
		String name = msg.body().getString("appName");
		JsonObject application = msg.body().getJsonObject("app");
		body.put("appConfig", application);

		// 获得全局黑名单并部署应用
		vertx.eventBus().<JsonArray>send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_BLACK_IP_FIND, null,
				iplist -> {
					if (iplist.succeeded()) {
						// 添加到加载配置
						body.put("blackIpSet", iplist.result().body());
						DeploymentOptions options = new DeploymentOptions(config());
						options.setIsolationGroup(name);
						options.setConfig(body);
						vertx.deployVerticle(VxApiApplication.class.getName(), options, res -> {
							if (res.succeeded()) {
								LOG.info("启动应用程序:" + name + "-->成功!");
								// 记录部署信息
								VxApiServerOptions serverOptions = VxApiServerOptions
										.fromJson(application.getJsonObject("serverOptions"));
								VxApiDeployInfos infos = new VxApiDeployInfos(name, res.result(), serverOptions);
								applicationMaps.put(name, infos);
								applicationApiMaps.put(name, new HashSet<>());
								// 设置端口服务号代理
								// http端口号
								Integer httpPort = infos.getHttpPort();
								if (httpPort != null) {
									if (portProxyMap.get(httpPort) == null) {
										portProxyMap.put(httpPort, infos);
									} else {
										List<VxApiDeployInfos> item = portStandbyProxyMap.get(httpPort) == null
												? new ArrayList<VxApiDeployInfos>() : portStandbyProxyMap.get(httpPort);
										item.add(infos);
										portStandbyProxyMap.put(httpPort, item);
									}
								}
								// https端口号
								Integer httpsPort = infos.getHttpsPort();
								if (httpsPort != null) {
									if (portProxyMap.get(httpsPort) == null) {
										portProxyMap.put(httpsPort, infos);
									} else {
										List<VxApiDeployInfos> item = portStandbyProxyMap.get(httpsPort) == null
												? new ArrayList<VxApiDeployInfos>()
												: portStandbyProxyMap.get(httpsPort);
										item.add(infos);
										portStandbyProxyMap.put(httpsPort, item);
									}
								}
								msg.reply("ok");
							} else {
								LOG.error("启动应用程序:" + name + "-->失败:" + res.cause());
								int code = 500;
								if (res.cause() != null
										&& res.cause().toString().indexOf("Address already in use: bind") > -1) {
									code = 1111;
								}
								msg.fail(code, res.cause().toString());
							}
						});
					} else {
						msg.fail(500, iplist.cause().toString());
					}
				});

	}

	/**
	 * 卸载应用程序
	 * 
	 * @param msg
	 */
	public void unDeploymentAPP(Message<JsonObject> msg) {
		if (vertx.isClustered()) {
			if (thisVertxName.equals(msg.body().getString("thisVertxName"))) {
				return;
			}
		}
		String name = msg.body().getString("appName");
		VxApiDeployInfos deployInfos = applicationMaps.get(name);
		if (deployInfos == null) {
			msg.reply("ok");
			return;
		}
		String deployId = deployInfos.getDeployId();

		Future<Void> undeplyFuture = Future.future();
		undeplyFuture.setHandler(handle -> {
			vertx.undeploy(deployId, res -> {
				if (res.succeeded()) {
					vertx.executeBlocking(futrue -> {
						LOG.info("暂停应用程序:" + name + "-->成功!");
						// 设置备用端口服务代理为主端口服务代理
						Integer httpPort = deployInfos.getHttpPort();
						if (httpPort != null) {
							// 代理类部署信息
							VxApiDeployInfos proxy = portProxyMap.get(httpPort);
							if (proxy != null && proxy.getHttpPort() != null) {
								if (portProxyApplicationMap == null) {
									portProxyApplicationMap = new HashMap<>();
								}
								if (portProxyApplicationApiMap == null) {
									portProxyApplicationApiMap = new HashMap<>();
								}
								Integer proxyPort = proxy.getHttpPort();
								// 查询代理类中是否有启动当前应用的API
								Set<String> proxySet = portProxyApplicationMap.get(proxyPort);
								String proxyKey = httpPort + deployInfos.getAppName();
								if (proxySet != null && !deployInfos.getAppName().equals(proxy.getAppName())
										&& proxySet.contains(proxyKey)) {
									proxySet.remove(proxyKey);
									portProxyApplicationMap.put(proxyPort, proxySet);
									portProxyApplicationApiMap.remove(proxyKey);
									Set<String> apiNames = applicationApiMaps.get(deployInfos.getAppName());
									stopApiRecursion(new ArrayList<>(apiNames), proxy.getAppName(), 0, 0, stopRes -> {
										JsonObject result = stopRes.result() == null ? new JsonObject()
												: stopRes.result();
										int success = result.getInteger("success", 0);
										int fail = result.getInteger("fail", 0);
										LOG.info(proxy.getAppName() + "HTTP服务端口代理暂停:" + deployInfos.getAppName()
												+ "所有API结果: 成功数量:" + success + ",失败数量:" + fail);
									});
								}
								// 设置备用代理为主代理并将代理的API移动转交给备用代理类,如果没有备用类则当前代理
								if (deployInfos.getAppName().equals(proxy.getAppName())) {
									List<VxApiDeployInfos> item = portStandbyProxyMap.get(proxyPort);
									if (item != null && item.size() > 0) {
										VxApiDeployInfos standby = item.remove(0);
										Set<String> set = portProxyApplicationMap.get(proxyPort);
										if (set != null) {
											String standbyAppName = standby.getAppName();
											JsonArray body = new JsonArray();
											set.forEach(k -> {
												body.addAll(portProxyApplicationApiMap.get(k));
											});
											startAllAPIRecursion(body, standbyAppName, 0, null, 0, true, 1,
													startProxy -> {
														JsonObject result = startProxy.result() == null
																? new JsonObject() : startProxy.result();
														int success = result.getInteger("success", 0);
														int fail = result.getInteger("fail", 0);
														LOG.info("将代理HTTP服务端口" + proxy.getAppName() + "的API迁移到"
																+ standbyAppName + "启动API结果: 成功数量:" + success + ",失败数量:"
																+ fail);
													});
										}
										portProxyMap.put(proxyPort, standby);
									} else {
										portProxyMap.remove(proxyPort);
									}
								}
							}
						}
						Integer httpsPort = deployInfos.getHttpsPort();
						if (httpsPort != null) {
							// 代理类部署信息
							VxApiDeployInfos proxy = portProxyMap.get(httpsPort);
							if (proxy != null && proxy.getHttpsPort() != null) {
								if (portProxyApplicationMap == null) {
									portProxyApplicationMap = new HashMap<>();
								}
								if (portProxyApplicationApiMap == null) {
									portProxyApplicationApiMap = new HashMap<>();
								}
								Integer proxyPort = proxy.getHttpsPort();
								// 查询代理类中是否有启动当前应用的API
								Set<String> proxySet = portProxyApplicationMap.get(proxyPort);
								String proxyKey = proxyPort + deployInfos.getAppName();
								if (proxySet != null && !deployInfos.getAppName().equals(proxy.getAppName())
										&& proxySet.contains(proxyKey)) {
									proxySet.remove(proxyKey);
									portProxyApplicationMap.put(proxyPort, proxySet);
									portProxyApplicationApiMap.remove(proxyKey);
									Set<String> apiNames = applicationApiMaps.get(deployInfos.getAppName());
									stopApiRecursion(new ArrayList<>(apiNames), proxy.getAppName(), 0, 0, stopRes -> {
										JsonObject result = stopRes.result() == null ? new JsonObject()
												: stopRes.result();
										int success = result.getInteger("success", 0);
										int fail = result.getInteger("fail", 0);
										LOG.info(proxy.getAppName() + "HTTPS服务端口代理暂停:" + deployInfos.getAppName()
												+ "所有API结果: 成功数量:" + success + ",失败数量:" + fail);
									});
								}
								// 设置备用代理为主代理并将代理的API移动转交给备用代理类,如果没有代理则删除自己的代理
								if (deployInfos.getAppName().equals(proxy.getAppName())) {
									List<VxApiDeployInfos> item = portStandbyProxyMap.get(proxyPort);
									if (item != null && item.size() > 0) {
										VxApiDeployInfos standby = item.remove(0);
										Set<String> set = portProxyApplicationMap.get(proxyPort);
										if (set != null) {
											String standbyAppName = standby.getAppName();
											JsonArray body = new JsonArray();
											set.forEach(k -> {
												body.addAll(portProxyApplicationApiMap.get(k));
											});
											startAllAPIRecursion(body, standbyAppName, 0, null, 0, true, 1,
													startProxy -> {
														JsonObject result = startProxy.result() == null
																? new JsonObject() : startProxy.result();
														int success = result.getInteger("success", 0);
														int fail = result.getInteger("fail", 0);
														LOG.info("将代理HTTP服务端口" + proxy.getAppName() + "的API迁移到"
																+ standbyAppName + "启动API结果: 成功数量:" + success + ",失败数量:"
																+ fail);
													});
										}
										portProxyMap.put(proxyPort, standby);
									} else {
										portProxyMap.remove(proxyPort);
									}
								}
							}
						}
						futrue.complete();
					}, futrueRes -> {
						applicationMaps.remove(deployInfos.getAppName());
						applicationApiMaps.remove(deployInfos.getAppName());
						msg.reply("ok");
					});
				} else {
					LOG.error("暂停应用程序:" + name + "-->失败:" + res.cause());
					msg.fail(500, res.cause().toString());
				}
			});

		});
		// 暂停API后暂停应用
		vertx.executeBlocking(futrue -> {
			stopApiRecursion(new ArrayList<>(applicationApiMaps.get(deployInfos.getAppName())),
					deployInfos.getAppName(), 0, 0, res -> {
						JsonObject result = res.result() == null ? new JsonObject() : res.result();
						int success = result.getInteger("success", 0);
						int fail = result.getInteger("fail", 0);
						LOG.info("执行暂停应用" + deployInfos.getAppName() + "->暂停所有API-->结果:成功数量:" + success + ",失败数量:"
								+ fail);
						futrue.complete();
					});
		}, undeplyFuture);
	}

	/**
	 * 启动所有API
	 * 
	 * @param msg
	 */
	public void startAllAPI(Message<JsonObject> msg) {
		if (vertx.isClustered()) {
			if (thisVertxName.equals(msg.body().getString("thisVertxName"))) {
				return;
			}
		}
		String appName = msg.body().getString("appName");
		VxApiDeployInfos deployInfos = applicationMaps.get(appName);
		if (deployInfos == null) {
			msg.reply(ResultFormat.format(HTTPStatusCodeMsgEnum.C1400, "应用尚未启动"));
			LOG.info("启动" + appName + "所有API-->失败:应用尚未启动");
			return;
		}
		JsonArray body = msg.body().getJsonArray("apis");
		if (body != null && body.size() > 0) {
			if (applicationApiMaps.get(appName) == null) {
				applicationApiMaps.put(appName, new HashSet<>());
			}
			// 启动所有API
			vertx.<String>executeBlocking(fut -> {
				// 代理是否启动http服务
				Integer httpPort = deployInfos.getHttpPort();
				if (httpPort != null) {
					VxApiDeployInfos proxy = portProxyMap.get(httpPort);
					if (proxy != null && proxy.getHttpPort() != null && proxy.getHttpPort().equals(httpPort)
							&& !proxy.getAppName().equals(deployInfos.getAppName())) {
						Integer proxyPort = proxy.getHttpPort();
						startAllAPIRecursion(body.copy(), proxy.getAppName(), 0, null, 0, true, 1, res -> {
							JsonObject result = res.result() == null ? new JsonObject() : res.result();
							int success = result.getInteger("success", 0);
							int fail = result.getInteger("fail", 0);
							LOG.info(proxy.getAppName() + "代理HTTP服务端口-->启动" + appName + "所有API结果: 成功数量:" + success
									+ ",失败数量:" + fail);
							Set<String> item = portProxyApplicationMap.get(proxyPort) == null ? new HashSet<>()
									: portProxyApplicationMap.get(proxyPort);
							item.add(proxyPort + appName);
							portProxyApplicationMap.put(proxyPort, item);
						});
					}
				}
				// 代理是否启动https服务
				Integer httpsPort = deployInfos.getHttpsPort();
				if (httpsPort != null) {
					VxApiDeployInfos proxy = portProxyMap.get(httpsPort);
					if (proxy != null && proxy.getHttpsPort() != null && proxy.getHttpsPort().equals(httpsPort)
							&& !proxy.getAppName().equals(deployInfos.getAppName())) {
						Integer proxyPort = proxy.getHttpsPort();
						startAllAPIRecursion(body.copy(), proxy.getAppName(), 0, null, 0, true, 2, res -> {
							JsonObject result = res.result() == null ? new JsonObject() : res.result();
							int success = result.getInteger("success", 0);
							int fail = result.getInteger("fail", 0);
							LOG.info(proxy.getAppName() + "HTTPS服务端口代理-->启动" + appName + "所有API结果: 成功数量:" + success
									+ ",失败数量:" + fail);
							Set<String> item = portProxyApplicationMap.get(proxyPort) == null ? new HashSet<>()
									: portProxyApplicationMap.get(proxyPort);
							item.add(proxyPort + appName);
							portProxyApplicationMap.put(proxyPort, item);
						});
					}
				}
				// 启动成功的API集合
				Set<String> successSet = new HashSet<>();
				startAllAPIRecursion(body.copy(), appName, 0, successSet, 0, res -> {
					JsonObject result = res.result() == null ? new JsonObject() : res.result();
					int success = result.getInteger("success", 0);
					int fail = result.getInteger("fail", 0);
					LOG.info("启动" + appName + "所有API结果: 成功数量:" + success + ",失败数量:" + fail);
					applicationApiMaps.put(appName, successSet);
					fut.complete(ResultFormat.format(HTTPStatusCodeMsgEnum.C200,
							"启动" + appName + "所有API结果: 成功数量:" + success + ",失败数量:" + fail));
				});
			}, res -> {
				msg.reply(res.result());
			});
		} else {
			msg.reply(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, "API数量为: 0"));
			if (LOG.isDebugEnabled()) {
				LOG.debug("启动" + appName + "所有API结果:API数量为 0");
			}
		}
	}

	/**
	 * 启动所有API的方法
	 * 
	 * @param apis
	 *            apis集合
	 * @param appName
	 *            应用的名称
	 * @param success
	 *            启动成功的种子
	 * @param successSet
	 *            启动成功的API,如果successSet等于null,则不添加成功的API
	 * @param fail
	 *            启动失败的种子
	 * @return 返回json key : success成功的数量,fail失败的数量
	 */
	public void startAllAPIRecursion(JsonArray apis, String appName, int success, Set<String> successSet, int fail,
			Handler<AsyncResult<JsonObject>> handler) {
		startAllAPIRecursion(apis, appName, success, successSet, fail, null, null, handler);
	}

	/**
	 * 启动所有API的方法
	 * 
	 * @param apis
	 *            apis集合
	 * @param appName
	 *            应用的名称
	 * @param success
	 *            启动成功的种子
	 * @param successSet
	 *            启动成功的API,如果successSet等于null,则不添加成功的API
	 * @param fail
	 *            启动失败的种子
	 * @param elseRouteToThis
	 *            是否代理应用启动
	 * @param serverType
	 *            服务的类型1=http,2=htpps,3=webSocket
	 * @param handler
	 */
	public void startAllAPIRecursion(JsonArray apis, String appName, int success, Set<String> successSet, int fail,
			Boolean elseRouteToThis, Integer serverType, Handler<AsyncResult<JsonObject>> handler) {
		if (apis == null || apis.size() < 1) {
			JsonObject result = new JsonObject();
			result.put("success", success);
			result.put("fail", fail);
			handler.handle(Future.succeededFuture(result));
			return;
		}
		JsonObject api = (JsonObject) apis.remove(0);
		String address = thisVertxName + appName + VxApiEventBusAddressConstant.APPLICATION_ADD_API_SUFFIX;
		JsonObject params = new JsonObject().put("api", api);
		if (elseRouteToThis != null) {
			params.put("elseRouteToThis", elseRouteToThis);
		}
		if (serverType != null) {
			params.put("serverType", serverType);
		}
		vertx.eventBus().send(address, params, reply -> {
			if (reply.succeeded()) {
				if (successSet != null) {
					successSet.add(api.getString("apiName"));
				}
				startAllAPIRecursion(apis, appName, success + 1, successSet, fail, elseRouteToThis, serverType,
						handler);
			} else {
				LOG.error("启动所有API->执行启动API" + api.getString("apiName") + "-->失败:" + reply.cause());
				startAllAPIRecursion(apis, appName, success, successSet, fail + 1, elseRouteToThis, serverType,
						handler);
			}
		});

	}

	/**
	 * 启动API
	 * 
	 * @param msg
	 */
	public void startAPI(Message<JsonObject> msg) {
		if (vertx.isClustered()) {
			if (thisVertxName.equals(msg.body().getString("thisVertxName"))) {
				return;
			}
		}
		String appName = msg.body().getString("appName");
		String apiName = msg.body().getString("apiName");
		VxApiDeployInfos deployInfos = applicationMaps.get(appName);
		if (deployInfos == null) {
			msg.reply(-1);
			return;
		}
		JsonObject body = msg.body().getJsonObject("api");
		vertx.<Integer>executeBlocking(future -> {
			// HTTP服务端口启动API
			Integer httpPort = deployInfos.getHttpPort();
			if (httpPort != null) {
				VxApiDeployInfos proxy = portProxyMap.get(httpPort);
				if (proxy != null && proxy.getHttpPort() != null && proxy.getHttpPort().equals(httpPort)
						&& !deployInfos.getAppName().equals(proxy.getAppName())) {
					startApiService(body.copy(), proxy.getAppName(), true, 1, res -> {
						if (res.succeeded()) {
							Integer proxyPort = proxy.getHttpPort();
							Set<String> set = portProxyApplicationMap.get(proxyPort) == null ? new HashSet<>()
									: portProxyApplicationMap.get(proxyPort);
							set.add(proxyPort + appName);
							portProxyApplicationMap.put(proxyPort, set);
							LOG.info(proxy.getAppName() + "HTTP服务端口代理-->启动" + appName + "应用的API:" + apiName + "-->成功!");
						} else {
							LOG.error(proxy.getAppName() + "HTTP服务端口代理-->启动" + appName + "应用的API:" + apiName + "失败:",
									res.cause());
							future.fail(res.cause());
						}
					}, null);
				}
			}
			// HTTPS服务端口启动API
			Integer httpsPort = deployInfos.getHttpsPort();
			if (httpsPort != null) {
				VxApiDeployInfos proxy = portProxyMap.get(httpsPort);
				if (proxy != null && proxy.getHttpsPort() != null && proxy.getHttpsPort().equals(httpsPort)
						&& !deployInfos.getAppName().equals(proxy.getAppName())) {
					startApiService(body.copy(), proxy.getAppName(), true, 2, res -> {
						if (res.succeeded()) {
							Integer proxyPort = proxy.getHttpsPort();
							Set<String> set = portProxyApplicationMap.get(proxyPort) == null ? new HashSet<>()
									: portProxyApplicationMap.get(proxyPort);
							set.add(proxyPort + appName);
							portProxyApplicationMap.put(proxyPort, set);
							LOG.info(proxy.getAppName() + "HTTP服务端口代理-->启动" + appName + "应用的API:" + apiName + "-->成功!");
						} else {
							LOG.error(proxy.getAppName() + "HTTPS服务端口代理-->启动" + appName + "应用的API:" + apiName + "失败:",
									res.cause());
							future.fail(res.cause());
						}
					}, null);
				}
			}
			startApiService(body.copy(), appName, null, null, res -> {
				if (res.succeeded()) {
					LOG.info("启动" + appName + "应用的API:" + apiName + "-->成功!");
					future.complete(res.result().body());
				} else {
					LOG.error("启动" + appName + "应用的API:" + apiName + "失败:", res.cause());
					future.fail(res.cause());
				}
			}, null);
		}, res -> {
			if (res.succeeded()) {
				LOG.info("启动" + appName + "应用的API:" + apiName + "-->成功!");
				Set<String> set = applicationApiMaps.get(appName) == null ? new HashSet<>()
						: applicationApiMaps.get(appName);
				set.add(apiName);
				applicationApiMaps.put(appName, set);
				msg.reply(1);
			} else {
				LOG.error("启动" + appName + "应用的API:" + apiName + "失败:", res.cause());
				msg.fail(500, res.cause().getMessage());
			}
		});
	}

	/**
	 * 启动API的服务,该服务为递归会阻塞线程,请在executeBlocking中执行,startResult参数传入null
	 * 
	 * @param body
	 *            引用信息
	 * @param appName
	 *            应用的名称
	 * @param elseRouteToThis
	 *            是否代理启动
	 * @param serverType
	 *            服务类型1=HTTP服务,2=HTTPS服务,3=webSocket服务
	 * @param handler
	 *            返回结果
	 * @param startResult
	 *            启动的结果,调用时需要传入null
	 */
	public void startApiService(JsonObject body, String appName, Boolean elseRouteToThis, Integer serverType,
			Handler<AsyncResult<Message<Integer>>> handler, AsyncResult<Message<Integer>> startResult) {
		if (startResult != null) {
			handler.handle(startResult);
			return;
		}
		String address = thisVertxName + appName + VxApiEventBusAddressConstant.APPLICATION_ADD_API_SUFFIX;
		JsonObject params = new JsonObject();
		params.put("api", body);
		if (elseRouteToThis != null) {
			params.put("elseRouteToThis", elseRouteToThis);
		}
		if (serverType != null) {
			params.put("serverType", serverType);
		}
		vertx.eventBus().<Integer>send(address, params, res -> {
			startApiService(null, null, null, null, handler, res);
		});
	}

	/**
	 * 停止一个API
	 * 
	 * @param msg
	 */
	public void stopAPI(Message<JsonObject> msg) {
		if (vertx.isClustered()) {
			if (thisVertxName.equals(msg.body().getString("thisVertxName"))) {
				return;
			}
		}
		String appName = msg.body().getString("appName");
		String apiName = msg.body().getString("apiName");
		VxApiDeployInfos deployInfos = applicationMaps.get(appName);
		if (deployInfos == null) {
			msg.reply(1);
			return;
		}
		vertx.<Integer>executeBlocking(futrue -> {
			Integer httpPort = deployInfos.getHttpPort();
			if (httpPort != null) {
				VxApiDeployInfos proxy = portProxyMap.get(httpPort);
				if (proxy != null && proxy.getHttpPort() != null && proxy.getHttpPort().equals(httpPort)
						&& !deployInfos.getAppName().equals(proxy.getAppName())) {
					stopApiServiceSingle(proxy.getAppName(), apiName, res -> {
						if (res.succeeded()) {
							LOG.info(proxy.getAppName() + "HTTP服务端口代理-->" + appName + "暂停API: " + apiName + "-->成功");
						} else {
							LOG.error(proxy.getAppName() + "HTTP服务端口代理-->" + appName + "暂停API: " + apiName + "-->失败:",
									res.cause());
							futrue.fail(res.cause());
						}
					}, null);
				}
			}
			Integer httpsPort = deployInfos.getHttpsPort();
			if (httpsPort != null) {
				VxApiDeployInfos proxy = portProxyMap.get(httpsPort);
				if (proxy != null && proxy.getHttpsPort() != null && proxy.getHttpsPort().equals(httpsPort)
						&& !deployInfos.getAppName().equals(proxy.getAppName())) {
					stopApiServiceSingle(proxy.getAppName(), apiName, res -> {
						if (res.succeeded()) {
							LOG.info(proxy.getAppName() + "HTTPS服务端口代理-->" + appName + "暂停API: " + apiName + "-->成功");
						} else {
							LOG.error(proxy.getAppName() + "HTTPS服务端口代理-->" + appName + "暂停API: " + apiName + "-->失败:",
									res.cause());
							futrue.fail(res.cause());
						}
					}, null);
				}
			}

			stopApiServiceSingle(appName, apiName, res -> {
				if (res.succeeded()) {
					LOG.info(appName + "暂停API: " + apiName + "-->成功");
					futrue.complete(res.result().body());
				} else {
					futrue.fail(res.cause());
				}
			}, null);
		}, res -> {
			if (res.succeeded()) {
				if (applicationApiMaps.get(appName) != null) {
					applicationApiMaps.get(appName).remove(apiName);
				}
				LOG.info(appName + "暂停启动API: " + apiName + "-->成功");
				msg.reply(1);
			} else {
				LOG.error(appName + "暂停API: " + apiName + "-->失败:", res.cause());
				msg.fail(500, res.cause().toString());
			}
		});
	}

	/**
	 * 停止一个API的服务,该服务为递归会阻塞线程,请在executeBlocking中执行,stopResult参数传入null
	 * 
	 * @param appName
	 *            应用的名称
	 * @param apiName
	 *            API的名称
	 * @param handler
	 *            操作结果
	 * @param stopResult
	 *            停止的结果,调用时需要传入null
	 */
	public void stopApiServiceSingle(String appName, String apiName, Handler<AsyncResult<Message<Integer>>> handler,
			AsyncResult<Message<Integer>> stopResult) {
		if (stopResult != null) {
			handler.handle(stopResult);
			return;
		}
		vertx.eventBus().<Integer>send(
				thisVertxName + appName + VxApiEventBusAddressConstant.APPLICATION_DEL_API_SUFFIX, apiName, reply -> {
					stopApiServiceSingle(null, null, handler, reply);
				});
	}

	/**
	 * 批量暂停API
	 * 
	 * @param apis
	 *            API的名字集
	 * @param appName
	 *            应用的名称
	 * @param success
	 *            暂停成功的数量
	 * @param fail
	 *            暂停失败的数量
	 * @param handler
	 */
	public void stopApiRecursion(List<String> apis, String appName, int success, int fail,
			Handler<AsyncResult<JsonObject>> handler) {
		if (apis == null || apis.size() < 1) {
			JsonObject result = new JsonObject();
			result.put("success", success);
			result.put("fail", fail);
			handler.handle(Future.succeededFuture(result));
			return;
		}
		String apiName = apis.remove(0);
		String address = thisVertxName + appName + VxApiEventBusAddressConstant.APPLICATION_DEL_API_SUFFIX;
		vertx.eventBus().send(address, apiName, reply -> {
			if (reply.succeeded()) {
				stopApiRecursion(apis, appName, success + 1, fail, handler);
			} else {
				stopApiRecursion(apis, appName, success, fail + 1, handler);
			}
		});
	}

	/**
	 * 查看所有在线的APP
	 * 
	 * @param msg
	 */
	public void findOnlineAPP(Message<JsonObject> msg) {
		Set<String> set = Optional.ofNullable(applicationApiMaps.keySet()).orElse(new HashSet<>());
		msg.reply(new JsonArray(new ArrayList<>(set)));
	}

	/**
	 * 查看APP是否在线
	 * 
	 * @param msg
	 */
	public void getAppIsOnline(Message<String> msg) {
		msg.reply(applicationMaps.get(msg.body()) != null);
	}

	/**
	 * 查看所有在线的API
	 */
	public void findOnlineAPI(Message<JsonObject> msg) {
		String appName = msg.body().getString("appName");
		Set<String> set = applicationApiMaps.get(appName);
		if (set == null) {
			msg.reply(new JsonArray());
		} else {
			msg.reply(new JsonArray(new ArrayList<>(set)));
		}
	}

	/**
	 * 查看API是否在线
	 * 
	 * @param msg
	 */
	public void getApiIsOnline(Message<JsonObject> msg) {
		String appName = msg.body().getString("appName");
		String apiName = msg.body().getString("apiName");
		if (applicationApiMaps.get(appName) == null) {
			msg.reply(false);
		} else {
			msg.reply(applicationApiMaps.get(appName).contains(apiName));
		}
	}

	/**
	 * 查看在线APP与API的数量
	 * 
	 * @param msg
	 */
	public void applicationCount(Message<JsonObject> msg) {
		JsonObject result = new JsonObject();
		if (applicationMaps != null) {
			result.put("app", applicationMaps.size());
		} else {
			result.put("app", 0);
		}
		if (applicationApiMaps != null) {
			int sum = applicationApiMaps.values().stream().mapToInt(Set::size).sum();
			result.put("api", sum);
		} else {
			result.put("api", 0);
		}
		msg.reply(result);
	}

}
