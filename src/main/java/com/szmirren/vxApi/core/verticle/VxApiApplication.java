package com.szmirren.vxApi.core.verticle;

import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.entity.VxApiTrackInfos;
import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.enums.ApiServerTypeEnum;
import com.szmirren.vxApi.core.enums.HttpMethodEnum;
import com.szmirren.vxApi.core.handler.route.VxApiRouteHandlerApiLimit;
import com.szmirren.vxApi.core.handler.route.VxApiRouteHandlerHttpService;
import com.szmirren.vxApi.core.handler.route.VxApiRouteHandlerParamCheck;
import com.szmirren.vxApi.core.handler.route.VxApiRouteHandlerRedirectType;
import com.szmirren.vxApi.core.options.VxApiApplicationDTO;
import com.szmirren.vxApi.core.options.VxApiApplicationOptions;
import com.szmirren.vxApi.core.options.VxApiCertOptions;
import com.szmirren.vxApi.core.options.VxApiCorsOptions;
import com.szmirren.vxApi.core.options.VxApiServerOptions;
import com.szmirren.vxApi.core.options.VxApisDTO;
import com.szmirren.vxApi.spi.auth.VxApiAuth;
import com.szmirren.vxApi.spi.auth.VxApiAuthFactory;
import com.szmirren.vxApi.spi.auth.VxApiAuthOptions;
import com.szmirren.vxApi.spi.customHandler.VxApiCustomHandler;
import com.szmirren.vxApi.spi.customHandler.VxApiCustomHandlerFactory;
import com.szmirren.vxApi.spi.customHandler.VxApiCustomHandlerOptions;
import com.szmirren.vxApi.spi.handler.VxApiAfterHandler;
import com.szmirren.vxApi.spi.handler.VxApiAfterHandlerFactory;
import com.szmirren.vxApi.spi.handler.VxApiAfterHandlerOptions;
import com.szmirren.vxApi.spi.handler.VxApiBeforeHandler;
import com.szmirren.vxApi.spi.handler.VxApiBeforeHandlerFactory;
import com.szmirren.vxApi.spi.handler.VxApiBeforeHandlerOptions;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PfxOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

/**
 * Api网关配置信息
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiApplication extends AbstractVerticle {
	private static final Logger LOG = LogManager.getLogger(VxApiApplication.class);

	/** HTTP服务器route集 */
	private Map<String, List<Route>> httpRouteMaps = new LinkedHashMap<>();
	/** HTTPS服务器route集 */
	private Map<String, List<Route>> httpsRouteMaps = new LinkedHashMap<>();
	/** 全局IP黑名单 */
	private Set<String> blackIpSet = new LinkedHashSet<>();
	/** HTTP的路由服务 */
	private Router httpRouter = null;
	/** HTTPS的路由服务 */
	private Router httpsRouter = null;
	/** http客户端 */
	private HttpClient httpClient = null;
	/** 应用的配置信息 */
	VxApiApplicationOptions appOption = null;
	/** 应用的名字 */
	private String appName = null;
	/** 应用的服务器与端口配置信息 */
	VxApiServerOptions serverOptions = null;
	/** 跨域设置 */
	VxApiCorsOptions corsOptions = null;
	/** 当前Vertx的唯一标识 */
	private String thisVertxName;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("加载应用配置信息...");
			}
			thisVertxName = System.getProperty("thisVertxName", "VX-API");
			VxApiApplicationDTO app = VxApiApplicationDTO.fromJson(config().getJsonObject("appConfig"));
			if (app.getWebClientCustom() != null) {
				JsonObject custom = new JsonObject(app.getWebClientCustom());
				this.appOption = new VxApiApplicationOptions(app, custom);
			} else {
				this.appOption = new VxApiApplicationOptions(app);
			}
			appName = appOption.getAppName();
			this.serverOptions = appOption.getServerOptions();
			if (LOG.isDebugEnabled()) {
				LOG.debug("加载全局黑名单");
			}
			config().getJsonArray("blackIpSet").forEach(ip -> {
				if (ip instanceof String) {
					blackIpSet.add(ip.toString());
				}
			});
			if (LOG.isDebugEnabled()) {
				LOG.debug("加载跨域处理信息");
			}
			this.corsOptions = appOption.getCorsOptions();
			if (appOption == null) {
				LOG.error("创建应用程序-->失败:配置信息为空");
				startFuture.fail("创建应用程序失败:配置信息为空");
				return;
			} else {
				this.httpClient = vertx.createHttpClient(appOption);
				Future<Void> httpFuture = Future.future(future -> {
					if (serverOptions.isCreateHttp()) {
						createHttpServer(res -> {
							if (res.succeeded()) {
								if (LOG.isDebugEnabled()) {
									LOG.debug("实例化应用程序->创建HTTP服务器-->成功!");
								}
								future.complete();
							} else {
								LOG.error("实例化应用程序->创建HTTP服务器-->失败:" + res.cause());
								future.fail(res.cause());
							}
						});
					} else {
						future.complete();
					}
				});
				Future<Void> httpsFuture = Future.future(future -> {
					if (serverOptions.isCreateHttps()) {
						createHttpsServer(res -> {
							if (res.succeeded()) {
								if (LOG.isDebugEnabled()) {
									LOG.debug("实例化应用程序->创建HTTPS服务器-->成功!");
								}
								future.complete();
							} else {
								LOG.error("实例化应用程序->创建HTTPS服务器-->失败:" + res.cause());
								future.fail(res.cause());
							}
						});
					} else {
						future.complete();
					}
				});
				Future<Void> eventFutrue = Future.future(future -> {
					// 注册操作地址
					vertx.eventBus().consumer(thisVertxName + appOption.getAppName() + VxApiEventBusAddressConstant.APPLICATION_ADD_API_SUFFIX,
							this::addRoute);
					vertx.eventBus().consumer(thisVertxName + appOption.getAppName() + VxApiEventBusAddressConstant.APPLICATION_DEL_API_SUFFIX,
							this::delRoute);
					vertx.eventBus().consumer(VxApiEventBusAddressConstant.SYSTEM_PUBLISH_BLACK_IP_LIST, this::updateIpBlackList);
					future.complete();
				});
				CompositeFuture.all(httpFuture, httpsFuture, eventFutrue).setHandler(res -> {
					if (res.succeeded()) {
						LOG.info("实例化应用程序-->成功");
						startFuture.complete();
					} else {
						LOG.error("实例化应用程序-->失败:", res.cause());
						startFuture.fail(res.cause());
					}
				});
			}
		} catch (Exception e) {
			LOG.error("实例化应用程序-->失败:", e);
			startFuture.fail(e);
		}
	}

	/**
	 * 创建http服务器
	 * 
	 * @param createHttp
	 */
	public void createHttpServer(Handler<AsyncResult<Void>> createHttp) {
		this.httpRouter = Router.router(vertx);
		httpRouter.route().handler(this::filterBlackIP);
		httpRouter.route().handler(CookieHandler.create());
		SessionStore sessionStore = null;
		if (vertx.isClustered()) {
			sessionStore = ClusteredSessionStore.create(vertx);
		} else {
			sessionStore = LocalSessionStore.create(vertx);
		}
		SessionHandler sessionHandler = SessionHandler.create(sessionStore);
		sessionHandler.setSessionCookieName(appOption.getSessionCookieName());
		sessionHandler.setSessionTimeout(appOption.getSessionTimeOut());
		httpRouter.route().handler(sessionHandler);
		// 跨域处理
		if (corsOptions != null) {
			CorsHandler corsHandler = CorsHandler.create(corsOptions.getAllowedOrigin());
			if (corsOptions.getAllowedHeaders() != null) {
				corsHandler.allowedHeaders(corsOptions.getAllowedHeaders());
			}
			corsHandler.allowCredentials(corsOptions.isAllowCredentials());
			if (corsOptions.getExposedHeaders() != null) {
				corsHandler.exposedHeaders(corsOptions.getExposedHeaders());
			}
			if (corsOptions.getAllowedMethods() != null) {
				corsHandler.allowedMethods(corsOptions.getAllowedMethods());
			}
			corsHandler.maxAgeSeconds(corsOptions.getMaxAgeSeconds());
			httpRouter.route().handler(corsHandler);
		}
		// 如果在linux系统开启epoll
		if (vertx.isNativeTransportEnabled()) {
			serverOptions.setTcpFastOpen(true).setTcpCork(true).setTcpQuickAck(true).setReusePort(true);
		}
		// 404页面
		httpRouter.route().order(999999).handler(rct -> {
			if (LOG.isDebugEnabled()) {
				LOG.debug("用户: " + rct.request().remoteAddress().host() + "请求的了不存的路径: " + rct.request().method() + ":" + rct.request().path());
			}
			HttpServerResponse response = rct.response();
			if (appOption.getNotFoundContentType() != null) {
				response.putHeader("Content-Type", appOption.getNotFoundContentType());
			}
			response.end(appOption.getNotFoundResult());
		});
		// 创建http服务器
		vertx.createHttpServer(serverOptions).requestHandler(httpRouter::accept).listen(serverOptions.getHttpPort(), res -> {
			if (res.succeeded()) {
				System.out.println(
						MessageFormat.format("{0} Running on port {1} by HTTP", appOption.getAppName(), Integer.toString(serverOptions.getHttpPort())));
				createHttp.handle(Future.succeededFuture());
			} else {
				System.out.println("create HTTP Server failed : " + res.cause());
				createHttp.handle(Future.failedFuture(res.cause()));
			}
		});
	}

	/**
	 * 创建https服务器
	 * 
	 * @param createHttp
	 */
	public void createHttpsServer(Handler<AsyncResult<Void>> createHttps) {
		this.httpsRouter = Router.router(vertx);
		httpsRouter.route().handler(this::filterBlackIP);
		httpsRouter.route().handler(CookieHandler.create());
		SessionStore sessionStore = null;
		if (vertx.isClustered()) {
			sessionStore = ClusteredSessionStore.create(vertx);
		} else {
			sessionStore = LocalSessionStore.create(vertx);
		}
		SessionHandler sessionHandler = SessionHandler.create(sessionStore);
		sessionHandler.setSessionCookieName(appOption.getSessionCookieName());
		sessionHandler.setSessionTimeout(appOption.getSessionTimeOut());
		httpsRouter.route().handler(sessionHandler);
		// 跨域处理
		if (corsOptions != null) {
			CorsHandler corsHandler = CorsHandler.create(corsOptions.getAllowedOrigin());
			if (corsOptions.getAllowedHeaders() != null) {
				corsHandler.allowedHeaders(corsOptions.getAllowedHeaders());
			}
			corsHandler.allowCredentials(corsOptions.isAllowCredentials());
			if (corsOptions.getExposedHeaders() != null) {
				corsHandler.exposedHeaders(corsOptions.getExposedHeaders());
			}
			if (corsOptions.getAllowedMethods() != null) {
				corsHandler.allowedMethods(corsOptions.getAllowedMethods());
			}
			corsHandler.maxAgeSeconds(corsOptions.getMaxAgeSeconds());
			httpsRouter.route().handler(corsHandler);
		}
		// 创建https服务器
		serverOptions.setSsl(true);
		VxApiCertOptions certOptions = serverOptions.getCertOptions();
		if (certOptions.getCertType().equalsIgnoreCase("pem")) {
			serverOptions
					.setPemKeyCertOptions(new PemKeyCertOptions().setCertPath(certOptions.getCertPath()).setKeyPath(certOptions.getCertKey()));
		} else if (certOptions.getCertType().equalsIgnoreCase("pfx")) {
			serverOptions.setPfxKeyCertOptions(new PfxOptions().setPath(certOptions.getCertPath()).setPassword(certOptions.getCertKey()));
		} else {
			LOG.error("创建https服务器-->失败:无效的证书类型,只支持pem/pfx格式的证书");
			createHttps.handle(Future.failedFuture("创建https服务器-->失败:无效的证书类型,只支持pem/pfx格式的证书"));
			return;
		}
		Future<Boolean> createFuture = Future.future();
		vertx.fileSystem().exists(certOptions.getCertPath(), createFuture);
		createFuture.setHandler(check -> {
			if (check.succeeded()) {
				if (check.result()) {
					// 404页面
					httpsRouter.route().order(999999).handler(rct -> {
						if (LOG.isDebugEnabled()) {
							LOG.debug(
									"用户: " + rct.request().remoteAddress().host() + "请求的了不存的路径: " + rct.request().method() + ":" + rct.request().path());
						}
						HttpServerResponse response = rct.response();
						if (appOption.getNotFoundContentType() != null) {
							response.putHeader("Content-Type", appOption.getNotFoundContentType());
						}
						response.end(appOption.getNotFoundResult());
					});
					// 如果在linux系统开启epoll
					if (vertx.isNativeTransportEnabled()) {
						serverOptions.setTcpFastOpen(true).setTcpCork(true).setTcpQuickAck(true).setReusePort(true);
					}
					vertx.createHttpServer(serverOptions).requestHandler(httpsRouter::accept).listen(serverOptions.getHttpsPort(), res -> {
						if (res.succeeded()) {
							System.out.println(appOption.getAppName() + " Running on port " + serverOptions.getHttpsPort() + " by HTTPS");
							createHttps.handle(Future.succeededFuture());
						} else {
							System.out.println("create HTTPS Server failed : " + res.cause());
							createHttps.handle(Future.failedFuture(res.cause()));
						}
					});
				} else {
					LOG.error("执行创建https服务器-->失败:无效的证书或者错误的路径:如果证书存放在conf/cert中,路径可以从cert/开始,示例:cert/XXX.XXX");
					createHttps.handle(Future.failedFuture("无效的证书或者错误的路径"));
				}
			} else {
				LOG.error("执行创建https服务器-->失败:无效的证书或者错误的路径:如果证书存放在conf/cert中,路径可以从cert/开始,示例:cert/XXX.XXX", check.cause());
				createHttps.handle(Future.failedFuture(check.cause()));
			}
		});
	}

	/**
	 * 过滤黑名单
	 * 
	 * @param rct
	 */
	public void filterBlackIP(RoutingContext rct) {
		// 添加请求到达VX-API的数量
		vertx.eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_VX_REQUEST, null);
		String host = rct.request().remoteAddress().host();
		if (blackIpSet.contains(host)) {
			HttpServerResponse response = rct.response();
			if (appOption.getBlacklistIpContentType() != null) {
				response.putHeader(CONTENT_TYPE, appOption.getBlacklistIpContentType());
			}
			response.setStatusCode(appOption.getBlacklistIpCode());
			if (appOption.getBlacklistIpResult() != null) {
				response.setStatusMessage(appOption.getBlacklistIpResult());
			} else {
				response.setStatusMessage("you can't access this service");
			}
			response.end();
		} else {
			rct.next();
		}
	}

	/**
	 * 更新ip黑名单
	 */
	@SuppressWarnings("unchecked")
	public void updateIpBlackList(Message<JsonArray> msg) {
		if (msg.body() != null) {
			this.blackIpSet = new LinkedHashSet<>(msg.body().getList());
		} else {
			blackIpSet = new LinkedHashSet<>();
		}
	}

	/**
	 * 添加一个路由
	 * 
	 * @param msg
	 */
	public void addRoute(Message<JsonObject> msg) {
		JsonObject body = msg.body().getJsonObject("api");
		VxApisDTO dto = VxApisDTO.fromJson(body);
		if (dto != null) {
			VxApis api = new VxApis(dto);
			// 是否代理启动API到当前应用
			boolean otherRouteAdd = msg.body().getBoolean("elseRouteToThis", false);
			if (otherRouteAdd) {
				// 服务器的类型1=http,2=https,3=webSocket
				int type = msg.body().getInteger("serverType", 0);
				if (type == 1) {
					addHttpRouter(api, res -> {
						if (res.succeeded()) {
							msg.reply(1);
						} else {
							msg.fail(500, res.cause().getMessage());
						}
					});
				} else if (type == 2) {
					addHttpsRouter(api, res -> {
						if (res.succeeded()) {
							msg.reply(1);
						} else {
							msg.fail(500, res.cause().getMessage());
							res.cause().printStackTrace();
						}
					});
				} else {
					msg.fail(500, "不存在的服务");
				}
			} else {
				// 本应用添加API,既属于自己的网关应用添加API
				if (httpRouter != null && httpsRouter != null) {
					Future<Boolean> httpFuture = Future.future(http -> addHttpRouter(api, http));
					Future<Boolean> httpsFuture = Future.<Boolean>future(https -> addHttpsRouter(api, https));
					CompositeFuture.all(httpFuture, httpsFuture).setHandler(res -> {
						if (res.succeeded()) {
							msg.reply(1);
						} else {
							msg.fail(500, res.cause().getMessage());
						}
					});
				} else if (httpRouter != null) {
					addHttpRouter(api, res -> {
						if (res.succeeded()) {
							msg.reply(1);
						} else {
							msg.fail(500, res.cause().getMessage());
						}
					});
				} else if (httpsRouter != null) {
					addHttpsRouter(api, res -> {
						if (res.succeeded()) {
							msg.reply(1);
						} else {
							msg.fail(500, res.cause().getMessage());
						}
					});
				} else {
					msg.fail(404, "找不到的服务器可以加载API");
				}
			}
		} else {
			msg.fail(1400, "API参数不能为null,请检查APIDTO需要实例化的JSON编写是否正确");
		}
	}

	/**
	 * 添加HTTP服务器的路由
	 * 
	 * @param result
	 */
	public void addHttpRouter(VxApis api, Handler<AsyncResult<Boolean>> result) {
		addRouteToRouter(api, httpRouter, httpRouteMaps, result);
	}

	/**
	 * 添加HTTPS服务器的路由
	 * 
	 * @param result
	 */
	public void addHttpsRouter(VxApis api, Handler<AsyncResult<Boolean>> result) {
		addRouteToRouter(api, httpsRouter, httpsRouteMaps, result);
	}

	/**
	 * 通用给Router添加route
	 * 
	 * @param api
	 *          配置信息
	 * @param router
	 *          要添加的router
	 * @param routeMaps
	 *          要添加的route集合
	 * @param result
	 *          结果
	 */
	public void addRouteToRouter(VxApis api, Router router, Map<String, List<Route>> routeMaps, Handler<AsyncResult<Boolean>> result) {
		vertx.executeBlocking(fut -> {
			List<Route> routes = new ArrayList<>();// 存储部署的路由
			// 流量限制处理器
			if (api.getLimitUnit() != null) {
				Route limitRoute = router.route();// 流量限制的route;
				initApiLimit(api, limitRoute);
				routes.add(limitRoute);
			}
			Route checkRoute = router.route();// 参数检查的的route;
			try {
				initParamCheck(api, checkRoute);
				routes.add(checkRoute);
			} catch (Exception e) {
				checkRoute.remove();
				routes.forEach(r -> r.remove());// 清空已经成功的路由
				LOG.error(appName + ":API:" + api.getApiName() + "添加参数检查-->失败:" + e);
				fut.fail(e);
				return;
			}

			// 认证处理器
			if (api.getAuthOptions() != null) {
				Route authRoute = router.route();// 权限认证的route;
				try {
					initAuthHandler(api, authRoute);
					routes.add(authRoute);
				} catch (Exception e) {
					authRoute.remove();
					routes.forEach(r -> r.remove());// 清空已经成功的路由
					LOG.error(appName + ":API:" + api.getApiName() + "添加权限认证-->失败:" + e);
					fut.fail(e);
					return;
				}
			}

			// 前置处理器
			if (api.getBeforeHandlerOptions() != null) {
				Route beforeRoute = router.route();// 前置处理器的route;
				try {
					initBeforeHandler(api, beforeRoute);
					routes.add(beforeRoute);
				} catch (Exception e) {
					LOG.error(appName + ":API:" + api.getApiName() + "添加前置处理器-->失败:" + e);
					beforeRoute.remove();
					routes.forEach(r -> r.remove());// 清空已经成功的路由
					fut.fail(e);
					return;
				}
			}
			// 检查是否有后置处理器,有next给后置处理器,如果没有则response
			boolean isAfterHandler = api.getAfterHandlerOptions() != null;
			// 添加与后台交互的中心处理器
			Route serverRoute = router.route();
			try {
				initServerHandler(isAfterHandler, api, serverRoute);
				routes.add(serverRoute);
			} catch (Exception e) {
				LOG.error(appName + ":API:" + api.getApiName() + "添加服务处理器-->失败:" + e);
				serverRoute.remove();
				routes.forEach(r -> r.remove());// 清空已经成功的路由
				fut.fail(e);
				return;
			}
			// 后置处理器
			if (isAfterHandler) {
				Route afterRoute = router.route();// 前置处理器的route;
				try {
					initAfterHandler(api, afterRoute);
					routes.add(afterRoute);
				} catch (Exception e) {
					LOG.error(appName + ":API:" + api.getApiName() + "添加后置处理器-->失败:" + e);
					afterRoute.remove();
					routes.forEach(r -> r.remove());// 清空已经成功的路由
					fut.fail(e);
					return;
				}
			}
			// 添加异常处理器
			Route exRoute = router.route();
			initExceptionHanlder(api, exRoute);
			routes.add(exRoute);
			routeMaps.put(api.getApiName(), routes);
			fut.complete();
			if (LOG.isDebugEnabled()) {
				LOG.debug("启动" + appName + ": API:" + api.getApiName() + "-->成功");
			}
		}, result);

	}

	/**
	 * 初始化权限认证
	 * 
	 * @param path
	 *          路径
	 * @param method
	 *          类型
	 * @param consumes
	 *          接收类型
	 * @param route
	 *          路由
	 * @throws Exception
	 */
	public void initAuthHandler(VxApis api, Route route) throws Exception {
		route.path(api.getPath());
		if (api.getMethod() != HttpMethodEnum.ALL) {
			route.method(HttpMethod.valueOf(api.getMethod().getVal()));
		}
		// 添加consumes
		if (api.getConsumes() != null) {
			api.getConsumes().forEach(va -> route.consumes(va));
		}
		// 添加handler
		VxApiAuthOptions authOptions = api.getAuthOptions();
		VxApiAuth authHandler = VxApiAuthFactory.getVxApiAuth(authOptions.getInFactoryName(), authOptions.getOption(), api, httpClient);
		route.handler(authHandler);
	}

	/**
	 * 初始化前置路由器
	 * 
	 * @param path
	 *          路径
	 * @param method
	 *          类型
	 * @param consumes
	 *          接收类型
	 * @param route
	 *          路由
	 * @throws Exception
	 */
	public void initBeforeHandler(VxApis api, Route route) throws Exception {
		route.path(api.getPath());
		if (api.getMethod() != HttpMethodEnum.ALL) {
			route.method(HttpMethod.valueOf(api.getMethod().getVal()));
		}
		// 添加consumes
		if (api.getConsumes() != null) {
			api.getConsumes().forEach(va -> route.consumes(va));
		}
		// 添加handler
		VxApiBeforeHandlerOptions options = api.getBeforeHandlerOptions();
		VxApiBeforeHandler beforeHandler = VxApiBeforeHandlerFactory.getBeforeHandler(options.getInFactoryName(), options.getOption(), api,
				httpClient);
		route.handler(beforeHandler);
	}

	/**
	 * 初始化后置路由器
	 * 
	 * @param path
	 *          路径
	 * @param method
	 *          类型
	 * @param consumes
	 *          接收类型
	 * @param route
	 *          路由
	 * @throws Exception
	 */
	public void initAfterHandler(VxApis api, Route route) throws Exception {
		route.path(api.getPath());
		if (api.getMethod() != HttpMethodEnum.ALL) {
			route.method(HttpMethod.valueOf(api.getMethod().getVal()));
		}
		// 添加consumes
		if (api.getConsumes() != null) {
			api.getConsumes().forEach(va -> route.consumes(va));
		}
		// 添加handler
		VxApiAfterHandlerOptions options = api.getAfterHandlerOptions();
		VxApiAfterHandler afterHandler = VxApiAfterHandlerFactory.getAfterHandler(options.getInFactoryName(), options.getOption(), api,
				httpClient);
		route.handler(afterHandler);
	}

	/**
	 * 初始化流量限制
	 * 
	 * @param api
	 * @param route
	 */
	public void initApiLimit(VxApis api, Route route) {
		route.path(api.getPath());
		if (api.getMethod() != HttpMethodEnum.ALL) {
			route.method(HttpMethod.valueOf(api.getMethod().getVal()));
		}
		// 添加consumes
		if (api.getConsumes() != null) {
			api.getConsumes().forEach(va -> route.consumes(va));
		}
		if (api.getLimitUnit() != null) {
			if (api.getApiLimit() <= -1 && api.getIpLimit() <= -1) {
				api.setLimitUnit(null);
			}
		}
		// 流量限制处理处理器
		VxApiRouteHandlerApiLimit apiLimitHandler = VxApiRouteHandlerApiLimit.create(api);
		route.handler(apiLimitHandler);
	}

	/**
	 * 初始化参数检查,参数检查已经交给处理器做了
	 * 
	 * @param api
	 * @param route
	 */
	public void initParamCheck(VxApis api, Route route) {
		route.path(api.getPath());
		if (api.getMethod() != HttpMethodEnum.ALL) {
			route.method(HttpMethod.valueOf(api.getMethod().getVal()));
		}
		// 添加consumes
		if (api.getConsumes() != null) {
			api.getConsumes().forEach(va -> route.consumes(va));
		}
		VxApiRouteHandlerParamCheck paramCheckHandler = VxApiRouteHandlerParamCheck.create(api, appOption.getContentLength());
		route.handler(paramCheckHandler);

	}

	/**
	 * 初始化与后端服务交互
	 * 
	 * @param isNext
	 *          下一步还是结束(也就是说如有后置处理器inNext=true,反则false)
	 * @param api
	 * @param route
	 * @throws Exception
	 */
	public void initServerHandler(boolean isNext, VxApis api, Route route) throws Exception {
		route.path(api.getPath());
		if (api.getMethod() != HttpMethodEnum.ALL) {
			route.method(HttpMethod.valueOf(api.getMethod().getVal()));
		}
		if (api.getConsumes() != null) {
			api.getConsumes().forEach(va -> route.consumes(va));
		}
		if (api.getServerEntrance().getServerType() == ApiServerTypeEnum.CUSTOM) {
			serverCustomTypeHandler(isNext, api, route);
		} else if (api.getServerEntrance().getServerType() == ApiServerTypeEnum.REDIRECT) {
			serverRedirectTypeHandler(isNext, api, route);
		} else if (api.getServerEntrance().getServerType() == ApiServerTypeEnum.HTTP_HTTPS) {
			serverHttpTypeHandler(isNext, api, route);
		} else {
			route.handler(rct -> {
				// TODO 当没有响应服务时next或者结束请求
				if (isNext) {
					rct.next();
				} else {
					rct.response().putHeader(SERVER, VxApiGatewayAttribute.FULL_NAME).putHeader(CONTENT_TYPE, api.getContentType()).setStatusCode(404)
							.end();
				}
			});
		}
	}

	/**
	 * 初始化异常Handler
	 * 
	 * @param api
	 * @param route
	 */
	public void initExceptionHanlder(VxApis api, Route route) {
		route.path(api.getPath());
		if (api.getMethod() != HttpMethodEnum.ALL) {
			route.method(HttpMethod.valueOf(api.getMethod().getVal()));
		}
		if (api.getConsumes() != null) {
			api.getConsumes().forEach(va -> route.consumes(va));
		}
		route.failureHandler(rct -> {
			rct.response().putHeader(SERVER, VxApiGatewayAttribute.FULL_NAME).putHeader(CONTENT_TYPE, api.getContentType())
					.setStatusCode(api.getResult().getFailureStatus()).end(api.getResult().getFailureExample());
			VxApiTrackInfos infos = new VxApiTrackInfos(appName, api.getApiName());
			if (rct.failure() != null) {
				infos.setErrMsg(rct.failure().getMessage());
				infos.setErrStackTrace(rct.failure().getStackTrace());
			} else {
				infos.setErrMsg("没有进一步信息 failure 为 null");
			}
			vertx.eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_ERROR, infos.toJson());
		});
	}

	/**
	 * 自定义服务类型处理器
	 * 
	 * @param isNext
	 * @param api
	 * @param route
	 * @throws Exception
	 */
	public void serverCustomTypeHandler(boolean isNext, VxApis api, Route route) throws Exception {
		JsonObject body = api.getServerEntrance().getBody();
		VxApiCustomHandlerOptions options = VxApiCustomHandlerOptions.fromJson(body);
		if (options == null) {
			throw new NullPointerException("自定义服务类型的配置文件无法装换为服务类");
		}
		if (body.getValue("isNext") == null) {
			body.put("isNext", isNext);
		}
		options.setOption(body);
		VxApiCustomHandler customHandler = VxApiCustomHandlerFactory.getCustomHandler(options.getInFactoryName(), options.getOption(), api,
				httpClient);
		route.handler(customHandler);
	}

	/**
	 * 页面跳转服务类型处理器
	 * 
	 * @param isNext
	 * @param api
	 * @param route
	 * @throws NullPointerException
	 */
	public void serverRedirectTypeHandler(boolean isNext, VxApis api, Route route) throws NullPointerException {
		VxApiRouteHandlerRedirectType redirectTypehandler = VxApiRouteHandlerRedirectType.create(isNext, api);
		route.handler(redirectTypehandler);
	}

	/**
	 * HTTP/HTTPS服务类型处理器
	 * 
	 * @param isNext
	 * @param api
	 * @param route
	 * @throws NullPointerException
	 * @throws MalformedURLException
	 */
	public void serverHttpTypeHandler(boolean isNext, VxApis api, Route route) throws NullPointerException, MalformedURLException {
		VxApiRouteHandlerHttpService httpTypeHandler = VxApiRouteHandlerHttpService.create(appName, isNext, api, httpClient);
		route.handler(httpTypeHandler);
	}

	/**
	 * 更新一个路由
	 * 
	 * @param msg
	 */
	public void updtRoute(Message<JsonObject> msg) {
		if (msg.body() == null) {
			msg.fail(1400, "参数不能为空");
			return;
		}
		VxApisDTO dto = VxApisDTO.fromJson(msg.body());
		if (dto == null) {
			msg.fail(1405, "参数不能为空");
			return;
		}
		String apiName = dto.getApiName();
		if (httpRouteMaps.get(apiName) != null) {
			httpRouteMaps.get(apiName).forEach(r -> r.disable().remove());
		}
		if (httpsRouteMaps.get(apiName) != null) {
			httpsRouteMaps.get(apiName).forEach(r -> r.disable().remove());
		}
		addRoute(msg);
	}

	/**
	 * 删除一个路由
	 * 
	 * @param msg
	 */
	public void delRoute(Message<String> msg) {
		if (StrUtil.isNullOrEmpty(msg.body())) {
			msg.fail(1400, "参数:API名字不能为空");
			return;
		}
		String apiName = msg.body();
		if (httpRouteMaps.get(apiName) != null) {
			httpRouteMaps.get(apiName).forEach(r -> r.disable().remove());
		}
		if (httpsRouteMaps.get(apiName) != null) {
			httpsRouteMaps.get(apiName).forEach(r -> r.disable().remove());
		}
		msg.reply(1);
	}

	/**
	 * 返回类型
	 */
	private static String CONTENT_TYPE = "Content-Type";
	/**
	 * 服务器类型
	 */
	private static String SERVER = "Server";

}
