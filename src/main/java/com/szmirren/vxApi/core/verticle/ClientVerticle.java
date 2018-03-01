package com.szmirren.vxApi.core.verticle;

import java.io.File;
import java.text.MessageFormat;
import java.time.Instant;

import org.apache.log4j.Logger;

import com.szmirren.vxApi.core.auth.VxApiClientStaticAuth;
import com.szmirren.vxApi.core.auth.VxApiRolesConstant;
import com.szmirren.vxApi.core.common.PathUtil;
import com.szmirren.vxApi.core.common.ResultFormat;
import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiDATAStoreConstant;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.enums.ContentTypeEnum;
import com.szmirren.vxApi.core.enums.HTTPStatusCodeMsgEnum;
import com.szmirren.vxApi.core.handler.freemarker.VxApiFreeMarkerTemplateEngine;
import com.szmirren.vxApi.core.options.VxApiApplicationDTO;
import com.szmirren.vxApi.core.options.VxApisDTO;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.FileResolver;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.TemplateEngine;

/**
 * VX-API页面客户端Verticle
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class ClientVerticle extends AbstractVerticle {
	private final Logger LOG = Logger.getLogger(this.getClass());

	/**
	 * 返回的CONTENT_TYPE值JSON
	 */
	private final String CONTENT_TYPE = "Content-Type";
	/**
	 * 返回的CONTENT_TYPE值JSON
	 */
	private final String CONTENT_VALUE_JSON_UTF8 = ContentTypeEnum.JSON_UTF8.val();
	/**
	 * 返回的CONTENT_TYPE值HTML
	 */
	private final String CONTENT_VALUE_HTML_UTF8 = ContentTypeEnum.HTML_UTF8.val();
	/**
	 * 没有权限返回
	 */
	private final String UNAUTHORIZED_RESULT = "<h2 style='text-align: center;line-height: 80px;'>对不起!你没有该操作行为的权限  <a href='javascript:history.go(-1);'>返回</a></h2>";
	/**
	 * 没有权限返回
	 */
	private final String _404 = "<h2 style='text-align: center;line-height: 80px;'>Resource not found  <a href='javascript:history.go(-1);'>返回</a></h2>";

	@Override
	public void start(Future<Void> fut) throws Exception {
		Router router = Router.router(vertx);
		router.route().handler(FaviconHandler.create(getFaviconPath()));
		router.route().handler(BodyHandler.create().setUploadsDirectory(getUploadsDirectory()));
		router.route().handler(CookieHandler.create());
		if (vertx.isClustered()) {
			router.route().handler(SessionHandler.create(ClusteredSessionStore.create(vertx))
					.setSessionCookieName(VxApiGatewayAttribute.SESSION_COOKIE_NAME));
		} else {
			router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx))
					.setSessionCookieName(VxApiGatewayAttribute.SESSION_COOKIE_NAME));
		}
		// 通过html的方式管理应用网关
		TemplateEngine engine = VxApiFreeMarkerTemplateEngine.create(getTemplateRoot());
		TemplateHandler tempHandler = TemplateHandler.create(engine, getTemplateRoot(), CONTENT_VALUE_HTML_UTF8);
		router.getWithRegex(".+\\.ftl").handler(tempHandler);
		// 权限相关
		router.route("/static/*").handler(VxApiClientStaticAuth.create());
		router.route("/static/*").handler(this::staticAuth);
		router.route("/loginOut").handler(this::loginOut);
		router.route("/static/CreateAPI.html").handler(this::staticAPI);
		router.route("/static/CreateAPP.html").handler(this::staticAPP);

		router.route("/static/*").handler(StaticHandler.create(getStaticRoot()));
		// 查看系统信息
		router.route("/static/sysInfo").handler(this::sysInfo);
		router.route("/static/sysReplaceIpList").handler(this::sysReplaceIpList);
		// Application相关
		router.route("/static/findAPP").handler(this::findAPP);
		router.route("/static/getAPP/:name").handler(this::getAPP);
		router.route("/static/addAPP").handler(this::addAPP);
		router.route("/static/delAPP/:name").handler(this::delAPP);
		router.route("/static/updtAPP/:name").handler(this::loadUpdtAPP);
		router.route("/static/updtAPP").handler(this::updtAPP);

		router.route("/static/deployAPP/:name").handler(this::deployAPP);
		router.route("/static/unDeployAPP/:name").handler(this::unDeployAPP);
		// API相关
		router.route("/static/findAPI/:name").handler(this::findAPI);
		router.route("/static/getAPI/:name").handler(this::getAPI);
		router.route("/static/addAPI").handler(this::addAPI);
		router.route("/static/updtAPI/:name").handler(this::loadUpdtAPI);
		router.route("/static/updtAPI").handler(this::updtAPI);
		router.route("/static/delAPI/:name").handler(this::delAPI);

		router.route("/static/startAllAPI/:appName").handler(this::startAllAPI);
		router.route("/static/startAPI/:apiName").handler(this::startAPI);
		router.route("/static/stopAPI/:appName/:apiName").handler(this::stopAPI);

		router.route("/static/trackInfo/:appName/:apiName").handler(this::getTrackInfo);
		// 欢迎页
		router.route("/").handler(this::welcome);
		vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("clientPort", 5256), res -> {
			if (res.succeeded()) {
				System.out.println("The VX-API console running on port " + config().getInteger("clientPort", 5256));
				fut.complete();
			} else {
				fut.fail(res.cause());
			}
		});
	}

	/**
	 * 权限认证
	 * 
	 * @param rct
	 */
	public void staticAuth(RoutingContext rct) {
		User user = rct.user();
		if (user == null) {
			rct.response().end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C401));
		} else {
			user.isAuthorized(VxApiRolesConstant.READ, res -> {
				if (res.succeeded()) {
					if (res.result()) {
						rct.next();
					} else {
						rct.response().end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C401));
					}
				} else {
					rct.response().end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
				}
			});
		}
	}

	/**
	 * 进入创建Application
	 * 
	 * @param rct
	 */
	public void staticAPP(RoutingContext rct) {
		User user = rct.user();
		user.isAuthorized(VxApiRolesConstant.WRITE, res -> {
			if (res.succeeded()) {
				if (res.result()) {
					rct.next();
				} else {
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8).end(UNAUTHORIZED_RESULT);
				}
			} else {
				rct.response().end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
			}
		});
	}

	/**
	 * 进入创建API
	 * 
	 * @param rct
	 */
	public void staticAPI(RoutingContext rct) {
		User user = rct.user();
		user.isAuthorized(VxApiRolesConstant.WRITE, res -> {
			if (res.succeeded()) {
				if (res.result()) {
					rct.next();
				} else {
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8).end(UNAUTHORIZED_RESULT);
				}
			} else {
				rct.response().end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
			}
		});
	}

	/**
	 * 查看系统信息
	 * 
	 * @param rct
	 */
	public void sysInfo(RoutingContext rct) {
		LOG.debug(MessageFormat.format("[user : {0}] 执行查看运行状态...", rct.session().<String>get("userName")));
		vertx.eventBus().<JsonObject>send(VxApiEventBusAddressConstant.SYSTEM_GET_INFO, null, reply -> {
			if (reply.succeeded()) {
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, reply.result().body()));
				LOG.debug(MessageFormat.format("[user : {0}] 执行查看运行状态...", rct.session().<String>get("userName")));
			} else {
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, reply.cause().toString()));
				LOG.error(MessageFormat.format("[user : {0}] 查看运行状态-->失败:", rct.session().<String>get("userName"),
						reply.cause().toString()));
			}
		});
	}

	/**
	 * 更新黑名单地址
	 * 
	 * @param rct
	 */
	public void sysReplaceIpList(RoutingContext rct) {
		LOG.info(MessageFormat.format("[user : {0}] 执行添加IP黑名单...", rct.session().<String>get("userName")));
		JsonArray array = new JsonArray();
		if (rct.getBody() != null || !"".equals(rct.getBodyAsString().equals(""))) {
			array = new JsonArray(rct.getBodyAsString());
		}
		JsonObject param = new JsonObject().put(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME, array);
		vertx.eventBus().<Integer>send(VxApiEventBusAddressConstant.SYSTEM_BLACK_IP_REPLACE, param, reply -> {
			if (reply.succeeded()) {
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, reply.result().body()));
				LOG.info(MessageFormat.format("[user : {0}] 执行添加IP黑名单-->结果:", rct.session().<String>get("userName"),
						reply.result().body()));
			} else {
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, reply.cause().toString()));
				LOG.error(MessageFormat.format("[user : {0}] 执行添加IP黑名单-->失败:", rct.session().<String>get("userName"),
						reply.cause().toString()));
			}
		});
	}

	/**
	 * 查看所有应用程序
	 * 
	 * @param rct
	 */
	public void findAPP(RoutingContext rct) {
		LOG.debug(MessageFormat.format("[user : {0}] 执行查询应用...", rct.session().<String>get("userName")));
		vertx.eventBus().<JsonArray>send(VxApiEventBusAddressConstant.FIND_APP, null, res -> {
			if (res.succeeded()) {
				JsonArray body = res.result().body();
				if (body.size() > 0) {
					// 拓展原来没有显示是否正在运行的属性,如果后期需要优化,可以加多一层业务层,查看应用是否正在运行在业务层处理
					vertx.eventBus().<JsonObject>send(VxApiEventBusAddressConstant.DEPLOY_FIND_ONLINE_APP, null,
							dep -> {
								if (dep.succeeded()) {
									JsonArray array = new JsonArray();
									JsonObject online = dep.result().body();
									body.forEach(obj -> {
										JsonObject data = (JsonObject) obj;
										JsonObject newObj = new JsonObject();
										String appName = data.getString("appName");
										newObj.put("appName", appName);
										newObj.put("describe", data.getString("describe"));
										newObj.put("time", data.getInstant("time"));
										newObj.put("scope", data.getInteger("scope"));
										newObj.put("online", online.getString(appName) != null);
										array.add(newObj);
									});
									rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
											.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, array));
								} else {
									rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8).end(
											ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
								}
							});
				} else {
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, body));
				}
			} else {
				LOG.error(MessageFormat.format("[user : {0}] 执行查询应用-->失败:{1}", res.cause().getMessage(),
						rct.session().<String>get("userName")));
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
			}
		});
	}

	/**
	 * 查看一个应用程序
	 * 
	 * @param rct
	 */
	public void getAPP(RoutingContext rct) {
		String name = rct.request().getParam("name");
		if (StrUtil.isNullOrEmpty(name)) {
			rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
					.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1404));
		} else {
			LOG.debug(MessageFormat.format("[user : {0}] 执行查看应用-->{1}", rct.session().get("userName"), name));
			vertx.eventBus().<JsonObject>send(VxApiEventBusAddressConstant.GET_APP, name, res -> {
				if (res.succeeded()) {
					if (res.result().body() == null || res.result().body().isEmpty()) {
						rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8).setStatusCode(404).end(_404);
						return;
					}
					vertx.eventBus().<Boolean>send(VxApiEventBusAddressConstant.DEPLOY_APP_IS_ONLINE, name, dep -> {
						if (dep.succeeded()) {
							JsonObject body = new JsonObject(res.result().body().getString("content"));
							body.put("online", dep.result().body());
							rct.put("app", body);
							rct.reroute("/getAPP.ftl");
						} else {
							rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8).end(dep.cause().toString());
						}
					});
				} else {
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8).end(res.cause().toString());
				}
			});
		}
	}

	/**
	 * 添加应用
	 * 
	 * @param rct
	 */
	public void addAPP(RoutingContext rct) {
		User user = rct.user();
		user.isAuthorized(VxApiRolesConstant.WRITE, res -> {
			if (res.succeeded()) {
				if (res.result()) {
					LOG.info(MessageFormat.format("[user : {0}] 执行添加应用...", rct.session().<String>get("userName")));
					VxApiApplicationDTO dto = VxApiApplicationDTO.fromJson(rct.getBodyAsJson());
					JsonObject param = new JsonObject();
					param.put(VxApiDATAStoreConstant.APPLICATION_ID_NAME, dto.getAppName());
					param.put(VxApiDATAStoreConstant.APPLICATION_CONTENT_NAME, dto.toJson().put("time", Instant.now()));
					vertx.eventBus().<Integer>send(VxApiEventBusAddressConstant.ADD_APP, param, cres -> {
						if (cres.succeeded()) {
							rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
									.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, cres.result().body()));
							LOG.info(MessageFormat.format("[user : {0}] 执行添加应用-->结果: {1}",
									rct.session().<String>get("userName"), cres.result().body()));
						} else {
							LOG.error(MessageFormat.format("[user : {0}] 执行添加应用-->失败:{1}",
									rct.session().get("userName"), cres.cause()));
							HttpServerResponse response = rct.response().putHeader(CONTENT_TYPE,
									CONTENT_VALUE_JSON_UTF8);
							if (cres.cause().toString().contains("UNIQUE")) {
								response.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C1444, cres.cause().toString()));
							} else {
								response.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, cres.cause().toString()));
							}
						}
					});
				} else {
					LOG.error(MessageFormat.format("[user : {0}] 执行添加应用-->失败:未授权或者无权利", rct.session().get("userName")));
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C401));
				}
			} else {
				LOG.error(MessageFormat.format("[user : {0}] 执行添加应用-->失败:{1}", rct.session().get("userName"),
						res.cause()));
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
			}
		});
	}

	/**
	 * 渲染一个将要修改的应用程序
	 * 
	 * @param rct
	 */
	public void loadUpdtAPP(RoutingContext rct) {
		String name = rct.request().getParam("name");
		if (StrUtil.isNullOrEmpty(name)) {
			rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
					.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1404));
		} else {
			LOG.info(MessageFormat.format("[user : {0}] 执行查看应用-->{1}", rct.session().get("userName"), name));
			vertx.eventBus().<JsonObject>send(VxApiEventBusAddressConstant.GET_APP, name, res -> {
				if (res.succeeded()) {
					JsonObject body = res.result().body();
					JsonObject app = new JsonObject(body.getString("content"));
					if (app.isEmpty()) {
						rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8).setStatusCode(404).end(_404);
					} else {
						rct.put("app", app);
						rct.reroute("/updateAPP.ftl");
					}
				} else {
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8).end(res.cause().toString());
				}
			});
		}
	}

	/**
	 * 修改一个应用
	 * 
	 * @param rct
	 */
	public void updtAPP(RoutingContext rct) {
		User user = rct.user();
		user.isAuthorized(VxApiRolesConstant.WRITE, res -> {
			if (res.succeeded()) {
				if (res.result()) {
					LOG.info(MessageFormat.format("[user : {0}] 执行修改应用...", rct.session().<String>get("userName")));
					VxApiApplicationDTO dto = VxApiApplicationDTO.fromJson(rct.getBodyAsJson());
					JsonObject param = new JsonObject();
					param.put(VxApiDATAStoreConstant.APPLICATION_ID_NAME, dto.getAppName());
					param.put(VxApiDATAStoreConstant.APPLICATION_CONTENT_NAME, dto.toJson());
					vertx.eventBus().<Integer>send(VxApiEventBusAddressConstant.UPDT_APP, param, cres -> {
						if (cres.succeeded()) {
							rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
									.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, cres.result().body()));
							LOG.info(MessageFormat.format("[user : {0}] 执行修改应用:{2}-->结果: {1}",
									rct.session().<String>get("userName"), cres.result().body(), dto.getAppName()));
						} else {
							LOG.error(MessageFormat.format("[user : {0}] 执行修改应用-->失败:{1}",
									rct.session().get("userName"), cres.cause()));
							rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
									.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, cres.cause().toString()));
						}
					});
				} else {
					LOG.error(MessageFormat.format("[user : {0}] 执行修改应用-->失败:未授权或者无权利", rct.session().get("userName")));
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C401));
				}
			} else {
				LOG.error(MessageFormat.format("[user : {0}] 执行修改应用-->失败:{1}", rct.session().get("userName"),
						res.cause()));
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
			}
		});
	}

	/**
	 * 删除应用程序
	 * 
	 * @param rct
	 */
	public void delAPP(RoutingContext rct) {
		String name = rct.request().getParam("name");
		if (StrUtil.isNullOrEmpty(name)) {
			rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
					.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1404));
		} else {
			User user = rct.user();
			user.isAuthorized(VxApiRolesConstant.WRITE, res -> {
				if (res.succeeded()) {
					JsonObject config = new JsonObject().put("appName", name);
					// 将应用暂停
					vertx.eventBus().send(VxApiEventBusAddressConstant.DEPLOY_APP_UNDEPLOY, config);
					if (res.result()) {
						LOG.info(MessageFormat.format("[user : {0}] 执行删除应用{1}...",
								rct.session().<String>get("userName"), name));
						vertx.eventBus().<Integer>send(VxApiEventBusAddressConstant.DEL_APP, name, cres -> {
							if (cres.succeeded()) {
								rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
										.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, cres.result().body()));
								LOG.info(MessageFormat.format("[user : {0}] 执行删除应用:{2}-->结果: {1}",
										rct.session().<String>get("userName"), cres.result().body(), name));
							} else {
								LOG.error(MessageFormat.format("[user : {0}] 执行删除应用:{2}-->失败:{1}",
										rct.session().get("userName"), cres.cause(), name));
								rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
										.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, cres.cause().toString()));
							}
						});
					} else {
						LOG.error(MessageFormat.format("[user : {0}] 执行删除应用:{1}-->失败:未授权或者无权限",
								rct.session().get("userName"), name));
						rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
								.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C401));
					}
				} else {
					LOG.error(MessageFormat.format("[user : {0}] 执行删除应用:{2}-->失败:{1}", rct.session().get("userName"),
							res.cause(), name));
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
				}
			});
		}
	}

	/**
	 * 启动应用
	 * 
	 * @param rct
	 */
	public void deployAPP(RoutingContext rct) {
		String name = rct.request().getParam("name");
		LOG.debug("执行部署应用-->" + name + "...");
		if (StrUtil.isNullOrEmpty(name)) {
			rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
					.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1404));
		} else {
			vertx.eventBus().<JsonObject>send(VxApiEventBusAddressConstant.GET_APP, name, body -> {
				if (body.succeeded()) {
					if (body.result().body().isEmpty()) {
						rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
								.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1404));
					} else {
						JsonObject app = new JsonObject(body.result().body().getString("content"));
						JsonObject config = new JsonObject();
						config.put("app", app);
						config.put("appName", name);
						vertx.eventBus().<String>send(VxApiEventBusAddressConstant.DEPLOY_APP_DEPLOY, config,
								deploy -> {
									if (deploy.succeeded()) {
										LOG.debug("部署应用-->" + name + ":成功!");
										rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
												.end(ResultFormat.formatAsOne(HTTPStatusCodeMsgEnum.C200));
									} else {
										LOG.error("部署应用-->" + name + " 失败:" + deploy.cause());
										HTTPStatusCodeMsgEnum msgCode = HTTPStatusCodeMsgEnum.C500;
										if (deploy.cause() != null && deploy.cause() instanceof ReplyException) {
											ReplyException cause = (ReplyException) deploy.cause();
											if (cause.failureCode() == 1111) {
												msgCode = HTTPStatusCodeMsgEnum.C1111;
											}
										}
										rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
												.end(ResultFormat.formatAsZero(msgCode));
									}
								});
					}
				} else {
					LOG.error("部署应用-->" + name + " 失败:" + body.cause());
					System.out.println("部署应用-->" + name + " 失败:" + body.cause());
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C500));
				}
			});
		}
	}

	/**
	 * 卸载应用
	 * 
	 * @param rct
	 */
	public void unDeployAPP(RoutingContext rct) {
		String name = rct.request().getParam("name");
		LOG.debug("执行卸载应用-->" + name + "...");
		if (StrUtil.isNullOrEmpty(name)) {
			rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
					.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1404));
		} else {
			JsonObject config = new JsonObject().put("appName", name);
			vertx.eventBus().<String>send(VxApiEventBusAddressConstant.DEPLOY_APP_UNDEPLOY, config, deploy -> {
				if (deploy.succeeded()) {
					LOG.debug("执行卸载应用-->" + name + " 成功!");
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.formatAsOne(HTTPStatusCodeMsgEnum.C200));
				} else {
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C500));
					LOG.debug("执行卸载应用-->" + name + " 失败:" + deploy.cause());
				}
			});
		}
	}

	// =============================================
	// ====================APIS=====================
	// =============================================

	/**
	 * 查看所有API
	 * 
	 * @param rct
	 */
	public void findAPI(RoutingContext rct) {
		String name = rct.request().getParam("name");
		if (StrUtil.isNullOrEmpty(name)) {
			rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
					.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1404));
		} else {
			MessageFormat.format("[user : {0}] 执行查询所以API...", rct.session().<String>get("userName"));
			JsonObject body = new JsonObject();
			body.put(VxApiDATAStoreConstant.API_APP_ID_NAME, name);
			String limit = rct.request().getParam("limit");
			String offset = rct.request().getParam("offset");
			if (limit != null && !"".equals(limit.trim())) {
				body.put("limit", new Integer(limit));
			} else {
				body.put("limit", 20);
			}
			if (offset != null && !"".equals(offset.trim())) {
				body.put("offset", new Integer(offset));
			} else {
				body.put("offset", 0);
			}
			vertx.eventBus().<JsonObject>send(VxApiEventBusAddressConstant.FIND_API_BY_PAGE, body, res -> {
				if (res.succeeded()) {
					vertx.eventBus().<JsonArray>send(VxApiEventBusAddressConstant.DEPLOY_FIND_ONLINE_API,
							new JsonObject().put("appName", name), reply -> {
								if (reply.succeeded()) {
									JsonArray online = reply.result().body();
									JsonObject result = res.result().body();
									result.getJsonArray("data").forEach(data -> {
										String apiName = ((JsonObject) data).getString("apiName");
										((JsonObject) data).put("online", online.contains(apiName));
									});
									rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
											.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, result));
								} else {
									LOG.error(MessageFormat.format("[user : {0}] 执行查询在线API-->失败:{1}",
											res.cause().getMessage(), rct.session().<String>get("userName")));
									rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8).end(
											ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
								}
							});
				} else {
					LOG.error(MessageFormat.format("[user : {0}] 执行查询API-->失败:{1}", res.cause().getMessage(),
							rct.session().<String>get("userName")));
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
				}
			});
		}
	}

	/**
	 * 查看一个API
	 * 
	 * @param rct
	 */
	public void getAPI(RoutingContext rct) {
		String name = rct.request().getParam("name");
		if (StrUtil.isNullOrEmpty(name)) {
			rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
					.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1404));
		} else {
			LOG.debug(MessageFormat.format("[user : {0}] 执行查看API-->{1}", rct.session().get("userName"), name));
			vertx.eventBus().<JsonObject>send(VxApiEventBusAddressConstant.GET_API, name, res -> {
				if (res.succeeded()) {
					if (res.result().body() == null || res.result().body().isEmpty()) {
						rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8).setStatusCode(404).end(_404);
						return;
					}
					JsonObject body = res.result().body();
					String appName = body.getString(VxApiDATAStoreConstant.API_APP_ID_NAME);
					JsonObject isOnLineConf = new JsonObject();
					isOnLineConf.put("appName", appName);
					isOnLineConf.put("apiName", name);
					vertx.eventBus().<Boolean>send(VxApiEventBusAddressConstant.DEPLOY_API_IS_ONLINE, isOnLineConf,
							dep -> {
								if (dep.succeeded()) {
									JsonObject api = new JsonObject(
											body.getString(VxApiDATAStoreConstant.API_CONTENT_NAME));
									api.put("online", dep.result().body());
									if (api.getValue("serverEntrance") instanceof JsonObject) {
										if ("CUSTOM"
												.equals(api.getJsonObject("serverEntrance").getValue("serverType"))) {
											if (api.getJsonObject("serverEntrance")
													.getValue("body") instanceof JsonObject) {
												JsonObject custom = api.getJsonObject("serverEntrance")
														.getJsonObject("body");
												api.put("customFactoryName", custom.getString("inFactoryName"));
												custom.remove("inFactoryName");
												api.put("customBody", custom.toString());
											}
										}
									}
									rct.put("api", api);
									rct.reroute("/getAPI.ftl");
								} else {
									LOG.error(MessageFormat.format("[user : {0}] 执行查看API:{1}-->失败:{2}",
											rct.session().get("userName"), name, dep.cause().toString()));
									rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8)
											.end(dep.cause().toString());
								}
							});
				} else {
					LOG.error(MessageFormat.format("[user : {0}] 执行查看API:{1}-->失败:{2}", rct.session().get("userName"),
							name, res.cause().toString()));
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8).end(res.cause().toString());
				}
			});
		}
	}

	/**
	 * 添加一个API
	 * 
	 * @param rct
	 */
	public void addAPI(RoutingContext rct) {
		User user = rct.user();
		user.isAuthorized(VxApiRolesConstant.WRITE, res -> {
			if (res.succeeded()) {
				if (res.result()) {
					LOG.info(MessageFormat.format("[user : {0}] 执行添加API...", rct.session().<String>get("userName")));
					JsonObject bodyAsJson = rct.getBodyAsJson();
					VxApisDTO dto = VxApisDTO.fromJson(bodyAsJson);
					dto.setApiCreateTime(Instant.now());
					JsonObject param = new JsonObject();
					param.put(VxApiDATAStoreConstant.API_ID_NAME, dto.getApiName());
					param.put(VxApiDATAStoreConstant.API_APP_ID_NAME, dto.getAppName());
					param.put(VxApiDATAStoreConstant.API_CONTENT_NAME, dto.toJson());
					vertx.eventBus().<Integer>send(VxApiEventBusAddressConstant.ADD_API, param, cres -> {
						if (cres.succeeded()) {
							rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
									.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, cres.result().body()));
							LOG.info(MessageFormat.format("[user : {0}] 执行添加API-->结果: {1}",
									rct.session().<String>get("userName"), cres.result().body()));
						} else {
							LOG.error(MessageFormat.format("[user : {0}] 执行添加API-->失败:{1}",
									rct.session().get("userName"), cres.cause()));
							HttpServerResponse response = rct.response().putHeader(CONTENT_TYPE,
									CONTENT_VALUE_JSON_UTF8);
							if (cres.cause().toString().contains("UNIQUE")) {
								response.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C1444, cres.cause().toString()));
							} else {
								response.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, cres.cause().toString()));
							}
						}
					});
				} else {
					LOG.error(
							MessageFormat.format("[user : {0}] 执行添加API-->失败:未授权或者无权利", rct.session().get("userName")));
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C401));
				}
			} else {
				LOG.error(MessageFormat.format("[user : {0}] 执行添加API-->失败:{1}", rct.session().get("userName"),
						res.cause()));
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
			}
		});
	}

	/**
	 * 加载更新API需要的数据
	 * 
	 * @param rct
	 */
	public void loadUpdtAPI(RoutingContext rct) {
		String name = rct.request().getParam("name");
		if (StrUtil.isNullOrEmpty(name)) {
			rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
					.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1404));
		} else {
			LOG.debug(MessageFormat.format("[user : {0}] 执行查看API-->{1}", rct.session().get("userName"), name));
			vertx.eventBus().<JsonObject>send(VxApiEventBusAddressConstant.GET_API, name, res -> {
				if (res.succeeded()) {
					JsonObject body = res.result().body();
					JsonObject api = new JsonObject(body.getString(VxApiDATAStoreConstant.API_CONTENT_NAME));
					if (api.isEmpty()) {
						rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8).setStatusCode(404).end(_404);
					} else {
						// 添加认证配置文件为字符串
						if (api.getValue("authOptions") != null) {
							api.put("authOptionBody",
									api.getJsonObject("authOptions").getJsonObject("option").toString());
						}
						// 添加前置处理器配置文件为字符串
						if (api.getValue("beforeHandlerOptions") != null) {
							api.put("beforeHandlerOptionsBody",
									api.getJsonObject("beforeHandlerOptions").getJsonObject("option").toString());
						}
						// 添加后置处理器配置文件为字符串
						if (api.getValue("afterHandlerOptions") != null) {
							api.put("afterHandlerOptionsBody",
									api.getJsonObject("afterHandlerOptions").getJsonObject("option").toString());
						}
						if (api.getValue("serverEntrance") instanceof JsonObject) {
							if ("CUSTOM".equals(api.getJsonObject("serverEntrance").getValue("serverType"))) {
								if (api.getJsonObject("serverEntrance").getValue("body") instanceof JsonObject) {
									JsonObject custom = api.getJsonObject("serverEntrance").getJsonObject("body");
									api.put("customFactoryName", custom.getString("inFactoryName"));
									custom.remove("inFactoryName");
									custom.remove("params");
									api.put("customBody", custom.toString());
								}
							}
						}
						rct.put("api", api);
						rct.reroute("/updateAPI.ftl");
					}
				} else {
					LOG.error(MessageFormat.format("[user : {0}] 执行查看API:{1}-->失败:{2}", rct.session().get("userName"),
							name, res.cause().toString()));
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8).end(res.cause().toString());
				}
			});
		}
	}

	/**
	 * 更新一个API
	 * 
	 * @param rct
	 */
	public void updtAPI(RoutingContext rct) {
		User user = rct.user();
		user.isAuthorized(VxApiRolesConstant.WRITE, res -> {
			if (res.succeeded()) {
				if (res.result()) {
					LOG.info(MessageFormat.format("[user : {0}] 执行修改应用...", rct.session().<String>get("userName")));
					VxApisDTO dto = VxApisDTO.fromJson(rct.getBodyAsJson());
					if (dto.getApiCreateTime() == null) {
						dto.setApiCreateTime(Instant.now());
					}
					JsonObject param = new JsonObject();
					param.put(VxApiDATAStoreConstant.API_ID_NAME, dto.getApiName());
					param.put(VxApiDATAStoreConstant.API_CONTENT_NAME, dto.toJson());
					vertx.eventBus().<Integer>send(VxApiEventBusAddressConstant.UPDT_API, param, cres -> {
						if (cres.succeeded()) {
							rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
									.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, cres.result().body()));
							LOG.info(MessageFormat.format("[user : {0}] 执行修改API:{2}-->结果: {1}",
									rct.session().<String>get("userName"), cres.result().body(), dto.getApiName()));
						} else {
							LOG.error(MessageFormat.format("[user : {0}] 执行修改API-->失败:{1}",
									rct.session().get("userName"), cres.cause()));
							rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
									.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, cres.cause().toString()));
						}
					});
				} else {
					LOG.error(
							MessageFormat.format("[user : {0}] 执行修改API-->失败:未授权或者无权利", rct.session().get("userName")));
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C401));
				}
			} else {
				LOG.error(MessageFormat.format("[user : {0}] 执行修改API-->失败:{1}", rct.session().get("userName"),
						res.cause()));
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
			}
		});
	}

	/**
	 * 删除一个API
	 * 
	 * @param rct
	 */
	public void delAPI(RoutingContext rct) {
		String name = rct.request().getParam("name");
		if (StrUtil.isNullOrEmpty(name)) {
			rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
					.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1400));
		} else {
			User user = rct.user();
			user.isAuthorized(VxApiRolesConstant.WRITE, res -> {
				if (res.succeeded()) {
					if (res.result()) {
						LOG.info(MessageFormat.format("[user : {0}] 执行删除API:{1}...",
								rct.session().<String>get("userName"), name));
						vertx.eventBus().<Integer>send(VxApiEventBusAddressConstant.DEL_API, name, cres -> {
							if (cres.succeeded()) {
								rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
										.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, cres.result().body()));
								LOG.info(MessageFormat.format("[user : {0}] 执行删除API:{2}-->结果: {1}",
										rct.session().<String>get("userName"), cres.result().body(), name));
							} else {
								LOG.error(MessageFormat.format("[user : {0}] 执行删除API:{2}-->失败:{1}",
										rct.session().get("userName"), cres.cause(), name));
								rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
										.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, cres.cause().toString()));
							}
						});
					} else {
						LOG.error(MessageFormat.format("[user : {0}] 执行删除API:{1}-->失败:未授权或者无权限",
								rct.session().get("userName"), name));
						rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
								.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C401));
					}
				} else {
					LOG.error(MessageFormat.format("[user : {0}] 执行删除API:{2}-->失败:{1}", rct.session().get("userName"),
							res.cause(), name));
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().getMessage()));
				}
			});
		}

	}

	/**
	 * 启动所有API
	 * 
	 * @param rct
	 */
	public void startAllAPI(RoutingContext rct) {
		String appName = rct.request().getParam("appName");
		if (StrUtil.isNullOrEmpty(appName)) {
			rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
					.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1400));
		} else {
			LOG.debug(MessageFormat.format("[user : {0}] 执行启动应用{1}:所有API..", rct.session().get("userName"), appName));
			DeliveryOptions option = new DeliveryOptions();
			option.setSendTimeout(200 * 301);
			vertx.eventBus().<String>send(VxApiEventBusAddressConstant.DEPLOY_API_START_ALL, appName, option, reply -> {
				if (reply.succeeded()) {
					LOG.debug(MessageFormat.format("[user : {0}] 执行启动应用{1}:所有API-->成功", rct.session().get("userName"),
							appName));
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, reply.result().body()));
				} else {
					LOG.error(MessageFormat.format("[user : {0}] 执行启动所有API-->查看API:{1}-->失败:{2}",
							rct.session().get("userName"), appName, reply.cause().toString()));
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, reply.cause().toString()));
				}
			});
		}
	}

	/**
	 * 启动一个API
	 * 
	 * @param rct
	 */
	public void startAPI(RoutingContext rct) {
		String apiName = rct.request().getParam("apiName");
		if (StrUtil.isNullOrEmpty(apiName)) {
			rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
					.end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1400));
		} else {
			LOG.debug(MessageFormat.format("[user : {0}] 执行启动API-->{1}", rct.session().get("userName"), apiName));
			vertx.eventBus().<JsonObject>send(VxApiEventBusAddressConstant.GET_API, apiName, res -> {
				if (res.succeeded()) {
					JsonObject body = res.result().body();
					String appName = body.getString(VxApiDATAStoreConstant.API_APP_ID_NAME);
					JsonObject api = new JsonObject(body.getString(VxApiDATAStoreConstant.API_CONTENT_NAME));
					JsonObject data = new JsonObject();
					data.put("appName", appName);
					data.put("apiName", apiName);
					data.put("api", api);
					vertx.eventBus().<Integer>send(VxApiEventBusAddressConstant.DEPLOY_API_START, data, reply -> {
						if (reply.succeeded()) {
							Integer result = reply.result().body();
							LOG.debug(MessageFormat.format("[user : {0}] 执行启动API-->{1},结果:{2}",
									rct.session().get("userName"), apiName, result));
							rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
									.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, result));
						} else {
							LOG.error(MessageFormat.format("[user : {0}] 执行启动API:{1}-->失败:{2}",
									rct.session().get("userName"), apiName, reply.cause().toString()));
							rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
									.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, reply.cause().toString()));
						}
					});
				} else {
					LOG.error(MessageFormat.format("[user : {0}] 执行启动API-->查看API:{1}-->失败:{2}",
							rct.session().get("userName"), apiName, res.cause().toString()));
					rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
							.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().toString()));
				}
			});
		}

	}

	/**
	 * 启动一个API
	 * 
	 * @param rct
	 */
	public void stopAPI(RoutingContext rct) {
		String appName = rct.request().getParam("appName");
		String apiName = rct.request().getParam("apiName");
		JsonObject body = new JsonObject();
		body.put("appName", appName);
		body.put("apiName", apiName);
		LOG.debug(MessageFormat.format("[user : {0}] 执行暂停API-->{1}", rct.session().get("userName"), apiName));
		vertx.eventBus().<Integer>send(VxApiEventBusAddressConstant.DEPLOY_API_STOP, body, reply -> {
			if (reply.succeeded()) {
				Integer result = reply.result().body();
				LOG.debug(MessageFormat.format("[user : {0}] 执行暂停API:{1}-->结果:{2}", rct.session().get("userName"),
						apiName, result));
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, result));
			} else {
				LOG.error(MessageFormat.format("[user : {0}] 执行停止API:{1}-->失败:{2}", rct.session().get("userName"),
						apiName, reply.cause().toString()));
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, reply.cause().toString()));
			}
		});
	}

	/**
	 * 查看API监控信息
	 * 
	 * @param rct
	 */
	public void getTrackInfo(RoutingContext rct) {
		String appName = rct.request().getParam("appName");
		String apiName = rct.request().getParam("apiName");
		JsonObject msg = new JsonObject();
		msg.put("appName", appName);
		msg.put("apiName", apiName);
		vertx.eventBus().<JsonObject>send(VxApiEventBusAddressConstant.SYSTEM_GET_TRACK_INFO, msg, reply -> {
			if (reply.succeeded()) {
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, reply.result().body()));
			} else {
				rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_JSON_UTF8)
						.end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, reply.cause()));
			}
		});
	}

	/**
	 * 退出登录
	 * 
	 * @param rct
	 */
	public void loginOut(RoutingContext rct) {
		if (rct.user() != null) {
			rct.user().clearCache();
		}
		rct.session().remove(VxApiClientStaticAuth.AUTHORIZATION);
		rct.session().put(VxApiClientStaticAuth.IS_AUTH, "false");
		rct.response().putHeader("Location", "/").setStatusCode(302).end();
	}

	/**
	 * 欢迎
	 * 
	 * @param rct
	 */
	public void welcome(RoutingContext rct) {
		rct.response().putHeader(CONTENT_TYPE, CONTENT_VALUE_HTML_UTF8)
				.end("<h1 style='text-align: center;margin-top: 5%;'>Hello VX-API " + VxApiGatewayAttribute.VERSION
						+ " <br><a href=\"http://duhua.gitee.io/vx-api-gateway-doc/\"> 查看帮助文档</a> <br> <a href=\"static/Application.html\">进入首页</a></h1>");
	}

	/**
	 * 获得客户端的 Favicon图标
	 * 
	 * @return
	 */
	public String getFaviconPath() {
		if (PathUtil.isJarEnv()) {
			return "../conf/static/logo.png";
		} else {
			return "static/logo.png";
		}
	}

	/**
	 * 获得静态文件的根目录
	 * 
	 * @return
	 */
	public String getStaticRoot() {
		if (PathUtil.isJarEnv()) {
			return "../conf/static";
		} else {
			return "static";
		}
	}

	/**
	 * 获得模板的路径
	 * 
	 * @return
	 */
	public String getTemplateRoot() {
		if (PathUtil.isJarEnv()) {
			FileResolver resolver = new FileResolver(vertx);
			File file = resolver.resolveFile(new File(PathUtil.getPathString("templates")).getPath());
			return "/" + file.getPath();
		} else {
			return "target/classes/templates";
		}

	}

	/**
	 * 获得文件上传的文件夹路径
	 * 
	 * @return
	 */
	public String getUploadsDirectory() {
		if (PathUtil.isJarEnv()) {
			return "../temp/file-uploads";
		} else {
			return "file-uploads";
		}
	}

}
