package com.szmirren.vxApi.core.verticle;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.entity.VxApiAPILimit;
import com.szmirren.vxApi.core.entity.VxApiEntranceParam;
import com.szmirren.vxApi.core.entity.VxApiServerURL;
import com.szmirren.vxApi.core.entity.VxApiServerURLInfo;
import com.szmirren.vxApi.core.entity.VxApiServerURLPollingPolicy;
import com.szmirren.vxApi.core.entity.VxApiTrackInfos;
import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.enums.ApiServerTypeEnum;
import com.szmirren.vxApi.core.enums.HttpMethodEnum;
import com.szmirren.vxApi.core.enums.LoadBalanceEnum;
import com.szmirren.vxApi.core.enums.ParamPositionEnum;
import com.szmirren.vxApi.core.enums.ParamSystemVarTypeEnum;
import com.szmirren.vxApi.core.options.VxApiApplicationDTO;
import com.szmirren.vxApi.core.options.VxApiApplicationOptions;
import com.szmirren.vxApi.core.options.VxApiCertOptions;
import com.szmirren.vxApi.core.options.VxApiCorsOptions;
import com.szmirren.vxApi.core.options.VxApiParamCheckOptions;
import com.szmirren.vxApi.core.options.VxApiParamOptions;
import com.szmirren.vxApi.core.options.VxApiServerEntranceHttpOptions;
import com.szmirren.vxApi.core.options.VxApiServerEntranceRedirectOptions;
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
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.CaseInsensitiveHeaders;
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
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
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
	private final Logger LOG = Logger.getLogger(this.getClass());
	/**
	 * HTTP服务器route集
	 */
	private Map<String, List<Route>> httpRouteMaps = new LinkedHashMap<>();
	/**
	 * HTTPS服务器route集
	 */
	private Map<String, List<Route>> httpsRouteMaps = new LinkedHashMap<>();
	/**
	 * API后端服务连接策略集
	 */
	private Map<String, VxApiServerURLPollingPolicy> serverUrlPolicyMap = new LinkedHashMap<>();
	/**
	 * API访问限制
	 */
	private Map<String, VxApiAPILimit> apiLimitMap = new HashMap<>();
	/**
	 * 全局IP黑名单
	 */
	private Set<String> blackIpSet = new LinkedHashSet<>();
	/**
	 * HTTP的路由服务
	 */
	private Router httpRouter = null;
	/**
	 * HTTPS的路由服务
	 */
	private Router httpsRouter = null;
	/**
	 * http客户端
	 */
	private HttpClient httpClient = null;
	/**
	 * web客户端
	 */
	private WebClient webClient = null;
	/**
	 * 应用的配置信息
	 */
	VxApiApplicationOptions appOption = null;
	/**
	 * 应用的名字
	 */
	private String APP_NAME = null;
	/**
	 * 应用的服务器与端口配置信息
	 */
	VxApiServerOptions serverOptions = null;
	/**
	 * 跨域设置
	 */
	VxApiCorsOptions corsOptions = null;

	@Override
	public void start(Future<Void> fut) throws Exception {
		try {
			LOG.debug("加载应用配置信息");
			VxApiApplicationDTO app = VxApiApplicationDTO.fromJson(config().getJsonObject("appConfig"));
			this.appOption = new VxApiApplicationOptions(app);
			APP_NAME = appOption.getAppName();
			this.serverOptions = appOption.getServerOptions();
			LOG.debug("加载全局黑名单");
			config().getJsonArray("blackIpSet").forEach(ip -> {
				if (ip instanceof String) {
					blackIpSet.add(ip.toString());
				}
			});
			LOG.debug("加载跨域处理信息");
			this.corsOptions = appOption.getCorsOptions();
			if (appOption == null) {
				fut.fail("创建应用程序失败:配置信息为空");
				LOG.error("创建应用程序-->失败:配置信息为空");
			} else {
				this.httpClient = vertx.createHttpClient(appOption);
				this.webClient = WebClient.wrap(httpClient, appOption);
				Future.<Boolean>future(http -> {
					// 创建HTTP服务器
					if (serverOptions.isCreateHttp()) {
						createHttpServer(http);
					} else {
						http.handle(Future.<Boolean>succeededFuture(true));
					}
				}).compose(http -> Future.<Boolean>future(https -> {
					// 创建HTTPS服务器
					if (serverOptions.isCreateHttps()) {
						createHttpsServer(https);
					} else {
						https.handle(Future.<Boolean>succeededFuture());
					}
				})).setHandler(res -> {
					if (res.succeeded()) {
						// 注册操作地址
						vertx.eventBus().consumer(
								appOption.getAppName() + VxApiEventBusAddressConstant.APPLICATION_ADD_API_SUFFIX,
								this::addRoute);
						vertx.eventBus().consumer(
								appOption.getAppName() + VxApiEventBusAddressConstant.APPLICATION_UPDT_API_SUFFIX,
								this::updtRoute);
						vertx.eventBus().consumer(
								appOption.getAppName() + VxApiEventBusAddressConstant.APPLICATION_DEL_API_SUFFIX,
								this::delRoute);
						vertx.eventBus().consumer(VxApiEventBusAddressConstant.SYSTEM_PUBLISH_BLACK_IP_LIST,
								this::updateIpBlackList);
						fut.complete();
					} else {
						fut.fail(res.cause());
					}
				});
			}
		} catch (Exception e) {
			fut.fail("实例化应用程序失败:" + e);
			LOG.error("实例化应用程序-->失败:" + e);
		}
	}

	/**
	 * 创建http服务器
	 * 
	 * @param createHttp
	 */
	public void createHttpServer(Handler<AsyncResult<Boolean>> createHttp) {
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
		httpRouter.route().handler(BodyHandler.create().setUploadsDirectory("../temp/file-uploads")
				.setBodyLimit(appOption.getContentLength()));
		// 跨域处理
		if (corsOptions != null) {
			CorsHandler corsHandler = CorsHandler.create(corsOptions.getAllowedOrigin());
			corsHandler.allowedHeaders(corsOptions.getAllowedHeaders())
					.allowCredentials(corsOptions.isAllowCredentials()).exposedHeaders(corsOptions.getExposedHeaders())
					.allowedMethods(corsOptions.getAllowedMethods()).maxAgeSeconds(corsOptions.getMaxAgeSeconds());
			httpRouter.route().handler(corsHandler);
		}
		// 如果在linux系统开启epoll
		if (vertx.isNativeTransportEnabled()) {
			serverOptions.setTcpFastOpen(true).setTcpCork(true).setTcpQuickAck(true).setReusePort(true);
		}
		// 创建http服务器
		vertx.createHttpServer(serverOptions).requestHandler(httpRouter::accept).listen(serverOptions.getHttpPort(),
				res -> {
					if (res.succeeded()) {
						System.out.println(MessageFormat.format("{0} Running on port {1} by HTTP",
								appOption.getAppName(), Integer.toString(serverOptions.getHttpPort())));
						createHttp.handle(Future.<Boolean>succeededFuture(true));
					} else {
						System.out.println("create HTTP Server failed : " + res.cause());
						createHttp.handle(Future.<Boolean>failedFuture(res.cause()));
					}
				});
	}

	/**
	 * 创建https服务器
	 * 
	 * @param createHttp
	 */
	public void createHttpsServer(Handler<AsyncResult<Boolean>> createHttps) {
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
		httpsRouter.route().handler(BodyHandler.create().setUploadsDirectory("../temp/file-uploads")
				.setBodyLimit(appOption.getContentLength()));
		// 跨域处理
		if (corsOptions != null) {
			CorsHandler corsHandler = CorsHandler.create(corsOptions.getAllowedOrigin());
			corsHandler.allowedHeaders(corsOptions.getAllowedHeaders())
					.allowCredentials(corsOptions.isAllowCredentials()).exposedHeaders(corsOptions.getExposedHeaders())
					.allowedMethods(corsOptions.getAllowedMethods()).maxAgeSeconds(corsOptions.getMaxAgeSeconds());
			httpsRouter.route().handler(corsHandler);
		}
		// 创建https服务器
		boolean isCert = true;
		serverOptions.setSsl(true);
		VxApiCertOptions certOptions = serverOptions.getCertOptions();
		if (certOptions.getCertType().equalsIgnoreCase("pem")) {
			serverOptions.setPemKeyCertOptions(new PemKeyCertOptions().setCertPath(certOptions.getCertPath())
					.setKeyPath(certOptions.getCertKey()));
		} else if (certOptions.getCertType().equalsIgnoreCase("pfx")) {
			serverOptions.setPfxKeyCertOptions(
					new PfxOptions().setPath(certOptions.getCertPath()).setPassword(certOptions.getCertKey()));
		} else {
			LOG.equals("创建https服务器-->失败:无效的证书类型,只支持pem/pfx格式的证书");
			isCert = false;
			createHttps.handle(
					Future.<Boolean>failedFuture(new RuntimeException("创建https服务器-->失败:无效的证书类型,只支持pem/pfx格式的证书")));
		}
		if (isCert) {
			// 如果在linux系统开启epoll
			if (vertx.isNativeTransportEnabled()) {
				serverOptions.setTcpFastOpen(true).setTcpCork(true).setTcpQuickAck(true).setReusePort(true);
			}
			vertx.createHttpServer(serverOptions).requestHandler(httpsRouter::accept)
					.listen(serverOptions.getHttpsPort(), res -> {
						if (res.succeeded()) {
							System.out.println(MessageFormat.format("{0} Running on port {1} by HTTPS",
									appOption.getAppName(), Integer.toString(serverOptions.getHttpsPort())));
							createHttps.handle(Future.<Boolean>succeededFuture(true));
						} else {
							System.out.println("create HTTPS Server failed : " + res.cause());
							createHttps.handle(Future.<Boolean>failedFuture(res.cause()));
						}
					});
		}
	}

	/**
	 * 过滤黑名单
	 * 
	 * @param rct
	 */
	public void filterBlackIP(RoutingContext rct) {
		String host = rct.request().remoteAddress().host();
		if (blackIpSet.contains(host)) {
			rct.response().setStatusCode(404).setStatusMessage("you can't access this service").end();
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
			if (httpRouter != null && httpsRouter != null) {
				Future.<Boolean>future(http -> addHttpRouter(api, http))
						.compose(http -> Future.<Boolean>future(https -> addHttpsRouter(api, https)))
						.setHandler(res -> {
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
						res.cause().printStackTrace();
					}
				});
			} else {
				msg.fail(404, "找不到的服务器可以加载API");
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
	 *            配置信息
	 * @param router
	 *            要添加的router
	 * @param routeMaps
	 *            要添加的route集合
	 * @param result
	 *            结果
	 */
	public void addRouteToRouter(VxApis api, Router router, Map<String, List<Route>> routeMaps,
			Handler<AsyncResult<Boolean>> result) {
		vertx.executeBlocking(fut -> {
			List<Route> routes = new ArrayList<>();// 存储部署的路由
			// 流量限制处理器
			if (api.getLimitUnit() != null) {
				Route limitRoute = router.route();// 权限认证的route;
				initApiLimit(api, limitRoute);
				routes.add(limitRoute);
			}
			// 入口参数检查
			if (api.getEnterParam() != null) {
				Route checkRoute = router.route();// 权限认证的route;
				initParamCheck(api, checkRoute);
				routes.add(checkRoute);
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
					LOG.error("添加权限认证-->失败:" + e);
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
					LOG.error("添加前置处理器-->失败:" + e);
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
				LOG.error("添加服务处理器-->失败:" + e);
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
					LOG.error("添加后置处理器-->失败:" + e);
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
			LOG.debug(APP_NAME + ": 服务器创建API成功");
		}, result);

	}

	/**
	 * 初始化权限认证
	 * 
	 * @param path
	 *            路径
	 * @param method
	 *            类型
	 * @param consumes
	 *            接收类型
	 * @param route
	 *            路由
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
		VxApiAuth authHandler = VxApiAuthFactory.getVxApiAuth(authOptions.getInFactoryName(), authOptions.getOption(),
				api, httpClient);
		route.handler(authHandler);
	}

	/**
	 * 初始化前置路由器
	 * 
	 * @param path
	 *            路径
	 * @param method
	 *            类型
	 * @param consumes
	 *            接收类型
	 * @param route
	 *            路由
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
		VxApiBeforeHandler beforeHandler = VxApiBeforeHandlerFactory.getBeforeHandler(options.getInFactoryName(),
				options.getOption(), api, httpClient);
		route.handler(beforeHandler);
	}

	/**
	 * 初始化后置路由器
	 * 
	 * @param path
	 *            路径
	 * @param method
	 *            类型
	 * @param consumes
	 *            接收类型
	 * @param route
	 *            路由
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
		VxApiAfterHandler afterHandler = VxApiAfterHandlerFactory.getAfterHandler(options.getInFactoryName(),
				options.getOption(), api, httpClient);
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
		route.handler(rct -> {
			if (api.getLimitUnit() == null) {
				rct.next();
			} else {
				if (apiLimitMap.get(api.getApiName()) != null) {
					VxApiAPILimit limit = apiLimitMap.get(api.getApiName());
					if (limit.getIpTop() > -1) {
						Map<String, Long> ipPoints = limit.getUserIpCurPoints();
						String host = rct.request().remoteAddress().host();
						if (ipPoints.get(host) == null) {
							ipPoints.put(host, 1L);
						}
						if (ipPoints.get(host) >= limit.getIpTop()) {
							Instant oldTime = limit.getTimePoints().plusSeconds(api.getLimitUnit().getVal());
							Duration between = Duration.between(Instant.now(), oldTime);
							if (between.getSeconds() > 0) {
								rct.response().putHeader(SERVER, VxApiGatewayAttribute.FULL_NAME)
										.putHeader(CONTENT_TYPE, api.getContentType())
										.end(api.getResult().getLimitExample());
								return;
							} else {
								VxApiAPILimit newLimit = new VxApiAPILimit(api.getIpLimit(), api.getApiLimit());
								newLimit.setCurPoint(1);
								newLimit.addUserIpCurPotints(host, 1L);
								apiLimitMap.put(api.getApiName(), newLimit);
								rct.next();
								return;
							}
						} else {
							ipPoints.put(host, ipPoints.get(host) + 1);
							limit.setCurPoint(limit.getCurPoint() + 1);
							rct.next();
							return;
						}
					}
					if (limit.getApiTop() > -1) {
						if (limit.getCurPoint() >= limit.getApiTop()) {
							Instant oldTime = limit.getTimePoints().plusSeconds(api.getLimitUnit().getVal());
							Duration between = Duration.between(Instant.now(), oldTime);
							if (between.getSeconds() > 0) {
								rct.response().putHeader(SERVER, VxApiGatewayAttribute.FULL_NAME)
										.putHeader(CONTENT_TYPE, api.getContentType())
										.end(api.getResult().getLimitExample());
								return;
							} else {
								VxApiAPILimit newLimit = new VxApiAPILimit(api.getIpLimit(), api.getApiLimit());
								newLimit.setCurPoint(1);
								if (api.getIpLimit() != -1) {
									String host = rct.request().remoteAddress().host();
									newLimit.addUserIpCurPotints(host, 1L);
								}
								apiLimitMap.put(api.getApiName(), newLimit);
								rct.next();
								return;
							}
						} else {
							limit.setCurPoint(limit.getCurPoint() + 1);
							rct.next();
							return;
						}
					}
					rct.next();
				} else {
					VxApiAPILimit limit = new VxApiAPILimit(api.getIpLimit(), api.getApiLimit());
					limit.setCurPoint(1);
					if (api.getIpLimit() != -1) {
						String host = rct.request().remoteAddress().host();
						limit.addUserIpCurPotints(host, 1L);
					}
					apiLimitMap.put(api.getApiName(), limit);
					rct.next();
				}
			}
		});
	}

	/**
	 * 初始化参数检查
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
		route.handler(rct -> {
			if (api.getEnterParam() == null) {
				rct.next();
			} else {
				boolean flag = true;// 标记参数是否符合要求,符合true,不符合false
				for (VxApiEntranceParam p : api.getEnterParam()) {
					String param = null;
					if (p.getPosition() == ParamPositionEnum.HEADER) {
						param = rct.request().getHeader(p.getParamName());
					} else {
						param = rct.request().getParam(p.getParamName());
					}
					if (param != null) {
						param = param.trim();
					}
					if (param == null || "".equals(param)) {
						if (p.getDef() != null) {
							param = p.getDef().toString();
							if (p.getPosition() == ParamPositionEnum.HEADER) {
								rct.request().headers().add(p.getParamName(), param);
							} else {
								rct.request().params().add(p.getParamName(), param);
							}
						}
					}
					if (p.isNotNull()) {
						if (param == null || "".equals(param) || !StrUtil.isType(param, p.getParamType())) {
							flag = false;
							break;
						}
					}
					if (p.getCheckOptions() != null) {
						VxApiParamCheckOptions check = p.getCheckOptions();
						if (check.getMaxLength() != null) {
							if (param.length() > check.getMaxLength()) {
								flag = false;
								break;
							}
						}
						if (check.getMaxValue() != null) {
							if (StrUtil.numberGtNumber(p.getParamType(), param, check.getMaxValue())) {
								flag = false;
								break;
							}
						}
						if (check.getMinValue() != null) {
							if (StrUtil.numberLtNumber(p.getParamType(), param, check.getMinValue())) {
								flag = false;
								break;
							}
						}
						if (check.getRegex() != null) {
							if (!param.matches(check.getRegex())) {
								flag = false;
								break;
							}
						}
						if (check.getEnums() != null) {
							if (!check.getEnums().contains(param)) {
								flag = false;
								break;
							}
						}
					}
				}
				if (flag) {
					rct.next();
				} else {
					rct.response().putHeader(SERVER, VxApiGatewayAttribute.FULL_NAME)
							.putHeader(CONTENT_TYPE, api.getContentType())
							.end(api.getResult().getApiEnterCheckFailureExample());
				}
			}
		});

	}

	/**
	 * 初始化与后端服务交互
	 * 
	 * @param isNext
	 *            下一步还是结束(也就是说如有后置处理器inNext=true,反则false)
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
					rct.response().putHeader(SERVER, VxApiGatewayAttribute.FULL_NAME)
							.putHeader(CONTENT_TYPE, api.getContentType()).setStatusCode(404).end();
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
			rct.response().putHeader(SERVER, VxApiGatewayAttribute.FULL_NAME)
					.putHeader(CONTENT_TYPE, api.getContentType()).end(api.getResult().getFailureExample());
			VxApiTrackInfos infos = new VxApiTrackInfos(APP_NAME, api.getApiName());
			infos.setErrMsg(rct.failure().getMessage());
			infos.setErrStackTrace(rct.failure().getStackTrace());
			vertx.eventBus().send(VxApiEventBusAddressConstant.SYSTEM_PLUS_ERROR, infos.toJson());
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
		VxApiCustomHandler customHandler = VxApiCustomHandlerFactory.getCustomHandler(options.getInFactoryName(),
				options.getOption(), api, httpClient);
		route.handler(customHandler);
	}

	/**
	 * 页面跳转服务类型处理器
	 * 
	 * @param isNext
	 * @param api
	 * @param route
	 * @throws NullPointerException
	 * @throws UnsupportedEncodingException
	 */
	public void serverRedirectTypeHandler(boolean isNext, VxApis api, Route route)
			throws NullPointerException, UnsupportedEncodingException {
		JsonObject body = api.getServerEntrance().getBody();
		VxApiServerEntranceRedirectOptions options = VxApiServerEntranceRedirectOptions.fromJson(body);
		if (options == null) {
			throw new NullPointerException("页面跳转服务类型的配置文件无法装换为服务类");
		}
		route.handler(rct -> {
			rct.response().putHeader("Location", options.getUrl()).setStatusCode(302);
			if (isNext) {
				rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>succeededFuture(true));// 告诉后置处理器当前操作成功执行
				rct.next();
			} else {
				rct.response().end();
			}
		});
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
	public void serverHttpTypeHandler(boolean isNext, VxApis api, Route route)
			throws NullPointerException, MalformedURLException {
		JsonObject body = api.getServerEntrance().getBody();
		VxApiServerEntranceHttpOptions serOptions = VxApiServerEntranceHttpOptions.fromJson(body);
		if (serOptions == null) {
			throw new NullPointerException("HTTP/HTTPS服务类型的配置文件无法装换为服务类");
		}
		List<VxApiServerURL> urls = serOptions.getServerUrls();
		if (urls == null || urls.size() < 1) {
			throw new NullPointerException("服务端地址不存在");
		}
		if (serOptions.getBalanceType() == null) {
			serOptions.setBalanceType(LoadBalanceEnum.POLLING_AVAILABLE);
		}
		// 将地址策略添加到map中
		VxApiServerURLPollingPolicy pollingPolicy = new VxApiServerURLPollingPolicy(urls);
		serverUrlPolicyMap.put(api.getApiName(), pollingPolicy);

		route.handler(rct -> {
			VxApiServerURLPollingPolicy policy = serverUrlPolicyMap.get(api.getApiName());
			// 看有没有可用的服务连接
			if (policy.isHaveService()) {// 有可用连接
				// 后台服务连接信息
				VxApiServerURLInfo urlInfo;
				if (serOptions.getBalanceType() == LoadBalanceEnum.IP_HASH) {
					String ip = rct.request().remoteAddress().host();
					urlInfo = policy.getUrl(ip);
				} else {
					urlInfo = policy.getUrl();
				}
				// 执行监控
				VxApiTrackInfos trackInfo = new VxApiTrackInfos(APP_NAME, api.getApiName());
				trackInfo.setRequestBufferLen(rct.getBody() == null ? 0 : rct.getBody().length());
				String requestPath = urlInfo.getUrl();
				MultiMap headers = new CaseInsensitiveHeaders();
				MultiMap queryParams = new CaseInsensitiveHeaders();
				if (serOptions.getParams() != null) {
					for (VxApiParamOptions p : serOptions.getParams()) {
						String param = "";
						if (p.getType() == 0 || p.getType() == 2) {
							if (p.getApiParamPosition() == ParamPositionEnum.HEADER) {
								param = rct.request().getHeader(p.getApiParamName());
							} else {
								param = rct.request().getParam(p.getApiParamName());
							}
						} else if (p.getType() == 1) {
							if (p.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_HOST) {
								param = rct.request().remoteAddress().host();
							} else if (p.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_PORT) {
								param = Integer.toString(rct.request().remoteAddress().port());
							} else if (p.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_PATH) {
								param = rct.request().path() == null ? "" : rct.request().path();
							} else if (p.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_SESSION_ID) {
								param = rct.session().id();
							} else if (p.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_ABSOLUTE_URI) {
								param = rct.request().absoluteURI();
							} else if (p.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_REQUEST_SCHEMA) {
								param = rct.request().scheme();
							} else if (p.getSysParamType() == ParamSystemVarTypeEnum.SERVER_API_NAME) {
								param = api.getApiName();
							} else if (p.getSysParamType() == ParamSystemVarTypeEnum.SERVER_UNIX_TIME) {
								param = Long.toString(System.currentTimeMillis());
							} else if (p.getSysParamType() == ParamSystemVarTypeEnum.SERVER_USER_AGENT) {
								param = VxApiGatewayAttribute.VX_API_USER_AGENT;
							}
						} else if (p.getType() == 9) {
							param = p.getParamValue().toString();
						} else {
							continue;
						}
						if (p.getSerParamPosition() == ParamPositionEnum.HEADER) {
							headers.add(p.getSerParamName(), param);
						} else if (p.getSerParamPosition() == ParamPositionEnum.PATH) {
							requestPath.replace(":" + p.getSerParamName(), param);
						} else {
							queryParams.add(p.getSerParamName(), param);
						}
					}
				}
				HttpRequest<Buffer> request = webClient.requestAbs(serOptions.getMethod(), requestPath)
						.timeout(serOptions.getTimeOut());
				headers.forEach(va -> request.putHeader(va.getKey(), va.getValue()));
				queryParams.forEach(va -> request.addQueryParam(va.getKey(), va.getValue()));
				trackInfo.setRequestTime(Instant.now());
				request.send(res -> {
					trackInfo.setResponseTime(Instant.now());
					if (res.succeeded()) {
						HttpResponse<Buffer> result = res.result();
						if (api.getResult().getTranHeaders() != null && api.getResult().getTranHeaders().size() > 0) {
							api.getResult().getTranHeaders().forEach(h -> {
								rct.response().putHeader(h, result.getHeader(h) == null ? "" : result.getHeader(h));
							});
						}
						rct.response().putHeader(SERVER, VxApiGatewayAttribute.FULL_NAME)
								.putHeader(CONTENT_TYPE, api.getContentType()).setChunked(true).write(result.body());
						if (isNext) {
							rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>succeededFuture(true));// 告诉后置处理器当前操作成功执行
							rct.next();
						} else {
							rct.response().end();
						}
						trackInfo.setResponseBufferLen(result.body() == null ? 0 : result.body().length());
						trackInfo.setEndTime(Instant.now());
					} else {
						if (isNext) {
							rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>failedFuture(res.cause()));// 告诉后置处理器当前操作成功执行
							rct.next();
						} else {
							HttpServerResponse response = rct.response()
									.putHeader(SERVER, VxApiGatewayAttribute.FULL_NAME)
									.putHeader(CONTENT_TYPE, api.getContentType());
							// 如果是连接异常返回无法连接的错误信息,其他异常返回相应的异常
							if (res.cause() instanceof ConnectException) {
								response.end(api.getResult().getCantConnServerExample());
							} else {
								response.end(api.getResult().getFailureExample());
							}
						}
						// 提交连接请求失败
						policy.reportBadService(urlInfo.getIndex());
						trackInfo.setEndTime(Instant.now());
						// 记录与后台交互发生错误
						trackInfo.setSuccessful(false);
						trackInfo.setErrMsg(res.cause().getMessage());
						trackInfo.setErrStackTrace(res.cause().getStackTrace());
					}
					vertx.eventBus().send(VxApiEventBusAddressConstant.SYSTEM_PLUS_TRACK_INFO, trackInfo.toJson());
				});

				// 判断是否有坏的连接
				if (policy.isHaveBadService()) {
					if (!policy.isCheckWaiting()) {
						policy.setCheckWaiting(true);
						vertx.setTimer(serOptions.getRetryTime(), testConn -> {
							List<VxApiServerURLInfo> service = policy.getBadService();
							for (VxApiServerURLInfo urlinfo : service) {
								webClient.requestAbs(serOptions.getMethod(), urlinfo.getUrl())
										.timeout(serOptions.getTimeOut()).send(res -> {
											if (res.succeeded()) {
												policy.reportGreatService(urlinfo.getIndex());
											}
										});
							}
							policy.setCheckWaiting(false);
						});
					}
				}
			} else {
				// 无可用连接时,结束当前处理器并尝试重新尝试连接是否可用
				if (isNext) {
					// 告诉后置处理器当前操作执行结果
					rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY,
							Future.<Boolean>failedFuture(new ConnectException("无法连接上后台交互的服务器")));
					rct.next();
				} else {
					rct.response().putHeader(SERVER, VxApiGatewayAttribute.FULL_NAME)
							.putHeader(CONTENT_TYPE, api.getContentType())
							.end(api.getResult().getCantConnServerExample());
				}
				if (!policy.isCheckWaiting()) {
					policy.setCheckWaiting(true);
					vertx.setTimer(serOptions.getRetryTime(), testConn -> {
						List<VxApiServerURLInfo> service = policy.getBadService();
						for (VxApiServerURLInfo urlinfo : service) {
							webClient.requestAbs(serOptions.getMethod(), urlinfo.getUrl())
									.timeout(serOptions.getTimeOut()).send(res -> {
										if (res.succeeded()) {
											policy.reportGreatService(urlinfo.getIndex());
										}
									});
						}
						policy.setCheckWaiting(false);
					});
				}
			}
		});
	}

	/**
	 * 服务入口的参数检查与路径初始化
	 * 
	 * @param path
	 * @param ser
	 */
	public void chackVxApiParamOptions(String absoluteURI, VxApiServerEntranceHttpOptions ser) {
		if (ser.getParams() != null) {
			for (VxApiParamOptions p : ser.getParams()) {
				if (p.getType() == 0 && (p.getApiParamName() == null || p.getApiParamPosition() == null
						|| p.getSerParamName() == null || p.getSerParamPosition() == null)) {
					throw new NullPointerException("参数映射名字与位置不能为null");
				} else if (p.getType() == 1 && (p.getSysParamType() == null || p.getSerParamName() == null
						|| p.getSerParamPosition() == null)) {
					throw new NullPointerException("系统参数的名字,请求名字与位置不能为null");
				} else if (p.getType() == 2 && (p.getApiParamName() == null || p.getApiParamPosition() == null
						|| p.getSerParamName() == null || p.getSerParamPosition() == null)) {
					throw new NullPointerException("透传参数名字与位置不能为null");
				} else if (p.getType() == 9 && (p.getParamValue() == null || p.getSerParamName() == null
						|| p.getSerParamPosition() == null)) {
					throw new NullPointerException("自定义常量参数值,请求名字与位置不能为null");
				}
				if (p.getSerParamPosition() == ParamPositionEnum.PATH) {
					if (absoluteURI.indexOf(":" + p.getSerParamName()) < 0) {
						throw new NullPointerException("后台服务请求path中不存在名为:" + p.getSerParamName() + "的参数占位");
					}
				}
			}
		}
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
	 * 查看是否有API在运行
	 * 
	 * @param msg
	 */
	public void findAPIStatus(Message<JsonObject> msg) {

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
