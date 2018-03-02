package com.szmirren.vxApi.core.verticle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.szmirren.vxApi.core.common.ResultFormat;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.enums.HTTPStatusCodeMsgEnum;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 用于部署应用与部署API
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class DeploymentVerticle extends AbstractVerticle {
	private Logger LOG = Logger.getLogger(this.getClass());
	/**
	 * 存储已经在运行了的项目
	 */
	private Map<String, String> applicationMaps = new HashMap<>();
	/**
	 * 存储已经在运行的应用API集合
	 */
	private Map<String, Set<String>> applicationApiMaps = new HashMap<>();
	/**
	 * 当前Vertx的唯一标识
	 */
	private String thisVertxName;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		thisVertxName = System.getProperty("thisVertxName", "VX-API");
		if (vertx.isClustered()) {
			// 如果vert.x是以集群的方式运行添加接受广播的部署相关
			vertx.eventBus().consumer(VxApiEventBusAddressConstant.DEPLOY_APP_DEPLOY, this::deploymentAPP);
			vertx.eventBus().consumer(VxApiEventBusAddressConstant.DEPLOY_APP_UNDEPLOY, this::unDeploymentAPP);
			vertx.eventBus().consumer(VxApiEventBusAddressConstant.DEPLOY_API_START_ALL, this::startAllAPI);
			vertx.eventBus().consumer(VxApiEventBusAddressConstant.DEPLOY_API_START, this::startAPI);
			vertx.eventBus().consumer(VxApiEventBusAddressConstant.DEPLOY_API_STOP, this::stopAPI);
		}

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

		startFuture.complete();
	}

	/**
	 * 部署应用程序
	 * 
	 * @param msg
	 */
	public void deploymentAPP(Message<JsonObject> msg) {
		JsonObject body = new JsonObject();
		String name = msg.body().getString("appName");
		body.put("appConfig", msg.body().getJsonObject("app"));
		if (vertx.isClustered()) {
			if (thisVertxName.equals(body.getString("thisVertxName"))) {
				return;
			}
		}
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
								// 部署成功正在运行的应用数量+1
								vertx.eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_APP,
										null);
								applicationMaps.put(name, res.result());
								applicationApiMaps.put(name, new HashSet<>());
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
		String name = msg.body().getString("appName");
		if (applicationMaps.get(name) == null) {
			msg.reply("ok");
			return;
		}
		if (vertx.isClustered()) {
			if (thisVertxName.equals(msg.body().getString("thisVertxName"))) {
				return;
			}
		}
		vertx.undeploy(applicationMaps.get(name), res -> {
			if (res.succeeded()) {
				LOG.info("暂停应用程序:" + name + "-->成功!");
				vertx.eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_MINUS_APP, null);
				applicationMaps.remove(name);
				applicationApiMaps.remove(name);
				msg.reply("ok");
			} else {
				LOG.error("暂停应用程序:" + name + "-->失败:" + res.cause());
				msg.fail(500, res.cause().toString());
			}
		});
	}

	/**
	 * 启动所有API
	 * 
	 * @param msg
	 */
	public void startAllAPI(Message<JsonObject> msg) {
		String appName = msg.body().getString("appName");
		if (applicationMaps.get(appName) == null) {
			msg.reply(ResultFormat.format(HTTPStatusCodeMsgEnum.C1400, "应用尚未启动"));
			LOG.info("启动" + appName + "所有API-->失败:应用尚未启动");
			return;
		}
		if (vertx.isClustered()) {
			if (thisVertxName.equals(msg.body().getString("thisVertxName"))) {
				return;
			}
		}
		JsonArray body = msg.body().getJsonArray("apis");
		if (body != null && body.size() > 0) {
			if (applicationApiMaps.get(appName) == null) {
				applicationApiMaps.put(appName, new HashSet<>());
			}
			vertx.<String>executeBlocking(fut -> {
				AtomicInteger suc = new AtomicInteger(0);// 存储启动成功API的次数
				AtomicInteger er = new AtomicInteger(0);// 存储启动失败API的次数
				body.forEach(va -> {
					JsonObject api = (JsonObject) va;
					vertx.eventBus().send(
							thisVertxName + appName + VxApiEventBusAddressConstant.APPLICATION_ADD_API_SUFFIX,
							new JsonObject().put("api", api), reply -> {
								if (reply.succeeded()) {
									applicationApiMaps.get(appName).add(api.getString("apiName"));
									suc.incrementAndGet();
								} else {
									er.incrementAndGet();
									LOG.error(
											"启动所有API-->执行启动API" + api.getString("apiName") + "-->失败:" + reply.cause());
								}
							});
				});
				// 存储轮询了几次
				AtomicInteger periodicCount = new AtomicInteger(0);
				vertx.setPeriodic(200, time -> {
					if (periodicCount.get() >= 300) {
						vertx.cancelTimer(time);
						fut.complete(ResultFormat.format(HTTPStatusCodeMsgEnum.C500,
								"启动" + appName + "所有API时间大于6万毫秒,启动成功数量:" + suc.get() + ",失败数量:" + er.get()));
						LOG.error("启动" + appName + "所有API时间大于6万毫秒,启动成功数量:" + suc.get() + ",失败数量:" + er.get());
					}
					int size = suc.get() + er.get();
					if (size >= body.size()) {
						fut.complete(ResultFormat.format(HTTPStatusCodeMsgEnum.C200,
								"启动" + appName + "所有API结果: 成功数量:" + suc.get() + ",失败数量:" + er.get()));
						LOG.info("启动" + appName + "所有API结果: 成功数量:" + suc.get() + ".失败数量:" + er.get());
						vertx.cancelTimer(time);
					}
					periodicCount.incrementAndGet();
				});
			}, res -> {
				msg.reply(res.result());
			});
		} else {
			msg.reply(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, "API数量为: 0"));
			LOG.debug("启动" + appName + "所有API结果:API数量为 0");
		}
	}

	/**
	 * 启动API
	 * 
	 * @param msg
	 */
	public void startAPI(Message<JsonObject> msg) {
		String appName = msg.body().getString("appName");
		String apiName = msg.body().getString("apiName");
		if (applicationMaps.get(appName) == null) {
			msg.reply(-1);
			return;
		}
		if (vertx.isClustered()) {
			if (thisVertxName.equals(msg.body().getString("thisVertxName"))) {
				return;
			}
		}
		JsonObject body = new JsonObject();
		body.put("api", msg.body().getJsonObject("api"));
		vertx.eventBus().<Integer>send(
				thisVertxName + appName + VxApiEventBusAddressConstant.APPLICATION_ADD_API_SUFFIX, body, reply -> {
					if (reply.succeeded()) {
						LOG.info("执行启动API: " + apiName + "-->结果:" + reply.result().body());
						if (reply.result().body() == 1) {
							msg.reply(1);
							if (applicationApiMaps.get(appName) == null) {
								applicationApiMaps.put(appName, new HashSet<>());
							} else {
								applicationApiMaps.get(appName).add(apiName);
							}
						} else {
							msg.reply(0);
						}
					} else {
						LOG.error("执行启动API: " + apiName + "-->失败:" + reply.cause());
						msg.fail(500, reply.cause().getMessage());
					}
				});

	}

	/**
	 * 停止一个API
	 * 
	 * @param msg
	 */
	public void stopAPI(Message<JsonObject> msg) {
		String appName = msg.body().getString("appName");
		String apiName = msg.body().getString("apiName");
		if (vertx.isClustered()) {
			if (thisVertxName.equals(msg.body().getString("thisVertxName"))) {
				return;
			}
		}
		vertx.eventBus().send(thisVertxName + appName + VxApiEventBusAddressConstant.APPLICATION_DEL_API_SUFFIX,
				apiName, reply -> {
					if (reply.succeeded()) {
						if (applicationApiMaps.get(appName) != null) {
							applicationApiMaps.get(appName).remove(apiName);
						}
						LOG.info("暂停启动API: " + apiName + "-->成功");
						msg.reply(1);
					} else {
						LOG.error("暂停应用:" + appName + " API:" + apiName + "-->失败:" + reply.cause());
						msg.fail(500, reply.cause().toString());
					}
				});
	}

	/**
	 * 查看所有在线的APP
	 * 
	 * @param msg
	 */
	public void findOnlineAPP(Message<JsonObject> msg) {
		String encode = Json.encode(applicationMaps);
		msg.reply(new JsonObject(encode));
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

}
