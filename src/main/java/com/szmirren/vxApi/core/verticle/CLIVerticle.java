package com.szmirren.vxApi.core.verticle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
/**
 * VX-API内部使用的客户端,既可以让他做操作VX-API相关
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class CLIVerticle extends AbstractVerticle {
	private static final Logger LOG = LogManager.getLogger(CLIVerticle.class);

	/**
	 * 当前Vertx的唯一标识
	 */
	private String thisVertxName;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOG.info("start CLI Verticle ...");
		thisVertxName = System.getProperty("thisVertxName", "VX-API");
		vertx.eventBus().consumer(VxApiEventBusAddressConstant.CLI_START_EVERYTHING, this::startEverything);
		vertx.eventBus().consumer(VxApiEventBusAddressConstant.CLI_START_ALL_APP, this::startAllAPP);
		vertx.eventBus().consumer(VxApiEventBusAddressConstant.CLI_START_APP_EVERYTHING, this::startAPPEverything);
		vertx.eventBus().consumer(VxApiEventBusAddressConstant.CLI_START_APP, this::startAPP);
		LOG.info("start CLI Verticle successful");
		super.start(startFuture);
	}
	/**
	 * 启动所有网关应用与API
	 * 
	 * @param msg
	 */
	public void startEverything(Message<JsonObject> msg) {
		LOG.info("cli->执行启动所有网关应用与API...");
		vertx.executeBlocking(futrue -> {
			vertx.eventBus().<JsonArray>send(thisVertxName + VxApiEventBusAddressConstant.FIND_APP, null, reply -> {
				if (reply.succeeded()) {
					JsonArray body = reply.result().body();
					if (body != null && body.size() > 0) {
						startAppService(body, 0, 0, true, handler -> {
							Integer success = handler.result().getInteger("success", 0);
							Integer fail = handler.result().getInteger("fail", 0);
							LOG.info("cli->执行启动所有网关应用与API-->结果:成功数量:" + success + ",失败数量:" + fail);
							futrue.complete();
						});
					} else {
						LOG.info("cli->执行启动所有网关应用与API-->结果:网关应用数量为:0");
						futrue.complete();
					}
				} else {
					LOG.error("cli->执行启动所有网关应用与API-->失败:" + reply.cause());
					futrue.fail(reply.cause());
				}
			});
		}, result -> {
			if (result.succeeded()) {
				msg.reply("ok");
			} else {
				msg.reply("500");
				LOG.error("cli->执行启动所有网关应用与API-->失败:" + result.cause());
			}
		});
	}

	/**
	 * 启动所有网关应用
	 * 
	 * @param msg
	 */
	public void startAllAPP(Message<JsonObject> msg) {
		LOG.info("cli->执行启动所有网关应用...");
		vertx.executeBlocking(futrue -> {
			vertx.eventBus().<JsonArray>send(thisVertxName + VxApiEventBusAddressConstant.FIND_APP, null, reply -> {
				if (reply.succeeded()) {
					JsonArray body = reply.result().body();
					if (body != null && body.size() > 0) {
						startAppService(body, 0, 0, false, handler -> {
							Integer success = handler.result().getInteger("success", 0);
							Integer fail = handler.result().getInteger("fail", 0);
							LOG.info("cli->执行启动所有网关应用-->结果:成功数量:" + success + ",失败数量:" + fail);
							futrue.complete();
						});
					} else {
						LOG.info("cli->执行启动所有网关应用-->结果:网关应用数量为:0");
						futrue.complete();
					}
				} else {
					LOG.error("cli->执行启动所有网关应用-->失败:" + reply.cause());
					futrue.fail(reply.cause());
				}
			});
		}, result -> {
			if (result.succeeded()) {
				msg.reply("ok");
			} else {
				msg.reply("500");
				LOG.error("cli->执行启动所有网关应用-->失败:" + result.cause());
			}
		});

	}
	/**
	 * 启动指定网关应用并启动所有的API
	 * 
	 * @param msg
	 */
	public void startAPPEverything(Message<JsonArray> msg) {
		LOG.info("cli->执行启动指定网关应用并启动所有的API...");
		JsonArray body = msg.body();
		if (body == null || body.size() < 1) {
			msg.reply("1400");
			if (LOG.isDebugEnabled()) {
				LOG.debug("cli->执行启动指定网关应用并启动所有的API-->失败:缺少参数,系统接收到信息:" + body);
			}
			return;
		}
		vertx.executeBlocking(futrue -> {
			vertx.eventBus().<JsonArray>send(thisVertxName + VxApiEventBusAddressConstant.FIND_APP, null, reply -> {
				if (reply.succeeded()) {
					JsonArray allAPP = reply.result().body();
					if (allAPP != null && allAPP.size() > 0) {
						JsonArray apps = new JsonArray();
						allAPP.forEach(va -> {
							JsonObject json = (JsonObject) va;
							String key = json.getString("appName", "");
							if (body.contains(key)) {
								apps.add(json);
							}
						});
						if (apps.size() < 1) {
							LOG.info("cli->执行启动指定网关应用并启动所有的API-->结果:没有查询到应用网关,请查看是否输入了不存在的网关应用名字或者是否存在空格或者大小写");
							futrue.complete();
						} else {
							startAppService(apps, 0, 0, true, handler -> {
								Integer success = handler.result().getInteger("success", 0);
								Integer fail = handler.result().getInteger("fail", 0);
								LOG.info("cli->执行启动指定网关应用并启动所有的API-->结果:成功数量:" + success + ",失败数量:" + fail);
								futrue.complete();
							});
						}
					} else {
						futrue.complete();
						LOG.info("cli->执行启动指定网关应用并启动所有的API-->失败:没有查询到应用网关");
					}
				} else {
					futrue.fail(reply.cause());
					LOG.error("cli->执行启动指定网关应用并启动所有的API-->失败:" + reply.cause());
				}
			});
		}, result -> {
			if (result.succeeded()) {
				msg.reply("ok");
			} else {
				msg.reply("500");
				LOG.error("cli->执行启动指定网关应用并启动所有的API-->失败:" + result.cause());
			}
		});
	}
	/**
	 * 启动指定网关应用
	 * 
	 * @param msg
	 */
	public void startAPP(Message<JsonArray> msg) {
		LOG.info("cli->执行启动指定网关应用...");
		JsonArray body = msg.body();
		if (body == null || body.size() < 1) {
			msg.reply("1400");
			if (LOG.isDebugEnabled()) {
				LOG.debug("cli->执行启动指定网关应用-->失败:缺少参数,系统接收到信息:" + body);
			}
			return;
		}
		vertx.executeBlocking(futrue -> {
			vertx.eventBus().<JsonArray>send(thisVertxName + VxApiEventBusAddressConstant.FIND_APP, null, reply -> {
				if (reply.succeeded()) {
					JsonArray allAPP = reply.result().body();
					if (allAPP != null && allAPP.size() > 0) {
						JsonArray apps = new JsonArray();
						allAPP.forEach(va -> {
							JsonObject json = (JsonObject) va;
							String key = json.getString("appName", "");
							if (body.contains(key)) {
								apps.add(json);
							}
						});
						if (apps.size() < 1) {
							LOG.info("cli->执行启动指定网关应用-->结果:没有查询到应用网关,请查看是否输入了不存在的APP名字或者是否存在空格或者大小写");
							futrue.complete();
						} else {
							startAppService(apps, 0, 0, false, handler -> {
								Integer success = handler.result().getInteger("success", 0);
								Integer fail = handler.result().getInteger("fail", 0);
								LOG.info("cli->执行启动指定网关应用-->结果:成功数量:" + success + ",失败数量:" + fail);
								futrue.complete();
							});
						}
					} else {
						futrue.complete();
						LOG.info("cli->执行启动指定网关应用-->失败:没有查询到应用网关");
					}
				} else {
					futrue.fail(reply.cause());
					LOG.error("cli->执行启动指定网关应用-->失败:" + reply.cause());
				}
			});
		}, result -> {
			if (result.succeeded()) {
				msg.reply("ok");
			} else {
				msg.reply("500");
				LOG.error("cli->执行启动指定网关应用-->失败:" + result.cause());
			}
		});
	}

	/**
	 * 启动所有网关应用与API服务
	 * 
	 * @param apps
	 *          应用集合
	 * @param success
	 *          成功的数量
	 * @param fail
	 *          失败的数量
	 * @param startAPI
	 *          是否启动API true启动false不启动
	 * @param handler
	 *          返回json key : success成功的数量,fail失败的数量
	 */
	public void startAppService(JsonArray apps, int success, int fail, boolean startAPI, Handler<AsyncResult<JsonObject>> handler) {
		if (apps == null || apps.size() < 1) {
			JsonObject result = new JsonObject();
			result.put("success", success);
			result.put("fail", fail);
			handler.handle(Future.succeededFuture(result));
			return;
		}
		// 网关应用
		JsonObject body = (JsonObject) apps.remove(0);
		// 网关应用的名字
		final String appName = body.getString("appName");
		if (appName == null) {
			startAppService(apps, success, fail + 1, startAPI, handler);
			LOG.error("cli->执行启动网关应用-->失败:获取不到应用的名字:" + body);
			return;
		}
		// 启动网关应用需要的配置信息
		JsonObject config = new JsonObject();
		config.put("app", body);
		config.put("appName", appName);
		String startAppAddress = thisVertxName + VxApiEventBusAddressConstant.DEPLOY_APP_DEPLOY;
		vertx.eventBus().send(startAppAddress, config, res -> {
			if (res.succeeded()) {
				// 判断是否启动所有API
				if (startAPI) {
					startAllAPI(appName);
				}
				startAppService(apps, success + 1, fail, startAPI, handler);
			} else {
				startAppService(apps, success, fail + 1, startAPI, handler);
				LOG.error("cli->执行启动网关应用-->失败:" + res.cause());
			}
		});
	}
	/**
	 * 启动所有API
	 * 
	 * @param appName
	 *          应用的名称
	 */
	public void startAllAPI(String appName) {
		if (StrUtil.isNullOrEmpty(appName)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("cli->执行启动网关应用->启动所有API-->失败:应用名称为空");
			}
			return;
		}
		// 获取所有API
		JsonObject message = new JsonObject().put("appName", appName);
		vertx.eventBus().<JsonArray>send(thisVertxName + VxApiEventBusAddressConstant.FIND_API_ALL, message, reply -> {
			if (reply.succeeded()) {
				JsonArray apis = reply.result().body();
				DeliveryOptions option = new DeliveryOptions();
				option.setSendTimeout(200 * 301);
				JsonObject config = new JsonObject();
				config.put("appName", appName);
				config.put("apis", apis);
				String startApiAddress = thisVertxName + VxApiEventBusAddressConstant.DEPLOY_API_START_ALL;
				vertx.eventBus().<String>send(startApiAddress, config, option, res -> {
					if (res.succeeded()) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("cli->执行启动网关应用:" + appName + "->启动所有API-->成功");
						}
					} else {
						LOG.error("cli->执行启动网关应用:" + appName + "->启动所有API-->失败:" + reply.cause());
					}
				});
			} else {
				LOG.error("cli->执行启动网关应用:" + appName + "->获取所有API-->失败:" + reply.cause());
			}
		});
	}

}
